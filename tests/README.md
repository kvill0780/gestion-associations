# Tests E2E Selenium (Python + Chrome)

## Pré-requis
- Python 3.10+
- Chrome installé

## Installation
```bash
cd /home/kvill/Documents/Projets/Spring\ Boot/associa/tests
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
```

## Configuration
Copie `.env.example` vers `.env` et ajuste si besoin.
- `BASE_URL`: URL frontend (par défaut `http://localhost:5174`)
- `API_BASE_URL`: URL backend (par défaut `http://localhost:8080`)
- `HEADLESS`: `true|false`
- `ADMIN_EMAIL` / `ADMIN_PASSWORD`: compte admin (par défaut `president.test@associa.bf`)
- `TREASURER_EMAIL` / `TREASURER_PASSWORD`: compte trésorier
- `MEMBER_EMAIL` / `MEMBER_PASSWORD`: compte membre

### ChromeDriver
Option recommandée en dev local: installer `chromedriver` et définir :
```
export CHROMEDRIVER_PATH=/chemin/vers/chromedriver
```
Option alternative: autoriser le téléchargement automatique :
```
export ALLOW_DRIVER_DOWNLOAD=true
```

## Exécuter les tests
```bash
pytest -q
```

Pour les scénarios dashboard persona uniquement:
```bash
pytest -q test_dashboard_personas.py
```

## Notes
- Les tests vérifient que frontend/backend sont joignables avant de démarrer.
- Mettre `HEADLESS=false` pour voir le navigateur.
- Ajouter `-rs` pour afficher les raisons de skip: `pytest -q -rs`.
