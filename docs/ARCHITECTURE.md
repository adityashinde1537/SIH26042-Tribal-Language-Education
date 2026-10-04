# Architecture

## Design goals

1. Keep the teacher workflow simple.
2. Prefer offline/local content whenever it is already synchronized.
3. Isolate translation, speech and pedagogy modules so they can improve independently.
4. Measure latency instead of assuming the 3-second requirement is met.
5. Preserve a path to Ho and Mundari without hard-coding UI logic around one model.

## Android layer

- **Jetpack Compose UI**: text, voice, materials and offline-sync screens.
- **Room/SQLite**: stores translations synchronized or previously generated.
- **Retrofit**: talks to the FastAPI service when reachable.
- **SpeechRecognizer**: requests Hindi recognition and sets `EXTRA_PREFER_OFFLINE=true`.
- **TextToSpeech**: requests `sat-IN`; capability depends on installed voice data.

### Offline decision

For text translation the repository first checks the local Room database. If the phrase is present, no network call is required. If it is absent, the app requests the backend and stores the successful result for future offline use.

This design makes planned FLN lesson content reliable after a sync. It does **not** provide arbitrary neural translation offline until an Android-compatible model pack is added.

## Backend layer

- FastAPI endpoints for translation, starter-pack sync, worksheets and flashcards.
- SQLite cache keyed by language/model namespace.
- IndicTrans2 distilled INT8 ONNX runtime for Hindi → Santhali.
- Ol Chiki ratio check rejects clearly invalid-script model responses.
- `seed-only` mode supports CI and an offline/no-download demo path.

## Data flow

```text
Hindi text/speech
      │
      ▼
Android normalization
      │
      ├── Room hit ───────────────► Ol Chiki output
      │
      └── Room miss
              │
              ▼
          FastAPI
              │
        ┌─────┴─────┐
        │ cache/seed│ hit
        └─────┬─────┘
              │ miss
              ▼
        IndicTrans2 ONNX
              │
              ▼
       Ol Chiki validation
              │
              ▼
       cache + response
```

## Production path

A production classroom build should replace or augment the remote model with an optimized on-device runtime, ship curated NIPUN-tagged packs, and use verified offline ASR/TTS assets for Santhali rather than depending on device-vendor speech support.
