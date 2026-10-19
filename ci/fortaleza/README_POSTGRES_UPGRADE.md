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

---

## 4. Por qué esta actualización NO viaja en el despliegue de aplicación

> Añadido el **2026-10-18** tras revisar los servidores reales.

Entre Sprint 2 y el 2026-10-18, `ci/fortaleza/docker-compose.yml` declaraba
`postgres:16.14-alpine` y montaba el `postgresql.conf` afinado. Como el stage de
despliegue a producción del `Jenkinsfile` ejecutaba
`docker compose up -d --force-recreate` **sin `--no-deps` y sin nombre de
servicio**, cada despliegue de aplicación recreaba también la base de datos y
Keycloak, aplicando esta actualización sin ventana, sin respaldo específico y
sin las verificaciones de la sección 3.

Nada de esto se había ejercitado: el despliegue de **staging** usa
`up -d --no-deps --force-recreate inatrace-backend`, así que solo toca el
backend. El cambio de imagen de la base nunca corrió en ningún entorno.

Desde el 2026-10-18:
- el compose declara `postgres:16.10`, que es lo que realmente corre;
- el despliegue a producción usa `--no-deps inatrace-backend`;
- esta actualización se ejecuta **solo** siguiendo este protocolo, en su ventana.

---

## 5. Advertencia no cubierta por la sección 1: glibc → musl

La sección 1 es correcta en que 16.10 → 16.14 es una actualización **menor** y
que los archivos del directorio de datos son compatibles. Pero el salto que
propone no es solo de versión: es de **variante de imagen**.

| | `postgres:16.10` | `postgres:16.14-alpine` |
|:---|:---|:---|
| Base | Debian | Alpine |
| libc | glibc | **musl** |
| Locales disponibles | `en_US.utf8` entre muchos | prácticamente solo `C` y `C.UTF-8` |

Las bases de Fortaleza se inicializaron con `Locale Provider: libc`,
`Collate: en_US.utf8` (verificado con `\l` el 2026-10-18). Ese locale **no
existe en musl**. Las dos consecuencias:

1. **Arranque:** PostgreSQL puede negarse a iniciar o registrar
   `FATAL: ... locale "en_US.utf8" ... not available` al abrir una base cuyo
   collation no existe en el sistema.
2. **Índices:** si arranca, el orden de las cadenas cambia respecto al que se
   usó para construir los índices B-tree sobre columnas de texto. Los índices
   quedan silenciosamente inconsistentes: búsquedas que no encuentran filas que
   sí existen. **No da error**, que es lo peligroso.

### Opciones

- **(a) Quedarse en la variante Debian.** Actualizar a `postgres:16.14`
  (sin `-alpine`). Es la misma versión del motor, mantiene glibc y el
  `en_US.utf8` de las bases. **Recomendada.**
- **(b) Ir a Alpine con recarga lógica.** `pg_dumpall` → `initdb` nuevo con un
  collation disponible en musl → `pg_restore`. Es una migración completa, no un
  cambio de imagen, y cambia el orden de las cadenas en toda la plataforma.
- **(c) Ir a Alpine sobre el directorio existente.** **No hacer.** Es el camino
  que describía el Paso 3 antes de esta nota.

Si se toma (b), verificar después: `REINDEX DATABASE` completo y
`SELECT datcollate, datctype FROM pg_database;` con el collation esperado.

---

## 6. Referencia desde el compose

`ci/fortaleza/docker-compose.yml` apunta a esta sección en el comentario del
servicio `inatrace-postgres`. Si se cambia la numeración de este documento,
actualizar también ese comentario.
