import os
from pathlib import Path

os.environ["TRANSLATION_MODE"] = "seed-only"
os.environ["TRANSLATION_CACHE_DB"] = str(Path(__file__).parent / "test-cache.db")
