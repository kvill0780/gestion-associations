# Associa Backend (Spring Boot)

API REST pour la gestion des associations.

## Stack

- Java 21 / Spring Boot 3
- PostgreSQL
- Spring Security + JWT
- Maven

## Configuration

Les valeurs par defaut sont dans `src/main/resources/application.properties` et `application-dev.properties`.
Vous pouvez surcharger via variables d'environnement :

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`

## Demarrage

```bash
./mvnw spring-boot:run
```

API disponible sur `http://localhost:8080`.
Swagger/OpenAPI : `http://localhost:8080/swagger-ui/index.html`.

## Modules principaux

- Authentification (JWT, refresh tokens)
- Associations
- Membres
- Roles et permissions
- Postes et mandats
- Finances (transactions, cotisations)
- Evenements et participations
- Documents
- Annonces
- Messages
- Votes
- Dashboard
- Audit

## Tests

```bash
./mvnw -q test
```

## Donnees de dev

Le `DataSeeder` peut initialiser des comptes et donnees locales pour le developpement.
