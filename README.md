<div align="center">

# JanSetu — SIH26042

### AI-Powered Vernacular Pedagogy and Real-Time Translation Tool for Mother Tongue-Based Primary Education

**Smart India Hackathon 2026 · Smart Education · Software · Team JanSetu**

Hindi → Santhali (Ol Chiki) prototype for mother-tongue-based primary classrooms.

</div>

## What this repository implements

- Hindi `hin_Deva` → Santhali `sat_Olck` translation through IndicTrans2 INT8 ONNX.
- **3,730-entry Hindi source vocabulary library** bundled for full offline-library generation.
- Full Hindi→Santhali lexicon generation and paginated Android synchronization.
- Model fallback for words/sentences outside the bundled vocabulary.
- Ol Chiki output validation before model output is stored.
- Android 9+ app built with Kotlin + Jetpack Compose.
- Room/SQLite offline translation library.
- Hindi voice input with offline preference.
- Santhali TTS request through the installed Android TTS engine.
- Voice-cycle latency measurement against the SIH ≤3 second target.
- Bilingual worksheets and flashcards.
- FastAPI backend, SQLite cache, Docker support, tests and CI.
- Architecture, validation, privacy, offline strategy and demo documentation.

## Translation coverage

JanSetu does **not** rely on a tiny fixed dictionary.

```text
Hindi input
   │
   ├── Room/SQLite offline hit ─────► return immediately
   │
   └── local miss
          │
          ▼
       FastAPI
          │
          ├── translated lexicon/cache hit
          │
          └── miss ─────► IndicTrans2 INT8 ONNX
                              │
                              ▼
                        Ol Chiki validation
                              │
                              ▼
                        cache + return
```

The repository includes **3,730 unique Hindi terms/phrases** from a redistributable MIT-licensed source vocabulary. During **Sync full translation library**, missing entries are translated to Santhali and downloaded into Room for offline use.

A literal file containing “all Hindi words” cannot exist because natural languages are open-vocabulary: inflections, compounds, names, numbers and new words create unlimited unseen forms. JanSetu therefore keeps **IndicTrans2 as the fallback** for any word or sentence outside the offline library.

Source attribution is documented in [backend/data/HINDI_LEXICON_LICENSE.md](backend/data/HINDI_LEXICON_LICENSE.md).

## Architecture

```text
Teacher
  │
  ▼
Android 9+ app (Kotlin / Compose)
  ├── Text translation
  ├── Voice classroom mode
  ├── Worksheets + flashcards
  ├── Full lexicon sync
  ├── Android SpeechRecognizer / TTS
  └── Room (SQLite) offline library
          │
          │ initial sync / model fallback
          ▼
FastAPI service
  ├── 3,730-term Hindi source lexicon
  ├── SQLite translated lexicon/cache
  ├── Worksheet / flashcard generator
  └── IndicTrans2 INT8 ONNX
          Hindi: hin_Deva
          Santhali: sat_Olck
```

See [Architecture](docs/ARCHITECTURE.md), [Offline Strategy](docs/OFFLINE_STRATEGY.md), and [SIH Requirement Matrix](docs/REQUIREMENTS_MATRIX.md).

## Build the full translated library

Start the backend in model mode:

```bash
cd backend
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
PYTHONPATH=. python scripts/build_lexicon.py
```

The build is resumable: already-cached entries are reused.

Or start the API and let Android build/download the lexicon page-by-page using **Offline translation library → Sync full translation library**.

## Run backend

```bash
cd backend
uvicorn app.main:app --host 0.0.0.0 --port 8000
```

CI/no-model mode:

```bash
TRANSLATION_MODE=seed-only uvicorn app.main:app --host 0.0.0.0 --port 8000
```

Tests:

```bash
cd backend
PYTHONPATH=. pytest -q
```

## Run Android

The emulator debug default is:

```text
http://10.0.2.2:8000/
```

For a physical tablet on the same LAN:

```bash
gradle :app:assembleDebug -PJANSETU_API_BASE_URL=http://192.168.1.10:8000/
```

## Offline behavior

After full lexicon synchronization, the downloaded Hindi→Santhali entries are stored on-device and work without internet. Any additional successful model translation is also cached automatically.

**Remaining production gap:** translating a never-before-seen word/sentence while the device has never synchronized and has no backend connection still requires a future quantized on-device model pack.

## Responsible use

The full lexicon is **machine translated**, not automatically human-validated. Santhali is a low-resource language, so classroom content should be reviewed by fluent/native speakers before being treated as authoritative curriculum.

## Demo path

1. Start FastAPI in model mode.
2. Open **Offline translation library**.
3. Tap **Sync full translation library**.
4. Disconnect connectivity and demonstrate offline word lookup.
5. Enter an unseen sentence while online to show model fallback and automatic caching.
6. Demonstrate voice classroom mode.
7. Generate bilingual worksheets/flashcards.

See [docs/DEMO_SCRIPT.md](docs/DEMO_SCRIPT.md).

---

**Problem Statement ID:** SIH26042  
**Organization:** Government of Jharkhand  
**Department:** Department of Higher & Technical Education  
**Theme:** Smart Education  
**Prototype language:** Santhali / Ol Chiki
