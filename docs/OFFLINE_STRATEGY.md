# Offline strategy

The source requirement says the application must work offline on low-cost Android 9+ tablets after initial content synchronization.

## Current prototype

JanSetu now supports two offline-library layers:

1. **Full lexicon sync** — the repository bundles 3,730 unique Hindi source terms/phrases. During initial synchronization, the backend translates missing entries into Santhali (Ol Chiki), validates them, and the Android app downloads them into Room/SQLite.
2. **Learn-as-you-translate cache** — any other Hindi word or sentence translated through IndicTrans2 is also stored locally and becomes available offline on that tablet.

This means the app is no longer restricted to a two-phrase demo pack.

## Important language boundary

There is no finite file containing literally every possible Hindi word or sentence. Hindi and Santhali are productive natural languages: inflections, compounds, names, numbers and new words can always create unseen forms. For that reason, JanSetu combines the large offline lexicon with a model fallback instead of pretending a fixed dictionary is universal.

## What remains for complete arbitrary offline generation

For an entirely disconnected device to translate never-before-seen input, a quantized on-device translation model and tokenizer are still required. The current full lexicon and all previously translated content work offline after synchronization.

## Validation checklist

- Complete lexicon sync reaches 100% or records failed entries.
- Airplane-mode lookup across random synced terms.
- Cold start on a 2-GB RAM Android 9 device.
- Database size after full lexicon + curriculum content.
- Offline ASR availability for Hindi.
- Offline Santhali TTS/voice availability.


## On-device neural translation branch (experimental)

The Android app now has a user-triggered **Install offline AI model** action on the
Offline translation library screen. It downloads a *pinned* IndicTrans2 ONNX INT8
encoder, cached decoder, external weight data, and two tokenizers into private app
storage. The neural translator runs before any network request whenever the pack is
present. This is different from the older exact-match Room cache or word concatenation.

- Model: `hari31416/indictrans2-indic-indic-dist-320M-ONNX-int8`
- Snapshot revision: `b04956dee2f2a3e06e44bf09f8d654a6b81af99a`
- Runtime: ONNX Runtime Android and DJL Android Rust tokenizer JNI
- Usage: install once online, then put the phone in airplane mode, type a **new**
  Hindi sentence, and verify the Santhali output is Ol Chiki.
- Android voice mode now disallows the network-capable recognizer fallback. Voice
  input needs a Hindi on-device recognizer and language data on Android 12+.
- Speech output only accepts an installed Santhali TTS voice marked offline by
  Android. There is **no bundled Santhali voice model yet**.

### Important remaining tests

This branch has not been certified on a physical Android phone. Run a clean APK
build, a model download and load test, a new-sentence airplane-mode test, and an
Android 9 / 2 GB RAM memory benchmark before claiming production readiness.
A first-run model download needs ample storage and Wi-Fi. Upstream model/script
compatibility can change, which is why this build pins its model revision.

A standalone offline Hindi ASR for Android 9 and a verified bundled Santhali TTS
would require separate model integration. A fully offline speech-to-speech pipeline
is not complete solely because text translation runs on-device.
