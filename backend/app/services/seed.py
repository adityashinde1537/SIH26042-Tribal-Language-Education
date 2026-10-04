import json
from pathlib import Path


class SeedPack:
    def __init__(self, path: Path) -> None:
        self.path = path
        self.data = self._load()
        self.index = {
            self._normalize(item["hindi"]): item["santhali"]
            for item in self.data.get("items", [])
        }

    @staticmethod
    def _normalize(text: str) -> str:
        return " ".join(text.strip().split())

    def _load(self) -> dict:
        if not self.path.exists():
            return {"version": "0", "language_pair": "hin_Deva→sat_Olck", "items": []}
        with self.path.open("r", encoding="utf-8") as handle:
            return json.load(handle)

    def lookup(self, text: str) -> str | None:
        return self.index.get(self._normalize(text))

    @property
    def count(self) -> int:
        return len(self.data.get("items", []))
