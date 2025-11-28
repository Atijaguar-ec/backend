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

### 5.1 Inicialización automática mediante migración Flyway JPA

Además del flujo histórico basado en scripts (`post-deploy-init.sh` e `init-company-and-admin.sh`), el backend incluye una migración JPA específica para garantizar que **siempre exista un usuario administrador y una empresa asociada** en instalaciones nuevas:

- Clase de migración: `com.abelium.inatrace.db.migrations.V2025_11_19_02_00__Create_Default_Admin_Company`.
- Tipo: migración Java basada en JPA, ejecutada por Flyway en el arranque de la aplicación.
- Responsabilidad:
  - Crear un usuario con rol `SYSTEM_ADMIN`.
  - Crear una empresa con estado `ACTIVE`.
  - Crear la relación `CompanyUser` entre ambos con rol `COMPANY_ADMIN`.

La migración es **idempotente**: si ya existe un usuario con el email configurado, **no realiza ningún cambio** (no sobreescribe contraseña ni datos). Esto permite ejecutarla de forma segura en todos los entornos.

### 5.2 Variables de configuración del usuario administrador

El comportamiento de la migración se controla mediante propiedades de aplicación que se alimentan desde variables de entorno. Las propiedades expuestas en `application.properties` son:

- `INATrace.admin.email`
- `INATrace.admin.password`
- `INATrace.admin.name`
- `INATrace.admin.surname`
- `INATrace.admin.company.name`

Cada una de ellas se mapea a una variable de entorno estándar, con valores por defecto pensados para desarrollo:

- `INATrace.admin.email  = ${INATRACE_ADMIN_EMAIL:admin@example.com}`
- `INATrace.admin.password  = ${INATRACE_ADMIN_PASSWORD:Admin123!}`
- `INATrace.admin.name  = ${INATRACE_ADMIN_NAME:Admin}`
- `INATrace.admin.surname  = ${INATRACE_ADMIN_SURNAME:User}`
- `INATrace.admin.company.name  = ${INATRACE_ADMIN_COMPANY_NAME:Demo Company}`

Recomendaciones operativas:

- En **desarrollo**, se pueden usar los valores por defecto o configurar credenciales específicas mediante variables de entorno locales.
- En **staging** y **producción**, es obligatorio definir valores propios para `INATRACE_ADMIN_EMAIL` y `INATRACE_ADMIN_PASSWORD` a través de mecanismos seguros (Secrets de GitHub Actions, credenciales de Jenkins, vaults, etc.).
- Tras el primer acceso, se recomienda forzar el cambio de contraseña del administrador inicial siguiendo los procesos de seguridad de la organización.

### 5.3 Integración con pipelines de CI/CD

Los pipelines de despliegue están preparados para inyectar estas variables en el contenedor del backend a través de los archivos `.env` y `docker-compose`.

- `ci/docker-compose.yml` pasa directamente al contenedor las variables:
  - `INATRACE_ADMIN_EMAIL`
  - `INATRACE_ADMIN_PASSWORD`
  - `INATRACE_ADMIN_NAME`
  - `INATRACE_ADMIN_SURNAME`
  - `INATRACE_ADMIN_COMPANY_NAME`
- Estas variables son resueltas por Spring Boot hacia las propiedades `INATrace.admin.*` descritas arriba.

Flujos concretos:

- **GitHub Actions (UNOCACE/DUFER)**
  - Los jobs de `deploy-*` generan un `.env` remoto para cada organización/entorno.
  - Ese `.env` se construye combinando valores por defecto y **GitHub Secrets** específicos por entorno (por ejemplo: `DEV_ADMIN_EMAIL`, `TEST_UNOCACE_ADMIN_EMAIL`, `PROD_DUFER_ADMIN_EMAIL`, etc.).
  - El workflow escribe en el `.env` las claves `INATRACE_ADMIN_*`, que posteriormente `docker-compose` inyecta en el contenedor.

- **Jenkins (Fortaleza/CEDIA)**
  - El pipeline `ci/Jenkinsfile` utiliza credenciales tipo "secret file" (`fortaleza-env-staging`, `fortaleza-env-prod`) que se copian como `.env` en el servidor.
  - Estos `.env` deben contener explícitamente las variables `INATRACE_ADMIN_EMAIL`, `INATRACE_ADMIN_PASSWORD`, `INATRACE_ADMIN_NAME`, `INATRACE_ADMIN_SURNAME` e `INATRACE_ADMIN_COMPANY_NAME` para que la migración pueda crear el usuario/empresa inicial.
  - Durante el despliegue, el Jenkinsfile solo actualiza dinámicamente `TAG` e `IMAGE_NAME`, respetando el resto del contenido del `.env` (incluido el bloque de `INATRACE_ADMIN_*`).

Con esta configuración, todas las instalaciones nuevas (tanto en entornos gestionados por GitHub Actions como por Jenkins) disponen de un usuario administrador y una empresa inicial listos para acceder al sistema nada más completar el despliegue.

### 5.4 Configuración del tipo de producto y FacilityTypes

INATrace permite prefijar el catálogo de tipos de instalaciones (`FacilityType`) según el **tipo de producto** (por ejemplo, café o cocoa) mediante:

- Propiedad de aplicación: `INATrace.product.type`
- Variable de entorno: `INATRACE_PRODUCT_TYPE` (por defecto `COCOA` si no se define)

La migración JPA `com.abelium.inatrace.db.migrations.V2021_08_11_11_33__Prefill_FacilityTypes` se encarga de inicializar la tabla `FacilityType` en **bases de datos nuevas**:

- Solo inserta datos si la tabla `FacilityType` está vacía (es decir, no modifica instalaciones ya existentes).
- Selecciona el conjunto de tipos de instalación según el valor de `INATrace.product.type`:

  - `COFFEE`: se cargan los tipos por defecto para café (`WASHING_STATION`, `DRYING_BED`, `HULLING_STATION`, `STORAGE`).
  - `COCOA`: se cargan los tipos para cocoa, con códigos estables y etiquetas en inglés:
    - `ACOPIO` – `Collection Center`
    - `ESCURRIDO` – `Draining`
    - `FERMENTACION` – `Fermentation Area`
    - `SECADO` – `Drying Area`
    - `SECADON` – `Natural Drying`
    - `SECADOA` – `Artificial Drying`
    - `ALIMPIEZA` – `Cleaning Area`
    - `ACLASIFICADO` – `Grading Area`
    - `AEMPACADO` – `Packing Area`
    - `ALMACEN` – `Storage Area`
    - `VENTA` – `Point of Sale`

Recomendaciones operativas:

- Definir explícitamente `INATRACE_PRODUCT_TYPE` en los `.env` de cada entorno:
  - **UNOCACE/DUFER (GitHub Actions)**: el workflow que genera el `.env` remoto puede añadir una línea `INATRACE_PRODUCT_TYPE=COFFEE` o `COCOA` según la organización/proyecto.
  - **Fortaleza/CEDIA (Jenkins)**: incluir `INATRACE_PRODUCT_TYPE=COFFEE` o `COCOA` en los envfiles secretos (`fortaleza-env-staging`, `fortaleza-env-prod`) que se copian como `.env` al servidor.
- En bases de datos ya existentes donde la tabla `FacilityType` no esté vacía, esta migración **no modifica** los datos actuales; para cambios de modelo (por ejemplo, pasar de café a cocoa) se recomienda preparar una migración específica adicional que gestione el cambio de manera controlada.

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
