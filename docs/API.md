# API reference

Default development base URL: `http://127.0.0.1:8000`.

## Health

`GET /health`

Returns model/runtime status and starter-pack size.

## Translate

`POST /translate`

```json
{
  "text": "नमस्ते, आप कैसे हैं?"
}
```

Response:

```json
{
  "translated_text": "ᱦᱚᱞᱮ, ᱟᱢ ᱪᱮᱫ ᱞᱮᱠᱟ?",
  "source_language": "Hindi",
  "target_language": "Santhali (Ol Chiki)",
  "latency_ms": 12,
  "cached": true,
  "engine": "seed-pack"
}
```

## Starter synchronization

`GET /sync/seed`

Returns reviewed/status-tagged seed entries for Room synchronization.

## Worksheet

`POST /materials/worksheet`

```json
{
  "title": "Classroom practice",
  "prompts": ["नमस्ते, आप कैसे हैं?", "आज मौसम अच्छा है।"]
}
```

Returns bilingual rows plus printable HTML.

## Flashcards

`POST /materials/flashcards`

```json
{
  "terms": ["नमस्ते, आप कैसे हैं?"]
}
```

Returns Hindi/Santhali card pairs.
