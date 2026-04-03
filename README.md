# ⭐ Avaliator

### Product Catalog & Reviews Platform — Microservices Architecture

[![Java 21](https://img.shields.io/badge/Java-21-blue?logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot 3.5](https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![PostgreSQL 15](https://img.shields.io/badge/PostgreSQL-15-blue?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Kubernetes](https://img.shields.io/badge/Kubernetes-Ready-326CE5?logo=kubernetes&logoColor=white)](https://kubernetes.io/)
[![Jenkins CI/CD](https://img.shields.io/badge/Jenkins-CI%2FCD-D24939?logo=jenkins&logoColor=white)](https://www.jenkins.io/)

---

## 📋 Table of Contents

1. [Overview](#-overview)
2. [Architecture](#-architecture)
3. [Tech Stack](#-tech-stack)
4. [Microservices](#-microservices)
5. [Project Structure](#-project-structure)
6. [Data Model](#-data-model)
7. [API Reference](#-api-reference)
8. [Running Locally](#-running-locally)
9. [Kubernetes Deployment](#-kubernetes-deployment)
10. [CI/CD Pipeline](#-cicd-pipeline)
11. [Observability](#-observability)

---

## 🎯 Overview

**Avaliator** is a platform composed of independent microservices for product management and user reviews. The project applies modern software architecture concepts:

- **Domain-Driven Design (DDD)** — Rich domain modeling with Aggregates, Entities, Value Objects, and Domain Events
- **Hexagonal Architecture (Ports & Adapters)** — Clear separation between business logic and infrastructure
- **Microservices** — Independent services with well-defined responsibilities
- **Full CI/CD** — Automated pipeline with Jenkins + Kubernetes deployment

---

## 🏗 Architecture

```
                             ┌──────────────┐
                             │     USER     │
                             └──────┬───────┘
                                    │
                               HTTP Requests
                                    │
                                    ▼
                          ┌─────────────────┐
                          │    INGRESS      │
                          │  (NGINX / K8s)  │
                          └────┬───────┬────┘
                               │       │
                  /product/*   │       │  /review/*
                               │       │
              ┌────────────────▼┐     ┌▼─────────────────┐
              │                 │     │                   │
              │  CATALOG        │     │  FEEDBACK         │
              │  SERVICE        │◄────│  SERVICE          │
              │                 │Feign│                   │
              │  :8081          │     │  :8882            │
              │                 │     │                   │
              │  ┌───────────┐  │     │  ┌────────────┐   │
              │  │  Product  │  │     │  │   Review   │   │
              │  │  Domain   │  │     │  │   Domain   │   │
              │  └───────────┘  │     │  └────────────┘   │
              └────────┬────────┘     └────────┬──────────┘
                       │                       │
                       │    ┌──────────────┐   │
                       └───►│  PostgreSQL  │◄──┘
                            │    :5432     │
                            │             │
                            │ ┌─────────┐ │
                            │ │catalog_ │ │
                            │ │schema   │ │
                            │ ├─────────┤ │
                            │ │feedback_│ │
                            │ │schema   │ │
                            │ └─────────┘ │
                            └──────────────┘
```

### Inter-Service Communication

| From → To | Method | Purpose |
|-----------|--------|---------|
| Feedback → Catalog | **OpenFeign** (synchronous HTTP) | Validate product existence before creating a review |

---

## 🛠 Tech Stack

### Backend

| Technology | Version | Purpose |
|-----------|---------|---------|
| **Java** | 21 (LTS) | Main language |
| **Spring Boot** | 3.5.x | Application framework |
| **Spring Data JPA** | — | Data access with Hibernate |
| **Spring Validation** | — | Request validation (`@Valid`) |
| **Spring Cloud OpenFeign** | 2025.0.1 | HTTP communication between services |
| **Flyway** | — | Database migrations |
| **PostgreSQL** | 15 Alpine | Relational database |
| **H2** | 2.4.x | In-memory database for tests |
| **Apache Kafka** | — | Async messaging (planned) |

### DevOps & Infrastructure

| Technology | Purpose |
|-----------|---------|
| **Docker** | Service containerization |
| **Kubernetes (kind)** | Container orchestration |
| **Jenkins (project pipeline contract)** | Repository-defined CI/CD stages (`Jenkinsfile`) |
| **Docker Registry (optional local infra)** | Image storage for local platform simulations |
| **CI Platform (external)** | Shared Jenkins runtime, credentials, governance and reusable libraries |

### Patterns & Practices

| Pattern | Where it's applied |
|---------|-------------------|
| **DDD (Domain-Driven Design)** | Aggregates, Entities, Value Objects, Domain Events |
| **Hexagonal Architecture** | Ports (interfaces) & Adapters (implementations) |
| **Use Case Pattern** | Each business operation is an isolated UseCase |
| **Multi-stage Docker Build** | Optimized images (build with Maven, runtime with JRE) |
| **Database per Schema** | Services isolated by schema within the same database |

---

## 📦 Microservices

### 1. Catalog Service — Product Management

| | |
|---|---|
| **Port** | `8081` |
| **Base path** | `/product` |
| **Responsibility** | Product CRUD with domain validation |

#### Features

- Product registration with rich validation (name, price, category)
- Lookup by ID with domain exception handling
- Paginated search by name and description
- Product categorization (`ProductCategory`)
- Product status control (`ProductStatus`)

#### Domain Entity: `Product`

```java
Product (AggregateRoot)
├── id: ProductId (UUID)         // Value Object
├── name: String                  // max 50 chars, required
├── price: BigDecimal             // non-negative, required
├── description: String           // optional
├── category: ProductCategory     // validated enum
├── status: ProductStatus         // AVAILABLE (default)
└── createdAt: LocalDate          // auto-generated
```

**Business rules:**
- Name cannot be null, empty, or exceed 50 characters
- Price cannot be negative
- Category must be a valid `ProductCategory` enum value
- Status starts as `AVAILABLE`
- Duplicate product names are rejected (`ProductAlreadyExistsException`)

#### Endpoints

| Method | Path | Description | Status |
|--------|------|-------------|--------|
| `POST` | `/product/create` | Create product | `201 Created` |
| `GET` | `/product/{id}` | Get by ID | `200 OK` |
| `GET` | `/product/get-products?name=&description=&page=&size=&sort=` | Paginated search | `200 OK` |

---

### 2. Feedback Service — Product Reviews

| | |
|---|---|
| **Port** | `8882` |
| **Base path** | `/review` |
| **Responsibility** | User review management |

#### Features

- Review creation with domain validation
- Integration with Catalog Service via **OpenFeign** to validate product existence
- Integration error handling (`ApiIntegrationException`, `ResourceNotFoundException`)
- Custom Feign error decoder configuration

#### Domain Entity: `Review`

```java
Review (AggregateRoot)
├── id: ReviewId (UUID)          // Value Object
├── productId: ProductId (UUID)  // Value Object — product reference
├── rating: int                   // 1 to 5 stars
├── comment: String               // max 500 chars, required
└── createdAt: LocalDate          // auto-generated
```

**Business rules:**
- Rating must be between 1 and 5
- Comment is required, maximum 500 characters
- ProductId must be valid (verified via Feign against Catalog Service)
- If the product doesn't exist, throws `ProductNotFoundException`

#### Endpoints

| Method | Path | Description | Status |
|--------|------|-------------|--------|
| `POST` | `/review/create` | Create review | `201 Created` |

---

## 📁 Project Structure

```
springboot-avaliator/
├── Jenkinsfile                          # CI/CD pipeline (build → test → K8s deploy)
├── compose-standalone.yaml              # Docker Compose app stack (local dev)
├── scripts/                             # Local operation commands (up/down/logs/health/rebuild/reset)
├── db/
│   └── init.sql                         # Database and schema creation
│
├── catalogservice/
│   ├── Dockerfile                       # Multi-stage build (Maven → JRE Alpine)
│   ├── pom.xml                          # Spring Boot 3.5.6, Java 21
│   └── src/main/java/.../product/
│       ├── domain/                      # 🟠 Domain (application core)
│       │   ├── Product.java             #    AggregateRoot — main entity
│       │   ├── ProductId.java           #    Value Object — identity
│       │   ├── ProductCategory.java     #    Category enum
│       │   ├── ProductStatus.java       #    Status enum
│       │   └── ProductRepository.java   #    Repository interface (Port)
│       ├── application/                 # 🔵 Application (use cases)
│       │   ├── ports/
│       │   │   ├── input/               #    Input interfaces (UseCases)
│       │   │   └── output/              #    Output interfaces (Repositories)
│       │   ├── useCases/                #    UseCase implementations
│       │   ├── input/                   #    Input DTOs
│       │   ├── output/                  #    Output DTOs
│       │   └── mapper/                  #    Domain ↔ DTO mapping
│       ├── infrastruct/                 # 🟢 Infrastructure (adapters)
│       │   ├── input/                   #    Controllers (HTTP adapter)
│       │   └── output/                  #    JPA Repositories (DB adapter)
│       ├── config/                      #    Configuration and exception handlers
│       └── common/                      #    Base classes (Entity, AggregateRoot)
│
├── feedbackservice/
│   ├── Dockerfile                       # Multi-stage build
│   ├── pom.xml                          # Spring Boot 3.5.9, Java 21, OpenFeign
│   └── src/main/java/.../review/
│       ├── domain/                      # Same hexagonal structure
│       ├── application/
│       ├── infrastruct/
│       │   └── output/adapter/product/  #    Feign Client → Catalog Service
│       └── config/                      #    Custom Feign error decoder
│
└── k8s/                                 # Kubernetes manifests
    ├── kind-config.yaml                 # Local cluster config
    ├── setup-cluster.sh                 # Automated setup script
    ├── namespace.yaml                   # "avaliator" namespace
    ├── ingress.yaml                     # HTTP routing
    ├── postgres/                        # DB: Secret, ConfigMap, PVC, Deployment
    ├── catalogservice/                  # App: ConfigMap, Deployment, Service
    └── feedbackservice/                 # App: ConfigMap, Deployment, Service
```

### Hexagonal Architecture (per service)

```
┌───────────────────────────────────────────────────────┐
│                                                       │
│   ┌─────────────────────────────────────────────┐     │
│   │            INFRASTRUCTURE                    │     │
│   │   ┌───────────┐          ┌──────────────┐   │     │
│   │   │Controller │          │JPA Repository│   │     │
│   │   │(HTTP in)  │          │(DB out)      │   │     │
│   │   └─────┬─────┘          └──────▲───────┘   │     │
│   │         │    ┌───────────────┐  │            │     │
│   │         │    │  APPLICATION  │  │            │     │
│   │         ▼    │               │  │            │     │
│   │    Input ────►   USE CASE   ─── Output       │     │
│   │    Port  │   │               │  Port         │     │
│   │         │    │  ┌─────────┐  │  │            │     │
│   │         │    │  │ DOMAIN  │  │  │            │     │
│   │         │    │  │         │  │  │            │     │
│   │         └────┤  │Entities │  ├──┘            │     │
│   │              │  │V.Objects│  │               │     │
│   │              │  │Events   │  │               │     │
│   │              │  └─────────┘  │               │     │
│   │              └───────────────┘               │     │
│   └─────────────────────────────────────────────┘     │
│                                                       │
└───────────────────────────────────────────────────────┘

Rule: Dependencies always point INWARD.
The domain knows NOTHING about the outside (JPA, HTTP, frameworks).
```

---

## 💾 Data Model

### Database: `avaliator` (PostgreSQL 15)

#### Schema: `catalog_schema`

```sql
CREATE TABLE product (
    id          UUID PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    price       NUMERIC(15,2) NOT NULL,
    description TEXT,
    category    VARCHAR(50) NOT NULL,
    status      VARCHAR(50) NOT NULL,
    created_at  DATE DEFAULT CURRENT_DATE
);
```

#### Schema: `feedback_schema`

```sql
CREATE TABLE review (
    id          UUID PRIMARY KEY,
    product_id  UUID NOT NULL,
    rating      SMALLINT NOT NULL,       -- 1 to 5
    comment     TEXT,
    created_at  DATE DEFAULT CURRENT_DATE
);
```

> **Isolation strategy:** Each microservice owns its own schema within the same PostgreSQL database. Each service uses **Flyway** to manage its migrations independently.

---

## 📡 API Reference

### Catalog Service (`:8081`)

#### Create Product

```http
POST /product/create
Content-Type: application/json

{
    "name": "Gaming Laptop",
    "price": 4599.90,
    "description": "Laptop with RTX 4060",
    "category": "ELECTRONICS"
}
```

**Response:** `201 Created`
```json
{
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "name": "Gaming Laptop",
    "price": 4599.90,
    "category": "ELECTRONICS",
    "status": "AVAILABLE"
}
```

#### Get by ID

```http
GET /product/{id}
```

#### Paginated Search

```http
GET /product/get-products?name=Laptop&description=Gaming&page=0&size=10&sort=name
```

### Feedback Service (`:8882`)

#### Create Review

```http
POST /review/create
Content-Type: application/json

{
    "productId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "rating": 5,
    "comment": "Excellent product, highly recommended!"
}
```

**Response:** `201 Created`
```json
{
    "id": "f1e2d3c4-b5a6-7890-fedc-ba0987654321",
    "productId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "rating": 5,
    "comment": "Excellent product, highly recommended!"
}
```

---

## 🚀 Quick Start (Recommended)

Use explicit local scripts instead of a single orchestrator:

```bash
chmod +x scripts/*.sh
./scripts/up.sh k8s
```

Alternative lightweight mode (no Kubernetes):

```bash
./scripts/up.sh compose
```

---

## 🏗 Running Locally

### Prerequisites

- **Docker** 20.10+
- **Kind** (Kubernetes in Docker)
- **Kubectl**

### Local Scripts & Modes

The project supports two local modes:

#### 1. Kubernetes Mode
Builds services locally, loads images in Kind, and deploys manifests.

| Command | Purpose |
|--------|---------|
| `./scripts/up.sh k8s` | Provision/update local K8s app stack |
| `./scripts/down.sh k8s` | Remove app manifests from K8s |
| `./scripts/logs.sh k8s [service]` | Follow K8s logs |
| `./scripts/health.sh k8s` | Show pods and ingress status |
| `./scripts/rebuild.sh k8s` | Rebuild images and restart deployments |
| `./scripts/reset.sh k8s` | Delete Kind cluster |

#### 2. Compose Mode
Runs only app + database in Docker Compose.

| Command | Purpose |
|--------|---------|
| `./scripts/up.sh compose` | Start compose stack |
| `./scripts/down.sh compose` | Stop compose stack |
| `./scripts/logs.sh compose [service]` | Follow compose logs |
| `./scripts/health.sh compose` | Check local health endpoints |
| `./scripts/rebuild.sh compose` | Rebuild and restart compose stack |
| `./scripts/reset.sh compose` | Remove compose stack and volumes |

### Access Endpoints

Once deployed, the services are unified under the Ingress controller:

| Service | Local URL |
|---------|-----------|
| **Ingress Gateway** | [http://localhost](http://localhost) |
| **Catalog API**     | [http://localhost/catalog/](http://localhost/catalog/) |
| **Feedback API**    | [http://localhost/feedback/](http://localhost/feedback/) |

---

## ☸️ Kubernetes Details

The cluster is managed by **Kind** and comes with:

- **Resource Labeling**: All project resources are labeled with `app.kubernetes.io/part-of=avaliator` for easy filtering.
- **Auto-Wait**: The setup scripts use `kubectl wait` to ensure services are fully initialized before completion.
- **Private Registry**: expected to be provided by your external CI/CD platform.
- **Probes**: Liveness and Readiness probes using **Spring Boot Actuator** HTTP endpoints.

### Useful Commands

```bash
# List all resources in the project namespace
kubectl get all -n avaliator

# Check application logs
kubectl logs -n avaliator -l app=catalogservice -f

# Check Ingress status
kubectl get ingress -n avaliator
```

---

## 🔄 CI/CD & Observability

### Unified Pipeline
This repository owns its `Jenkinsfile` as a project-level pipeline contract. The runtime Jenkins instance is expected to be provided by a shared CI platform project. The pipeline automates:
1. Standard Maven Build & Test.
2. Docker Image creation with build-number tags.
3. Pushing images to the **Local Registry**.
4. Rolling updates to the Kubernetes cluster.

### Jenkins (Docker) ↔ Kind Connectivity
If Jenkins runs inside Docker and Kind runs on the host, do this:

1. Keep `k8s/kind-config.yaml` with fixed API settings:
   - `networking.apiServerAddress: "0.0.0.0"`
   - `networking.apiServerPort: 6443`
   - `kubeadmConfigPatches` with `certSANs: [host.docker.internal]`
2. Recreate the Kind cluster after changing the config:
   - `kind delete cluster --name avaliator`
   - `kind create cluster --config k8s/kind-config.yaml --name avaliator`
3. In Jenkins container Compose config, add:
   - `extra_hosts: ["host.docker.internal:host-gateway"]`
4. In Jenkins credentials/env template, set:
   - `URL_SERVER_KUB=https://host.docker.internal:6443`
5. Generate kubeconfig env variables from Kind:
   - `./scripts/export-kind-kubeconfig-env.sh avaliator https://host.docker.internal:6443 .env.k8s`
   - Use generated values for `CLIENT_CERTIFICATE`, `CLIENT_KEY`, `CERTIFICATE_AUTH`, and `URL_SERVER_KUB`.

### Monitoring
- Monitoring stack is optional and can be added incrementally without coupling app startup flow.

---

## 🗺️ Roadmap

- [x] Catalog Service (product CRUD)
- [x] Feedback Service (review creation)
- [x] Feign integration (product validation)
- [x] Docker Compose (local environment)
- [x] CI/CD pipeline with Jenkins
- [x] Kubernetes deployment with kind
- [x] Observability (Prometheus + Grafana)
- [x] Spring Boot Actuator — Health endpoints for K8s probes
- [x] Unified "One-Click" Setup Script
- [ ] Kafka — Async events between services
- [ ] Metrics Service — Aggregated calculations (average, distribution)
- [ ] Automated integration tests
- [ ] Canary deployments with Istio
- [ ] GitOps with ArgoCD

---

## 👤 Author

**Pedro Rosário** — [@pdrohrosario](https://github.com/pdrohrosario)
