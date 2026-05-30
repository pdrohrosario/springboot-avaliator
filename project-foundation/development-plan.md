# Development Plan — Avaliator

## Purpose

This document is the execution-oriented roadmap for remaining implementation work, based on project-foundation specifications and repository evidence.

Update rule:
- Keep this file live.
- Re-evaluate statuses whenever new code or infrastructure evidence appears.

## Current Implementation Snapshot

Date baseline: 2026-04-03.

| Area | Status | Evidence |
|---|---|---|
| catalogservice core (domain + app + infra) | completed | `catalogservice/src/main/java/**`, `catalogservice/src/test/java/**` |
| feedbackservice core (domain + app + infra) | completed | `feedbackservice/src/main/java/**`, `feedbackservice/src/test/java/**` |
| feedbackservice -> catalogservice validation (Feign) | completed | `feedbackservice/src/main/java/com/project/feedbackservice/review/infrastruct/output/adapter/product/ProductClient.java` |
| metricservice bootstrap app | in-progress | `metricservice/src/main/java/com/project/metricservice/MetricserviceApplication.java` |
| metricservice DB migrations | in-progress | `metricservice/src/main/resources/db/migration/V1__create_product_metric_table.sql`, `metricservice/src/main/resources/db/migration/V2__create_processed_review_event_table.sql` |
| metricservice migration test coverage | in-progress | `metricservice/src/test/java/com/project/metricservice/infrastruct/FlywayMigrationTests.java` |
| feedbackservice Kafka producer for ReviewCreated | not-started | no producer classes/usages in `feedbackservice/src/main/java/**` |
| metricservice Kafka consumer | not-started | no `@KafkaListener` usages in `metricservice/src/main/java/**` |
| metricservice domain/application/infrastructure implementation | not-started | no dedicated feature packages beyond application bootstrap |
| metricservice API for metrics query | not-started | no controller/use case/repository for metrics query |
| Compose/K8s/Jenkins integration for metricservice and Kafka | not-started | `compose-standalone.yaml`, `k8s/**`, `Jenkinsfile` currently include catalogservice + feedbackservice only |

## Gap Analysis

### Functional gaps

1. Missing ReviewCreated v1 integration contract in executable code shared by producer and consumer.
2. Missing event publication after review creation in feedbackservice.
3. Missing event consumption and idempotent update flow in metricservice.
4. Missing metrics query endpoint and response model for "no reviews yet" state.

### Architecture gaps

1. metricservice lacks full hexagonal layering (domain, application, infrastruct adapters).
2. Missing persistence adapters and repositories for `product_metric` and `processed_review_event`.
3. Missing explicit transactional boundary for aggregate update + processed event insertion.

### Quality/validation gaps

1. Missing unit tests for metricservice use cases.
2. Missing Kafka integration tests (producer and consumer).
3. Missing contract tests for ReviewCreated v1 payload compatibility.

### Infrastructure gaps

1. No Kafka service in Compose topology.
2. No Kafka/metricservice manifests in Kubernetes.
3. No metricservice stages in Jenkins pipeline.

## Updated Timeline (Ordered Phases)

| Phase | Name | Status | Depends on |
|---|---|---|---|
| 0 | Contract and planning freeze | in-progress | none |
| 1 | Metricservice domain and application core | not-started | 0 |
| 2 | Metricservice persistence and transactional idempotency | not-started | 1 |
| 3 | Feedbackservice ReviewCreated producer | not-started | 0 |
| 4 | Metricservice Kafka consumer | not-started | 1,2,3 |
| 5 | Metrics query API | not-started | 2 |
| 6 | Runtime infrastructure integration (Compose/K8s) | not-started | 3,4,5 |
| 7 | CI/CD integration | not-started | 6 |
| 8 | Resilience and observability hardening | not-started | 4,5,6 |

## Stage Checklist

### Phase 0 — Contract and planning freeze

Objective:
- Remove ambiguity before implementation starts.

Tasks:
- Define canonical ReviewCreated v1 payload and validation rules.
- Confirm topic naming, partition key, retry and DLQ policy.
- Align project-foundation docs with actual metricservice status.

Validation criteria:
- Contract documented and approved.
- No unresolved decision on payload fields and error handling.

Deliverables:
- Approved contract section in foundation docs.
- Updated timeline statuses.

### Phase 1 — Metricservice domain and application core

Objective:
- Implement pure business flow independent of Kafka transport.

Tasks:
- Create domain model for product metrics aggregate and invariants.
- Implement application use case: consume normalized event input and update aggregate state.
- Define input/output ports.

Validation criteria:
- Unit tests for aggregate calculations and invariants.
- Unit tests for idempotency decision path.

Deliverables:
- Domain + application packages in metricservice.
- Green unit test suite for business rules.

### Phase 2 — Persistence and transactional idempotency

Objective:
- Persist metric updates safely and atomically.

Tasks:
- Implement repositories/adapters for `product_metric` and `processed_review_event`.
- Enforce transaction: update metric + register processed review in same commit.
- Map DB entities and mappers.

Validation criteria:
- Integration tests proving duplicate `review_id` does not double count.
- Integration tests for constraint and rollback behavior.

Deliverables:
- Persistence adapters and integration tests.

### Phase 3 — Feedbackservice producer

Objective:
- Publish ReviewCreated when review is successfully created.

Tasks:
- Add publisher port and Kafka adapter in feedbackservice.
- Emit event after successful write path.
- Add serialization/validation safety for payload.

Validation criteria:
- Unit tests for publisher invocation and payload mapping.
- Integration tests with Kafka test utilities.

Deliverables:
- Producer implementation and tests in feedbackservice.

### Phase 4 — Metricservice consumer

Objective:
- Consume event and delegate to application use case.

Tasks:
- Add Kafka listener adapter in metricservice.
- Wire listener to use case and transaction boundary.
- Commit processing only after successful persistence.

Validation criteria:
- Integration tests for happy path and duplicate event path.
- Integration tests for malformed event handling.

Deliverables:
- Consumer implementation and tests.

### Phase 5 — Metrics query API

Objective:
- Expose consolidated metrics for internal consumption.

Tasks:
- Add query use case, controller, response DTO.
- Implement explicit NO_REVIEWS_YET business state.

Validation criteria:
- Web layer tests for response contract.
- Integration test against populated and empty states.

Deliverables:
- Query endpoint and tests.

### Phase 6 — Runtime infrastructure integration

Objective:
- Make local and K8s runtime support metricservice and Kafka.

Tasks:
- Update Compose with Kafka and metricservice services.
- Add K8s manifests for metricservice and Kafka (or existing broker strategy).
- Add health probes and dependencies.

Validation criteria:
- `scripts/up.sh compose` starts all components.
- `scripts/up.sh k8s` deploys and reaches healthy state.

Deliverables:
- Updated Compose and K8s manifests.

### Phase 7 — CI/CD integration

Objective:
- Add metricservice to build/test/image/deploy pipeline.

Tasks:
- Add metricservice build and test stages in Jenkinsfile.
- Add image build/push and deployment rollout checks.

Validation criteria:
- Jenkins pipeline executes metricservice stages successfully.
- JUnit reports collected for metricservice tests.

Deliverables:
- Updated Jenkins pipeline.

### Phase 8 — Resilience and observability hardening

Objective:
- Make event flow operable under failure conditions.

Tasks:
- Add retry/backoff and DLQ behavior.
- Emit operational metrics and correlation logs.
- Document runbook notes for failure scenarios.

Validation criteria:
- Tests for transient failure retry and non-recoverable routing.
- Observable counters/logs for processed, duplicate, retried, DLQ events.

Deliverables:
- Resilience configs, observability artifacts, and tests.

## Requirement -> Task -> Validation Traceability

| Requirement | Implementation task | Validation gate |
|---|---|---|
| US-05 update metrics from event | phases 1,2,4 | unit + integration Kafka + DB idempotency tests |
| US-06 query metrics | phase 5 | WebMvc + integration tests |
| idempotent processing | phases 2,4 | duplicate review tests and transactional integrity checks |
| eventual consistency via async messaging | phases 3,4,8 | producer/consumer integration tests + retry/DLQ tests |
| operational reliability | phases 6,7,8 | compose/k8s health checks + CI pipeline success |

## Risks, Assumptions, Open Questions

Risks:
1. Contract drift between producer and consumer payloads.
2. Double-count under reprocessing if transaction boundaries are wrong.
3. Runtime complexity increase when adding Kafka to local and K8s flows.

Assumptions:
1. At-least-once delivery semantics will be used.
2. `review_id` is the primary idempotency key.
3. metricservice remains inside the same repository lifecycle as other services.

Open questions:
1. Should event publish failure block review creation, or use eventual recovery strategy?
2. Which exact topic naming convention and partition strategy should be adopted?
3. Is transactional outbox required in this phase or planned as follow-up?

## Recommended Next Immediate Step

Implement Phase 0 now:
1. finalize ReviewCreated v1 contract,
2. lock producer/consumer payload rules,
3. record decision in project-foundation and keep this timeline as the execution source.