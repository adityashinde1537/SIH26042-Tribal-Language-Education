.PHONY: backend-test backend-run android-build

backend-test:
	cd backend && PYTHONPATH=. pytest -q

backend-run:
	cd backend && uvicorn app.main:app --host 0.0.0.0 --port 8000

android-build:
	gradle :app:assembleDebug
