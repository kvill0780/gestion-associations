# Tests

## Backend

```bash
cd associa-backend
./mvnw -q test
```

## Frontend

```bash
cd associa-frontend
npm run lint
```

## E2E (Selenium)

```bash
cd tests
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
cp .env.example .env
pytest -q
```
