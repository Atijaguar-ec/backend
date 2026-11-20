# INATrace Backend & Operations Installation Scripts (`v1.0.0`)

Standardized, idempotent shell scripts for deploying and operating INATrace (Cacao edition) on Ubuntu 22.04/24.04 x86_64 LTS with Docker Compose, PostgreSQL 16, Keycloak 26, Spring Boot 3.3, and Nginx TLS.

> Frontend image build (`build-frontend.sh`), institutional Keycloak login theme deployment (`install-keycloak-theme.sh`), and Nginx/TLS configuration (`configure-nginx-tls.sh`) live in `scripts/install/` of the INATrace Frontend repository.

## Inventory

| Script | Responsibility | Manual Reference |
| :--- | :--- | :--- |
| `01-prepare-host.sh` | OS packages, Docker Engine + Compose v2, Nginx, Certbot, UFW (`22/80/443`), Docker log rotation, Fail2ban SSH jail | Manual 01, 04 |
| `02-build-backend.sh` | Build `inatrace-backend` Docker image (`--push` optional) | Manual 01 |
| `03-init-environment.sh` | Create `/opt/inatrace/*`, copy Compose/initdb templates, generate cryptographic `.env` (`0600`) | Manual 01 |
| `04-deploy-and-bootstrap-keycloak.sh` | Start Compose stack, configure Keycloak 26 realm (`inatrace-browser-flow`, `manage-users`, PKCE S256), wait for health | Manual 01 |
| `05-create-admin-and-seed.sh` | Register & promote `SYSTEM_ADMIN` in PostgreSQL + Keycloak, apply idempotent Cacao catalog seed (`PESOLIBRA`, `QUINTALES`, `Cacao en Baba`, `COLLECTING`) | Manual 01 |
| `06-verify-installation.sh` | Verify containers, Flyway history, JWKS, OpenAPI, HTTPS endpoints, canonical redirect, and optional `--check-qr` LOPDP audit | Manual 01, 03, 04 |
| `07-configure-cacao-company.sh` | Apply multi-tenant profile (`seco-certificado`, `baba-organico`, `privado`), activate company, link value chain | Manual 02, 03 |
| `08-provision-erp-account.sh` | Idempotently provision technical ERP/scale user in `public."User"` + `public.companyuser` (optional confidential Keycloak client) | Manual 04 |
| `09-backup-inatrace.sh` | Atomic PostgreSQL (`inatrace` + `keycloak`) custom dumps + `file_storage`/`documents` + `SHA256SUMS` + `--install-cron` | Manual 05 |
| `10-restore-backup.sh` | Checksum-verified restore of PostgreSQL and binary storage with controlled service restart | Manual 05 |

## Coordinated Full Installation Sequence

```bash
# 1. Host & Backend image (Backend repo)
sudo ./scripts/install/01-prepare-host.sh --user ubuntu
./scripts/install/02-build-backend.sh --image-prefix my-org --tag v1.0.0

# 2. Frontend image (Frontend repo)
sudo ../fe/scripts/install/build-frontend.sh --image-prefix my-org --tag v1.0.0

# 3. Environment & Keycloak theme
sudo ./scripts/install/03-init-environment.sh --domain trazabilidad.mi-cooperativa.org --image-prefix my-org --tag v1.0.0
sudo ../fe/scripts/install/install-keycloak-theme.sh

# 4. Deploy stack + bootstrap Keycloak
sudo ./scripts/install/04-deploy-and-bootstrap-keycloak.sh

# 5. TLS reverse proxy (Frontend repo)
sudo ../fe/scripts/install/configure-nginx-tls.sh --email ops@mi-cooperativa.org

# 6. Initial administrator, Cacao seed, and full verification
sudo ./scripts/install/05-create-admin-and-seed.sh --email admin@mi-cooperativa.org --password 'ChangeMe!2026'
sudo ./scripts/install/06-verify-installation.sh --check-qr
```

All scripts expose `--help` (`-h`) cleanly without root privileges or side effects.
