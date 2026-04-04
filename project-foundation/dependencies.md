# Dependencies — Avaliator

## Runtime Platform

- Java: 21 (required by both services — `<java.version>21</java.version>`)
- Maven: wrapper included per service (`./mvnw`)

## Catalogservice

Spring Boot parent: `org.springframework.boot:spring-boot-starter-parent:3.5.6`

| Dependency | GroupId | Scope | Purpose |
|---|---|---|---|
| spring-boot-starter-web | org.springframework.boot | compile | REST controllers, embedded Tomcat |
| spring-boot-starter-data-jpa | org.springframework.boot | compile | JPA repositories, Hibernate |
| spring-boot-starter-validation | org.springframework.boot | compile | Bean validation (@NotBlank, @NotNull) |
| spring-boot-starter-actuator | org.springframework.boot | compile | Health endpoints, readiness/liveness probes |
| flyway-core | org.flywaydb | compile | Database migration engine |
| flyway-database-postgresql | org.flywaydb | compile | Flyway PostgreSQL dialect support |
| postgresql | org.postgresql | runtime | JDBC driver for PostgreSQL |
| h2 | com.h2database | test | In-memory DB for tests (version pinned: 2.4.240) |
| spring-boot-starter-test | org.springframework.boot | test | JUnit 5, MockMvc, assertions |

Notes:

- H2 version is explicitly pinned to `2.4.240` (overrides BOM default).
- No dependency management BOM beyond spring-boot-starter-parent.

## Feedbackservice

Spring Boot parent: `org.springframework.boot:spring-boot-starter-parent:3.5.9`

Dependency management: `org.springframework.cloud:spring-cloud-dependencies:2025.0.1`

| Dependency | GroupId | Scope | Purpose |
|---|---|---|---|
| spring-boot-starter-web | org.springframework.boot | compile | REST controllers, embedded Tomcat |
| spring-boot-starter-data-jpa | org.springframework.boot | compile | JPA repositories, Hibernate |
| spring-boot-starter-validation | org.springframework.boot | compile | Bean validation |
| spring-boot-starter-actuator | org.springframework.boot | compile | Health endpoints, probes |
| flyway-core | org.flywaydb | compile | Database migration engine |
| flyway-database-postgresql | org.flywaydb | compile | Flyway PostgreSQL dialect support |
| spring-cloud-starter-openfeign | org.springframework.cloud | compile | Declarative HTTP client for catalogservice integration |
| spring-kafka | org.springframework.kafka | compile | Kafka producer support (present, not yet active) |
| postgresql | org.postgresql | runtime | JDBC driver for PostgreSQL |
| h2 | com.h2database | test | In-memory DB for tests (BOM-managed version) |
| spring-boot-starter-test | org.springframework.boot | test | JUnit 5, MockMvc, assertions |
| spring-kafka-test | org.springframework.kafka | test | Kafka testing utilities |

Notes:

- Spring Cloud BOM `2025.0.1` manages OpenFeign version.
- `spring-kafka` is declared but no Kafka producer/consumer code is active yet (planned for metricservice integration).
- H2 version is NOT pinned (uses BOM default, unlike catalogservice).

## MetricService — Planned

Status: planned. No pom.xml exists yet.

Expected dependencies:

- Same Spring Boot baseline: web, data-jpa, validation, actuator.
- Spring Kafka (+ test) for `ReviewCreated v1` event consumption.
- Flyway (core + PostgreSQL) for `metric_schema` migrations.
- PostgreSQL driver (runtime).
- H2 (tests).

## Event Contract Dependencies

- Shared logical event contract: `ReviewCreated v1`.
- Producer: feedbackservice (via spring-kafka, planned activation).
- Consumer: metricservice (planned).
- Versioning rule: additive changes only for the same major version.
- Consumer must reject invalid contract payload and route to DLQ.

## Inter-service Integration Dependencies

| From | To | Mechanism | Library |
|---|---|---|---|
| feedbackservice | catalogservice | Synchronous HTTP | Spring Cloud OpenFeign |
| feedbackservice | metricservice (planned) | Async event | Spring Kafka |

Feign configuration:

- `@FeignClient(name = "productClient", url = "http://catalogservice:8081")`
- Custom error decoder: `CustomFeignErrorDecoder` (maps 400→IllegalArgument, 404→ResourceNotFound, 5xx→ApiIntegration).
- Configuration class: `FeignClientConfiguration`.

## Docker Build Dependencies

Both services use the same multi-stage Docker build chain:

- Build stage: `maven:3.9.7-eclipse-temurin-21-alpine`
- Runtime stage: `eclipse-temurin:21-jre-alpine`

## Dependency Guidelines

- Keep Java 21 compatibility.
- Prefer BOM-managed versions where available.
- Pin versions for non-BOM third-party libraries.
- Use test/runtime scopes correctly.
- Both services should converge on the same Spring Boot version when practical.
