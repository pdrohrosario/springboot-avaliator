# Repository Onboarding & AI Agent Instructions

> [!IMPORTANT]
> **MANDATORY FIRST-LOAD RULE:** This document is the absolute first source of truth and entry point that must be loaded, read, and validated by any AI agent or coding assistant before starting any task or executing any command in this repository. All other specialized prompts and agent definitions inherit from these global instructions.

These instructions are the repository onboarding source of truth for AI agents. Use them first and only search the repository when this file is incomplete or proven outdated.

## Source Of Truth In This Repo

Always align changes with these prompt files:
- [prompts/instructions-general-rules.md](./prompts/instructions-general-rules.md)
- [prompts/instructions-coding-commands.md](./prompts/instructions-coding-commands.md)
- [prompts/instructions-automated-tests.md](./prompts/instructions-automated-tests.md)
- [prompts/instructions-software-architecture.md](./prompts/instructions-software-architecture.md)
- [prompts/instructions-local-infra-automation.md](./prompts/instructions-local-infra-automation.md)
- [prompts/instructions-project-foundations.md](./prompts/instructions-project-foundations.md)
- [prompts/instructions-readme-from-foundations.md](./prompts/instructions-readme-from-foundations.md)

Priority rules to apply on every task:
- Keep scope minimal, safe, idempotent.
- Prefer simple and clear solutions (KISS, YAGNI).
- Respect OOP + SOLID + DRY when adding or changing code.
- Do not run destructive commands without explicit confirmation.
- Always report what changed, why, and how it was validated.

## High-Level Details

Repository summary:
- Avaliator is a Java microservices project for product catalog and product reviews.
- Architecture style: DDD + Hexagonal Architecture (Ports and Adapters).
- Services: catalogservice (port 8081) and feedbackservice (port 8882), backed by PostgreSQL.
- Local run modes: Docker Compose and Kubernetes (kind).
- CI/CD contract: Jenkins pipeline in [Jenkinsfile](../Jenkinsfile).

Project profile:
- Size: medium multi-module repository with 2 Spring Boot services plus infra/docs folders.
- Languages: Java, SQL, Bash, YAML.
- Runtime: Java 21.
- Frameworks/libraries: Spring Boot 3.5.x, Spring Data JPA, Flyway, OpenFeign, Actuator, JUnit.
- Infra: Docker, Docker Compose, Kubernetes kind, NGINX Ingress.

## Build And Validation Instructions

Tooling and versions (from repository files):
- Java: 21 (required by both service pom files).
- Maven: use service wrappers (`./mvnw`) when possible.
- Docker: required for Compose and image builds.
- Kubernetes local mode: kind + kubectl required; [k8s/setup-cluster.sh](../k8s/setup-cluster.sh) installs if missing.

### Bootstrap

Always do this first for local Compose:
1. Create `.env` from `.env.example`.
2. Fill at least `POSTGRES_USER`, `POSTGRES_PASSWORD`, `POSTGRES_DB`.
3. Ensure scripts are executable: `chmod +x scripts/*.sh k8s/setup-cluster.sh`.

Recommended bootstrap commands:
```bash
cp .env.example .env
chmod +x scripts/*.sh k8s/setup-cluster.sh
```

Kubernetes-specific bootstrap:
- Run `./k8s/setup-cluster.sh` before `./scripts/up.sh k8s`.
- Script behavior includes kind cluster creation, ingress install, namespace/postgres manifests.
- If local registry or jenkins containers do not exist, setup prints warnings (not fatal for all flows).

### Build

Always build each service from its own folder.

Preferred commands:
```bash
(cd catalogservice && ./mvnw clean package -DskipTests)
(cd feedbackservice && ./mvnw clean package -DskipTests)
```

Alternative used by CI:
```bash
(cd catalogservice && mvn clean package -DskipTests)
(cd feedbackservice && mvn clean package -DskipTests)
```

### Test

Always run tests per service after build:
```bash
(cd catalogservice && ./mvnw test)
(cd feedbackservice && ./mvnw test)
```

CI also runs `mvn test` per service in parallel.

### Run (Compose)

Preferred reproducible sequence:
```bash
./scripts/up.sh compose
./scripts/health.sh compose
./scripts/logs.sh compose
```

Stop/reset:
```bash
./scripts/down.sh compose
./scripts/reset.sh compose
```

### Run (Kubernetes kind)

Preferred reproducible sequence:
```bash
./k8s/setup-cluster.sh
./scripts/up.sh k8s
./scripts/health.sh k8s
./scripts/logs.sh k8s
```

Stop/reset:
```bash
./scripts/down.sh k8s
./scripts/reset.sh k8s
```

### Lint / Static Checks

No dedicated lint toolchain is explicitly defined at root. Use existing validations:
- Maven compile/package
- Maven test
- Docker/Compose config validity by running compose flow
- Kubernetes manifests validation through `kubectl apply` in setup/deploy flows

### Known Workarounds And Failure Prevention

- In Jenkins shell steps, always use POSIX source style `. ./.env` (not `source .env`) to avoid `/bin/sh` compatibility issues.
- For Compose mode, missing `.env` values cause startup/migration failures; always set DB variables before `up`.
- For K8s mode, always run setup first; deploying services without namespace/postgres/ingress baseline may fail.
- When changing Dockerfiles, rebuild images before rollout:
  - Compose: `./scripts/rebuild.sh compose`
  - K8s: `./scripts/rebuild.sh k8s`

### Validation Order For Code Changes

Always use this order for lower rejection risk:
1. Build changed service(s).
2. Run changed service tests.
3. Run both service tests if shared code/contracts changed.
4. Validate runtime mode impacted by the change (compose or k8s).
5. If endpoint behavior changed, run health and smoke checks.

## Project Layout And Architecture

Root critical files/folders:
- [README.md](../README.md): architecture and usage overview.
- [Jenkinsfile](../Jenkinsfile): CI/CD contract and deployment stages.
- [compose-standalone.yaml](../compose-standalone.yaml): local Docker Compose topology.
- [scripts/](../scripts): operational entrypoints (`up`, `down`, `logs`, `health`, `rebuild`, `reset`).
- [k8s/](../k8s): kind config, ingress, namespace, and service manifests.
- [db/init.sql](../db/init.sql): DB/schema bootstrap SQL.
- [project-foundation/](../project-foundation): architecture, DB, dependencies, infrastructure, user stories.
- [prompts/](./prompts): behavior and workflow rules for coding agents.

Service locations:
- [catalogservice](../catalogservice): product domain and APIs.
- [feedbackservice](../feedbackservice): review domain and integration with catalog via Feign.

Internal service architecture (both services):
- `domain/`: entities, value objects, domain rules.
- `application/`: use cases, ports, DTOs, mappers.
- `infrastruct/`: adapters (controllers, JPA, external clients).
- `config/`: configuration and error handling.

## CI/CD And Pre-Checkin Checks

Jenkins stages in [Jenkinsfile](../Jenkinsfile):
1. Checkout.
2. Generate configs via envsubst templates.
3. Build services in parallel.
4. Test services in parallel with JUnit report collection.
5. Build and push Docker images.
6. Deploy to Kubernetes and wait for rollout.

To replicate CI intent locally before check-in:
1. Build both services.
2. Test both services.
3. Build Docker images for impacted services.
4. Validate local deployment path (compose or k8s).

## Dependencies Not Obvious From Layout

- feedbackservice depends on catalogservice availability for product validation via OpenFeign.
- Both services depend on PostgreSQL schemas and Flyway migration correctness.
- K8s local workflow depends on kind network interplay with optional local registry/jenkins containers.

## Agent Operating Mode

- Trust this file first.
- Only search repository files when required information is missing or known to be wrong.
- Keep changes surgical and avoid unrelated refactors.
- If any command fails, capture the exact error, explain root cause, apply minimal fix, and rerun validation.
