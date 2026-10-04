import logging
import time

from fastapi import FastAPI, HTTPException, Query
from fastapi.middleware.cors import CORSMiddleware

from app.config import settings
from app.schemas import (
    FlashcardRequest,
    FlashcardResponse,
    HealthResponse,
    LexiconEntry,
    LexiconMetaResponse,
    LexiconPageResponse,
    SeedPackResponse,
    TranslationRequest,
    TranslationResponse,
    WorksheetRequest,
    WorksheetResponse,
)
from app.services.materials import MaterialsService
from app.services.translation import TranslationService

logging.basicConfig(level="INFO")
logger = logging.getLogger("jansetu-api")

app = FastAPI(
    title="JanSetu SIH26042 API",
    version="1.1.0",
    description="Hindi → Santhali (Ol Chiki) translation, full lexicon sync and FLN material generation.",
)
origins = ["*"] if settings.cors_origins == "*" else [x.strip() for x in settings.cors_origins.split(",") if x.strip()]
app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,
    allow_credentials=False,
    allow_methods=["GET", "POST"],
    allow_headers=["*"],
)

translator = TranslationService()
materials = MaterialsService(translator)


def lexicon_coverage() -> int:
    return translator.cache.count_present(translator.source_lexicon.terms)


@app.get("/", response_model=HealthResponse)
@app.get("/health", response_model=HealthResponse)
def health() -> HealthResponse:
    return HealthResponse(
        status="ok",
        model=settings.model_name,
        model_loaded=translator.model_loaded,
        runtime="ONNX Runtime INT8 / CPU + SQLite cache",
        offline_seed_items=translator.seed.count,
        source_lexicon_items=translator.source_lexicon.count,
        translated_lexicon_items=lexicon_coverage(),
    )


@app.post("/translate", response_model=TranslationResponse)
def translate(request: TranslationRequest) -> TranslationResponse:
    started = time.perf_counter()
    try:
        translated, cached, engine = translator.translate(request.text)
    except Exception as exc:
        logger.exception("Translation failed")
        raise HTTPException(status_code=503, detail=str(exc)) from exc
    return TranslationResponse(
        translated_text=translated,
        latency_ms=round((time.perf_counter() - started) * 1000),
        cached=cached,
        engine=engine,
    )


@app.get("/sync/seed", response_model=SeedPackResponse)
def seed_pack() -> SeedPackResponse:
    return SeedPackResponse.model_validate(translator.seed.data)


@app.get("/lexicon/meta", response_model=LexiconMetaResponse)
def lexicon_meta() -> LexiconMetaResponse:
    translated = lexicon_coverage()
    source_terms = translator.source_lexicon.count
    return LexiconMetaResponse(
        source_terms=source_terms,
        translated_entries=translated,
        complete=source_terms > 0 and translated >= source_terms,
    )


@app.get("/lexicon/page", response_model=LexiconPageResponse)
def lexicon_page(
    offset: int = Query(default=0, ge=0),
    limit: int = Query(default=20, ge=1, le=100),
    generate: bool = Query(default=False),
) -> LexiconPageResponse:
    terms = translator.source_lexicon.terms[offset: offset + limit]
    items: list[LexiconEntry] = []

    for source in terms:
        try:
            cached = translator.cache.get(source)
            if cached and translator.has_meaningful_ol_chiki(cached):
                target = cached
            elif generate:
                target, _, _ = translator.translate(source)
            else:
                continue
            items.append(LexiconEntry(hindi=source, santhali=target))
        except Exception:
            logger.exception("Lexicon translation failed for %r", source)

    return LexiconPageResponse(
        offset=offset,
        limit=limit,
        total=translator.source_lexicon.count,
        items=items,
    )


@app.post("/materials/worksheet", response_model=WorksheetResponse)
def worksheet(request: WorksheetRequest) -> WorksheetResponse:
    try:
        rows, printable_html = materials.worksheet(request.title, request.prompts)
    except Exception as exc:
        raise HTTPException(status_code=503, detail=str(exc)) from exc
    return WorksheetResponse(title=request.title, rows=rows, printable_html=printable_html)


@app.post("/materials/flashcards", response_model=FlashcardResponse)
def flashcards(request: FlashcardRequest) -> FlashcardResponse:
    try:
        return FlashcardResponse(cards=materials.flashcards(request.terms))
    except Exception as exc:
        raise HTTPException(status_code=503, detail=str(exc)) from exc
