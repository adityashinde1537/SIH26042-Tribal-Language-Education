#!/usr/bin/env python3
"""Import a licensed Hindi→Santhali parallel corpus into JanSetu."""
import argparse
import csv
import json
import re
from pathlib import Path

from app.services.translation import TranslationService

DEVANAGARI = re.compile(r"[\u0900-\u097F]")


def normalize(text: str) -> str:
    return " ".join(str(text).strip().split())


def safe_name(value: str) -> str:
    cleaned = re.sub(r"[^A-Za-z0-9._-]+", "-", value.strip()).strip("-")
    if not cleaned:
        raise ValueError("Source name must contain letters or numbers")
    return cleaned


def iter_pairs(path: Path, fmt: str, source_field: str, target_field: str):
    if fmt == "auto":
        suffix = path.suffix.lower()
        fmt = {".jsonl": "jsonl", ".csv": "csv", ".tsv": "tsv"}.get(suffix, "tsv")

    if fmt == "jsonl":
        with path.open("r", encoding="utf-8") as handle:
            for line in handle:
                if line.strip():
                    item = json.loads(line)
                    yield item.get(source_field, ""), item.get(target_field, "")
        return

    delimiter = "," if fmt == "csv" else "\t"
    with path.open("r", encoding="utf-8", newline="") as handle:
        reader = csv.DictReader(handle, delimiter=delimiter)
        for row in reader:
            yield row.get(source_field, ""), row.get(target_field, "")


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--input", required=True, type=Path)
    parser.add_argument("--name", required=True)
    parser.add_argument("--format", choices=["auto", "jsonl", "csv", "tsv"], default="auto")
    parser.add_argument("--source-field", default="hindi")
    parser.add_argument("--target-field", default="santhali")
    parser.add_argument("--source-url", default="")
    parser.add_argument("--license", default="")
    parser.add_argument("--review-status", default="source-provided")
    args = parser.parse_args()

    if not args.input.exists():
        raise SystemExit(f"Input file not found: {args.input}")

    service = TranslationService()
    root = Path(__file__).resolve().parents[1]
    out_dir = root / "data" / "lexicon_sources"
    out_dir.mkdir(parents=True, exist_ok=True)
    name = safe_name(args.name)
    out_path = out_dir / f"{name}.txt"
    meta_path = out_dir / f"{name}.source.json"

    terms: set[str] = set()
    imported = 0
    rejected = 0

    for raw_source, raw_target in iter_pairs(args.input, args.format, args.source_field, args.target_field):
        source = normalize(raw_source)
        target = normalize(raw_target)
        if not source or not DEVANAGARI.search(source) or not service.has_meaningful_ol_chiki(target):
            rejected += 1
            continue
        terms.add(source)
        service.cache.put(source, target)
        imported += 1

    out_path.write_text("\n".join(sorted(terms)) + "\n", encoding="utf-8")
    meta_path.write_text(
        json.dumps(
            {
                "name": args.name,
                "source_url": args.source_url,
                "license": args.license,
                "review_status": args.review_status,
                "entry_count": len(terms),
                "imported_pairs": imported,
                "rejected_rows": rejected,
                "contains_target_translations": True,
            },
            ensure_ascii=False,
            indent=2,
        )
        + "\n",
        encoding="utf-8",
    )
    print(f"Imported {imported} Hindi→Santhali pairs; rejected={rejected}. Restart the API to reload source terms.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
