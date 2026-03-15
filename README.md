# Associa

Plateforme de gestion d'associations (membres, mandats, roles, cotisations, finances, evenements, documents, messages, votes).

## Structure du depot

- `associa-backend` : API Spring Boot (Java 21)
- `associa-frontend` : Frontend React (Vite)
- `docs` : documentation produit et technique
- `tests` : tests E2E Selenium

## Prerequis

- Java 21
- Maven 3.8+
- Node.js 18+
- PostgreSQL 14+

## Demarrage rapide

Backend :

```bash
cd associa-backend
./mvnw spring-boot:run
```

Frontend :

```bash
cd associa-frontend
cp .env.example .env
npm install
npm run dev
```

Par defaut, l'API tourne sur `http://localhost:8080`. Configure `VITE_API_BASE_URL` cote frontend.

## Documentation

Voir `docs/README.md` pour l'index complet.

## Tests

- Backend : `cd associa-backend && ./mvnw -q test`
- Frontend : `cd associa-frontend && npm run lint`
- E2E : `cd tests && pytest -q`
