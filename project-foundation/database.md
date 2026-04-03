# Database — Avaliator

## Overview

The system uses PostgreSQL 15 (Alpine) with a single database instance (`avaliator`) and isolated schemas per bounded context:

| Schema | Service | Status |
|---|---|---|
| `catalog_schema` | catalogservice | Implemented |
| `feedback_schema` | feedbackservice | Implemented |
| `metrics_schema` | metricservice | Planned |

## Initialization

`db/init.sql` bootstraps the database and schemas. It is mounted by both Docker Compose and Kubernetes PostgreSQL.

Content of `init.sql`:

```sql
SELECT 'CREATE DATABASE avaliator'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'avaliator');

CREATE SCHEMA IF NOT EXISTS catalog_schema;
CREATE SCHEMA IF NOT EXISTS feedback_schema;
```

`metrics_schema` must be introduced through a controlled migration/update step when metricservice is added.

## Flyway Configuration

Each service manages its own Flyway migrations independently.

Rules:

- Migration naming: `V{n}__description.sql`.
- Never edit an applied migration.
- Always create a new migration version for changes.

Flyway properties (both services):

- `spring.flyway.schemas` — set to the service's own schema.
- `spring.flyway.locations` — `classpath:db/migration`.
- Migrations located under `src/main/resources/db/migration/`.

## Catalog Schema — `catalog_schema`

### Table: `product`

Flyway migration: `V1__create_product_table.sql`

```sql
CREATE TABLE product (
    id          UUID            PRIMARY KEY,
    name        VARCHAR(255)    NOT NULL,
    price       NUMERIC(15,2)  NOT NULL,
    description TEXT,
    category    VARCHAR(50)     NOT NULL,
    status      VARCHAR(50)     NOT NULL,
    created_at  DATE
);
```

Column details:

| Column | Type | Constraints | Domain mapping |
|---|---|---|---|
| `id` | UUID | PK | ProductId (Value Object) |
| `name` | VARCHAR(255) | NOT NULL | Unique per domain rule (max 50 chars validated in domain) |
| `price` | NUMERIC(15,2) | NOT NULL | Non-negative validated in domain |
| `description` | TEXT | nullable | Optional field |
| `category` | VARCHAR(50) | NOT NULL | ProductCategory enum (ELECTRONICS, CLOTHING, TOYS, BOOKS, SPORTS_EQUIPMENT) |
| `status` | VARCHAR(50) | NOT NULL | ProductStatus enum (AVAILABLE, SOLD_OUT, INACTIVE); default AVAILABLE |
| `created_at` | DATE | nullable | Auto-generated on creation |

JPA mapping: `JpaProduct` entity, schema `catalog_schema`.

Notes:

- Name uniqueness is enforced at application level (use case checks before insert), not by database constraint.
- Category and status are stored as strings; enum mapping is handled in JPA entity.

## Feedback Schema — `feedback_schema`

### Table: `review`

Flyway migration: `V1__create_review_table.sql`

```sql
CREATE TABLE review (
    id          UUID        PRIMARY KEY,
    product_id  UUID        NOT NULL,
    rating      SMALLINT    NOT NULL,
    comment     TEXT,
    created_at  DATE
);
```

Column details:

| Column | Type | Constraints | Domain mapping |
|---|---|---|---|
| `id` | UUID | PK | ReviewId (Value Object) |
| `product_id` | UUID | NOT NULL | ProductId (Value Object); validated via Feign, no FK |
| `rating` | SMALLINT | NOT NULL | Range 1..5 validated in domain |
| `comment` | TEXT | nullable | Required by domain (max 500 chars), but DB allows null |
| `created_at` | DATE | nullable | Auto-generated on creation |

JPA mapping: `JpaReview` entity, schema `feedback_schema`.

Notes:

- `product_id` has no foreign key to `catalog_schema.product`; referential integrity is ensured by feedbackservice calling catalogservice via Feign before persisting.
- `comment` is required at domain level (Review.create validates non-blank, max 500), but the column is TEXT without NOT NULL.

## Logical Integrity

No cross-schema foreign keys exist. Referential integrity per schema boundary:

- `review.product_id` → validated by Feign call to catalogservice (synchronous).
- `metrics_schema` data (planned) → validated from Kafka event contract, no FK to other schemas.

## Metrics Schema — Planned

Status: not yet provisioned in `init.sql` or Flyway migrations.

### Table: `product_metrics`

| Column | Type | Constraints | Description |
|---|---|---|---|
| `product_id` | UUID | PK | Aggregate key |
| `total_reviews` | BIGINT | NOT NULL | Count of unique reviews processed |
| `rating_sum` | BIGINT | NOT NULL | Sum of all ratings |
| `average_rating` | NUMERIC | NOT NULL | Pre-computed average |
| `rating_1_count` | BIGINT | NOT NULL | Distribution bucket: rating 1 |
| `rating_2_count` | BIGINT | NOT NULL | Distribution bucket: rating 2 |
| `rating_3_count` | BIGINT | NOT NULL | Distribution bucket: rating 3 |
| `rating_4_count` | BIGINT | NOT NULL | Distribution bucket: rating 4 |
| `rating_5_count` | BIGINT | NOT NULL | Distribution bucket: rating 5 |
| `updated_at` | TIMESTAMP | NOT NULL | Last update timestamp |

### Table: `processed_review_events`

| Column | Type | Constraints | Description |
|---|---|---|---|
| `review_id` | UUID | PK or UNIQUE | Idempotency key |
| `event_id` | UUID | UNIQUE, nullable | Optional secondary dedup key |
| `processed_at` | TIMESTAMP | NOT NULL | Processing timestamp |

Idempotency guarantee:

- A duplicated `review_id` must not mutate `product_metrics` more than once.
- Aggregate update and processed-event registration must be atomic (single transaction).

## Database Connectivity

### Docker Compose

PostgreSQL container `postgres-avaliator`:

- Image: `postgres:15-alpine`
- Port: `5432`
- Environment: `POSTGRES_USER`, `POSTGRES_PASSWORD`, `POSTGRES_DB` from `.env` file.
- Volume: `db/init.sql` mounted to `/docker-entrypoint-initdb.d/init.sql`.

Application connection strings (from compose env):

- catalogservice: `jdbc:postgresql://postgres-avaliator:5432/avaliator`
- feedbackservice: `jdbc:postgresql://postgres-avaliator:5432/avaliator`

### Kubernetes

PostgreSQL deployment in namespace `avaliator`:

- Image: `postgres:15-alpine`
- PersistentVolumeClaim: `postgres-pvc` (1Gi, ReadWriteOnce)
- Probes: readiness and liveness via `pg_isready -U $POSTGRES_USER`
- ConfigMap: mounts `init.sql`.
- Secret: generated from `secret.yaml.tpl` via `envsubst`.

Application connection strings (from K8s configmaps):

- catalogservice: `jdbc:postgresql://postgres:5432/avaliator`
- feedbackservice: `jdbc:postgresql://postgres:5432/avaliator`

### Test Environment

- catalogservice uses H2 in-memory (version 2.4.240 pinned in pom.xml).
- feedbackservice uses H2 in-memory (version managed by Spring Boot BOM).
- Flyway is typically auto-configured against H2 for test scope.

## Safety Rules

- Add required columns with default values or staged rollout.
- Avoid destructive migration edits.
- Each service must only access its own schema.
- Schema creation order: init.sql first, then Flyway migrations per service.
