# Protocolo de Actualización y Verificación de PostgreSQL a 16.14 (HU-20)

**Ámbito:** Servidor Fortaleza del Valle (CEDIA / ESPAM) — Staging y Producción  
**Historia de Usuario:** HU-20 (ADR-007, ADR-014)  
**Objetivo:** Homogeneizar el motor relacional desde la versión `16.10` a la versión canónica `16.14` (`postgres:16.14-alpine`), sincronizando con UNOCACE y DUFER.

---

## 1. Naturaleza de la Actualización (Compatibilidad Binaria)
La actualización de PostgreSQL `16.10` a `16.14` es una **actualización menor** dentro de la misma rama mayor (16.x).  
- **No requiere** `pg_upgrade` ni recarga lógica con `pg_dump`/`pg_restore`.
- Los archivos en disco del directorio `${DB_VOLUME}` (`/var/lib/postgresql/data`) son **100% compatibles a nivel de catálogo y almacenamiento físico**.

---

## 2. Procedimiento de Ejecución Paso a Paso

### Paso 1: Respaldo Preventivo de Seguridad (Pre-Upgrade Dump)
Antes de detener el contenedor existente, ejecutar un volcado lógico completo:

```bash
# Definir directorio y marca de tiempo
BACKUP_DIR="/opt/backups"
mkdir -p "${BACKUP_DIR}"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

# Volcado lógico completo con pg_dumpall
docker exec -t inatrace-postgres pg_dumpall -c -U "${DATASOURCE_USERNAME:-inatrace}" \
  > "${BACKUP_DIR}/full_backup_pre_upgrade_16.10_${TIMESTAMP}.sql"

# Verificar tamaño e integridad
ls -lh "${BACKUP_DIR}/full_backup_pre_upgrade_16.10_${TIMESTAMP}.sql"
head -n 20 "${BACKUP_DIR}/full_backup_pre_upgrade_16.10_${TIMESTAMP}.sql"
```

### Paso 2: Descarga de la Nueva Imagen Estandarizada
Descargar la imagen `postgres:16.14-alpine`:

```bash
docker pull postgres:16.14-alpine
```

### Paso 3: Reinicio del Servicio de Base de Datos
Detener el contenedor antiguo y recrearlo con la nueva imagen y la configuración de afinado:

```bash
cd /opt/inatrace/backend/ci/fortaleza  # o ruta del docker-compose en el servidor

# Detener contenedor postgres
docker compose stop inatrace-postgres

# Recrear contenedor con nueva imagen
docker compose up -d inatrace-postgres
```

---

## 3. Criterios de Aceptación y Comandos de Verificación (DoD)

### Verificación 1: Confirmación de Versión del Motor
Ejecutar la consulta SQL de versión en el contenedor activo:

```bash
docker exec -it inatrace-postgres psql -U "${DATASOURCE_USERNAME:-inatrace}" -d "${DATABASE_NAME:-inatrace}" -c "SELECT version();"
```
**Resultado esperado:**
Debe reportar explícitamente:
```
PostgreSQL 16.14 on x86_64-pc-linux-musl ...
```

### Verificación 2: Confirmación de Parámetros de Rendimiento Afinados (HU-08)
Verificar que la configuración montada desde `postgresql.conf` está activa:

```bash
docker exec -it inatrace-postgres psql -U "${DATASOURCE_USERNAME:-inatrace}" -d "${DATABASE_NAME:-inatrace}" -c "
SHOW shared_buffers;
SHOW effective_cache_size;
SHOW work_mem;
SHOW random_page_cost;
"
```
**Resultado esperado:**
- `shared_buffers = 1GB`
- `effective_cache_size = 3GB`
- `work_mem = 16MB`
- `random_page_cost = 1.1`

### Verificación 3: Registro de Logs Limpio y Recuperación Exitosa
Inspeccionar los logs del motor:

```bash
docker logs --tail 50 inatrace-postgres
```
**Resultado esperado:**
- `database system was shut down at ...`
- `database system is ready to accept connections`
- Ausencia total de `FATAL` o errores de corrupción de catálogo.

### Verificación 4: Validación de Esquema con Flyway y Arranque del Backend
Reiniciar el backend para certificar que Flyway aplica las migraciones `V2` (Envers) y `V3` (Índices FK) y que Hibernate valida el esquema en verde:

```bash
docker compose up -d --force-recreate inatrace-backend
docker logs -f inatrace-be
```
**Resultado esperado en logs:**
- `Flyway: Successfully applied 2 migrations to schema "public"` (V2__envers_audit_tables, V3__add_fk_indexes)
- `HHH000227: Running hbm2ddl schema validation`
- `Started INATraceBackendApplication in X.XXX seconds`
