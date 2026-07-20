# Brochure Request & Salesforce Sync Platform

A backend service that captures brochure requests from a marketing site or landing page, persists them, and syncs them into Salesforce as Leads.
The Salesforce sync is decoupled via Kafka event messaging so that a slow or unreliable third-party API never blocks the public-facing request flow.

This project mirrors real enterprise integration patterns: REST API design, layered architecture, database persistence, and resilient third-party sync via messaging.

---

## Tech Stack

| Layer | Technology |
|-------|------------|
| Language | Java 21 |
| Framework | Spring Boot 4 (Spring Web MVC, Spring Data JPA, Validation, Actuator) |
| Messaging | Apache Kafka (KRaft mode - no Zookeeper) |
| Database | PostgreSQL |
| Containerization | Docker / Docker Compose |
| Build | Maven |
| Boilerplate | Lombok |
| Testing | JUnit 5, Mockito, Spring MVC Test, Cucumber (BDD) |

---

## Architecture

The write path and the sync path are decoupled by a Kafka topic, so a failing Salesforce push never blocks or fails the original HTTP request:

```
Client → Controller → Service → Repository → PostgreSQL
                          │
                          │ (publishes AFTER the DB commit)
                          ▼
                 Kafka: brochure-requests-created
                          │
                          ▼
        SalesforceSyncListener → SalesforceClient (stub)
                          │
                          ▼
            updates status: SYNCED or FAILED
```

- **Controller** - HTTP request/response handling and input validation.
- **Service** - business logic; creates the request as `PENDING`, saves it, and publishes a `BrochureRequestCreatedEvent` **only after the database transaction commits** (via a transaction synchronization), so a rolled-back request never leaks an event.
- **Repository** - data access via Spring Data JPA (`JpaRepository`), including paginated `findByStatus`.
- **Model** - the `BrochureRequest` entity with an auto-managed `createdAt` / `updatedAt` and a `SyncStatus` enum.
- **DTOs** - `BrochureRequestRequest` (validated input) and `BrochureRequestResponse` (output); the JPA entity is never exposed over the API.
- **Event** - `BrochureRequestCreatedEvent`, serialized to JSON on the topic.
- **Listener** - `SalesforceSyncListener` consumes the event, calls the (stubbed) `SalesforceClient`, and records the outcome as `SYNCED` or `FAILED`.

### Package layout

```
controller/   REST endpoints
service/      business logic + after-commit event publishing
repository/    Spring Data JPA
model/         BrochureRequest entity + SyncStatus enum
dto/           request/response DTOs
event/         BrochureRequestCreatedEvent
kafka/         SalesforceSyncListener + topic constants
salesforce/    SalesforceClient interface + fake implementation
config/        Kafka producer wiring
exception/     global @RestControllerAdvice
```

---

## Running

### Prerequisites

- Java 21
- Docker Desktop

### Option A - everything in Docker

`docker compose up` builds the app image and starts Postgres, Kafka, and the service together:

```bash
docker compose up --build
```

The app is available at `http://localhost:8080`.
First copy `.env.example` to `.env` and set `POSTGRES_PASSWORD` (the `.env` file is git-ignored and auto-loaded by Docker Compose); `POSTGRES_USER` and `POSTGRES_DB` default to `devadmin` / `brochure_sync_db` if unset.

### Option B - infra in Docker, app on the host

Start just the infrastructure, then run the app from your IDE / Maven:

```bash
docker compose up -d postgres-db kafka
./mvnw spring-boot:run
```

The app runs with the `local` Spring profile by default, reading its datasource from `application-local.properties` (see [Configuration](#configuration)).

---

## Configuration

Database credentials are **not** committed. `application.properties` references environment placeholders (`${DB_URL}`, `${DB_USERNAME}`, `${DB_PASSWORD}`, `${KAFKA_BOOTSTRAP_SERVERS}`), resolved locally via a git-ignored `application-local.properties` and via real environment variables in a deployed environment.

To run locally (Option B), create `src/main/resources/application-local.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/brochure_sync_db
spring.datasource.username=<your-local-username>
spring.datasource.password=<your-local-password>
```

---

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/brochure-requests` | Create a new brochure request (returns `201` + `Location`) |
| `GET` | `/brochure-requests` | List requests, **paginated** (`?page=`, `?size=`, `?sort=`) |
| `GET` | `/brochure-requests?status={status}` | Filter by status (`PENDING`, `SYNCED`, `FAILED`) |
| `GET` | `/brochure-requests/{id}` | Get a single request by id (`404` if missing) |
| `POST` | `/brochure-requests/{id}/retry` | Re-drive a request (typically `FAILED`) back through the sync pipeline |
| `GET` | `/actuator/health` | Liveness/readiness health |

### Example

```bash
curl -X POST http://localhost:8080/brochure-requests \
  -H 'Content-Type: application/json' \
  -d '{"name":"Ada Lovelace","email":"ada@example.com","company":"Analytical Engines","productInterest":"Widgets"}'
```

The request is created as `PENDING`; the Salesforce sync runs asynchronously and flips it to `SYNCED` or `FAILED`.
The bundled `FakeSalesforceClient` succeeds ~70% of the time so both paths are exercisable - use the `retry` endpoint to reprocess a `FAILED` request.

### Validation

`name`, `email`, `company`, and `productInterest` are required, and `email` must be a valid format.
Invalid requests return a `400` with field-level error messages:

```json
{
  "timestamp": "2026-07-20T23:10:58.430168Z",
  "status": 400,
  "message": "Validation failed",
  "errors": { "name": "Name is required", "email": "Email should be valid" }
}
```

---

## Testing

```bash
./mvnw test
```

- `BrochureRequestServiceTest` - unit tests with a mocked repository and Kafka template.
- `BrochureRequestControllerTest` - `@WebMvcTest` slice covering success, validation failures, and 404s.
- `RunCucumberTest` / `brochure_request.feature` - BDD scenario for request submission.

---

## Current Status

- [x] REST API - Model / Repository / Service / Controller layers
- [x] Request/response DTOs (entity not exposed over the API)
- [x] Input validation with global exception handling
- [x] PostgreSQL persistence via Spring Data JPA + Hibernate
- [x] Pagination + status filtering
- [x] Salesforce sync via event-driven Kafka messaging (published after DB commit)
- [x] Retry endpoint for failed syncs
- [x] Automated tests (unit, MVC slice, BDD)
- [x] `docker compose up` runs Postgres + Kafka + the app
- [ ] Real Salesforce client (currently a stub)
- [ ] CI/CD pipeline

---

## License

Personal learning / portfolio project.
