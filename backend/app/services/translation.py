import importlib.util
import re
import threading
from pathlib import Path
from typing import Any

from huggingface_hub import snapshot_download

from app.config import settings
from app.services.cache import TranslationCache
from app.services.lexicon import SourceLexicon
from app.services.seed import SeedPack

OL_CHIKI_START = 0x1C50
OL_CHIKI_END = 0x1C7F

# Curated classroom/geography proper nouns that are important for the SIH26042
# Jharkhand use case. Keep these explicit and reviewable instead of accepting
# foreign-script model leakage.
KNOWN_PROPER_NOUNS = {
    "झारखंड": "ᱡᱷᱟᱨᱠᱷᱚᱸᱰ",
}


class TranslationService:
    def __init__(self) -> None:
        namespace = f"{settings.model_name}@{settings.model_revision}|{settings.source_code}|{settings.target_code}"
        self.cache = TranslationCache(settings.cache_db, namespace)
        self.seed = SeedPack(settings.seed_pack)
        self.source_lexicon = SourceLexicon(
            settings.hindi_lexicon_path,
            settings.lexicon_sources_dir,
        )
        self._runtime: Any | None = None
        self._runtime_lock = threading.Lock()
        self._inference_lock = threading.Lock()
        self._prime_seed_cache()

    @staticmethod
    def _is_ol_chiki_letter(char: str) -> bool:
        return char.isalpha() and OL_CHIKI_START <= ord(char) <= OL_CHIKI_END

    def _prime_seed_cache(self) -> None:
        for item in self.seed.data.get("items", []):
            source = str(item.get("hindi", "")).strip()
            target = str(item.get("santhali", "")).strip()
            if source and target and self.has_meaningful_ol_chiki(target):
                self.cache.put(source, self.normalize_target(target))

    @staticmethod
    def normalize_target(text: str) -> str:
        clean = " ".join(text.strip().split())
        # IndicTrans2 can preserve extra ASCII periods after an Ol Chiki sentence
        # terminator when Hindi input contains repeated dots, e.g. "....".
        clean = re.sub(r"᱾\.+", "᱾", clean)
        clean = re.sub(r"\s+([?!,;:])", r"\1", clean)
        return clean

    @staticmethod
    def _token_has_foreign_letters(token: str) -> bool:
        return any(
            char.isalpha() and not (OL_CHIKI_START <= ord(char) <= OL_CHIKI_END)
            for char in token
        )

    @classmethod
    def repair_known_proper_nouns(cls, source: str, target: str) -> str:
        repaired = target
        for hindi_name, ol_chiki_name in KNOWN_PROPER_NOUNS.items():
            if hindi_name not in source or ol_chiki_name in repaired:
                continue

            tokens = repaired.split()
            foreign_indexes = [
                index
                for index, token in enumerate(tokens)
                if cls._token_has_foreign_letters(token)
            ]

            # Replace only when there is one unambiguous foreign-script token.
            # Otherwise fail strict validation rather than guessing.
            if len(foreign_indexes) == 1:
                tokens[foreign_indexes[0]] = ol_chiki_name
                repaired = " ".join(tokens)

        return repaired

    @classmethod
    def ol_chiki_ratio(cls, text: str) -> float:
        alphabetic = [char for char in text if char.isalpha()]
        if not alphabetic:
            return 0.0
        count = sum(1 for char in alphabetic if cls._is_ol_chiki_letter(char))
        return count / len(alphabetic)

    @classmethod
    def has_meaningful_ol_chiki(cls, text: str) -> bool:
        alphabetic = [char for char in text if char.isalpha()]
        if len(alphabetic) < 2:
            return False

        ol_chiki_letters = [char for char in alphabetic if cls._is_ol_chiki_letter(char)]
        foreign_letters = [char for char in alphabetic if not cls._is_ol_chiki_letter(char)]

        # Santali output for this app must be fully Ol Chiki. Previously the
        # threshold-only check allowed mixed Arabic/Ol Chiki strings to pass.
        return len(ol_chiki_letters) >= 2 and not foreign_letters

    def _load_runtime(self) -> Any:
        snapshot_path = snapshot_download(
            repo_id=settings.model_name,
            revision=settings.model_revision,
        )
        helper_path = Path(snapshot_path) / "translate.py"
        if not helper_path.exists():
            raise RuntimeError("IndicTrans2 ONNX helper was not found in the model snapshot")

        spec = importlib.util.spec_from_file_location("jansetu_indictrans_onnx", helper_path)
        if spec is None or spec.loader is None:
            raise RuntimeError("Could not load IndicTrans2 ONNX helper")

        module = importlib.util.module_from_spec(spec)
        spec.loader.exec_module(module)
        runtime_class = getattr(module, "IndicTransONNX", None)
        if runtime_class is None:
            raise RuntimeError("IndicTransONNX runtime class is unavailable")
        return runtime_class(snapshot_path)

    def get_runtime(self) -> Any:
        if self._runtime is not None:
            return self._runtime
        with self._runtime_lock:
            if self._runtime is None:
                self._runtime = self._load_runtime()
        return self._runtime

    @property
    def model_loaded(self) -> bool:
        return self._runtime is not None

    def translate(self, text: str) -> tuple[str, bool, str]:
        clean = " ".join(text.strip().split())
        cached = self.cache.get(clean)
        if cached:
            cached = self.normalize_target(
                self.repair_known_proper_nouns(clean, cached)
            )
            if self.has_meaningful_ol_chiki(cached):
                self.cache.put(clean, cached)
                return cached, True, "sqlite-cache"

        seeded = self.seed.lookup(clean)
        if seeded:
            seeded = self.normalize_target(
                self.repair_known_proper_nouns(clean, seeded)
            )
            if self.has_meaningful_ol_chiki(seeded):
                self.cache.put(clean, seeded)
                return seeded, True, "seed-pack"

        if settings.translation_mode == "seed-only":
            raise RuntimeError("No offline seed translation is available for this text")

        runtime = self.get_runtime()
        with self._inference_lock:
            translated = runtime.translate(
                clean,
                src_lang=settings.source_code,
                tgt_lang=settings.target_code,
            )

        if not isinstance(translated, str):
            raise RuntimeError("Translation runtime returned an unexpected response")

        translated = self.normalize_target(
            self.repair_known_proper_nouns(clean, translated)
        )
        if not translated or not self.has_meaningful_ol_chiki(translated):
            raise RuntimeError(
                "Translation output was not clean Ol Chiki; mixed-script output was rejected"
            )

        self.cache.put(clean, translated)
        return translated, False, "indictrans2-onnx-int8"
