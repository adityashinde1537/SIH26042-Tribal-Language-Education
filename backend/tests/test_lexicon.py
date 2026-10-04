from pathlib import Path

from app.services.lexicon import SourceLexicon


def test_source_lexicon_merges_deduplicates_and_ignores_comments(tmp_path: Path):
    base = tmp_path / "base.txt"
    sources = tmp_path / "sources"
    sources.mkdir()

    base.write_text(
        "# bundled words\nनमस्ते\nविद्यालय\nएक   शब्द\n",
        encoding="utf-8",
    )
    (sources / "extra-a.txt").write_text(
        "विद्यालय\nशिक्षक\n# comment\n",
        encoding="utf-8",
    )
    (sources / "extra-b.txt").write_text(
        "विद्यार्थी\nएक शब्द\n",
        encoding="utf-8",
    )
    # Provenance sidecars must not be treated as vocabulary.
    (sources / "extra-a.source.json").write_text('{"license":"test"}', encoding="utf-8")

    lexicon = SourceLexicon(base, sources)

    assert lexicon.terms == ["नमस्ते", "विद्यालय", "एक शब्द", "शिक्षक", "विद्यार्थी"]
    assert lexicon.count == 5
    assert lexicon.source_count == 3
