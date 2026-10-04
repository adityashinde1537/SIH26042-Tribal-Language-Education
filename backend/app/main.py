import logging
import time

from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware

from app.config import settings
from app.schemas import (
    FlashcardRequest,
    FlashcardResponse,
    HealthResponse,
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
    version="1.0.0",
    description="Hindi → Santhali (Ol Chiki) translation and FLN material generation for MTB-MLE classrooms.",
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


@app.get("/", response_model=HealthResponse)
@app.get("/health", response_model=HealthResponse)
def health() -> HealthResponse:
    return HealthResponse(
        status="ok",
        model=settings.model_name,
        model_loaded=translator.model_loaded,
        runtime="ONNX Runtime INT8 / CPU + SQLite cache",
        offline_seed_items=translator.seed.count,
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
