import hashlib
import sqlite3
from pathlib import Path


class TranslationCache:
    def __init__(self, db_path: Path, namespace: str) -> None:
        self.db_path = db_path
        self.namespace = namespace
        self.db_path.parent.mkdir(parents=True, exist_ok=True)
        self._init_db()

    def _init_db(self) -> None:
        with sqlite3.connect(self.db_path, timeout=30) as connection:
            connection.execute("PRAGMA journal_mode=WAL")
            connection.execute(
                """
                CREATE TABLE IF NOT EXISTS translations (
                    cache_key TEXT PRIMARY KEY,
                    source_text TEXT NOT NULL,
                    translated_text TEXT NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """
            )
            connection.commit()

    def _key(self, text: str) -> str:
        raw = f"{self.namespace}|{text.strip()}"
        return hashlib.sha256(raw.encode("utf-8")).hexdigest()

    def get(self, text: str) -> str | None:
        with sqlite3.connect(self.db_path, timeout=30) as connection:
            row = connection.execute(
                "SELECT translated_text FROM translations WHERE cache_key = ?",
                (self._key(text),),
            ).fetchone()
        return row[0] if row else None

    def put(self, text: str, translated_text: str) -> None:
        with sqlite3.connect(self.db_path, timeout=30) as connection:
            connection.execute(
                "INSERT OR REPLACE INTO translations(cache_key, source_text, translated_text) VALUES (?, ?, ?)",
                (self._key(text), text.strip(), translated_text.strip()),
            )
            connection.commit()
