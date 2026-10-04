# Privacy and child-safety design notes

- The app does not intentionally persist raw microphone audio.
- Translation history is stored locally as text in Room for offline availability.
- Avoid collecting student names, identifiers or voice recordings unless a future deployment has a clear legal/educational basis and appropriate consent/governance.
- Do not send classroom data to third-party analytics by default.
- Treat generated educational language as assistive content requiring expert validation.
