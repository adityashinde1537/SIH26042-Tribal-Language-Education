# SIH26042 requirements matrix

| Source requirement | Repository support | Status | Evidence still needed |
| --- | --- | --- | --- |
| Hindi → tribal-language translation | IndicTrans2 Hindi → Santhali / Ol Chiki | Implemented | Native-speaker FLN accuracy evaluation |
| Minimum one tribal language in prototype | Santhali | Implemented | Demo on target device |
| Contextually accurate FLN lesson/activity/assessment translation | Model + cached curated content architecture | Partial | Teacher/native-speaker benchmark corpus |
| Synthesized target-language audio | Android TTS requests `sat-IN` | Partial | Verified offline Santhali voice engine on target tablet |
| Real-time voice-to-voice translation | Hindi recognizer → translation → Santhali TTS flow | Implemented flow | Repeated end-to-end tests under 3 seconds |
| Latency ≤ 3 seconds | Voice screen measures cycle duration and flags target | Instrumented | Hardware measurements on 2-GB Android device |
| Bilingual worksheets | Backend generator + Android share view | Implemented | NIPUN-tagged templates |
| Visual flashcards | Bilingual flashcard data/UI | Implemented basic | Add age-appropriate licensed/generated visuals |
| Offline after initial sync | Room cache + starter sync pack | Implemented for synchronized/cached content | Arbitrary offline neural translation still pending |
| Low-cost tablet ~2 GB RAM | Min SDK 28, lightweight Android client | Designed for | Device memory/thermal benchmark |
| Android 9+ | `minSdk 28` | Implemented | Physical-device smoke test |
| Future Ho/Mundari | Modular language architecture documented | Roadmap | Models, data, native review |
| GitHub repository + demo | This repository + demo script | Implemented repo | Record final demo video |

## Why some rows are deliberately marked Partial

The problem statement requires capabilities that cannot be responsibly claimed from source code alone. Translation quality, TTS availability, 3-second latency and 2-GB-device performance need measured evidence on the actual target hardware and language data.
