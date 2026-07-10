# Brochure Request & Salesforce Sync Platform

A backend service that captures brochure requests from a marketing site or landing page, persists them, and (in a future phase) syncs them into Salesforce as Leads - decoupled via event messaging so that a slow or unreliable third-party API never blocks the public-facing request flow.

This project is a hands-on build mirroring real enterprise integration patterns: REST API design, layered architecture, database persistence, and (later) resilient third-party sync via messaging.

---

## Tech Stack

| Layer | Technology |
|-------|------------|
| Language | Java 21 |
| Framework | Spring Boot (Spring Web, Spring Data JPA, Validation) |
| Database | PostgreSQL (local dev database, run via Docker) |
| Containerization | Docker / Docker Compose |
| Build | Maven |
| Boilerplate | Lombok |

---

## Architecture

The request flow follows a standard layered architecture:

```
Client → Controller → Service → Repository → PostgreSQL
```

- **Controller** - handles HTTP requests/responses and triggers input validation.
- **Service** - business logic (e.g. setting initial status and timestamps).
- **Repository** - data access via Spring Data JPA (`JpaRepository`).
- **Model** - the `BrochureRequest` entity, mapped directly to a Postgres table via Hibernate.

---

## Running Locally

### Prerequisites

- Java 21
- Docker Desktop

### 1. Start the database

```bash
docker compose up -d
```

### 2. Run the app

```bash
./mvnw spring-boot:run
```

The app runs with the `local` Spring profile active by default, loading database credentials from `application-local.properties` (git-ignored - see [Configuration](#configuration) below).

The app runs on: `http://localhost:8080`

---

## Configuration

Database credentials are **not** committed to this repo. `application.properties` references environment placeholders (`${DB_URL}`, `${DB_USERNAME}`, `${DB_PASSWORD}`), which are resolved locally via a git-ignored `application-local.properties` file, and would be provided via real environment variables in any deployed environment.

To run locally, create `src/main/resources/application-local.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/brochure_sync_db
spring.datasource.username=<your-local-username>
spring.datasource.password=<your-local-password>
```

---

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/brochure-requests` | Create a new brochure request |
| `GET` | `/brochure-requests` | Get all brochure requests |
| `GET` | `/brochure-requests/{id}` | Get a single request by id |
| `GET` | `/brochure-requests?status={status}` | Filter requests by status (`PENDING`, `SYNCED`, `FAILED`) |

### Validation

Requests are validated on creation - `name`, `email`, `company`, and `productInterest` are required, and `email` must be a valid format. Invalid requests return a `400` with field-level error messages.

---

## Current Status

- [x] REST API - Model / Repository / Service / Controller layers
- [x] Input validation with global exception handling
- [x] PostgreSQL persistence via Spring Data JPA + Hibernate (Dockerized locally)
- [ ] Automated tests (TDD/BDD)
- [ ] Salesforce sync via event-driven messaging (Kafka/RabbitMQ)
- [ ] CI/CD pipeline

---

## License

Personal learning / portfolio project.
