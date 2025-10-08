# Flight Deals Alert Service

Production-ready MVP backend for European flight deals alerts built with Spring Boot 3, PostgreSQL, and Docker.

## Features
- Persist users, alert preferences, deal history, and price samples.
- Scheduled and on-demand ingestion from Travelpayouts crowdsourced fares with live validation and enrichment via Amadeus, persisting a `validatedByAmadeus` flag per deal.
- Deal scoring with historical baselines, distance-aware pricing, and error fare detection.
- REST API for administration, browsing deals, managing users, and diagnostics.
- Docker Compose stack with PostgreSQL and optional Mailhog.
- Flyway migrations, Caffeine caching, and integration/unit tests.

## Getting started

1. Copy the environment template and adjust secrets:
   ```bash
   cp .env.example .env
   ```

2. Build and run the stack:
   ```bash
   docker-compose --env-file .env up -d --build
   ```

3. Verify health:
   ```bash
   curl http://localhost:8080/api/v1/health
   ```

4. Trigger an on-demand fetch (requires `ADMIN_TOKEN` from `.env`):
   ```bash
   curl -X POST http://localhost:8080/api/v1/admin/fetch \
     -H "X-ADMIN-TOKEN: <ADMIN_TOKEN>" \
     -H "Content-Type: application/json" \
     -d '{"origins":["LYS"]}'
   ```
   The response reports how many deals were fetched, validated via Amadeus, and persisted.

### External API credentials

- **Travelpayouts**: create a free account at [travelpayouts.com](https://www.travelpayouts.com/) and generate an API token. Assign it to `TRAVELPAYOUTS_TOKEN` in your `.env` file.
- **Amadeus**: register for a free test account at [developers.amadeus.com](https://developers.amadeus.com/), create an application, and copy the client ID/secret into `AMADEUS_CLIENT_ID` and `AMADEUS_CLIENT_SECRET`.

### Local development

- Run tests: `mvn test`
- Start only the API (requires local PostgreSQL):
  ```bash
  ./mvnw spring-boot:run
  ```
- Developer seed endpoint (dev profile only): `POST /api/v1/test/seed`

### Postman collection

Import the files under `postman/` for ready-to-use requests and environment variables. Set the `adminToken` variable to your admin token to authorize admin operations.

## REST API overview

| Method | Path | Description |
| --- | --- | --- |
| GET | `/api/v1/health` | Service liveness |
| GET | `/api/v1/airports` | Fuzzy search European airports |
| POST | `/api/v1/users` | Create or reuse a user by email |
| POST | `/api/v1/prefs` | Upsert alert preferences |
| GET | `/api/v1/deals` | List deals with filters |
| GET | `/api/v1/deals/{id}` | Deal details |
| POST | `/api/v1/admin/fetch` | Trigger fetch for origins (admin token) |
| POST | `/api/v1/test/seed` | Insert sample deals (dev only) |

### Configuration keys

See `.env.example` for all environment variables. Key runtime options include cron frequency (`CRON_FETCH`), monitored origins (`MONITORED_ORIGINS`), detection thresholds, and admin access token.

## Testing

The project includes unit tests for deal detection and utilities, repository tests backed by Testcontainers, and MockWebServer-based integration tests for external API clients. Run `mvn test` to execute the full suite.

## Docker images

- `app`: Spring Boot service built from this repository.
- `postgres`: PostgreSQL 16 with Flyway migrations.
- `mailhog` (optional): available under the `mail` profile for future email workflows.

## License

This repository is provided as-is for MVP evaluation.
