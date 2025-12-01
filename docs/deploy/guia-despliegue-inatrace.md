# Guía de despliegue de INATrace

Esta guía describe el flujo de despliegue con GitHub Actions (UNOCACE/DUFER) y Jenkins (Fortaleza/CEDIA), el uso de Docker Compose y los archivos .env por entorno/organización.

## 1. Prerrequisitos
- Acceso a GHCR y servidores (SSH).
- Secrets configurados por entorno en GitHub/Jenkins (DB, Mail, FE URLs, AgStack si aplica).
- Imagen Docker generada desde el monorepo (`backend`), publicada en GHCR.

## 2. Artefactos del repo😔
- `Dockerfile`: build multi-stage del backend.
- `ci/docker-compose.yml`: stack backend+mysql.
- `ci/docker-compose.backend-only.yml`: solo backend (MySQL externo/compartido).
- `ci/post-deploy-init.sh` + `ci/scripts/init-company-and-admin.sh`: inicialización única (admin, empresa, país).
- Plantillas .env en `ci/env-templates/` (dev/test/prod por organización).
- Workflows: `.github/workflows/deploy-backend.yml`.

## 3. Variables de entorno (archivo .env)
Variables típicas usadas por `ci/docker-compose*.yml`:
- Imagen: `IMAGE_NAME`, `TAG`.
- DB: `DATABASE_NAME`, `DB_ROOT_PASSWORD`, `DATASOURCE_USERNAME`, `DATASOURCE_PASSWORD`, `DB_VOLUME`.
- Volúmenes: `FILE_STORAGE_ROOT[_VOL]`, `IMPORT_PATH[_VOL]`, `DOCUMENTS_ROOT[_VOL]`.
- Puertos/containers: `HOST_PORT_BE`, `HOST_PORT_DB`, `CONTAINER_NAME_BE`, `CONTAINER_NAME_DB`.
- FE/Integraciones: `FE_URL`, `FE_CONFIRM_URL`, `FE_RESET_URL`, `REQUESTLOG_TOKEN`, `EXCHANGERATE_APIKEY`.
- Mail: `SPRING_MAIL_*`, `INATRACE_MAIL_TEMPLATE_FROM`, `INATRACE_MAIL_REDIRECT`, `INATRACE_MAIL_SENDING_ENABLED`.
- AgStack (si aplica): `INATRACE_AGSTACK_EMAIL`, `INATRACE_AGSTACK_PASSWORD`.

## 4. Flujos de despliegue
### 4.1 GitHub Actions (UNOCACE/DUFER)
1) `quality` (tests/scan) → 2) `build` (push a GHCR y export `be-image.tar`) → 3) `deploy-<env>`
- El job de deploy:
  - Preflight de secretos.
  - Copia `ci/docker-compose.yml` y `.env` al servidor.
  - `docker-compose up -d` y healthcheck (`/actuator/health`).

### 4.2 Jenkins (Fortaleza/CEDIA)
- Pipeline con stages: checkout, init, tests (opcional), build/push, backup DB (prod), deploy, healthcheck.
- Usa un envfile de credenciales (copiado como `.env`) y `docker-compose.yml`.

## 5. Inicialización (UNA SOLA VEZ por organización)
- Personalizar `ci/env-templates/.env.<org>.<env>.example`.
- Copiar al server junto a `ci/post-deploy-init.sh`.
- Ejecutar: `bash post-deploy-init.sh`.
- Cambiar la contraseña del admin inicial tras el primer login.

## 6. Rollback y verificación
- Para rollback: re-desplegar un tag previo (`develop`, `test-<sha>`, `latest`).
- Validación: `docker ps`, `docker logs inatrace-be --tail 200`, healthcheck.

## 7. Conversión a guion de video
- Intro (objetivo y entornos).
- Repos/artefactos claves.
- Creación de secrets en GitHub/Jenkins.
- Ejecución del workflow (staging) y validación.
- Ejecución del pipeline Jenkins y validación.
- Post-deploy init (una vez) y verificación de usuario/empresa.
- Rollback y tips finales.
