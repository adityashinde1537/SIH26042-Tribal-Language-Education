from pathlib import Path


class SourceLexicon:
    def __init__(self, path: Path) -> None:
        self.path = path
        self.terms = self._load()

    def _load(self) -> list[str]:
        if not self.path.exists():
            return []
        seen: set[str] = set()
        terms: list[str] = []
        for raw in self.path.read_text(encoding="utf-8").splitlines():
            term = " ".join(raw.strip().split())
            if not term or term in seen:
                continue
            seen.add(term)
            terms.append(term)
        return terms

    @property
    def count(self) -> int:
        return len(self.terms)
