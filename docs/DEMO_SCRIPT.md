# SIH demo script

## 1. Problem (30–45 sec)

Explain that many Hindi-medium primary teachers in tribal areas may not know the child's home language. JanSetu acts as a classroom language bridge, starting with Santhali.

## 2. Text translation

Enter `नमस्ते, आप कैसे हैं?` and show Ol Chiki output. Point out the engine/cache and latency label.

## 3. Offline proof

Open **Offline content**, sync starter content, then disable connectivity. Return to text translation and demonstrate a synchronized phrase from Room/SQLite.

## 4. Voice classroom mode

Tap **Speak Hindi**, say a short Hindi phrase, show the recognized Hindi and Santhali result. Point at the measured cycle time and whether it is inside the three-second target. Use **Speak Santhali output** only if the tablet's TTS engine reports usable Santhali support.

## 5. Pedagogy

Open **Worksheets & flashcards**, enter one prompt per line and generate bilingual material. Share the worksheet output.

## 6. Technical close

Show the repository architecture: Android + Room, FastAPI, SQLite, IndicTrans2 INT8 ONNX. Explain the explicit roadmap from synchronized offline content to a fully on-device arbitrary translation model and expansion to Ho/Mundari.

## 7. Evidence to capture in final video

- Android version and RAM of test device.
- Airplane-mode success after sync.
- Three voice-cycle latency measurements.
- One worksheet generation sequence.
- One native-speaker feedback example if available.
