#!/usr/bin/env python3
"""Import a licensed Hindi vocabulary file into JanSetu's merged source lexicon."""
import argparse
import csv
import json
import re
from pathlib import Path

DEVANAGARI = re.compile(r"[\u0900-\u097F]")


def normalize(text: str) -> str:
    return " ".join(str(text).strip().split())


def safe_name(value: str) -> str:
    cleaned = re.sub(r"[^A-Za-z0-9._-]+", "-", value.strip()).strip("-")
    if not cleaned:
        raise ValueError("Source name must contain letters or numbers")
    return cleaned


def iter_values(path: Path, fmt: str, field: str):
    if fmt == "auto":
        suffix = path.suffix.lower()
        fmt = {".jsonl": "jsonl", ".csv": "csv", ".tsv": "tsv"}.get(suffix, "text")

    if fmt == "text":
        yield from path.read_text(encoding="utf-8").splitlines()
        return

    if fmt == "jsonl":
        with path.open("r", encoding="utf-8") as handle:
            for line in handle:
                if line.strip():
                    item = json.loads(line)
                    value = item.get(field)
                    if value is not None:
                        yield str(value)
        return

    delimiter = "," if fmt == "csv" else "\t"
    with path.open("r", encoding="utf-8", newline="") as handle:
        reader = csv.DictReader(handle, delimiter=delimiter)
        for row in reader:
            value = row.get(field)
            if value is not None:
                yield str(value)


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--input", required=True, type=Path)
    parser.add_argument("--name", required=True, help="Short dataset/source name")
    parser.add_argument("--format", choices=["auto", "text", "jsonl", "csv", "tsv"], default="auto")
    parser.add_argument("--field", default="word", help="JSON/CSV/TSV field containing Hindi text")
    parser.add_argument("--source-url", default="")
    parser.add_argument("--license", default="")
    parser.add_argument("--append", action="store_true")
    args = parser.parse_args()

    if not args.input.exists():
        raise SystemExit(f"Input file not found: {args.input}")

    root = Path(__file__).resolve().parents[1]
    out_dir = root / "data" / "lexicon_sources"
    out_dir.mkdir(parents=True, exist_ok=True)
    name = safe_name(args.name)
    out_path = out_dir / f"{name}.txt"
    meta_path = out_dir / f"{name}.source.json"

    terms = set()
    if args.append and out_path.exists():
        terms.update(normalize(x) for x in out_path.read_text(encoding="utf-8").splitlines() if normalize(x))

    for raw in iter_values(args.input, args.format, args.field):
        term = normalize(raw)
        if term and DEVANAGARI.search(term):
            terms.add(term)

    out_path.write_text("\n".join(sorted(terms)) + "\n", encoding="utf-8")
    meta_path.write_text(
        json.dumps(
            {
                "name": args.name,
                "source_url": args.source_url,
                "license": args.license,
                "entry_count": len(terms),
                "contains_target_translations": False,
            },
            ensure_ascii=False,
            indent=2,
        )
        + "\n",
        encoding="utf-8",
    )
    print(f"Imported {len(terms)} unique Hindi terms -> {out_path}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
