# Book Rating Service - Technical Overview & Runbook

This repository contains my solution for example project of book rating service.
The app is a Spring Boot–based REST API for a simplified book‑rating service with users, books, and ratings.
It demonstrates API design, database versioning, layered Docker setup, JWT security with refresh tokens, 
Redis‑backed rate limiting, and test coverage around the persistence and service layers.

---

## 1) What this project delivers 

**Data**
- Persists a list of **users**, **books**, and **ratings** in PostgreSQL.
- Ratings are owned by users; a user can update their rating for a book.

**API**
- **List books (public)** — supports pagination/sorting.
- **Top‑rated book (public)** — a dedicated endpoint.
- **Add/Update/Delete a rating (authorized)** — a user can rate a book and change or delete their rating.
- Proper HTTP semantics: verbs, codes, content types, validation and structured error responses.

**Tech**
- **Java 21** (Azul Zulu base image).
- **Spring Boot 3** (Spring Web MVC, Spring Data JPA/Hibernate, Spring Security).
- **PostgreSQL 17** with **Flyway** migrations.
- **Redis 7** for **rate limiting** via Bucket4j + Lettuce.
- **Docker Compose** for one‑command local runs, including a variant to **seed sample data**.
- **Tests**: unit tests + DB‑backed tests with Testcontainers (Postgres).

Bonus material
- Centralized, structured error handling with **ProblemDetail** responses.
- **Optimistic locking** + **ETag / If‑Match** preconditions on updates for safer concurrency.
- (Optional) Actuator health endpoint can be enabled for container healthchecks (commented in Dockerfile).

## 2) Architecture & Key Components
- **Application**: Spring Boot (Web MVC). Domain split into controllers, services, repositories, DTOs.
- **Persistence**: Spring Data JPA withs PostgresSQL; Flyway handles schema & baseline data.
- **Security**:
    - **JWT** access tokens (RSA), **refresh tokens** stored in DB (revocable).
    - Private RSA key is read from a mounted PEM: `app.security.jwt.private-pem-path`.
    - Roles: `ADMIN`, `USER`.
- **Rate Limiting** (Redis + Bucket4j):
    - **Authorization API bucket**: ~**5 req/min** sustained with a small burst allowance.
    - **JWT API bucket**: ~**3 req/sec** sustained with a larger cold‑start burst.
- **Validation & Errors**:
    - Jakarta Bean Validation on DTOs.
    - Global `@RestControllerAdvice` produces `application/problem+json` with details and a per‑field `errors` array.
    - Helpful messages for unreadable payloads, unknown fields, invalid enums, missing headers, etc.
- **Concurrency**:
    - JPA optimistic locking.
    - Patch endpoints require **`If-Match`** header with the entity’s **ETag**; conflicting updates return **412/409**.
- **Documentation**
    - There is Swagger documentation support to look and try Rest APIs.
    - All information available at http://localhost:8080/swagger-ui/index.html

## 3) How to run (Docker compose)

This brings up Postgres, Redis, runs Flyway migrations, and starts the app.
Please, run following command to build executable JAR which will be used during docker image building

```bash
mvn clean package
```

Then you can start the program
```bash
docker compose up --build -d

docker compose logs -f book-app
```

**App URL**: `http://localhost:8080`  
**Swagger UI**: `http://localhost:8080/swagger-ui/index.html`

### 3.3 Start **with sample data**
This path **cleans** the DB schema and re‑applies migrations **plus** generate test data (users, books, ratings), then you can start the app normally.
```bash
docker compose -f docker-compose.yaml -f docker-compose-sample-data.yaml up --build -d
```

> **Note:** The generated job mounts `./src/main/resources/db/sample-data` (`V1000__sample_data_users.sql`).
> If you edit the generation sql file, rerun the reset job to re‑apply it.
> All generated username/passwords (for the simplicity) you can see in V1000__sample_data_users.sql.

### 3.4 Stop the stack
```bash
docker compose down
```

### 3.5 Reset everything (containers **and** volumes)
```bash
docker compose down -v
```

## 4) API Overview

The definitive list (with models, params, examples) is available in **Swagger UI**.
Below is a compact summary of the main resources.

### 4.1 Authentication
- `POST /api/v1/auth/register` — Register a new user.
- `POST /api/v1/auth/login` — Exchange email/password for **JWT access token** + **refresh token**.
- `POST /api/v1/auth/refresh` — Exchange a valid refresh token for a new access/refresh token pair.
- `POST /api/v1/auth/revoke` — Revoke current refresh token.

**Validation**: Password length **10–30**; email must be unique; nickname must be unique.
**Rate limiting**: Auth endpoints are constrained to mitigate brute force.

### 4.2 Books (public + authenticated)
- `GET /api/v1/books` — List books (public) with pagination and sorting.
- `GET /api/v1/books/top-rated` — Get the top‑rated book (public).
- `POST /api/v1/books` — Add a new book (authorized, ADMIN role required).
- `PATCH /api/v1/books/{id}` — Update book details (authorized, ADMIN role required, required **If-Match** locking with ETag).

### 4.3 Ratings (authorized users)
- `POST /api/v1/books/{id}/rating` — Add a rating to a book.
- `GET /api/v1/books/{id}/rating` — Get the current user’s rating for a book.
- `PATCH /api/v1/books/{id}/rating` — Update the current user’s rating for a book (required **If-Match** header).
- `DELETE /api/v1/books/{id}/rating` — Delete the current user’s rating for a book (required **If-Match** header).

**Rules**: A user can only have one rating per book. Updates require the current ETag to prevent lost updates. Deletion also requires ETag to ensure the rating hasn’t changed since last retrieval.
**Rate limiting**: General JWT API bucket applies.

### 4.4 Users (admin only role)
- `GET /api/v1/users` — List of all registered users, supports pagination/sorting.
- `GET /api/v1/users/{userId}` — Get info of about registered user.
- `GET /api/v1/users/{userId}/rating` — List of all user ratings, supports pagination/sorting.

**Rate limiting**: General JWT API bucket applies.

---
## 5) Sample Accounts (when seed data is loaded)

All generated username/passwords (for the simplicity) you can see in V1000__sample_data_users.sql. 

## 6) Validation & Error Handling

- DTOs use **Jakarta Bean Validation** (e.g., `@NotBlank`, size ranges).
- Central `GlobalApiExceptionHandler` returns **Problem Details** with ProblemDetails Spring implementation:
  - `title`, `status`, `detail`, and a structured `errors` array (field, message, rejected).
  - Helpful diagnostics for unknown JSON fields, invalid enums (with `allowedValues`), missing headers (e.g., `If-Match`), etc.
- Concurrency and preconditions:
  - `If-Match` missing → **428 Precondition Required**.
  - ETag mismatch or optimistic lock failure → **412 Precondition Failed** / **409 Conflict**.

---

## 7) Testing

- **Unit tests** for services.
- **DB tests** with **Testcontainers (Postgres 16)** + Flyway (`clean` + `migrate` before tests).
- **Controller tests**: not included due to time constraints (would add MockMvc/WebTestClient with positive/negative scenarios).

---