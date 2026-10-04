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
