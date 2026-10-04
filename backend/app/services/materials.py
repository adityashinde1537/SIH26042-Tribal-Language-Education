from html import escape

from app.schemas import Flashcard, WorksheetRow
from app.services.translation import TranslationService


class MaterialsService:
    def __init__(self, translator: TranslationService) -> None:
        self.translator = translator

    def worksheet(self, title: str, prompts: list[str]) -> tuple[list[WorksheetRow], str]:
        rows: list[WorksheetRow] = []
        for index, prompt in enumerate(prompts, start=1):
            translated, _, _ = self.translator.translate(prompt)
            rows.append(WorksheetRow(number=index, hindi=prompt.strip(), santhali=translated))

        html_rows = "".join(
            f"<tr><td>{row.number}</td><td>{escape(row.hindi)}</td><td>{escape(row.santhali)}</td><td>________________</td></tr>"
            for row in rows
        )
        printable = f"""<!doctype html><html><head><meta charset='utf-8'><title>{escape(title)}</title>
<style>body{{font-family:sans-serif;margin:32px}}table{{width:100%;border-collapse:collapse}}th,td{{border:1px solid #777;padding:10px;text-align:left}}h1{{font-size:22px}}</style>
</head><body><h1>{escape(title)}</h1><p>Hindi + Santhali (Ol Chiki) bilingual practice sheet</p>
<table><thead><tr><th>#</th><th>Hindi</th><th>Santhali</th><th>Student response</th></tr></thead><tbody>{html_rows}</tbody></table></body></html>"""
        return rows, printable

    def flashcards(self, terms: list[str]) -> list[Flashcard]:
        cards: list[Flashcard] = []
        for term in terms:
            translated, _, _ = self.translator.translate(term)
            cards.append(Flashcard(hindi=term.strip(), santhali=translated))
        return cards
