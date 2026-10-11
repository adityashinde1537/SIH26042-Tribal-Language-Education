#!/usr/bin/env python3
"""Export every validated Hindi→Santali cache entry as an Android TSV asset."""
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

    with output.open("w", encoding="utf-8", newline="\n") as handle:
        for source, target in rows:
            safe_source = source.replace("\t", " ").replace("\n", " ")
            safe_target = target.replace("\t", " ").replace("\n", " ")
            handle.write(f"{safe_source}\t{safe_target}\n")

    total = service.source_lexicon.count
    coverage = (len(rows) / total * 100.0) if total else 0.0
    print(f"Wrote {len(rows)}/{total} validated offline translations ({coverage:.2f}% coverage) to {output}")

    if missing:
        print(f"Excluded {len(missing)} terms because no clean Ol Chiki translation was available.")
        for term in missing[:100]:
            print(f"EXCLUDED: {term}")

    # Do not package a weak or partial library by accident.
    return 0 if len(rows) >= 3700 else 2


if __name__ == "__main__":
    raise SystemExit(main())
