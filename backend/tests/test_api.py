from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)


def test_health():
    response = client.get("/health")
    assert response.status_code == 200
    payload = response.json()
    assert payload["status"] == "ok"
    assert payload["offline_seed_items"] >= 2
    assert payload["source_lexicon_items"] >= 3000
    assert payload["translated_lexicon_items"] >= 2


def test_seed_translation_works_without_model_download():
    response = client.post("/translate", json={"text": "नमस्ते, आप कैसे हैं?"})
    assert response.status_code == 200
    payload = response.json()
    assert "ᱦ" in payload["translated_text"]
    assert payload["cached"] is True
    assert payload["engine"] in {"seed-pack", "sqlite-cache"}


def test_unknown_seed_fails_cleanly_in_seed_only_mode():
    response = client.post("/translate", json={"text": "यह वाक्य सीड पैक में नहीं है।"})
    assert response.status_code == 503


def test_lexicon_metadata_reports_full_source_library():
    response = client.get("/lexicon/meta")
    assert response.status_code == 200
    payload = response.json()
    assert payload["source_terms"] >= 3000
    assert payload["translated_entries"] >= 2


def test_worksheet_generation_from_seed_items():
    response = client.post(
        "/materials/worksheet",
        json={"title": "Demo", "prompts": ["नमस्ते, आप कैसे हैं?", "आज मौसम अच्छा है।"]},
    )
    assert response.status_code == 200
    payload = response.json()
    assert len(payload["rows"]) == 2
    assert "<table>" in payload["printable_html"]
