# SIH demo script

## 1. Problem (30–45 sec)

Explain that many Hindi-medium primary teachers in tribal areas may not know the child's home language. JanSetu acts as a classroom language bridge, starting with Santhali.

## 2. Device evidence

Open **Offline translation library** and show the Android API level and total RAM reported by the app. This gives visible evidence for the Android 9+ / low-cost-device target.

## 3. Text translation

Test the tracked examples:

- \`नमस्कार विद्यार्थी\`
- \`मेरा कॉलेज है....\`
- \`हमारा राज्य झारखंड है।\`
- \`सॉफ्टवेयर क्या है?\`

Show that output is clean Ol Chiki and point out cache/engine information.

## 4. Offline proof

Sync the translation library. Disable connectivity and demonstrate:

1. an exact synchronized phrase;
2. an unseen sentence made only from words already in the local library, showing the labeled \`offline-word-composition\` fallback.

Be explicit that a truly unseen word still needs the backend until an on-device neural model pack is available.

## 5. Voice classroom mode

Tap **Speak Hindi**, say a short Hindi phrase, show recognized Hindi and the Santhali result. Record at least three measured cycle times and keep all results. Use **Speak Santhali output** only if the installed TTS engine reports usable Santali support.

## 6. NIPUN-aligned pedagogy

Open **Bilingual learning materials**, select a NIPUN FLN domain such as Vocabulary or Reading Comprehension, generate a worksheet, then generate visual flashcards. Show the domain label and the offline visual cues.

## 7. Validation evidence

Show \`validation/native_review_template.csv\` and \`validation/device_test_template.csv\`. If a fluent/native Santali reviewer has completed the language sheet, include one reviewed example in the final video.

## 8. Technical close

Show the architecture: Android + Room, FastAPI, SQLite, IndicTrans2 INT8 ONNX, lexicon import pipeline, strict Ol Chiki validation, CI, and APK artifact.

## Final video evidence checklist

- Android API level and RAM.
- Airplane-mode success after sync.
- Three voice-cycle latency measurements.
- One NIPUN-tagged worksheet.
- Visual flashcards.
- One native-speaker feedback example, if completed.
- GitHub Actions passing and downloadable APK artifact.
