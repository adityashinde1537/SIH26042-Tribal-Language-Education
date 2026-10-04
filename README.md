<div align="center">

# JanSetu — SIH26042

### AI-Powered Vernacular Pedagogy and Real-Time Translation Tool for Mother Tongue-Based Primary Education

**Smart India Hackathon 2026 · Smart Education · Software · Team JanSetu**

Hindi → Santhali (Ol Chiki) prototype for mother-tongue-based primary classrooms.

</div>

## Problem

Jharkhand's PALASH MTB-MLE programme shows the value of mother-tongue instruction, but scaling is constrained by the shortage of teachers who can teach in tribal languages such as Ho, Mundari and Santhali. SIH26042 asks for an AI-assisted software suite that helps Hindi-medium teachers translate FLN classroom content, conduct voice-assisted dialogue, generate bilingual learning material, and keep working in low-connectivity schools.

## What this repository implements

- Hindi `hin_Deva` → Santhali `sat_Olck` translation through IndicTrans2 INT8 ONNX.
- Ol Chiki output validation before a model result is accepted.
- Android 9+ app built with Kotlin + Jetpack Compose.
- Text translation with Room/SQLite offline cache.
- Initial content synchronization for offline classroom use.
- Hindi voice input using Android speech recognition with offline preference.
- Santhali text-to-speech request through the installed Android TTS engine.
- End-to-end voice-cycle latency measurement against the SIH 3-second target.
- Auto-generated bilingual worksheets and flashcards.
- FastAPI backend, SQLite cache, Docker support, tests and CI.
- Documentation for validation, offline deployment and native-speaker review.

> **Important prototype boundary:** synced/cached classroom content works offline today. Arbitrary uncached offline neural translation is not falsely claimed; it needs a separately optimized on-device model pack that is listed in the roadmap.

## Architecture

```text
Teacher
  │
  ▼
Android 9+ app (Kotlin / Compose)
  ├── Text translation
  ├── Voice classroom mode
  ├── Worksheet + flashcard UI
  ├── Android SpeechRecognizer / TTS
  └── Room (SQLite) offline content cache
          │
          │ online/local-LAN when available
          ▼
FastAPI service
  ├── SQLite translation cache
  ├── Seed synchronization pack
  ├── Worksheet / flashcard generator
  └── IndicTrans2 INT8 ONNX
          Hindi: hin_Deva
          Santhali: sat_Olck
```

See [Architecture](docs/ARCHITECTURE.md) and [SIH requirement matrix](docs/REQUIREMENTS_MATRIX.md).

## Repository layout

```text
.
├── app/                    # Android app
├── backend/                # FastAPI + NLP + content generation
├── docs/                   # SIH problem, architecture, validation and demo docs
├── .github/workflows/      # Android + backend CI
├── build.gradle
├── settings.gradle
└── README.md
```

## Run backend

```bash
cd backend
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8000
```

No-model-download demo/CI mode:

```bash
TRANSLATION_MODE=seed-only uvicorn app.main:app --host 0.0.0.0 --port 8000
```

Test:

```bash
cd backend
PYTHONPATH=. pytest -q
```

## Run Android

The debug app uses `http://10.0.2.2:8000/` by default for an Android emulator.

For a physical tablet on the same LAN:

```bash
gradle :app:assembleDebug -PJANSETU_API_BASE_URL=http://192.168.1.10:8000/
```

Install the generated debug APK on an Android 9+ device. For production-style deployments use HTTPS or a local packaged service/model strategy.

## Demo path

1. Start the backend in `seed-only` or model mode.
2. Open **Offline content** and sync the starter pack.
3. Turn off connectivity and show the synced phrases still translating.
4. Show **Text translation** with Hindi → Ol Chiki output.
5. Show **Voice classroom mode** and its measured latency.
6. Generate a bilingual worksheet and flashcards.
7. Explain that model-generated educational content must be reviewed with native speakers before classroom rollout.

A presenter-ready flow is in [docs/DEMO_SCRIPT.md](docs/DEMO_SCRIPT.md).

## Validation status

The repository includes code paths for all major prototype modules, but not every SIH acceptance criterion is already proven on the target 2-GB tablet. The exact status and evidence needed are tracked in [docs/REQUIREMENTS_MATRIX.md](docs/REQUIREMENTS_MATRIX.md).

## Roadmap

- Native-speaker evaluation corpus for FLN vocabulary.
- Bundled/quantized on-device translation model pack for arbitrary offline text.
- Confirmed Santhali offline ASR/TTS model support independent of vendor TTS engines.
- Expand language adapters to Ho and Mundari.
- NIPUN Bharat tagged curriculum packs and teacher analytics.

## Responsible use

Santhali is a low-resource language. Machine output can be grammatically or contextually wrong. Treat AI output as teacher assistance, not authoritative curriculum, until reviewed by fluent/native language experts. Voice content is processed transiently by the Android speech service and this app does not intentionally persist raw audio.

---

**Problem Statement ID:** SIH26042  
**Organization:** Government of Jharkhand  
**Department:** Department of Higher & Technical Education  
**Theme:** Smart Education  
**Prototype language:** Santhali / Ol Chiki
