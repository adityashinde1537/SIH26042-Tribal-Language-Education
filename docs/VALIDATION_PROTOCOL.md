# JanSetu validation protocol

Code and CI can prove build/test behavior, but SIH26042 also requires evidence from people and physical devices.

## Santali language review

Use \`validation/native_review_template.csv\`.

A fluent/native Santali reviewer or teacher should score each Hindi→Santali item from 1–5, mark whether it is acceptable for classroom use, and provide corrected Ol Chiki text when needed.

Summarize a completed sheet:

\`\`\`bash
cd backend
python scripts/summarize_native_review.py --input ../validation/native_review_template.csv
\`\`\`

Do not mark machine-translated material as human-validated until the reviewer fields are actually completed.

## Low-end Android device evidence

Use \`validation/device_test_template.csv\`.

Capture Android API level, total RAM, at least three voice-cycle measurements, airplane-mode lookup after synchronization, and whether an installed Android TTS engine can speak Santali.

## Voice latency

Record at least three representative classroom phrases. Keep the raw measurements, not just the best run. The UI measures from speech-cycle start to translated text availability and indicates whether a run is within the 3-second target.

## Offline scope

Three levels must be distinguished:

1. exact synchronized phrase: fully offline;
2. unseen sentence whose individual words are all synchronized: JanSetu can use a labeled offline word-composition fallback;
3. genuinely unseen words: still require a backend/on-device neural model.

The word-composition fallback is useful for resilience but must not be presented as equivalent to context-aware neural sentence translation.

## Demo evidence

The final submission video should show the device API/RAM line, offline synchronization, airplane-mode translation, three voice latency runs, a NIPUN-tagged worksheet, visual flashcards, and—when available—one completed reviewer record.
