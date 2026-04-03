# Architecture — Avaliator

## Architectural Pattern

Services follow Hexagonal Architecture (Ports and Adapters) combined with DDD.

- Driving side: HTTP controllers and async consumers.
- Core domain: entities, value objects, aggregates, and business rules.
- Application: use cases and ports orchestrating domain operations.
- Driven side: persistence adapters, Feign adapters, broker adapters.

## Service Landscape

| Service | Port | Status | Responsibility |
|---|---|---|---|
| catalogservice | 8081 | Implemented | Product lifecycle, catalog queries |
| feedbackservice | 8882 | Implemented | Review lifecycle, product validation via Feign |
| metricservice | TBD | Planned | Asynchronous consolidation of product rating metrics |

## Implementation Status

Current state (as-is):

- Implemented services: catalogservice and feedbackservice.
- Current integration: synchronous validation from feedbackservice to catalogservice (OpenFeign).
- Database: PostgreSQL 15 with `catalog_schema` and `feedback_schema`.
- Migrations managed by Flyway per service.
- Deployment: Docker Compose and Kubernetes (kind).
- CI/CD: Jenkins pipeline covering build, test, image push, and K8s deploy for both services.

Target state (to-be):

- Add metricservice as independent bounded context.
- feedbackservice emits review-created events via Kafka.
- metricservice consumes events and exposes aggregated metrics query API.
- Add `metrics_schema` to PostgreSQL.
- Add Kafka broker to compose and K8s topology.

## Key Boundaries

- Domain is framework-agnostic; no Spring/JPA/Kafka imports in domain layer.
- Use cases depend on ports (abstractions), not concrete adapters.
- Controllers, listeners, and JPA entities remain in infrastructure layer.
- No cross-service database access; each service owns its own schema.
- Inter-service communication via explicit contracts (Feign or event).

---

## Catalogservice — Detailed Architecture

### Package Structure

```
com.project.catalogservice.product
├── domain/                    # Aggregate, Value Objects, Enums, Exceptions
├── application/
│   ├── ports/input/           # Use case interfaces (driving)
│   ├── ports/output/          # Repository abstractions (driven)
│   ├── useCases/              # Use case implementations
│   ├── input/                 # Input DTOs (records)
│   ├── output/                # Output DTOs (records)
│   └── mapper/                # Domain ↔ DTO mapping
├── infrastruct/
│   ├── input/input/           # HTTP controller, request/response records, mapper
│   └── input/output/          # JPA entities, JPA repository, persistence adapters
├── config/
│   └── exception/             # @RestControllerAdvice, ErrorMessage
└── common/
    ├── domain/                # Entity, AggregateRoot, ValueObject, DomainEvent base classes
    ├── output/                # PaginatedResponse record
    └── infrastruct/           # EventPublisher
```

### Domain Layer

Product (AggregateRoot):

- id: ProductId (Value Object wrapping UUID)
- name: String (required, unique, max 50 chars)
- price: BigDecimal (required, non-negative)
- description: String (optional)
- category: ProductCategory enum (ELECTRONICS, CLOTHING, TOYS, BOOKS, SPORTS_EQUIPMENT)
- status: ProductStatus enum (AVAILABLE, SOLD_OUT, INACTIVE) — defaults to AVAILABLE
- createdAt: LocalDate (auto-generated)

Factory methods:

- `Product.create(name, price, description, category)` — generates ProductId, validates, sets defaults.
- `Product.fromEntity(id, name, price, description, category, status, createdAt)` — reconstitution from persistence.

Domain exceptions:

- `ProductNotFound` — product ID does not match any record.
- `ProductAlreadyExistsException` — duplicate product name.

Repository contract (domain interface):

- `ProductRepository` — save, findById, findByName, findProductsByNameAndDescription (paginated).

### Application Layer

Input ports (driving interfaces):

- `CreateProduct` — receives CreateProductInput, returns CreateProductOutput.
- `GetProductById` — receives String id, returns GetProductOutput.
- `GetProductsByNameAndDescription` — receives filter input, returns PaginatedResponse of GetProductOutput.
- `UpdateProduct` — receives Product, returns Product.

Output ports (driven interfaces):

- `SaveProduct` — persists a Product domain object.
- `FindById` — finds Product by ProductId.
- `FindProductByName` — finds Product by name (uniqueness check).
- `FindProductsByNameAndDescription` — paginated search with name/description filters.

Use cases:

- `CreateProductUseCase` — checks name uniqueness via FindProductByName, creates domain Product, persists via SaveProduct.
- `GetProductByIdUseCase` — converts string to ProductId, fetches via FindById, throws ProductNotFound if absent.
- `GetProductsByUsernameAndDescriptionUseCase` — delegates paginated query and maps results.

DTOs (records):

- `CreateProductInput(name, price, description, category)`
- `CreateProductOutput(id, name, price, description, category, status, createdAt)`
- `GetProductOutput(id, name, price, description, category, status, createdAt)`
- `GetProductsByNameAndDescriptionInput(name, description, page, size, sort)`

### Infrastructure Layer

HTTP adapter (controller):

- `ProductController` (@RestController, base path `/product`)
  - `POST /product/create` — 201 Created
  - `GET /product/{id}` — 200 OK
  - `GET /product/get-products?name=&description=&page=&size=&sort=` — 200 OK (paginated)

JPA persistence:

- `JpaProduct` (@Entity, table `product`, schema `catalog_schema`) — maps all domain fields.
- `JpaProductRepository` (extends JpaRepository) — `findByName`, custom JPQL `searchByNameAndDescription`.
- Adapters: `SaveProductAdapter`, `FindByIdAdapter`, `FindProductByNameAdapter`, `FindProductsByNameAndDescriptionAdapter`.

Mapper classes:

- `ProductControllerMapper` — request ↔ input, output ↔ response.
- `ProductUseCaseMapper` — domain ↔ output DTOs.
- `ProductPersistenceMapper` — domain ↔ JPA entity.

Config:

- `CustomExceptionHandler` (@RestControllerAdvice) — handles MethodArgumentNotValidException (400), ProductAlreadyExistsException (400), IllegalArgumentException (400), ProductNotFound (404).
- `ErrorMessage` record — message, description, timestamp.

Common base:

- `Entity<T>` — abstract base with generic ID.
- `AggregateRoot<T>` — extends Entity, manages domain events list.
- `ValueObject` — abstract base for value objects.
- `DomainEvent` — abstract base with occurredAt timestamp.
- `EventPublisher` — wraps Spring ApplicationEventPublisher.
- `PaginatedResponse<T>(items, currentPage, hasNextPage)` — generic pagination wrapper.

---

## Feedbackservice — Detailed Architecture

### Package Structure

```
com.project.feedbackservice.review
├── domain/                    # Aggregate, Value Objects, Exceptions
├── application/
│   ├── ports/input/           # Use case interface
│   ├── ports/output/          # Repository and integration abstractions
│   ├── useCases/              # Use case implementation
│   ├── input/                 # Input DTO
│   ├── output/                # Output DTO
│   └── mapper/                # Domain ↔ DTO mapping
├── infrastruct/
│   ├── input/                 # HTTP controller, request/response records
│   ├── output/
│   │   ├── adapter/           # SaveReviewAdapter
│   │   │   └── product/       # Feign client + FindProductByIdAdapter
│   │   ├── entities/          # JpaReview
│   │   ├── repository/        # JpaReviewRepository, ReviewRepositoryImpl
│   │   └── mapper/            # ReviewPersistenceMapper
│   └── mapper/                # ReviewControllerMapper
├── config/
│   ├── exception/             # CustomExceptionHandler, ErrorMessage, ApiIntegrationException, ResourceNotFoundException
│   ├── FeignClientConfiguration.java
│   └── CustomFeignErrorDecoder.java
└── common/
    ├── domain/                # Entity, AggregateRoot, ValueObject, DomainEvent
    ├── output/                # PaginatedResponse
    └── infrastruct/           # EventPublisher
```

### Domain Layer

Review (AggregateRoot):

- id: ReviewId (Value Object wrapping UUID)
- productId: ProductId (Value Object wrapping UUID)
- rating: int (1..5, validated)
- comment: String (required, max 500 chars)
- createdAt: LocalDate (auto-generated)

Factory methods:

- `Review.create(productId, rating, comment)` — generates ReviewId, validates all fields.
- `Review.fromEntity(id, productId, rating, comment, createdAt)` — reconstitution from persistence.

Domain exceptions:

- `ProductNotFoundException` — product ID does not exist in catalogservice.
- `ProductIdIsNotValidException` — malformed UUID for product ID.

Repository contract:

- `ReviewRepository` — save, findById, findReviewsByProductId (paginated).

### Application Layer

Input ports:

- `CreateReview` — receives CreateReviewInput, returns CreateReviewOutput.

Output ports:

- `SaveReview` — persists a Review domain object.
- `FindProductById` — validates product existence (returns boolean).

Use case:

- `CreateReviewUseCase` — parses and validates ProductId, checks product existence via FindProductById (Feign), creates Review, persists via SaveReview.

DTOs (records):

- `CreateReviewInput(productId, rating, comment)`
- `CreateReviewOutput(reviewId, productId, rating, comment, createdAt)`

### Infrastructure Layer

HTTP adapter (controller):

- `ReviewController` (@RestController, base path `/review`)
  - `POST /review/create` — 201 Created

Request/response records:

- `CreateReviewRequest(productId @NotBlank, rating @NotNull, comment)`
- `CreateReviewResponse(reviewId, productId, rating, comment, createdAt)`
- `GetProductResponse` — maps catalogservice product response for Feign.

JPA persistence:

- `JpaReview` (@Entity, table `review`, schema `feedback_schema`) — id, productId, rating, comment, createdAt.
- `JpaReviewRepository` (extends JpaRepository).
- `ReviewRepositoryImpl` — implements domain ReviewRepository using JPA.
- `SaveReviewAdapter` — implements SaveReview port.

Feign integration:

- `ProductClient` (@FeignClient, url `http://catalogservice:8081`) — `GET /product/{id}`.
- `FindProductByIdAdapter` — implements FindProductById, catches `ResourceNotFoundException` → `ProductNotFoundException`, `RetryableException` → `ApiIntegrationException`.
- `CustomFeignErrorDecoder` — maps HTTP status to domain-relevant exceptions (400→IllegalArgument, 404→ResourceNotFound, 5xx→ApiIntegration).
- `FeignClientConfiguration` — registers the custom error decoder.

Config:

- `CustomExceptionHandler` (@RestControllerAdvice) — handles MethodArgumentNotValidException (400), ProductNotFoundException (404), IllegalArgumentException (400), ApiIntegrationException (503).
- `ErrorMessage`, `ApiIntegrationException`, `ResourceNotFoundException`.

Common base (same structure as catalogservice):

- `Entity<T>`, `AggregateRoot<T>`, `ValueObject`, `DomainEvent`, `EventPublisher`, `PaginatedResponse`.

---

## Inter-service Flows

### Current: feedbackservice → catalogservice (Feign)

1. Client sends `POST /review/create` to feedbackservice.
2. `CreateReviewUseCase` parses ProductId and calls `FindProductById` port.
3. `FindProductByIdAdapter` calls `ProductClient.getProductById(id)` — synchronous HTTP to catalogservice.
4. If product exists, review is created and persisted.
5. If product not found, `ProductNotFoundException` (404) is returned.
6. If catalogservice is unreachable, `ApiIntegrationException` (503) is returned.

### Planned: feedbackservice → metricservice (Kafka event)

1. After review creation, feedbackservice publishes `ReviewCreated v1` event to Kafka topic.
2. metricservice consumes the event asynchronously.
3. metricservice updates pre-computed `ProductMetrics` aggregate idempotently.

---

## Metricservice — Planned Architecture

Business scope mapped to architecture:

- US-05: update product metrics from review-created event.
- US-06: query consolidated product rating metrics.

Constraints:

- Asynchronous processing with eventual consistency.
- Idempotent event handling.
- Safe reprocessing after temporary failures.
- Minimal and evolvable design aligned to DDD + Hexagonal.

## Decision Log (ADR-style)

### ADR-01 — Dedicated bounded context for metrics

Decision:
- Introduce metricservice as an independent bounded context.

Rationale:
- Keeps single responsibility and isolates analytical aggregation logic.

Trade-off:
- Additional operational footprint (deployment and monitoring).

### ADR-02 — Integration by ReviewCreated v1 event

Decision:
- feedbackservice publishes ReviewCreated events to a broker topic.

Rationale:
- Decouples write path from aggregation and supports eventual consistency.

Trade-off:
- Distributed troubleshooting across producer, broker, and consumer.

### ADR-03 — At-least-once delivery with idempotent consumer

Decision:
- Accept possible redelivery and ensure consumer-side idempotency.

Rationale:
- Simpler and more robust than end-to-end exactly-once.

Trade-off:
- Requires processed-event registry and transactional safeguards.

### ADR-04 — Pre-computed aggregate per product

Decision:
- Persist product-level consolidated metrics for read optimization.

Rationale:
- Fast and deterministic query for internal consumers.

Trade-off:
- Slightly more complex write path.

### ADR-05 — Explicit no-reviews business state

Decision:
- Return an explicit state for products without any reviews.

Rationale:
- Removes ambiguity between "no data" and valid zero values.

Trade-off:
- API response includes one additional semantic field.

## Logical Component Map

### Domain Layer (metrics domain)

- ProductMetrics aggregate
	- productId
	- totalReviews
	- ratingSum
	- averageRating
	- ratingDistribution (buckets 1..5)
	- updatedAt
- ProcessedReviewEvent entity
	- reviewId (idempotency key)
	- eventId (optional secondary key)
	- processedAt

Domain invariants:

- rating must be in range 1..5.
- totalReviews increases exactly once per unique reviewId.
- ratingDistribution bucket must match received rating.
- averageRating must be recalculated after each valid update.

### Application Layer

Input ports:

- ConsumeReviewCreated
- GetProductMetrics

Output ports:

- LoadOrCreateProductMetrics
- SaveProductMetrics
- RegisterProcessedReviewEvent
- ExistsProcessedReviewEvent

Use cases:

- ConsumeReviewCreatedUseCase
	- validates event contract
	- checks idempotency
	- updates aggregate
	- persists aggregate and processed-event marker atomically
- GetProductMetricsUseCase
	- returns consolidated metrics
	- returns explicit NO_REVIEWS_YET state when applicable

### Infrastructure Layer

Driving adapters:

- Broker consumer adapter (Kafka listener).
- HTTP query adapter (REST controller for internal consumers).

Driven adapters:

- JPA repository adapters for product metrics and processed events.

Config:

- Broker, retry, and DLQ settings.
- Exception handling strategy for contract and integration failures.
- Health/readiness checks for database and broker connectivity.

## Interface and Integration Contracts

### Event Contract — ReviewCreated v1

Envelope:

- eventId: UUID
- eventType: REVIEW_CREATED
- eventVersion: 1
- occurredAt: timestamp
- producer: feedbackservice

Payload:

- reviewId: UUID
- productId: UUID
- rating: integer (1..5)
- createdAt: timestamp

Integration rules:

- Partition key: productId (ordering guarantee per product stream).
- Delivery semantic: at-least-once.
- Invalid payload/schema: route to DLQ with failure metadata.
- Retry policy: exponential backoff for transient failures.
- Offset commit: only after successful transactional persistence.

### Query Contract — Product Metrics

Endpoint:

- GET /metrics/product/{productId}

Success response (with reviews):

- productId
- totalReviews
- averageRating
- ratingDistribution (1..5)
- consistency: EVENTUAL

Success response (without reviews):

- productId
- state: NO_REVIEWS_YET
- totalReviews: 0
- averageRating: null
- ratingDistribution all buckets as 0

## Data Consistency and Failure Handling

Transactional strategy in consumer flow:

1) Check whether reviewId is already processed.
2) If not processed, update ProductMetrics aggregate.
3) Persist aggregate and processed-event row in one transaction.
4) Commit consumer offset only after transaction success.

Duplicate event behavior:

- If reviewId already exists in processed_review_events, skip metric mutation.

Failure behavior:

- Transient DB/broker issues: retry with backoff.
- Non-recoverable contract failures: send message to DLQ and continue stream.

## Data Model (metrics schema)

Schema:

- metrics_schema

Tables:

- product_metrics
	- product_id (PK)
	- total_reviews (bigint)
	- rating_sum (bigint)
	- average_rating (numeric)
	- rating_1_count .. rating_5_count (bigint)
	- updated_at (timestamp)
- processed_review_events
	- review_id (PK or unique)
	- event_id (unique, optional)
	- processed_at (timestamp)

Index recommendations:

- Unique index on review_id.
- Primary key index on product_id.

## NFR and Risk Matrix

### Reliability

- Idempotent consumer semantics.
- Retry and DLQ strategy.
- Atomic persistence before offset commit.

### Maintainability

- Clear layer boundaries and port contracts.
- Versioned event schema.
- Centralized domain rules in aggregate.

### Observability

Expose and monitor:

- consumed events count
- duplicate events ignored count
- processing latency
- retry count and DLQ count
- consumer lag

Log correlation fields:

- eventId, reviewId, productId, traceId

### Performance

- Read model is pre-aggregated for O(1) query per product.
- Horizontal scale through topic partitions.

### Security

- Internal-only metrics endpoint exposure.
- No sensitive data in events.
- Avoid full payload logging on failures.

### Main Risks and Mitigations

- Risk: producer writes review but publish fails.
	- Mitigation: evolve to transactional outbox in feedbackservice.
- Risk: event schema drift.
	- Mitigation: schema versioning and contract tests.
- Risk: concurrent updates for same product.
	- Mitigation: partition by productId and transactional updates.

## TDD Handoff Checklist (SDET-first)

Priority 1 — Domain behavior:

- Average and distribution updates are correct.
- Rating out-of-range is rejected.
- NO_REVIEWS_YET state is returned correctly.

Priority 2 — Idempotency behavior:

- Duplicate reviewId does not mutate metrics.
- Reprocessing after failure preserves correctness.

Priority 3 — Async integration behavior:

- Valid event updates metrics.
- Transient failure path retries and recovers.
- Non-recoverable event path moves to DLQ.

Priority 4 — Contract behavior:

- ReviewCreated v1 producer/consumer contract tests.
- HTTP response contract tests for both states.

Priority 5 — Non-functional behavior:

- Basic throughput and lag assertions.
- Required telemetry emitted (metrics and structured logs).
