# JanSetu backend

FastAPI service for the SIH26042 prototype. It provides Hindi `hin_Deva` → Santhali `sat_Olck` translation, SQLite caching, a small synchronization seed pack, bilingual worksheet generation, and flashcard generation.

## Modes

- `TRANSLATION_MODE=model` (default): cache → seed pack → IndicTrans2 INT8 ONNX.
- `TRANSLATION_MODE=seed-only`: cache → seed pack only. Useful for fully offline demos and CI because it never downloads the model.

The seed pack intentionally contains only translations already smoke-tested in the earlier prototype. It is **not** a substitute for native-speaker validation.

## Run

```bash
cd backend
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8000
```

For a no-download demo:

```bash
TRANSLATION_MODE=seed-only uvicorn app.main:app --host 0.0.0.0 --port 8000
```

## API

- `GET /health`
- `POST /translate`
- `GET /sync/seed`
- `POST /materials/worksheet`
- `POST /materials/flashcards`

See `docs/API.md` in the repository root for request/response examples.
