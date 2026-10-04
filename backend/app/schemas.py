from pydantic import BaseModel, Field


class TranslationRequest(BaseModel):
    text: str = Field(..., min_length=1, max_length=1500)


class TranslationResponse(BaseModel):
    translated_text: str
    source_language: str = "Hindi"
    target_language: str = "Santhali (Ol Chiki)"
    latency_ms: int
    cached: bool
    engine: str


class HealthResponse(BaseModel):
    status: str
    model: str
    model_loaded: bool
    translation_mode: str
    runtime: str
    offline_seed_items: int
    source_lexicon_items: int
    translated_lexicon_items: int


class SeedItem(BaseModel):
    hindi: str
    santhali: str
    review_status: str


class SeedPackResponse(BaseModel):
    version: str
    language_pair: str
    items: list[SeedItem]


class LexiconEntry(BaseModel):
    hindi: str
    santhali: str


class LexiconMetaResponse(BaseModel):
    source_terms: int
    translated_entries: int
    complete: bool
    page_size_max: int = 100


class LexiconPageResponse(BaseModel):
    offset: int
    limit: int
    total: int
    items: list[LexiconEntry]


class WorksheetRequest(BaseModel):
    title: str = Field(default="FLN Practice Worksheet", max_length=120)
    prompts: list[str] = Field(..., min_length=1, max_length=20)
    nipun_domain: str = Field(default="Vocabulary", max_length=80)


class WorksheetRow(BaseModel):
    number: int
    hindi: str
    santhali: str
    nipun_domain: str


class WorksheetResponse(BaseModel):
    title: str
    nipun_domain: str
    rows: list[WorksheetRow]
    printable_html: str


class FlashcardRequest(BaseModel):
    terms: list[str] = Field(..., min_length=1, max_length=30)
    nipun_domain: str = Field(default="Vocabulary", max_length=80)


class Flashcard(BaseModel):
    hindi: str
    santhali: str
    visual: str
    nipun_domain: str


class FlashcardResponse(BaseModel):
    cards: list[Flashcard]
