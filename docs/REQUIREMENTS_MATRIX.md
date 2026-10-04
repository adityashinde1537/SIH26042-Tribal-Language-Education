# SIH26042 requirements matrix

| Source requirement | Repository support | Status | Evidence still needed |
| --- | --- | --- | --- |
| Hindi → tribal-language translation | IndicTrans2 Hindi → Santhali / Ol Chiki with strict mixed-script validation | Implemented | Native-speaker FLN accuracy evaluation |
| Minimum one tribal language in prototype | Santhali | Implemented | Demo on target device |
| Contextually accurate FLN lesson/activity/assessment translation | Model + cached curated content architecture | Partial | Teacher/native-speaker benchmark corpus |
| Synthesized target-language audio | Android TTS requests \`sat-IN\` | Partial | Verified offline Santhali voice engine on target tablet |
| Real-time voice-to-voice translation | Hindi recognizer → translation → Santhali TTS flow | Implemented flow | Repeated end-to-end tests under 3 seconds |
| Latency ≤ 3 seconds | Voice screen measures cycle duration and flags target | Instrumented | Hardware measurements on ~2-GB Android device |
| Bilingual worksheets | Backend generator + Android share view + NIPUN FLN domain metadata | Implemented | Teacher review of selected learning domains |
| Visual flashcards | Bilingual cards with offline visual cues and NIPUN domain metadata | Implemented prototype | Replace/extend cues with a larger reviewed illustration set if desired |
| Offline after initial sync | Room cache + full lexicon sync + offline word-composition fallback | Partial+ | Arbitrary unseen-word neural translation still needs an on-device model pack |
| Low-cost tablet ~2 GB RAM | Min SDK 28, lightweight Android client, API/RAM evidence shown in app | Designed for | Physical memory/thermal benchmark |
| Android 9+ | \`minSdk 28\` | Implemented | Physical-device smoke test |
| Future Ho/Mundari | Modular language architecture documented | Roadmap | Models, data, native review |
| GitHub repository + demo | Repository, CI, validation templates, demo script, downloadable debug APK artifact | Implemented repo | Record final demo video |

## Automated evidence already completed

- Backend test suite passes.
- Real IndicTrans2 Hindi→Santali smoke test passes for the four tracked classroom examples.
- Strict Ol Chiki validation rejects mixed-script model leakage.
- Android CI builds the app; the workflow also uploads \`jansetu-debug-apk\` when the current build succeeds.

## Evidence that cannot be manufactured by code

The problem statement requires claims that need real-world evidence. Translation quality requires a fluent/native Santali reviewer. Offline TTS support, ≤3-second voice latency, and ~2-GB-device performance require a physical Android device.

Use \`docs/VALIDATION_PROTOCOL.md\` and the CSV templates in \`validation/\` to capture those results.
