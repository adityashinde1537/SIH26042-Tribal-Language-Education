from dataclasses import dataclass
from pathlib import Path
import os

BASE_DIR = Path(__file__).resolve().parents[1]


@dataclass(frozen=True)
class Settings:
    model_name: str = os.getenv(
        "MODEL_NAME", "hari31416/indictrans2-indic-indic-dist-320M-ONNX-int8"
    )
    model_revision: str = os.getenv(
        "MODEL_REVISION", "b04956dee2f2a3e06e44bf09f8d654a6b81af99a"
    )
    source_code: str = "hin_Deva"
    target_code: str = "sat_Olck"
    source_name: str = "Hindi"
    target_name: str = "Santhali (Ol Chiki)"
    max_input_chars: int = int(os.getenv("MAX_INPUT_CHARS", "1500"))
    min_ol_chiki_ratio: float = float(os.getenv("MIN_OL_CHIKI_RATIO", "0.50"))
    cache_db: Path = Path(
        os.getenv("TRANSLATION_CACHE_DB", str(BASE_DIR / "data" / "runtime" / "jansetu.db"))
    )
    seed_pack: Path = Path(
        os.getenv("SEED_PACK_PATH", str(BASE_DIR / "data" / "seed_pack.json"))
    )
    translation_mode: str = os.getenv("TRANSLATION_MODE", "model").lower()
    cors_origins: str = os.getenv("CORS_ORIGINS", "*")


settings = Settings()
