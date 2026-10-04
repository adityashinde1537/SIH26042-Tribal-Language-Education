from app.services.translation import TranslationService


def test_rejects_mixed_script_output():
    mixed = "ᱤᱧᱟᱹᱜ ᱯᱚᱱᱚᱛ جھارکھنᱰ ᱾"
    assert TranslationService.has_meaningful_ol_chiki(mixed) is False


def test_accepts_clean_ol_chiki_output():
    clean = "ᱥᱳᱯᱴᱳᱭᱟᱨ ᱫᱚ ᱪᱮᱫ?"
    assert TranslationService.has_meaningful_ol_chiki(clean) is True


def test_normalizes_extra_periods_after_ol_chiki_terminator():
    raw = "ᱤᱧᱟᱹᱜ ᱠᱚᱞᱮᱡᱽ ᱢᱮᱱᱟᱜᱼᱟ ᱾.."
    assert TranslationService.normalize_target(raw) == "ᱤᱧᱟᱹᱜ ᱠᱚᱞᱮᱡᱽ ᱢᱮᱱᱟᱜᱼᱟ ᱾"


def test_repairs_known_jharkhand_proper_noun():
    source = "हमारा राज्य झारखंड है।"
    mixed = "ᱤᱧᱟᱹᱜ ᱯᱚᱱᱚᱛ ᱫᱚ ᱦᱩᱭᱩᱜ ᱠᱟᱱᱟ جھارکھنᱰ ᱾"
    repaired = TranslationService.repair_known_proper_nouns(source, mixed)

    assert "جھار" not in repaired
    assert "ᱡᱷᱟᱨᱠᱷᱚᱸᱰ" in repaired
    assert TranslationService.has_meaningful_ol_chiki(repaired) is True


def test_does_not_guess_when_multiple_foreign_tokens_exist():
    source = "झारखंड"
    mixed = "ABC جھارکھنᱰ ᱾"
    repaired = TranslationService.repair_known_proper_nouns(source, mixed)

    assert repaired == mixed
    assert TranslationService.has_meaningful_ol_chiki(repaired) is False
