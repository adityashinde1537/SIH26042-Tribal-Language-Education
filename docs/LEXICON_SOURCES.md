# Expanding the JanSetu translation library

JanSetu treats vocabulary coverage as a mergeable library rather than claiming that one static file contains every possible Hindi word.

## Bundled source

The repository currently includes the MIT-licensed `hindi_open_lexicon.txt` vocabulary and records its attribution in `backend/data/HINDI_LEXICON_LICENSE.md`.

## Import additional Hindi vocabulary

Any licensed UTF-8 text/CSV/TSV/JSONL vocabulary can be merged:

```bash
cd backend
PYTHONPATH=. python scripts/import_hindi_vocabulary.py \
  --input /path/to/hindi-vocabulary.jsonl \
  --name my-open-vocabulary \
  --format jsonl \
  --field word \
  --source-url "https://example.org/dataset" \
  --license "CC BY-SA 4.0"
```

The importer writes a deduplicated source file and a matching provenance JSON file under `backend/data/lexicon_sources/`. Restart the API, then run `scripts/build_lexicon.py` or use Android's full-library sync.

## Import an existing Hindi → Santhali corpus

If a dataset already contains Ol Chiki translations, load them directly instead of re-translating:

```bash
cd backend
PYTHONPATH=. python scripts/import_parallel_corpus.py \
  --input /path/to/santali-train.csv \
  --name dataset-name \
  --format csv \
  --source-field hindi \
  --target-field santhali \
  --source-url "https://example.org/dataset" \
  --license "DATASET LICENSE" \
  --review-status "human-reviewed"
```

Rows without Hindi Devanagari text or meaningful Ol Chiki output are rejected. Imported translations are inserted into the SQLite translation library and included in Android synchronization after the API restarts.

## Useful external datasets

- **AdiBhashaa**: roughly 20,000 Hindi–Santali parallel sentence pairs, Santali in Ol Chiki, community translated and independently validated. License: CC BY-NC-SA 4.0. Access requires accepting the dataset conditions first.
- **Governance_v2**: Hindi→Santali is among its language pairs. License: CC BY 4.0. Its files are also gated behind dataset access conditions.
- **Wiktionary/Kaikki-derived Hindi vocabulary** can substantially expand Hindi lemmas and inflected forms. Preserve the applicable Wiktionary/Kaikki attribution and share-alike license when redistributing extracted data.

Do not copy data from another repository merely because it is publicly visible. Import it only when the dataset license permits reuse and retain provenance.

## What “all words” means in practice

No natural language has a final exhaustive word list: names, numbers, compounds, inflections, borrowed words and newly coined terms continuously expand vocabulary. JanSetu therefore combines:

1. a large synchronized offline lexicon;
2. licensed parallel corpora;
3. cached successful translations; and
4. IndicTrans2 fallback for unseen input.

This gives much broader practical coverage than a fixed dictionary while keeping dataset licensing auditable.
