# Despliegue Backend - Trazabilidad de Cacao ESPAM

## Arquitectura de Despliegue

**Sistema:** Trazabilidad de Cacao  
**Institución:** Universidad ESPAM  
**Infraestructura:** CEDIA  
**Entornos:** Staging (test) + Producción

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

## Cómo configurar credenciales en Jenkins

### Para Staging (Test)
1. **Ir a Jenkins UI**
   - Navigate to: `Manage Jenkins` → `Credentials` → `System` → `Global credentials`

2. **Agregar credencial de staging**
   - Kind: **Secret file**
   - File: Subir tu `.env` de staging (basado en `ci/.env.example`)
   - ID: `fortaleza-env-staging`
   - Description: `ESPAM Staging - Environment Variables`

### Para Producción
1. **Crear credencial de producción** (SEPARADA de staging)
   - Kind: **Secret file**
   - File: Subir tu `.env` de **producción** (passwords diferentes)
   - ID: `fortaleza-env-prod`
   - Description: `ESPAM Producción - Environment Variables (CEDIA)`

2. **Validación automática**
   - El pipeline validará que existan las 3 variables críticas:
     - `DATABASE_NAME`
     - `DATASOURCE_USERNAME`
     - `DATASOURCE_PASSWORD`
   - Si falta alguna, usará valores por defecto (staging) o fallará (prod)

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

## Proceso de Despliegue a Producción

### Pre-requisitos
1. ✅ Código testeado en staging
2. ✅ Credencial `fortaleza-env-prod` configurada en Jenkins
3. ✅ Credencial SSH `usuario-prod-ssh` configurada
4. ✅ Merge de `staging` → `main` aprobado

### Pasos para Deploy a Producción

1. **Preparar el código**
   ```bash
   git checkout staging
   git pull origin staging
   # Verificar que todo esté OK en staging
   
   git checkout main
   git merge staging
   git push origin main
   ```

2. **Ejecutar Pipeline en Jenkins**
   - Ir a job `Deploy-Backend`
   - Click en **"Build with Parameters"**
   - Seleccionar:
     - `BRANCH = main`
     - `SKIP_TESTS = false` (SIEMPRE correr tests en prod)
     - `SKIP_DB_BACKUP = false` (SIEMPRE hacer backup)

3. **Aprobación Manual** ⏸️
   - Jenkins pausará en stage **"🔐 Aprobar Deploy a Producción"**
   - Mostrará resumen del deploy:
     - Commit a desplegar
     - Imagen Docker
     - Estado del backup
   - Solo usuarios autorizados pueden aprobar:
     - `admin`
     - `devops-espam`
     - `alvaro-sanchez`
   - Click en **"Desplegar Ahora"** para continuar

4. **Monitoreo durante Deploy**
   - Seguir logs en Jenkins en tiempo real
   - Verificar que backup se completó ✅
   - Observar healthcheck automático
   - Esperar mensaje de éxito

5. **Validación Post-Deploy**
   ```bash
   # Verificar contenedor corriendo
   docker ps | grep backend
   
   # Verificar logs
   docker logs <container-name> --tail 50
   
   # Verificar health endpoint
   curl https://inatrace.espam.edu.ec/actuator/health
   ```

6. **Rollback (si es necesario)**
   - Jenkins automáticamente hará rollback si falla healthcheck
   - Manual: desplegar el tag anterior desde Jenkins
   - Restaurar backup: `docker exec mysql mysql ... < backup-YYYYMMDD.sql.gz`

### Checklist de Seguridad

Antes del primer deploy a producción, verificar:

- [ ] Passwords de producción diferentes a staging
- [ ] Certificados SSL configurados
- [ ] Backup automático funcionando
- [ ] Logs rotando correctamente
- [ ] Firewall configurado
- [ ] Acceso SSH solo por llave
- [ ] Variables sensibles en Jenkins Credentials (NO en código)
- [ ] Documentación actualizada
- [ ] Contactos de emergencia documentados
- [ ] Plan de rollback probado

## Referencia rápida

- **Template completo**: `ci/.env.example`
- **Docker Compose**: `ci/docker-compose.yml`
- **Post-deploy script**: `ci/post-deploy-init.sh`
- **Jenkinsfile**: `ci/Jenkinsfile`

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
