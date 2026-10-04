#!/usr/bin/env python3
"""Summarize native-speaker/teacher review evidence from a CSV file."""
import argparse
import csv
from pathlib import Path


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--input", type=Path, required=True)
    parser.add_argument("--pass-score", type=float, default=4.0)
    args = parser.parse_args()

    rows = list(csv.DictReader(args.input.open("r", encoding="utf-8-sig", newline="")))
    reviewed = []
    for row in rows:
        raw = (row.get("reviewer_score_1_to_5") or "").strip()
        if not raw:
            continue
        try:
            score = float(raw)
        except ValueError:
            continue
        reviewed.append(score)

    total = len(rows)
    if not reviewed:
        print(f"rows={total} reviewed=0; no reviewer scores recorded yet")
        return 2

    passed = sum(score >= args.pass_score for score in reviewed)
    average = sum(reviewed) / len(reviewed)
    print(
        f"rows={total} reviewed={len(reviewed)} average_score={average:.2f}/5 "
        f"pass_at_{args.pass_score:g}={passed}/{len(reviewed)} ({passed / len(reviewed) * 100:.1f}%)"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
