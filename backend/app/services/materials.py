from html import escape

from app.schemas import Flashcard, WorksheetRow
from app.services.translation import TranslationService


NIPUN_LANGUAGE_DOMAINS = {
    "Oral Language Development",
    "Phonological Awareness",
    "Decoding",
    "Vocabulary",
    "Reading Comprehension",
    "Reading Fluency",
    "Concept about Print",
    "Writing",
    "Culture of Reading",
    "Foundational Numeracy",
}

VISUAL_HINTS = {
    "पानी": "💧",
    "जल": "💧",
    "सूरज": "☀️",
    "सूर्य": "☀️",
    "किताब": "📘",
    "पुस्तक": "📘",
    "विद्यालय": "🏫",
    "स्कूल": "🏫",
    "कॉलेज": "🏫",
    "घर": "🏠",
    "पेड़": "🌳",
    "वृक्ष": "🌳",
    "फल": "🍎",
    "फूल": "🌼",
    "विद्यार्थी": "🧒",
    "बच्चा": "🧒",
    "शिक्षक": "🧑‍🏫",
    "झारखंड": "🗺️",
    "राज्य": "🗺️",
    "कंप्यूटर": "💻",
    "कम्प्यूटर": "💻",
    "सॉफ्टवेयर": "💻",
    "गणित": "🔢",
    "संख्या": "🔢",
}


class MaterialsService:
    def __init__(self, translator: TranslationService) -> None:
        self.translator = translator

    @staticmethod
    def normalize_domain(domain: str) -> str:
        clean = " ".join(domain.strip().split())
        return clean if clean in NIPUN_LANGUAGE_DOMAINS else "Vocabulary"

    @staticmethod
    def visual_hint(term: str) -> str:
        normalized = " ".join(term.strip().split())
        for keyword, visual in VISUAL_HINTS.items():
            if keyword in normalized:
                return visual
        return "🔤"

    def worksheet(
        self,
        title: str,
        prompts: list[str],
        nipun_domain: str = "Vocabulary",
    ) -> tuple[list[WorksheetRow], str]:
        domain = self.normalize_domain(nipun_domain)
        rows: list[WorksheetRow] = []
        for index, prompt in enumerate(prompts, start=1):
            translated, _, _ = self.translator.translate(prompt)
            rows.append(
                WorksheetRow(
                    number=index,
                    hindi=prompt.strip(),
                    santhali=translated,
                    nipun_domain=domain,
                )
            )

        html_rows = "".join(
            f"<tr><td>{row.number}</td><td>{escape(row.hindi)}</td><td>{escape(row.santhali)}</td><td>________________</td></tr>"
            for row in rows
        )
        printable = f"""<!doctype html><html><head><meta charset='utf-8'><title>{escape(title)}</title>
<style>body{{font-family:sans-serif;margin:32px}}table{{width:100%;border-collapse:collapse}}th,td{{border:1px solid #777;padding:10px;text-align:left}}h1{{font-size:22px}}</style>
</head><body><h1>{escape(title)}</h1><p>Hindi + Santhali (Ol Chiki) bilingual practice sheet</p>
<p><strong>NIPUN Bharat FLN domain:</strong> {escape(domain)}</p>
<table><thead><tr><th>#</th><th>Hindi</th><th>Santhali</th><th>Student response</th></tr></thead><tbody>{html_rows}</tbody></table></body></html>"""
        return rows, printable

    def flashcards(
        self,
        terms: list[str],
        nipun_domain: str = "Vocabulary",
    ) -> list[Flashcard]:
        domain = self.normalize_domain(nipun_domain)
        cards: list[Flashcard] = []
        for term in terms:
            translated, _, _ = self.translator.translate(term)
            cards.append(
                Flashcard(
                    hindi=term.strip(),
                    santhali=translated,
                    visual=self.visual_hint(term),
                    nipun_domain=domain,
                )
            )
        return cards
