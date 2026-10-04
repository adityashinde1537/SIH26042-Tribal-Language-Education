# Offline strategy

The source requirement says the application must work offline on low-cost Android 9+ tablets after initial content synchronization.

## Current prototype

1. Teacher connects during setup or at a school/cluster synchronization point.
2. Starter phrases and planned lesson translations are stored in Room/SQLite on the tablet.
3. Later classroom requests first check Room and return instantly without internet.
4. Worksheet and flashcard generation can fall back to locally cached translations when every requested line is already available.

## What remains for complete arbitrary offline translation

A production implementation needs a quantized on-device translation model and tokenizer that fit memory/storage limits while meeting latency goals. The backend model architecture is isolated so an Android `LocalTranslationEngine` can replace the network call without changing the UI.

## Validation checklist

- Airplane-mode translation of synced content.
- Cold start on a 2-GB RAM Android 9 device.
- Database size after a representative school-term curriculum pack.
- Offline ASR availability for Hindi.
- Offline Santhali TTS/voice availability.
- Battery, thermal and latency measurements during a full class session.
