# SIH26042 problem statement summary

## Title

AI-Powered Vernacular Pedagogy and Real-Time Translation Tool for Mother Tongue-Based Primary Education

## Background

The source problem statement describes a scaling gap in Jharkhand's PALASH Mother Tongue-Based Multilingual Education programme. Many teachers posted in tribal-area primary schools are Hindi-medium trained and may not know Ho, Mundari or Santhali. Digital NLP resources for these languages are limited, making it difficult to provide mother-tongue instruction consistently across more than 5,000 tribal-area primary schools.

## Requested system

The requested software suite should:

- Translate standard Hindi Foundational Literacy and Numeracy lesson scripts, activities and assessment prompts into tribal languages.
- Produce contextually appropriate text and synthesized audio.
- Support real-time Hindi voice-to-tribal-language classroom dialogue with latency no greater than three seconds.
- Generate bilingual worksheets and visual flashcards aligned with NIPUN Bharat outcomes.
- Operate offline after initial content synchronization on low-cost tablets with approximately 2 GB RAM and Android 9+.
- Demonstrate at least one tribal language at prototype stage, with Ho, Mundari and Santhali in the broader scope.

## Prototype interpretation used in this repository

The team presentation selects **Hindi → Santhali** as the first language pair and specifies **Ol Chiki** output. It proposes Android/Kotlin for the client, Python/FastAPI for backend services, SQLite for local storage/cache, and modular speech/NLP components.
