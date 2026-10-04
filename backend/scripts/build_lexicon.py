#!/usr/bin/env python3
"""
Pre-translate every term in the bundled Hindi vocabulary into Santhali (Ol Chiki).

Run from backend/:
    PYTHONPATH=. python scripts/build_lexicon.py

The script is resumable because TranslationService checks SQLite first. Re-running it
only processes entries that are still missing.
"""
import argparse
import sys
import time

from app.services.translation import TranslationService


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--start", type=int, default=0)
    parser.add_argument("--limit", type=int, default=0, help="0 means all remaining terms")
    parser.add_argument("--sleep-ms", type=int, default=0)
    args = parser.parse_args()

    service = TranslationService()
    terms = service.source_lexicon.terms[args.start:]
    if args.limit > 0:
        terms = terms[: args.limit]

    total = len(terms)
    ok = 0
    failed = 0

    for index, term in enumerate(terms, start=1):
        try:
            translated, cached, engine = service.translate(term)
            ok += 1
            print(f"[{index}/{total}] {term} -> {translated} ({engine}{', cached' if cached else ''})")
        except Exception as exc:
            failed += 1
            print(f"[{index}/{total}] FAILED {term}: {exc}", file=sys.stderr)

        if args.sleep_ms > 0:
            time.sleep(args.sleep_ms / 1000)

    print(
        f"Completed. source_terms={service.source_lexicon.count} "
        f"translated_cache={service.cache.count()} ok={ok} failed={failed}"
    )
    return 0 if failed == 0 else 2


if __name__ == "__main__":
    raise SystemExit(main())
