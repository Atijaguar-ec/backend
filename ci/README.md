# Despliegue Backend - Fortaleza del Valle

## Credenciales requeridas en Jenkins

### Secret File: `fortaleza-env-staging`

El archivo de credenciales debe contener **todas** las siguientes variables:

```properties
# === Docker Image ===
IMAGE_NAME=ghcr.io/atijaguar-ec/backend-inatrace
TAG=staging

# === Database Configuration (CRÍTICO) ===
DATABASE_NAME=inatrace_test_fortaleza
DATASOURCE_USERNAME=inatrace_test_fortaleza
DATASOURCE_PASSWORD=<password_real>
DB_ROOT_PASSWORD=<root_password_real>

# === File Storage Paths ===
FILE_STORAGE_ROOT=/opt/inatrace/uploads
IMPORT_PATH=/opt/inatrace/import
DOCUMENTS_ROOT=/opt/inatrace/documents
FILE_STORAGE_ROOT_VOL=/opt/inatrace/uploads
IMPORT_PATH_VOL=/opt/inatrace/imports
DOCUMENTS_ROOT_VOL=/opt/inatrace/documents
DB_VOLUME=/opt/inatrace/mysql

# === External Services ===
REQUESTLOG_TOKEN=<token_real>
EXCHANGERATE_APIKEY=<api_key_real>

# === Frontend URLs ===
FE_URL=https://testinatrace.espam.edu.ec
FE_CONFIRM_URL=https://testinatrace.espam.edu.ec/confirm-email/
FE_RESET_URL=https://testinatrace.espam.edu.ec/reset-password/

# === Server Configuration ===
SERVER_PORT=8082

# === Email Configuration ===
MAIL_HOST=mail.atijaguar.com
MAIL_PORT=465
MAIL_USERNAME=desarrollo@atijaguar.com
MAIL_PASSWORD=<mail_password_real>
```

## Cómo configurar la credencial en Jenkins

1. **Ir a Jenkins UI**
   - Navigate to: `Manage Jenkins` → `Credentials` → `System` → `Global credentials`

2. **Agregar nueva credencial** (o editar existente)
   - Kind: **Secret file**
   - File: Subir `ci/.env.example` (modificado con passwords reales)
   - ID: `fortaleza-env-staging`
   - Description: `Fortaleza del Valle - Environment Variables (Staging)`

3. **Validar**
   - El pipeline validará automáticamente que existan las 3 variables críticas:
     - `DATABASE_NAME`
     - `DATASOURCE_USERNAME`
     - `DATASOURCE_PASSWORD`
   - Si falta alguna, el build fallará con mensaje claro

## Variables Críticas vs Opcionales

### Críticas (bloquean el deploy si faltan)
- `DATABASE_NAME`: Nombre de la base de datos
- `DATASOURCE_USERNAME`: Usuario de la aplicación
- `DATASOURCE_PASSWORD`: Password del usuario de la aplicación

### Opcionales (solo warning si faltan)
- `DB_ROOT_PASSWORD`: Password root de MySQL (para init scripts)
- `DB_VOLUME`: Path del volumen de MySQL
- `FILE_STORAGE_ROOT`: Rutas de almacenamiento
- Etc.

## Referencia rápida

- **Template completo**: `ci/.env.example`
- **Docker Compose**: `ci/docker-compose.yml`
- **Post-deploy script**: `ci/post-deploy-init.sh`

## Troubleshooting

### Error: "Variables críticas faltantes"
```
❌ ERROR: Variables críticas faltantes o vacías: DATABASE_NAME DATASOURCE_PASSWORD
```

**Solución**: La credencial `fortaleza-env-staging` en Jenkins está vacía o incompleta. Actualízala según la sección anterior.

### Error: "Connection refused" o "Access denied"
- Verificar que las credenciales de BD en el archivo `.env` de Jenkins coincidan con las del servidor
- Verificar que el contenedor MySQL esté corriendo: `docker ps | grep mysql`

## Notas importantes

1. **TAG e IMAGE_NAME**: Se sobrescriben automáticamente por el Jenkinsfile con el commit hash actual
2. **Passwords**: Nunca commitear passwords reales al repositorio. Usar solo Jenkins Credentials
3. **Environment**: Este setup es para entorno `test` (Fortaleza del Valle staging)
