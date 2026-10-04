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
    assert payload["translated_lexicon_items"] >= 0
    assert payload["translated_lexicon_items"] <= payload["source_lexicon_items"]


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
    assert payload["translated_entries"] >= 0
    assert payload["translated_entries"] <= payload["source_terms"]
    assert payload["complete"] is (payload["translated_entries"] >= payload["source_terms"])


def test_worksheet_generation_from_seed_items():
    response = client.post(
        "/materials/worksheet",
        json={
            "title": "Demo",
            "prompts": ["नमस्ते, आप कैसे हैं?", "आज मौसम अच्छा है।"],
            "nipun_domain": "Reading Comprehension",
        },
    )
    assert response.status_code == 200
    payload = response.json()
    assert len(payload["rows"]) == 2
    assert payload["nipun_domain"] == "Reading Comprehension"
    assert payload["rows"][0]["nipun_domain"] == "Reading Comprehension"
    assert "NIPUN Bharat FLN domain" in payload["printable_html"]


def test_unknown_nipun_domain_falls_back_to_vocabulary():
    response = client.post(
        "/materials/worksheet",
        json={"title": "Demo", "prompts": ["नमस्ते, आप कैसे हैं?"], "nipun_domain": "made-up"},
    )
    assert response.status_code == 200
    assert response.json()["nipun_domain"] == "Vocabulary"


def test_flashcards_include_offline_visual_cue():
    response = client.post(
        "/materials/flashcards",
        json={"terms": ["नमस्ते, आप कैसे हैं?"], "nipun_domain": "Vocabulary"},
    )
    assert response.status_code == 200
    card = response.json()["cards"][0]
    assert card["visual"]
    assert card["nipun_domain"] == "Vocabulary"
