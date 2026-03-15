# Associa

Associa est une plateforme complete de gestion d'associations. Elle centralise les membres, les mandats, les finances et les activites pour donner une vue claire du fonctionnement et faciliter la prise de decision.

## Ce que fait le projet

- Gestion des membres : creation, approbation, suspension, profils
- Organisation : postes, mandats et historique du bureau
- Roles et permissions : acces granulaires par fonctionnalite
- Finances : transactions, approbations, exports PDF/Excel
- Cotisations : generation, paiements, statistiques
- Evenements : creation, participation et presence
- Documents : depot, partage et telechargement
- Communication : annonces et messages internes
- Gouvernance : votes, options et resultats
- Dashboard : indicateurs cles et activite recente
- Audit : tracabilite des actions sensibles

## Stack technique

- Backend : Java 21, Spring Boot, Spring Security, JWT, PostgreSQL
- Frontend : React 18, Vite, TanStack Query, Zustand, Tailwind CSS

## Structure du depot

- `associa-backend` : API Spring Boot
- `associa-frontend` : application web React
- `docs` : documentation
- `tests` : tests E2E Selenium

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

API : `http://localhost:8080`
Swagger : `http://localhost:8080/swagger-ui/index.html`

## Configuration

Variables utiles :

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `VITE_API_BASE_URL`

## Documentation

Voir `docs/README.md` pour l'index.

## Tests

- Backend : `cd associa-backend && ./mvnw -q test`
- Frontend : `cd associa-frontend && npm run lint`
- E2E : `cd tests && pytest -q`
