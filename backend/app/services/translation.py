import importlib.util
import threading
from pathlib import Path
from typing import Any

from huggingface_hub import snapshot_download

from app.config import settings
from app.services.cache import TranslationCache
from app.services.lexicon import SourceLexicon
from app.services.seed import SeedPack


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

    def _prime_seed_cache(self) -> None:
        for item in self.seed.data.get("items", []):
            source = str(item.get("hindi", "")).strip()
            target = str(item.get("santhali", "")).strip()
            if source and target and self.has_meaningful_ol_chiki(target):
                self.cache.put(source, target)

    @staticmethod
    def ol_chiki_ratio(text: str) -> float:
        alphabetic = [c for c in text if c.isalpha()]
        if not alphabetic:
            return 0.0
        count = sum(1 for c in alphabetic if 0x1C5A <= ord(c) <= 0x1C7F)
        return count / len(alphabetic)

    @classmethod
    def has_meaningful_ol_chiki(cls, text: str) -> bool:
        letters = sum(1 for c in text if 0x1C5A <= ord(c) <= 0x1C7F)
        return letters >= 2 and cls.ol_chiki_ratio(text) >= settings.min_ol_chiki_ratio

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
        if cached and self.has_meaningful_ol_chiki(cached):
            return cached, True, "sqlite-cache"

        seeded = self.seed.lookup(clean)
        if seeded and self.has_meaningful_ol_chiki(seeded):
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
        translated = translated.strip()
        if not translated or not self.has_meaningful_ol_chiki(translated):
            raise RuntimeError("Translation output did not contain enough Ol Chiki text")

        self.cache.put(clean, translated)
        return translated, False, "indictrans2-onnx-int8"
