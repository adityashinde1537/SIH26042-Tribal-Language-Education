# Contributing

JanSetu is an education-focused low-resource-language prototype. Contributions should improve correctness, offline reliability, accessibility or evidence quality.

## Development flow

1. Create a feature branch.
2. Keep Android and backend changes small and testable.
3. Run backend tests with `cd backend && PYTHONPATH=. pytest -q`.
4. Build Android with `gradle :app:assembleDebug`.
5. For any new Santhali educational phrase, record the source and native-speaker review status. Do not silently label machine output as human-validated.
6. Open a pull request describing the SIH requirement improved by the change.

## Language data

Do not add scraped personal classroom data, children's voice recordings or copyrighted curriculum dumps. Use licensed/public material or team-created examples and document provenance.
