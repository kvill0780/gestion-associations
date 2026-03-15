# Backend - Installation

## Prerequis
- Java 21
- Maven 3.8+
- PostgreSQL 14+

## Configuration

Voir `associa-backend/src/main/resources/application.properties` et `application-dev.properties`.
Variables utiles :
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`

## Lancer

```bash
cd associa-backend
./mvnw spring-boot:run
```

API : `http://localhost:8080`
Swagger : `http://localhost:8080/swagger-ui/index.html`
