#!/usr/bin/env python3
"""Run a real IndicTrans2 Hindi→Santali smoke test and print Ol Chiki output."""
from app.services.translation import TranslationService

PHRASES = [
    "नमस्कार विद्यार्थी",
    "मेरा कॉलेज है....",
    "हमारा राज्य झारखंड है।",
    "सॉफ्टवेयर क्या है?",
    "आपका बहुत धन्यवाद",
]


def main() -> int:
    service = TranslationService()
    failures = 0

    for source in PHRASES:
        try:
            target, cached, engine = service.translate(source)
            valid = service.has_meaningful_ol_chiki(target)
            print(f"SOURCE: {source}")
            print(f"TARGET: {target}")
            print(f"ENGINE: {engine}")
            print(f"CACHED: {cached}")
            print(f"OL_CHIKI_VALID: {valid}")
            print("---")
            if not valid:
                failures += 1
        except Exception as exc:
            failures += 1
            print(f"SOURCE: {source}")
            print(f"ERROR: {type(exc).__name__}: {exc}")
            print("---")

    print(f"RESULT: {len(PHRASES) - failures}/{len(PHRASES)} translations passed Ol Chiki validation")
    return 0 if failures == 0 else 1


if __name__ == "__main__":
    raise SystemExit(main())
