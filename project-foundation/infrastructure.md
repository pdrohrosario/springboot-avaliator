# Infrastructure — Avaliator

## Local Runtime Modes

| Mode | Purpose | Entry point |
|---|---|---|
| `compose` | Fast local development with app + database | `./scripts/up.sh compose` |
| `k8s` | Local orchestration validation via Kind | `./scripts/up.sh k8s` |

Operational scripts (`scripts/`):

| Script | Description |
|---|---|
| `up.sh` | Start environment (compose or k8s) |
| `down.sh` | Stop environment |
| `logs.sh` | Tail service logs |
| `health.sh` | Check health endpoints |
| `rebuild.sh` | Rebuild Docker images and restart |
| `reset.sh` | Full teardown and clean restart |

---

## Docker — Container Images

### Dockerfile Pattern (both services)

Both catalogservice and feedbackservice use identical multi-stage Dockerfiles.

Build stage:

- Base image: `maven:3.9.7-eclipse-temurin-21-alpine`
- Working dir: `/app`
- Copies `pom.xml` first (layer cache for dependencies), then source code.
- Runs: `mvn clean package -DskipTests`

Runtime stage:

- Base image: `eclipse-temurin:21-jre-alpine`
- Working dir: `/app`
- Copies built JAR from build stage.
- Exposed port: `8080` (mapped externally to service-specific ports).
- Entrypoint: `java -jar <service>.jar`

Image naming convention in CI:

- `${REGISTRY}/catalogservice:latest`
- `${REGISTRY}/feedbackservice:latest`

---

## Docker Compose — `compose-standalone.yaml`

### Topology

```
┌─────────────────────────────────────────────────┐
│                  app-network (bridge)            │
│                                                  │
│  ┌──────────────────┐  ┌────────────────────┐   │
│  │ postgres-avaliator│  │                    │   │
│  │  :5432            │  │                    │   │
│  └──────────────────┘  │                    │   │
│         ▲               │                    │   │
│  ┌──────┴───────────┐  │                    │   │
│  │ app-catalog      │  │  app-feedback      │   │
│  │  :8081           │  │   :8882            │   │
│  └──────────────────┘  └────────────────────┘   │
│                              ▲                   │
│                  depends_on: app-catalog         │
└─────────────────────────────────────────────────┘
```

### Services

postgres-avaliator:

- Image: `postgres:15-alpine`
- Port mapping: `5432:5432`
- Environment variables from `.env`: `POSTGRES_USER`, `POSTGRES_PASSWORD`, `POSTGRES_DB`
- Volume: `./db/init.sql:/docker-entrypoint-initdb.d/init.sql`
- Network: `app-network`

app-catalog:

- Build context: `./catalogservice`
- Port mapping: `8081:8080`
- Depends on: `postgres-avaliator`
- Environment:
  - `SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-avaliator:5432/avaliator`
  - `SPRING_DATASOURCE_USERNAME` / `PASSWORD` from `.env`
  - `SPRING_FLYWAY_SCHEMAS=catalog_schema`
- Network: `app-network`

app-feedback:

- Build context: `./feedbackservice`
- Port mapping: `8882:8080`
- Depends on: `postgres-avaliator`, `app-catalog`
- Environment:
  - `SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-avaliator:5432/avaliator`
  - `SPRING_DATASOURCE_USERNAME` / `PASSWORD` from `.env`
  - `SPRING_FLYWAY_SCHEMAS=feedback_schema`
- Network: `app-network`

### Prerequisites

- `.env` file must exist with `POSTGRES_USER`, `POSTGRES_PASSWORD`, `POSTGRES_DB`.
- Create from template: `cp .env.example .env`

---

## Kubernetes — Kind Cluster

### Cluster Configuration (`k8s/kind-config.yaml`)

- Cluster name: `avaliator`
- Nodes: 1 control-plane + 1 worker
- Control-plane extra port mappings:
  - `80` → `hostPort 80` (HTTP ingress)
  - `443` → `hostPort 443` (HTTPS ingress)
- Control-plane labels: `ingress-ready: "true"`
- Container registry mirror: `localhost:5001` → `http://local-registry:5000`

### Namespace

- `k8s/namespace.yaml`: creates namespace `avaliator`.

### PostgreSQL (`k8s/postgres/`)

Deployment:

- 1 replica, `postgres:15-alpine`
- PersistentVolumeClaim: `postgres-pvc`, 1Gi, ReadWriteOnce
- Volume mounts:
  - PVC → `/var/lib/postgresql/data` (subPath: `postgres`)
  - ConfigMap `postgres-config` → `/docker-entrypoint-initdb.d/init.sql`
- Probes:
  - Readiness: `pg_isready -U $POSTGRES_USER`, period 10s
  - Liveness: `pg_isready -U $POSTGRES_USER`, period 30s, initial delay 30s
- Environment from:
  - ConfigMap: `postgres-config` (POSTGRES_DB)
  - Secret: `postgres-secret` (POSTGRES_USER, POSTGRES_PASSWORD)
- Service: ClusterIP on port `5432`, name `postgres`

ConfigMap (`postgres-config`):

- `POSTGRES_DB: avaliator`
- `init.sql`: embedded init SQL

Secret (`postgres-secret`):

- Generated from `secret.yaml.tpl` via `envsubst`.
- Contains: `POSTGRES_USER`, `POSTGRES_PASSWORD` (base64-encoded).

### Catalogservice (`k8s/catalogservice/`)

Deployment:

- 2 replicas
- Strategy: RollingUpdate (`maxUnavailable: 0`, `maxSurge: 1`)
- Init container: `wait-for-postgres` — busybox loop checking `postgres:5432` via `nc -z`
- Container: `catalogservice`, port `8080`
- Environment from ConfigMap `catalogservice-config`
- Probes:
  - Startup: HTTP GET `/actuator/health`, period 10s, failure threshold 30
  - Readiness: HTTP GET `/actuator/health`, period 15s, failure threshold 3
  - Liveness: HTTP GET `/actuator/health`, period 30s, failure threshold 3
- Resources:
  - Requests: 384Mi memory, 250m CPU
  - Limits: 768Mi memory, 1000m CPU
- Service: ClusterIP on port `8081` → targetPort `8080`

ConfigMap (`catalogservice-config`):

- `SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/avaliator`
- `SPRING_DATASOURCE_USERNAME` / `PASSWORD` (from envsubst)
- `SPRING_FLYWAY_SCHEMAS: catalog_schema`
- `SERVER_PORT: "8081"`

### Feedbackservice (`k8s/feedbackservice/`)

Deployment:

- 2 replicas
- Strategy: RollingUpdate (`maxUnavailable: 0`, `maxSurge: 1`)
- Init containers (sequential):
  1. `wait-for-postgres` — busybox loop checking `postgres:5432`
  2. `wait-for-catalog` — busybox loop checking `catalogservice:8081`
- Container: `feedbackservice`, port `8080`
- Environment from ConfigMap `feedbackservice-config`
- Probes: same pattern as catalogservice (startup, readiness, liveness on `/actuator/health`)
- Resources: same as catalogservice (384Mi-768Mi memory, 250m-1000m CPU)
- Service: ClusterIP on port `8882` → targetPort `8080`

ConfigMap (`feedbackservice-config`):

- `SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/avaliator`
- `SPRING_DATASOURCE_USERNAME` / `PASSWORD` (from envsubst)
- `SPRING_FLYWAY_SCHEMAS: feedback_schema`
- `SERVER_PORT: "8882"`

### NGINX Ingress (`k8s/ingress.yaml`)

- Class: `nginx`
- Annotation: `nginx.ingress.kubernetes.io/rewrite-target: /$2`
- Rules:
  - `/catalog(/|$)(.*)` → service `catalogservice:8081`
  - `/feedback(/|$)(.*)` → service `feedbackservice:8882`

### Cluster Setup Script (`k8s/setup-cluster.sh`)

Execution flow:

1. Installs `kubectl` if not found (downloads Linux AMD64 binary).
2. Installs `kind` if not found (downloads v0.27.0 binary).
3. Creates Kind cluster from `k8s/kind-config.yaml`.
4. Exports kubeconfig and fixes server address to `127.0.0.1`.
5. Connects `local-registry` container to kind network (warning if missing).
6. Connects `jenkins` container to kind network (warning if missing).
7. Installs NGINX Ingress Controller via kubectl apply (cloud-generic manifest).
8. Waits for ingress controller pods ready (timeout 90s).
9. Applies namespace, postgres manifests, and ingress in order.

---

## CI/CD — Jenkins Pipeline (`Jenkinsfile`)

### Environment Variables

- `REGISTRY`: Docker registry URL (from Jenkins env)
- `MAVEN_IMAGE`: `maven:3.9.7-eclipse-temurin-21-alpine`
- `K8S_NAMESPACE`: `avaliator`
- Credentials:
  - `DOCKER_CREDENTIALS_ID`: Docker registry login
  - `K8S_KUBECONFIG_CREDENTIALS_ID`: Kubeconfig file credential

### Pipeline Stages

Stage 1 — Checkout:

- Cleans workspace and checks out SCM.

Stage 2 — Generate Configs:

- Sources `.env` (POSIX `. ./.env` syntax).
- Runs `envsubst` on K8s template files to generate secrets and kubeconfig.

Stage 3 — Build Services (parallel):

- catalogservice: `mvn clean package -DskipTests` inside Maven Docker image.
- feedbackservice: same.

Stage 4 — Test Services (parallel):

- catalogservice: `mvn test` inside Maven Docker image.
- feedbackservice: same.
- JUnit report collection: `**/target/surefire-reports/*.xml`.

Stage 5 — Build & Push Docker Images (parallel):

- Builds Docker image for each service.
- Tags as `${REGISTRY}/<service>:latest`.
- Pushes to registry using Docker credentials.

Stage 6 — Deploy to Kubernetes:

- Sequential execution:
  1. Apply namespace manifest.
  2. Apply postgres manifests (configmap, secret, PVC, deployment, service).
  3. Apply catalogservice manifests (configmap, deployment) + set image + rollout status.
  4. Apply feedbackservice manifests (configmap, deployment) + set image + rollout status.
  5. Apply ingress manifest.

### CI Replication Locally

To replicate CI intent before check-in:

```bash
(cd catalogservice && ./mvnw clean package -DskipTests)
(cd feedbackservice && ./mvnw clean package -DskipTests)
(cd catalogservice && ./mvnw test)
(cd feedbackservice && ./mvnw test)
docker build -t catalogservice:latest ./catalogservice
docker build -t feedbackservice:latest ./feedbackservice
```

---

## Planned Infrastructure Evolution (metricservice)

When metricservice is implemented:

Compose additions:

- Kafka broker container (and optional Zookeeper/KRaft).
- metricservice container (new port, depends on postgres + kafka).
- Topic provisioning for `ReviewCreated v1`.

Kubernetes additions:

- Kafka broker deployment and service.
- metricservice deployment, configmap, and service.
- Ingress rule for `/metrics` path.
- metricservice readiness must verify DB connectivity and broker consumption readiness.
- metricservice liveness must detect stuck consumer conditions.

CI/CD additions:

- Add metricservice to parallel build and test stages.
- Add metricservice Docker image build and push.
- Add metricservice K8s deploy with rollout status.

Monitoring requirements:

- Consumer lag and DLQ growth should be monitored.
- Retry policy with exponential backoff for transient failures.
- DLQ for non-recoverable event failures.

## Security

- `.env` and generated secret files are not versioned (`.gitignore`).
- `k8s/postgres/secret.yaml` is generated from `secret.yaml.tpl` via `envsubst` — never committed.
- `k8s/jenkins-kubconfig.yaml` is generated from template — never committed.
- Docker images run as non-root where JRE Alpine allows.
- No sensitive data exposed in Kubernetes configmaps (secrets use Secret resources).
