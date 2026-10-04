.PHONY: backend-test backend-run build-lexicon android-build

backend-test:
	cd backend && PYTHONPATH=. pytest -q

backend-run:
	cd backend && uvicorn app.main:app --host 0.0.0.0 --port 8000

build-lexicon:
	cd backend && PYTHONPATH=. python scripts/build_lexicon.py

android-build:
	gradle :app:assembleDebug
