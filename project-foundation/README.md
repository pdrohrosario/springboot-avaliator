# Project Foundation — Avaliator

This directory is the source of truth for architecture, database, dependencies, user stories, and infrastructure of the Avaliator microservices project.

## Status Convention

- **As-is (current state)**: reflects what is implemented, tested, and versioned in the repository.
- **To-be (planned state)**: reflects approved evolution not yet coded (e.g., metricsservice).

## Services Covered

| Service | Status | Foundation scope |
|---|---|---|
| catalogservice | Implemented | Full architecture, DB schema, dependencies, infra |
| feedbackservice | Implemented | Full architecture, DB schema, dependencies, infra |
| metricsservice | Planned | Architecture design, DB model, dependency forecast |

## Index

| File | Scope |
|---|---|
| [architecture.md](./architecture.md) | Architectural style, per-module package structure, domain/application/infrastructure layers, inter-service flows, ADRs, metrics design |
| [database.md](./database.md) | Schemas, table definitions with column types, Flyway migrations, JPA mappings, connectivity config |
| [dependencies.md](./dependencies.md) | Per-service dependency tables with versions and scopes, inter-service integration libraries |
| [user-stories.md](./user-stories.md) | Product requirements and acceptance criteria (US-01 through US-06) |
| [infrastructure.md](./infrastructure.md) | Dockerfiles, Docker Compose topology, Kubernetes manifests detail, CI/CD pipeline stages, setup scripts |

## Usage Rules

- Read these files before changing core behavior.
- Preserve Hexagonal Architecture boundaries.
- Keep Flyway migrations immutable.
- Never hardcode secrets.
- Prefer incremental edits over full-file rewrites when updating these docs.
