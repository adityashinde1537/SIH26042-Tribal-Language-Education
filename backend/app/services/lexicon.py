from pathlib import Path


class SourceLexicon:
    def __init__(self, path: Path, sources_dir: Path | None = None) -> None:
        self.path = path
        self.sources_dir = sources_dir
        self.source_files: list[Path] = []
        self.terms = self._load()

    @staticmethod
    def _normalize(raw: str) -> str:
        return " ".join(raw.strip().split())

    def _candidate_files(self) -> list[Path]:
        paths: list[Path] = []
        if self.path.exists():
            paths.append(self.path)
        if self.sources_dir and self.sources_dir.exists():
            paths.extend(sorted(self.sources_dir.glob("*.txt")))
        return paths

    def _load(self) -> list[str]:
        seen: set[str] = set()
        terms: list[str] = []
        self.source_files = self._candidate_files()
        for path in self.source_files:
            for raw in path.read_text(encoding="utf-8").splitlines():
                term = self._normalize(raw)
                if not term or term.startswith("#") or term in seen:
                    continue
                seen.add(term)
                terms.append(term)
        return terms

    @property
    def count(self) -> int:
        return len(self.terms)

    @property
    def source_count(self) -> int:
        return len(self.source_files)
