# AGENTS.md - SRMS Backend

## Purpose

This file is the entry point for Codex or any coding agent working on the Smart Rental Management System (SRMS) backend.

Before making changes, read this file and the scoped rule files in `rules/`.

## Rule Loading Order

Read these files in order:

1. `rules/00-project-context.md`
2. `rules/01-architecture.md`
3. `rules/02-coding-style.md`
4. `rules/03-service-implementation.md`
5. `rules/04-database-and-migrations.md`
6. `rules/05-api-events-security.md`
7. `rules/06-testing-and-verification.md`
8. `rules/07-service-domain-rules.md`
9. `rules/90-prompt-templates.md` only when writing or refining prompts.

If a nested `AGENTS.md` exists in a subfolder, apply the nested file first for that subfolder, then these root rules.

## Project Snapshot

- Backend project for a rental property / rooming house management SaaS in Vietnam.
- Maven multi-module backend with `shared` as the required foundation module and service modules such as `user-service`.
- Target architecture is microservices plus event-driven messaging.
- Current implementation uses Java 25, Spring Boot 4.0.6, Maven, PostgreSQL, Spring Data JPA, Flyway, MapStruct, Lombok, Spotless, and Palantir Java Format.
- Current dev infrastructure is in `deployment/docker-compose/infra.yml`.
- Treat `document/` and uploaded design docs as target design, not proof that every service already exists.

## Shared-First Rule

`shared` is the canonical foundation and contract module. Before creating any DTO, constant, enum, global config, exception/error code, utility, CRUD base, or reusable contract, inspect `shared` first.

- Put API request/response/command DTOs in `shared/src/main/java/sharing/dto/...`.
- Put roles, statuses, policy effects, device types, audit event types, and public API enums in `shared/src/main/java/sharing/enums/...`.
- Put API paths, table names, header names, topic names, service names, and common validation constants in `shared/src/main/java/sharing/constant/...`.
- Put domain error-code enums and shared exception helpers in `shared/src/main/java/sharing/exception/...`.
- Put global/reusable config in `shared/src/main/java/sharing/config/...`; service config is exceptional and must be justified.
- Reuse shared CRUD base classes for simple CRUD resources.
- Do not duplicate API DTOs, constants, enums, or domain error codes inside service modules unless they are private provider/client payloads.

## Source of Truth Precedence

When sources disagree, use this order:

1. Actual source code, `pom.xml`, `Taskfile.yml`, and `application.yaml`.
2. `README.md` and files under `document/`.
3. Uploaded product/design documents.
4. Official framework/library documentation.
5. General assumptions.

Never invent missing commands, versions, services, database tables, or conventions. If something is not in the repo, say it is planned or assumed.

## Required Workflow For Every Task

1. Inspect the relevant module before editing.
2. Inspect `shared` before creating DTOs, constants, enums, config, exceptions, utilities, or CRUD infrastructure.
3. Identify whether the requested feature belongs to an existing module or a planned service.
4. Make the smallest change that satisfies the task while preserving the shared-first architecture.
5. Preserve existing architecture, naming, package layout, and formatting.
6. Add or update tests for changed business logic.
7. Run relevant verification commands from `rules/06-testing-and-verification.md`.
8. Report exactly what changed, what was verified, and what could not be verified.

## Repository Layout

```text
backend/
|-- pom.xml
|-- Taskfile.yml
|-- README.md
|-- deployment/docker-compose/infra.yml
|-- document/
|-- rules/
|-- shared/
|   |-- pom.xml
|   `-- src/main/java/sharing/
|       |-- base/
|       |-- config/
|       |-- constant/
|       |-- dto/
|       |-- enums/
|       |-- exception/
|       `-- utils/
`-- user-service/
    |-- pom.xml
    |-- src/main/java/com/motel/user_service/
    |-- src/main/resources/application.yaml
    `-- src/main/resources/db/migration/
```

## Hard Rules

- Do not change architecture from microservices to a monolith.
- Do not join across service databases.
- Do not create cross-service database foreign keys for another service table.
- Do not edit files under `target/`.
- Do not commit generated build artifacts, local IDE files, or downloaded dependencies.
- Do not hardcode production secrets, tokens, API keys, encryption keys, or bank credentials.
- Do not log plaintext PII such as CCCD, phone number, bank account number, OAuth tokens, or webhook secrets.
- Do not bypass Flyway by relying on Hibernate `ddl-auto=create/update`.
- Do not duplicate shared DTOs, constants, enums, exception/error-code contracts, or utilities inside service modules.
- Do not remove or bypass shared CRUD base classes for simple CRUD resources.
- Do not create placeholder packages or classes for future integrations.
- Do not silently ignore failed tests or failed migrations.

## Default Verification Commands

Prefer Taskfile commands when available:

```bash
task build_shared
task format
task start_infra
task run_user
```

Direct Maven equivalents:

```bash
./mvnw -pl shared clean install
./mvnw spotless:apply
./mvnw test
./mvnw -pl user-service test
./mvnw -pl user-service spring-boot:run
```

If Maven wrapper is not executable in a copied or unzipped workspace, run `chmod +x mvnw shared/mvnw user-service/mvnw` before using it. Do not change file permissions in a real git repo unless needed.

## Before Finishing

- Code compiles or the failure is clearly explained.
- Formatting is applied or the formatter failure is clearly explained.
- `shared` is built before affected service tests when shared contracts changed.
- Flyway migrations are added for schema changes and split by bounded concern.
- Tests cover changed logic where practical.
- API paths stay under `/api/v1/...`.
- API contracts use shared DTOs, constants, and enums.
- Soft delete, audit, PII, and tenancy constraints are respected.
- Cross-service calls use REST or Kafka events, not DB joins.
- The final response lists changed files and verification results.
