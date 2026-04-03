# Runbook — Book Rating (Docker Compose + Flyway)

These instructions explain how to:
- clean all containers/volumes for this project,
- run apps normally (without sample data),
- stop the apps,
- run apps with generated sample data,

> **Project files used**
>
> - `docker-compose.yaml` — application config without generated data (Postgres, Redis, Flyway migrate, App)
> - `docker-compose-sample-data.yaml` — flyway jobs to clean/generate data
> - Sample data file: `./src/main/resources/sample-data/V1000__sample_data_users.sql`

---

## 0) Prerequisites

- Docker & Docker Compose plugin installed or Podman & Podman Compose (substitute "docker" to "podman").
- In a terminal, `cd` to the project root (where both compose files live).

```bash
cd /path/to/book-rating
```

---

## 1) Build executable JAR file

After cloning the repository, please run 

```bash
mvn clean package
```
to build executable JAR, which later will be used for build app image. 

---

## 2) Start the app without sample data

This builds the app image (if needed), runs Postgres/Redis, applies Flyway **migrations only**, then starts the application.

```bash
docker compose -f docker-compose.yaml up --build -d
```

Check that migrations finished and the app started:

Application is listening on 8080. Port is already open.

---

## 3) Stop the stack (but keep DB contents)

Use `down` **without** `-v` so data persists.

```bash
docker compose -f docker-compose.yaml down
```

---

## 4) Run app with generated data (clean DB, run migrations, then apply sample data)

This runs the one-shot `flyway-reset` job from the override compose file.
It will:
1. `clean` the `book_rating` schema,
2. `migrate` using `src/main/resources/db/migration`,
3. load `src/main/resources/db/sample-data/V1000__sample_data.sql`.

```bash
docker compose -f docker-compose.yaml -f docker-compose-sample-data.yaml up --build -d
```

- Exit code `0` = success.

---

## 5) Run locally without Docker compose

To preprare project for local run you need to have Postgres and Redis running on your machine. You can use docker for that:

openssl genpkey -algorithm RSA -out jwt-private.pem -pkeyopt rsa_keygen_bits:4096
openssl rsa -in jwt-private.pem -pubout -out jwt-public.pem


### 5.1 For Postgres run following command:
```bash
docker run --name book-postgres-local -e POSTGRES_DB=book-service-db -e POSTGRES_USER=application -e POSTGRES_PASSWORD=topsecret123 -p 5432:5432 -d postgres:17
```

### 5.2 For Redis run following command:
```bash
docker run --name book-redis-local -p 6379:6379 -d redis:7.2 redis-server --requirepass secret
```

### 5.3 Then build the project with maven:
```bash
mvn clean install
```

### 5.4 Then run flyway migrations:
```bash
mvn flyway:migrate -P db-migrate
```

### 5.5 Then run the app:
```bash
mvn spring-boot:run
```