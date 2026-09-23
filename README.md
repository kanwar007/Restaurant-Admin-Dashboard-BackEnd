# Restaurant Admin Dashboard — Backend

Spring Boot microservices + PostgreSQL implementing the Restaurant Admin Dashboard API contract
(`docs/openapi.yaml`). Runs with Docker Compose locally and on Kubernetes / Azure AKS.

## Services

| Service | Port | Owns | Public routes |
| --- | --- | --- | --- |
| `api-gateway` | 8080 (4000 on the host) | Routing, CORS, dev reset fan-out | `/api/health`, `/api/reset`, proxies everything below |
| `identity-service` | 8081 | Restaurants, staff users, sessions | `/api/auth/login`, `/api/auth/me`, `/api/auth/logout`, `/api/profile` |
| `catalog-service` | 8082 | Categories, menu items, add-ons | `/api/menu/categories`, `/api/menu`, `/api/menu/{id}`, `/api/addons`, `/api/addons/{id}` |
| `floor-service` | 8083 | Dining tables and occupancy | `/api/tables`, `/api/tables/{id}` |
| `ordering-service` | 8084 | Orders, items, add-on snapshots, status history | `/api/orders`, `/api/orders/{id}/status`, `/api/guest/orders` |
| `billing-service` | 8085 | Invoices, lines, payments | `/api/bills/{orderNo}`, `/api/order-history` |
| `reporting-service` | 8086 | Dashboard read model (stateless aggregator) | `/api/dashboard` |

Each persistent service owns its own PostgreSQL schema (`identity`, `catalog`, `floor`, `ordering`,
`billing`) and its own Flyway migrations, so a schema can later be split onto its own database
without code changes — only `DB_URL` changes.

Money is `numeric(12,2)` / `BigDecimal`, identifiers are UUIDs and timestamps are `timestamptz`.
Passwords are bcrypt hashes; session tokens are opaque UUIDs stored as SHA-256 hashes.

## Run locally with Docker

```bash
docker compose up -d --build
curl http://localhost:4000/api/health
./scripts/smoke-test.sh http://localhost:4000
```

The gateway is published on `http://localhost:4000`, matching the frontend's expected API base URL.
Demo logins: `admin` / `admin123` and `cashier` / `cashier123`.

On Windows, follow [docs/windows-local-setup.md](docs/windows-local-setup.md) for the step-by-step
Docker Desktop / PowerShell / Git Bash walkthrough.

If your network cannot reach Maven Central directly, pass a mirror to the image build:

```bash
MAVEN_MIRROR_URL=https://maven-central.storage-download.googleapis.com/maven2 docker compose build
```

## Run locally without Docker

```bash
docker compose up -d postgres
mvn -B verify
mvn -B -pl services/identity-service spring-boot:run   # and the other services, in any order
```

## Deploy to Kubernetes / Azure AKS

Manifests are Kustomize-based:

```bash
kubectl apply -k deploy/k8s/overlays/local   # kind / minikube / docker-desktop, NodePort 30080
kubectl apply -k deploy/k8s/overlays/aks     # LoadBalancer + managed NGINX ingress
```

The AKS overlay expects images in `ghcr.io/<owner>/<repo>/<service>`. For a managed database, drop
`postgres.yaml` from the base kustomization and point `DB_URL` in `cafe-admin-config` at Azure
Database for PostgreSQL Flexible Server; keep credentials in the `cafe-admin-db` secret (ideally
projected from Key Vault via the CSI driver or workload identity).

## GitHub Actions

| Workflow | Trigger | Purpose |
| --- | --- | --- |
| `.github/workflows/ci.yml` | PRs and `main` | `mvn verify`, then a Docker Compose smoke test of the whole contract |
| `.github/workflows/images.yml` | `main`, tags, manual | Builds and pushes one GHCR image per service |
| `.github/workflows/deploy-aks.yml` | after images, or manual | Azure OIDC login, `kubectl kustomize` + apply, waits for rollouts |

`deploy-aks.yml` needs these repository secrets: `AZURE_CLIENT_ID`, `AZURE_TENANT_ID`,
`AZURE_SUBSCRIPTION_ID`, `AKS_RESOURCE_GROUP`, `AKS_CLUSTER_NAME`.

## Logging and correlation IDs

Every service logs through Log4j2 (`log4j2-spring.xml`, shipped in `common` and `api-gateway`) with
the pattern `timestamp level [service] [correlationId] [thread] logger - message`.

The gateway accepts an `X-Correlation-Id` request header and generates a UUID when it is absent, then
forwards it downstream and echoes it on the response. Each service puts it into the SLF4J MDC for the
duration of the request and re-attaches it to outbound service-to-service calls, so one request is
greppable across containers:

```bash
curl -s -D - -o /dev/null -H 'X-Correlation-Id: demo-trace-777' http://localhost:4000/api/dashboard
docker compose logs | grep demo-trace-777
```

Set `APP_LOG_LEVEL` / `ROOT_LOG_LEVEL` to change verbosity.

## Development reset

`POST /api/reset` re-seeds every service from its Flyway seed function. Set `RESET_ENABLED=false`
(gateway and services) in production.
