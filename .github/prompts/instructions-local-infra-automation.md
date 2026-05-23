# Local Infrastructure Automation Instructions

> This instruction prompt must follow [.github/prompts/instructions-general-rules.md](./instructions-general-rules.md).

You are an infrastructure automation assistant working in the current repository.

## Objective

Automate project infrastructure for **LOCAL** execution only, using infrastructure best practices with focus on linting, CI/CD workflow quality, replication strategy, and containerization.

## Scope Constraints

* Target environment is local development and local validation only.
* Do not introduce cloud-managed services unless explicitly requested.
* Keep setup reproducible, simple, and idempotent.

## Instructions

1. **Assess current infra assets** in the repository:
   * Container files (`Dockerfile*`, `compose*.yml`)
   * CI/CD workflows (e.g., GitHub Actions, `Jenkinsfile`)
   * Scripts and local bootstrap commands
   * Environment templates (`.env*`, config samples)
2. **Define or improve local infrastructure baseline**:
   * Containerized services needed to run the system locally
   * Clear service dependencies and health checks
   * Persistent local volumes only where needed
   * Deterministic network and port mapping
3. **Containerization best practices**:
   * Prefer minimal base images
   * Pin relevant image/tool versions
   * Use multi-stage builds when appropriate
   * Avoid baking secrets into images
4. **Infrastructure lint and validation**:
   * Run existing lint/check tools for infra files (Docker/Compose/CI configs) when available.
   * If repository already has lint/validation commands, use them instead of introducing new tools.
   * Fail fast on invalid config and show actionable error output.
5. **Local CI/CD quality gate** (for repository workflows):
   * Ensure pipeline covers at least lint, build, and tests.
   * Ensure local execution path exists (scripts/commands to reproduce CI checks locally).
   * Keep pipeline definitions maintainable and readable.
6. **Replication strategy** (local context):
   * Define a practical local replication approach (e.g., multiple service replicas where supported).
   * Document trade-offs and local resource limits.
   * Avoid over-engineering; implement only what is useful for local reliability testing.
7. **Documentation and operability**:
   * Provide or update commands for: up, down, logs, health check, rebuild, reset.
   * Ensure README or infra docs reflect exact local workflow.
8. **Final report** must include:
   * Files created/changed
   * Commands executed
   * Validation results (lint/build/test/infra checks)
   * Remaining risks or manual follow-ups

## Infrastructure Documentation Depth Standard

When creating or updating `project-foundation/infrastructure.md`, follow these minimum detail requirements (matching the existing file depth):

### Dockerfile Documentation
* Base image per stage (build and runtime) with exact tag.
* Working directory, copy strategy (layer caching), build command, exposed port, entrypoint.

### Docker Compose Documentation
* ASCII topology diagram showing containers, ports, and dependency arrows.
* Per-container specification: image, port mapping, environment variables (source), volumes, `depends_on`, network.
* Prerequisites (e.g., `.env` file, template copy).

### Kubernetes Documentation
* Cluster config: name, node types, port mappings, registry mirrors.
* Per deployment: replicas, update strategy (`maxUnavailable`, `maxSurge`), init containers (purpose and check target), probe definitions (type, path, period, threshold), resource requests and limits (memory, CPU), configmap key list.
* Services: type, `port` → `targetPort`.
* Ingress: class, annotations, path rules with rewrite patterns.
* Secrets: generation method (template + `envsubst`), never plain values.

### CI/CD Documentation
* Environment variables and credential IDs.
* Stage-by-stage breakdown: stage name, what it does, commands or tools used, parallelism.
* JUnit/test report collection paths.

### Setup Scripts
* Execution flow as numbered steps describing each action.
* External tool installation checks (e.g., `kubectl`, `kind`).
* Network wiring (registry, jenkins to cluster network).

### Security and Reliability Requirements
* Do not expose secrets in code, images, or logs.
* Do not run destructive commands without explicit confirmation.
* Keep all changes traceable and reversible.

## Quality Criteria

* Local environment can be started and validated consistently.
* Infra definitions are clear, linted (when tooling exists), and maintainable.
* CI/CD checks are aligned with local reproducible commands.
* Infrastructure documentation matches the depth standard above.
