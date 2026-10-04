# JanSetu backend

FastAPI service for the SIH26042 prototype. It provides Hindi `hin_Deva` → Santhali `sat_Olck` translation, a large Hindi source lexicon, SQLite caching, full-lexicon synchronization, bilingual worksheet generation, and flashcard generation.

## Translation coverage

The backend has three layers:

1. **SQLite cache / offline lexicon** — fastest deterministic result.
2. **Bundled Hindi source lexicon** — 3,730 unique terms/phrases that can be pre-translated or generated during full sync.
3. **IndicTrans2 model fallback** — handles arbitrary Hindi words/sentences that are not already in the lexicon/cache.

A literal “all words in Hindi” file does not exist because natural languages are open-vocabulary. The bundled lexicon is a large redistributable source vocabulary, while the model fallback handles unseen forms and sentences.

## Modes

- `TRANSLATION_MODE=model` (default): cache → seed pack → IndicTrans2 INT8 ONNX.
- `TRANSLATION_MODE=seed-only`: cache → seed pack only. Useful for CI because it never downloads the model.

## Build the entire translated offline library

From `backend/`:

```bash
PYTHONPATH=. python scripts/build_lexicon.py
```

The command walks through every bundled Hindi source term, translates missing entries to Santhali, validates Ol Chiki output, and stores successful results in SQLite. It is resumable.

You can test a subset first:

```bash
PYTHONPATH=. python scripts/build_lexicon.py --limit 50
```

The Android app can also request full generation/sync page-by-page from `GET /lexicon/page?generate=true`.

## Run

```bash
cd backend
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8000
```

## API

- `GET /health`
- `POST /translate`
- `GET /sync/seed`
- `GET /lexicon/meta`
- `GET /lexicon/page`
- `POST /materials/worksheet`
- `POST /materials/flashcards`

See `docs/API.md` for request/response details.
