# API reference

Default development base URL: `http://127.0.0.1:8000`.

## Health

`GET /health`

Returns model/runtime status, starter-pack size, bundled Hindi lexicon size, and translated lexicon coverage.

## Translate

`POST /translate`

```json
{
  "text": "नमस्ते, आप कैसे हैं?"
}
```

The service checks the SQLite cache/seed pack first and then uses IndicTrans2 for unseen input.

## Full lexicon metadata

`GET /lexicon/meta`

Example shape:

```json
{
  "source_terms": 3730,
  "translated_entries": 1250,
  "complete": false,
  "page_size_max": 1000
}
```

## Full lexicon page

`GET /lexicon/page?offset=0&limit=20&generate=true`

- `generate=false`: return already cached translations for that source-term page.
- `generate=true`: generate missing Santhali translations through IndicTrans2, cache them, then return successful entries.

The Android full-sync workflow loops over all source terms and uses `generate=true`.

## Starter synchronization

`GET /sync/seed`

Returns the tiny smoke-test pack used by CI/demo mode.

## Worksheet

`POST /materials/worksheet`

```json
{
  "title": "Classroom practice",
  "prompts": ["नमस्ते, आप कैसे हैं?", "आज मौसम अच्छा है।"]
}
```

## Flashcards

`POST /materials/flashcards`

```json
{
  "terms": ["नमस्ते, आप कैसे हैं?"]
}
```
