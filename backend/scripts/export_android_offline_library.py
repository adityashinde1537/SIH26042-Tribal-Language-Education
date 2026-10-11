#!/usr/bin/env python3
"""Export the complete validated Hindi→Santali cache as an Android TSV asset."""
from pathlib import Path

from app.services.translation import TranslationService


def main() -> int:
    service = TranslationService()
    output = Path(__file__).resolve().parents[2] / "app" / "src" / "main" / "assets" / "offline_library.tsv"
    output.parent.mkdir(parents=True, exist_ok=True)

    rows: list[tuple[str, str]] = []
    missing: list[str] = []

    for source in service.source_lexicon.terms:
        target = service.cache.get(source)
        if not target:
            missing.append(source)
            continue

        target = service.normalize_target(
            service.repair_known_proper_nouns(source, target)
        )
        if not service.has_meaningful_ol_chiki(target):
            missing.append(source)
            continue

        rows.append((source, target))

    if missing:
        print(f"ERROR: {len(missing)} source terms are missing valid Ol Chiki translations.")
        for term in missing[:50]:
            print(f"MISSING: {term}")
        return 2

    with output.open("w", encoding="utf-8", newline="\n") as handle:
        for source, target in rows:
            safe_source = source.replace("\t", " ").replace("\n", " ")
            safe_target = target.replace("\t", " ").replace("\n", " ")
            handle.write(f"{safe_source}\t{safe_target}\n")

    print(f"Wrote {len(rows)} offline translations to {output}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
