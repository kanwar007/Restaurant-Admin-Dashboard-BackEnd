# Running the backend on a local Windows machine

Step-by-step guide for Windows 10/11. Commands are given for PowerShell; the
`scripts/smoke-test.sh` shell script needs Git Bash (installed with Git for Windows).

## 1. Install the prerequisites

| Tool | Version | Install |
| --- | --- | --- |
| Docker Desktop (WSL 2 backend) | latest | `winget install Docker.DockerDesktop` |
| Git for Windows (includes Git Bash) | latest | `winget install Git.Git` |
| Temurin JDK | 17 | `winget install EclipseAdoptium.Temurin.17.JDK` |
| Maven | 3.9+ | `winget install Apache.Maven` |

Only Docker Desktop and Git are required to run the stack; the JDK and Maven are
needed for building or running services outside Docker.

After installing Docker Desktop, start it once and enable
**Settings → General → Use the WSL 2 based engine**, then verify:

```powershell
docker --version
docker compose version
java -version      # should print 17.x
mvn -version
```

If `java`/`mvn` are not found, open a new terminal so the updated `PATH` is picked
up, or set `JAVA_HOME` manually:

```powershell
setx JAVA_HOME "C:\Program Files\Eclipse Adoptium\jdk-17.0.12.7-hotspot"
```

## 2. Clone the repository

```powershell
git clone https://github.com/kanwar007/Restaurant-Admin-Dashboard-BackEnd.git
cd Restaurant-Admin-Dashboard-BackEnd
```

Line endings: the build works with either, but to avoid CRLF being committed run
`git config core.autocrlf input` once.

## 3. Start the whole stack with Docker

```powershell
docker compose up -d --build
```

The first build downloads Maven dependencies and takes several minutes. When it
finishes, all eight containers (PostgreSQL + six services + gateway) should be up:

```powershell
docker compose ps
```

The gateway listens on <http://localhost:4000>:

```powershell
curl.exe http://localhost:4000/api/health
```

Demo logins: `admin` / `admin123` and `cashier` / `cashier123`.

If your network cannot reach Maven Central directly, build through a mirror:

```powershell
$env:MAVEN_MIRROR_URL = "https://maven-central.storage-download.googleapis.com/maven2"
docker compose up -d --build
```

## 4. Run the smoke test

In **Git Bash** (not PowerShell):

```bash
./scripts/smoke-test.sh http://localhost:4000
```

The equivalent minimal check in PowerShell:

```powershell
$login = Invoke-RestMethod -Method Post -Uri http://localhost:4000/api/auth/login `
  -ContentType 'application/json' `
  -Body '{"username":"admin","password":"admin123"}'
Invoke-RestMethod -Uri http://localhost:4000/api/dashboard `
  -Headers @{ Authorization = "Bearer $($login.token)" }
```

## 5. Follow logs and trace a request

```powershell
docker compose logs -f api-gateway ordering-service
```

Every request carries an `X-Correlation-Id` (generated when the client does not
send one), so one request can be traced across all services:

```powershell
curl.exe -s -D - -o NUL -H "X-Correlation-Id: demo-trace-777" http://localhost:4000/api/dashboard
docker compose logs | Select-String demo-trace-777
```

## 6. Run services outside Docker (optional)

Start only the database in Docker, then run services from the command line:

```powershell
docker compose up -d postgres
mvn -B verify
mvn -B -pl services/identity-service spring-boot:run
```

Repeat the last command per service in separate terminals (`catalog-service`,
`floor-service`, `ordering-service`, `billing-service`, `reporting-service`,
`api-gateway`). Each service reads `DB_URL`, `DB_USERNAME` and `DB_PASSWORD`
and defaults to `jdbc:postgresql://localhost:5432/cafeadmin` with
`cafeadmin`/`cafeadmin`.

## 7. Deploy to the local Kubernetes in Docker Desktop (optional)

Enable **Settings → Kubernetes → Enable Kubernetes**, then:

```powershell
kubectl apply -k deploy/k8s/overlays/local
kubectl -n cafe-admin get pods
```

The gateway is exposed on NodePort `30080` (<http://localhost:30080/api/health>).

## 8. Stop and clean up

```powershell
docker compose down          # stop containers, keep data
docker compose down -v       # also delete the PostgreSQL volume
```

## Troubleshooting

| Symptom | Cause / fix |
| --- | --- |
| `docker: error during connect` | Docker Desktop is not running, or WSL 2 is not installed (`wsl --install`). |
| Port 4000 or 5432 already in use | Find the owner with `netstat -ano \| findstr :4000` and stop it, or change the host port mapping in `docker-compose.yml`. |
| Build fails with HTTP 429 from Maven Central | Rebuild with `MAVEN_MIRROR_URL` as shown in step 3. |
| `./scripts/smoke-test.sh` not recognised | Run it from Git Bash, not PowerShell or CMD. |
| Services return 500 right after startup | The gateway accepts traffic before downstream services finish migrating; wait until `docker compose ps` shows all services healthy. |
| Slow first build / high disk usage | Docker Desktop WSL disk; raise the memory limit in Settings → Resources (4 GB+ recommended). |
