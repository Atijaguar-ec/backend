# Plan de Capacitación: Mantenimiento y Operaciones INATrace Backend
## Perfil: Técnico Helpdesk e Infraestructura

---

## 📋 Información General

**Duración:** 5 días (3-4 horas por día)  
**Modalidad:** Práctica intensiva con ejercicios reales  
**Nivel:** Técnico con conocimientos básicos de Linux y Docker  
**Objetivo:** Capacitar al técnico para realizar mantenimiento operativo, respaldos, reinicio de servicios, monitoreo y resolución de problemas comunes del backend INATrace

---

## 🎯 Objetivos de Aprendizaje

Al finalizar la capacitación, el técnico será capaz de:

1. ✅ Verificar el estado de salud del sistema backend
2. ✅ Reiniciar servicios de forma segura (backend y base de datos)
3. ✅ Realizar respaldos manuales y verificar respaldos automáticos
4. ✅ Restaurar base de datos desde respaldos
5. ✅ Consultar y analizar logs para diagnóstico
6. ✅ Identificar y resolver problemas comunes
7. ✅ Ejecutar procedimientos de mantenimiento preventivo
8. ✅ Escalar problemas complejos con información adecuada

---

## 📚 Día 1: Arquitectura y Componentes del Sistema

### Sesión 1: Introducción a INATrace (1 hora)

#### Conceptos Clave
- **INATrace**: Sistema de trazabilidad para cadenas de valor agrícolas (cacao, camarón)
- **Arquitectura**: Backend Java + Base de datos MySQL + Frontend Angular
- **Tecnologías**: Spring Boot, Docker, MySQL 8.0, Jenkins CI/CD

#### Componentes del Backend
```
┌─────────────────────────────────────────────┐
│           FRONTEND (Angular)                │
│     https://testinatrace.espam.edu.ec      │
└─────────────────┬───────────────────────────┘
                  │ HTTPS/API REST
┌─────────────────▼───────────────────────────┐
│      BACKEND (Spring Boot + Java 17)        │
│   Container: inatrace-be-dev/test/prod      │
│   Puerto: 8080, 8082, etc.                  │
│   Health: /actuator/health                  │
└─────────────────┬───────────────────────────┘
                  │ JDBC
┌─────────────────▼───────────────────────────┐
│       BASE DE DATOS (MySQL 8.0.35)          │
│   Container: inatrace-mysql-dev/test/prod   │
│   Puerto: 3306                              │
│   Volumen: /var/lib/mysql                   │
└─────────────────────────────────────────────┘
```

#### Entornos Desplegados
1. **Desarrollo (dev)**: Local, para pruebas de desarrolladores
2. **Staging (test)**: https://testinatrace.espam.edu.ec - Pruebas pre-producción
3. **Producción (prod)**: https://inatrace.espam.edu.ec - Sistema en vivo

### Sesión 2: Acceso al Sistema y Herramientas (1.5 horas)

#### Práctica 1: Conectarse al Servidor
```bash
# Servidor de staging (mismo que Jenkins)
ssh usuario@servidor-staging

# Servidor de producción (CEDIA)
ssh administrador@10.10.102.26
```

#### Práctica 2: Verificar Contenedores Docker
```bash
# Listar todos los contenedores corriendo
docker ps

# Ver contenedores específicos de INATrace
docker ps | grep inatrace

# Ver todos los contenedores (incluyendo detenidos)
docker ps -a

# Inspeccionar un contenedor específico
docker inspect inatrace-be-test-fortaleza
```

**Salida esperada:**
```
CONTAINER ID   IMAGE                                    STATUS         PORTS                    NAMES
abc123def456   ghcr.io/atijaguar-ec/backend-inatrace   Up 2 hours     0.0.0.0:8082->8080/tcp   inatrace-be-test-fortaleza
xyz789ghi012   mysql:8.0.35                             Up 2 hours     0.0.0.0:3306->3306/tcp   inatrace-mysql-test-fortaleza
```

#### Práctica 3: Navegar Estructura de Directorios
```bash
# Directorio de despliegue staging
cd /opt/inatrace/backend/test/fortaleza
ls -la

# Archivos importantes:
# - docker-compose.yml: Configuración de servicios
# - .env: Variables de entorno (SENSIBLE - no compartir)
# - /opt/inatrace/uploads: Archivos subidos
# - /opt/inatrace/mysql: Datos de base de datos
# - /opt/inatrace/backups: Respaldos automáticos
```

### Sesión 3: Verificación de Estado del Sistema (1 hora)

#### Práctica 4: Healthcheck del Backend
```bash
# Verificar salud del backend (staging)
curl -s http://localhost:8082/actuator/health | jq

# Verificar salud del backend (producción)
curl -s http://localhost:8082/actuator/health | jq

# Salida esperada:
# {
#   "status": "UP",
#   "components": {
#     "db": { "status": "UP" },
#     "diskSpace": { "status": "UP" },
#     "ping": { "status": "UP" }
#   }
# }
```

#### Práctica 5: Verificar Estado de Contenedores
```bash
# Ver estado de salud nativo de Docker
docker inspect inatrace-be-test-fortaleza --format='{{.State.Health.Status}}'
# Salida esperada: healthy

# Ver logs recientes del backend
docker logs inatrace-be-test-fortaleza --tail 50

# Seguir logs en tiempo real
docker logs -f inatrace-be-test-fortaleza
# Presionar Ctrl+C para salir
```

#### Práctica 6: Verificar Conectividad Base de Datos
```bash
# Conectarse a MySQL desde el contenedor
docker exec -it inatrace-mysql-test-fortaleza mysql -uinatrace_test_fortaleza -p

# Dentro de MySQL, ejecutar:
SHOW DATABASES;
USE inatrace_test_fortaleza;
SHOW TABLES;
SELECT COUNT(*) FROM User;
SELECT COUNT(*) FROM Company;
EXIT;
```

### 📝 Ejercicio Día 1: Reporte de Estado del Sistema

**Tarea:** Crear un script de verificación rápida y ejecutarlo

```bash
#!/bin/bash
# Archivo: /home/usuario/check-inatrace-health.sh

echo "=== REPORTE DE ESTADO INATRACE ==="
echo "Fecha: $(date)"
echo ""

echo "1. Contenedores Docker:"
docker ps --filter "name=inatrace" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
echo ""

echo "2. Healthcheck Backend:"
curl -s http://localhost:8082/actuator/health | jq -r '.status'
echo ""

echo "3. Espacio en Disco:"
df -h | grep -E "Filesystem|/opt/inatrace"
echo ""

echo "4. Uso de Memoria Contenedores:"
docker stats --no-stream --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}" | grep inatrace
echo ""

echo "=== FIN REPORTE ==="
```

**Ejecutar:**
```bash
chmod +x /home/usuario/check-inatrace-health.sh
./check-inatrace-health.sh
```

---

## 🔧 Día 2: Operaciones de Mantenimiento Básico

### Sesión 1: Reinicio de Servicios (1.5 horas)

#### Práctica 7: Reinicio Seguro del Backend

**Escenario:** El backend responde lento o tiene errores. Necesitas reiniciarlo.

```bash
# Paso 1: Verificar estado actual
cd /opt/inatrace/backend/test/fortaleza
docker compose ps

# Paso 2: Reiniciar solo el backend (sin afectar MySQL)
docker compose restart backend

# Paso 3: Esperar y verificar healthcheck
sleep 30
docker inspect inatrace-be-test-fortaleza --format='{{.State.Health.Status}}'

# Paso 4: Ver logs para confirmar inicio exitoso
docker logs inatrace-be-test-fortaleza --tail 100 | grep -i "started"
```

**Señales de inicio exitoso en logs:**
```
Started INATraceBackendApplication in X.XXX seconds
Tomcat started on port(s): 8080 (http)
```

#### Práctica 8: Reinicio Completo del Sistema

**⚠️ ADVERTENCIA:** Esto causa downtime. Solo en mantenimiento programado.

```bash
cd /opt/inatrace/backend/test/fortaleza

# Paso 1: Detener todos los servicios
docker compose down

# Paso 2: Verificar que se detuvieron
docker ps | grep inatrace

# Paso 3: Levantar servicios
docker compose up -d

# Paso 4: Monitorear inicio
docker compose logs -f backend
# Esperar mensaje "Started INATraceBackendApplication"
# Presionar Ctrl+C

# Paso 5: Verificar healthcheck
curl -s http://localhost:8082/actuator/health | jq
```

#### Práctica 9: Manejo de Contenedores Problemáticos

**Escenario:** Un contenedor está en estado "unhealthy" o "restarting"

```bash
# Ver estado detallado
docker inspect inatrace-be-test-fortaleza | jq '.[0].State'

# Ver logs de errores
docker logs inatrace-be-test-fortaleza --tail 200 | grep -i error

# Forzar recreación del contenedor
cd /opt/inatrace/backend/test/fortaleza
docker compose up -d --force-recreate backend

# Si persiste, verificar variables de entorno
docker exec inatrace-be-test-fortaleza env | grep INATRACE
```

### Sesión 2: Gestión de Logs (1.5 horas)

#### Práctica 10: Consultar Logs del Backend

```bash
# Ver últimas 100 líneas
docker logs inatrace-be-test-fortaleza --tail 100

# Ver logs con timestamps
docker logs inatrace-be-test-fortaleza --timestamps --tail 50

# Buscar errores específicos
docker logs inatrace-be-test-fortaleza | grep -i "error\|exception\|failed"

# Buscar logs de un usuario específico
docker logs inatrace-be-test-fortaleza | grep "user@example.com"

# Exportar logs a archivo para análisis
docker logs inatrace-be-test-fortaleza > /tmp/backend-logs-$(date +%Y%m%d-%H%M%S).log
```

#### Práctica 11: Logs de Base de Datos

```bash
# Ver logs de MySQL
docker logs inatrace-mysql-test-fortaleza --tail 100

# Buscar errores de conexión
docker logs inatrace-mysql-test-fortaleza | grep -i "error\|warning"

# Ver queries lentas (si slow query log está habilitado)
docker exec inatrace-mysql-test-fortaleza cat /var/log/mysql/slow-query.log
```

#### Práctica 12: Análisis de Logs para Troubleshooting

**Errores Comunes y Cómo Identificarlos:**

1. **Error de conexión a base de datos:**
```bash
docker logs inatrace-be-test-fortaleza | grep "Communications link failure"
# Solución: Verificar que MySQL esté corriendo
```

2. **Error de autenticación:**
```bash
docker logs inatrace-be-test-fortaleza | grep "Access denied for user"
# Solución: Verificar credenciales en .env
```

3. **Error de migración Flyway:**
```bash
docker logs inatrace-be-test-fortaleza | grep "FlywayException"
# Solución: Ver sección de Flyway en Día 4
```

4. **Puerto en uso:**
```bash
docker logs inatrace-be-test-fortaleza | grep "Port.*already in use"
# Solución: Verificar conflictos de puerto
```

### Sesión 3: Monitoreo de Recursos (1 hora)

#### Práctica 13: Monitorear Uso de Recursos

```bash
# Ver uso de CPU y memoria en tiempo real
docker stats inatrace-be-test-fortaleza inatrace-mysql-test-fortaleza

# Ver uso de disco
df -h /opt/inatrace

# Ver tamaño de volúmenes Docker
docker system df -v | grep inatrace

# Ver procesos dentro del contenedor
docker top inatrace-be-test-fortaleza
```

#### Práctica 14: Limpieza de Recursos

```bash
# Limpiar imágenes no usadas (libera espacio)
docker image prune -a -f

# Limpiar volúmenes huérfanos
docker volume prune -f

# Ver espacio recuperado
docker system df
```

### 📝 Ejercicio Día 2: Procedimiento de Reinicio Documentado

**Tarea:** Documentar el procedimiento completo de reinicio para el equipo

Crear archivo: `/home/usuario/procedimiento-reinicio-inatrace.md`

```markdown
# Procedimiento de Reinicio INATrace Backend

## Pre-requisitos
- [ ] Acceso SSH al servidor
- [ ] Permisos sudo/docker
- [ ] Ventana de mantenimiento aprobada (si es producción)

## Pasos

1. **Notificar a usuarios** (solo producción)
2. **Verificar estado actual**: `docker ps | grep inatrace`
3. **Reiniciar backend**: `docker compose restart backend`
4. **Esperar 60 segundos**
5. **Verificar healthcheck**: `curl http://localhost:8082/actuator/health`
6. **Revisar logs**: `docker logs inatrace-be-test-fortaleza --tail 50`
7. **Confirmar funcionamiento**: Probar login en frontend

## Rollback
Si falla: `docker compose down && docker compose up -d`

## Contactos de Escalamiento
- Desarrollador: [nombre] - [email]
- DevOps: [nombre] - [email]
```

---

## 💾 Día 3: Respaldos y Restauración

### Sesión 1: Respaldos Manuales de Base de Datos (1.5 horas)

#### Práctica 15: Crear Respaldo Manual Completo

```bash
# Crear directorio de respaldos si no existe
mkdir -p /opt/inatrace/backups

# Respaldo completo de la base de datos
docker exec inatrace-mysql-test-fortaleza sh -c \
  'mysqldump -u root -p"${MYSQL_ROOT_PASSWORD}" \
  --single-transaction \
  --quick \
  --lock-tables=false \
  "${MYSQL_DATABASE}" | gzip -9' > \
  /opt/inatrace/backups/backup-manual-$(date +%Y%m%d-%H%M%S).sql.gz

# Verificar que se creó el respaldo
ls -lh /opt/inatrace/backups/backup-manual-*.sql.gz

# Ver tamaño del respaldo
du -h /opt/inatrace/backups/backup-manual-*.sql.gz
```

#### Práctica 16: Respaldo de Tablas Específicas

```bash
# Respaldo solo de tablas de usuarios y empresas
docker exec inatrace-mysql-test-fortaleza sh -c \
  'mysqldump -u root -p"${MYSQL_ROOT_PASSWORD}" \
  "${MYSQL_DATABASE}" User Company CompanyUser | gzip -9' > \
  /opt/inatrace/backups/backup-users-$(date +%Y%m%d-%H%M%S).sql.gz
```

#### Práctica 17: Verificar Integridad del Respaldo

```bash
# Descomprimir y verificar contenido (sin restaurar)
zcat /opt/inatrace/backups/backup-manual-*.sql.gz | head -n 50

# Verificar que contiene CREATE TABLE y INSERT
zcat /opt/inatrace/backups/backup-manual-*.sql.gz | grep -c "CREATE TABLE"
zcat /opt/inatrace/backups/backup-manual-*.sql.gz | grep -c "INSERT INTO"
```

### Sesión 2: Respaldos Automáticos (1 hora)

#### Práctica 18: Verificar Respaldos Automáticos de Jenkins

**Los respaldos automáticos se crean en cada despliegue a producción.**

```bash
# Ver respaldos automáticos
ls -lth /opt/inatrace/backups/backup-*-pre-deploy.sql.gz | head -10

# Ver respaldos de los últimos 7 días
find /opt/inatrace/backups -name "backup-*-pre-deploy.sql.gz" -mtime -7 -ls
```

#### Práctica 19: Configurar Respaldo Programado con Cron

**Crear script de respaldo:**

```bash
# Crear script
sudo nano /usr/local/bin/backup-inatrace.sh
```

**Contenido del script:**
```bash
#!/bin/bash
# Script de respaldo automático INATrace

BACKUP_DIR="/opt/inatrace/backups"
CONTAINER_NAME="inatrace-mysql-prod-fortaleza"
RETENTION_DAYS=30

# Crear respaldo
docker exec "${CONTAINER_NAME}" sh -c '
  mysqldump -u root -p"${MYSQL_ROOT_PASSWORD}" \
    --single-transaction \
    --quick \
    --lock-tables=false \
    "${MYSQL_DATABASE}" | gzip -9
' > "${BACKUP_DIR}/backup-auto-$(date +%Y%m%d-%H%M%S).sql.gz"

# Limpiar respaldos antiguos
find "${BACKUP_DIR}" -name "backup-auto-*.sql.gz" -mtime +${RETENTION_DAYS} -delete

# Log
echo "$(date): Respaldo completado" >> /var/log/inatrace-backup.log
```

**Hacer ejecutable:**
```bash
sudo chmod +x /usr/local/bin/backup-inatrace.sh
```

**Configurar cron (diario a las 2 AM):**
```bash
sudo crontab -e

# Agregar línea:
0 2 * * * /usr/local/bin/backup-inatrace.sh
```

### Sesión 3: Restauración de Base de Datos (1.5 horas)

#### Práctica 20: Restauración Completa (Ambiente de Prueba)

**⚠️ IMPORTANTE:** Practicar SOLO en ambiente de staging, NUNCA en producción sin autorización.

```bash
# Paso 1: Identificar respaldo a restaurar
ls -lth /opt/inatrace/backups/*.sql.gz | head -5

# Paso 2: Detener el backend (para evitar escrituras)
cd /opt/inatrace/backend/test/fortaleza
docker compose stop backend

# Paso 3: Crear respaldo de seguridad antes de restaurar
docker exec inatrace-mysql-test-fortaleza sh -c \
  'mysqldump -u root -p"${MYSQL_ROOT_PASSWORD}" \
  "${MYSQL_DATABASE}" | gzip -9' > \
  /opt/inatrace/backups/backup-pre-restore-$(date +%Y%m%d-%H%M%S).sql.gz

# Paso 4: Restaurar desde respaldo
zcat /opt/inatrace/backups/backup-manual-YYYYMMDD-HHMMSS.sql.gz | \
  docker exec -i inatrace-mysql-test-fortaleza \
  mysql -u root -p"${MYSQL_ROOT_PASSWORD}" "${MYSQL_DATABASE}"

# Paso 5: Verificar restauración
docker exec -it inatrace-mysql-test-fortaleza mysql \
  -u root -p"${MYSQL_ROOT_PASSWORD}" "${MYSQL_DATABASE}" \
  -e "SELECT COUNT(*) FROM User; SELECT COUNT(*) FROM Company;"

# Paso 6: Reiniciar backend
docker compose start backend

# Paso 7: Verificar funcionamiento
sleep 30
curl -s http://localhost:8082/actuator/health | jq
```

#### Práctica 21: Restauración de Tabla Específica

```bash
# Extraer solo una tabla del respaldo
zcat /opt/inatrace/backups/backup-manual-*.sql.gz | \
  sed -n '/CREATE TABLE.*`User`/,/UNLOCK TABLES/p' > /tmp/user-table.sql

# Restaurar solo esa tabla (después de hacer backup)
docker exec -i inatrace-mysql-test-fortaleza \
  mysql -u root -p"${MYSQL_ROOT_PASSWORD}" "${MYSQL_DATABASE}" < /tmp/user-table.sql
```

### 📝 Ejercicio Día 3: Plan de Respaldo y Recuperación

**Tarea:** Crear un plan de respaldo completo

Crear archivo: `/home/usuario/plan-respaldo-inatrace.md`

```markdown
# Plan de Respaldo y Recuperación INATrace

## Estrategia de Respaldos

### Respaldos Automáticos
- **Frecuencia**: Diario a las 2:00 AM
- **Retención**: 30 días
- **Ubicación**: /opt/inatrace/backups
- **Tipo**: Completo (todas las tablas)

### Respaldos Pre-Despliegue
- **Cuándo**: Antes de cada deploy a producción
- **Retención**: 14 días
- **Responsable**: Jenkins CI/CD

### Respaldos Manuales
- **Cuándo**: Antes de cambios críticos
- **Responsable**: Técnico de turno

## Procedimiento de Restauración

### Nivel 1: Restauración Parcial (< 1 hora downtime)
1. Detener backend
2. Restaurar tabla específica
3. Reiniciar backend
4. Verificar

### Nivel 2: Restauración Completa (< 2 horas downtime)
1. Notificar usuarios
2. Detener backend
3. Backup de seguridad
4. Restaurar BD completa
5. Reiniciar servicios
6. Validación funcional

## Pruebas de Restauración
- **Frecuencia**: Mensual
- **Ambiente**: Staging
- **Documentar**: Tiempo de restauración, problemas encontrados
```

---

## 🔍 Día 4: Diagnóstico y Resolución de Problemas

### Sesión 1: Problemas Comunes y Soluciones (2 horas)

#### Problema 1: Backend No Inicia

**Síntomas:**
- Contenedor en estado "Restarting"
- Healthcheck falla continuamente

**Diagnóstico:**
```bash
# Ver logs completos
docker logs inatrace-be-test-fortaleza --tail 500

# Buscar errores específicos
docker logs inatrace-be-test-fortaleza 2>&1 | grep -i "error\|exception\|failed"
```

**Causas Comunes:**

1. **Error de conexión a MySQL:**
```bash
# Verificar que MySQL esté corriendo
docker ps | grep mysql

# Verificar conectividad desde backend
docker exec inatrace-be-test-fortaleza ping -c 3 inatrace-mysql-test-fortaleza

# Verificar credenciales en .env
cd /opt/inatrace/backend/test/fortaleza
grep -E "DATABASE_NAME|DATASOURCE_USERNAME|DATASOURCE_PASSWORD" .env
```

**Solución:**
```bash
# Reiniciar MySQL primero
docker compose restart mysql
sleep 10

# Luego reiniciar backend
docker compose restart backend
```

2. **Error de Migraciones Flyway:**
```bash
# Buscar error específico
docker logs inatrace-be-test-fortaleza | grep "FlywayException"

# Ver estado de migraciones
docker exec -it inatrace-mysql-test-fortaleza mysql \
  -u root -p"${MYSQL_ROOT_PASSWORD}" "${MYSQL_DATABASE}" \
  -e "SELECT version, description, success FROM schema_version ORDER BY installed_rank DESC LIMIT 10;"
```

**Solución:**
```bash
# Si hay migración fallida (success=0), ejecutar repair
# Agregar variable de entorno en .env:
echo "INATRACE_FLYWAY_REPAIR_ON_STARTUP=true" >> .env

# Reiniciar backend
docker compose restart backend

# Verificar que se reparó
docker logs inatrace-be-test-fortaleza | grep "flyway.*repair"
```

#### Problema 2: Backend Lento o Sin Respuesta

**Síntomas:**
- Timeouts en el frontend
- Healthcheck tarda mucho

**Diagnóstico:**
```bash
# Ver uso de recursos
docker stats inatrace-be-test-fortaleza --no-stream

# Ver conexiones activas a MySQL
docker exec inatrace-mysql-test-fortaleza mysql \
  -u root -p"${MYSQL_ROOT_PASSWORD}" \
  -e "SHOW PROCESSLIST;"

# Ver threads del backend
docker top inatrace-be-test-fortaleza
```

**Soluciones:**

1. **Reinicio suave:**
```bash
docker compose restart backend
```

2. **Si persiste, verificar espacio en disco:**
```bash
df -h /opt/inatrace
docker system df
```

3. **Limpiar logs antiguos:**
```bash
# Truncar logs de Docker (libera espacio)
truncate -s 0 $(docker inspect --format='{{.LogPath}}' inatrace-be-test-fortaleza)
```

#### Problema 3: Errores 500 en el Frontend

**Diagnóstico:**
```bash
# Ver errores recientes en backend
docker logs inatrace-be-test-fortaleza --since 10m | grep "ERROR"

# Ver stack traces completos
docker logs inatrace-be-test-fortaleza --since 10m | grep -A 20 "Exception"
```

**Solución:**
```bash
# Exportar logs para análisis
docker logs inatrace-be-test-fortaleza > /tmp/backend-error-$(date +%Y%m%d-%H%M%S).log

# Enviar logs al equipo de desarrollo con contexto:
# - ¿Qué acción realizaba el usuario?
# - ¿Hora exacta del error?
# - ¿Usuario afectado?
```

### Sesión 2: Herramientas de Diagnóstico Avanzado (1.5 horas)

#### Práctica 22: Consultas SQL de Diagnóstico

**Crear script de diagnóstico:**

```bash
# Archivo: /home/usuario/diagnose-db.sh
#!/bin/bash

CONTAINER="inatrace-mysql-test-fortaleza"
DB="inatrace_test_fortaleza"

echo "=== DIAGNÓSTICO BASE DE DATOS INATRACE ==="
echo ""

echo "1. Tamaño de tablas principales:"
docker exec $CONTAINER mysql -u root -p"${MYSQL_ROOT_PASSWORD}" $DB -e "
SELECT 
  table_name AS 'Tabla',
  ROUND(((data_length + index_length) / 1024 / 1024), 2) AS 'Tamaño (MB)'
FROM information_schema.TABLES
WHERE table_schema = '$DB'
ORDER BY (data_length + index_length) DESC
LIMIT 10;
"

echo ""
echo "2. Conteo de registros principales:"
docker exec $CONTAINER mysql -u root -p"${MYSQL_ROOT_PASSWORD}" $DB -e "
SELECT 'User' AS tabla, COUNT(*) AS registros FROM User
UNION ALL
SELECT 'Company', COUNT(*) FROM Company
UNION ALL
SELECT 'UserCustomer', COUNT(*) FROM UserCustomer
UNION ALL
SELECT 'StockOrder', COUNT(*) FROM StockOrder
UNION ALL
SELECT 'Transaction', COUNT(*) FROM Transaction;
"

echo ""
echo "3. Últimas migraciones Flyway:"
docker exec $CONTAINER mysql -u root -p"${MYSQL_ROOT_PASSWORD}" $DB -e "
SELECT installed_rank, version, description, type, installed_on, success
FROM schema_version
ORDER BY installed_rank DESC
LIMIT 5;
"

echo ""
echo "4. Usuarios activos:"
docker exec $CONTAINER mysql -u root -p"${MYSQL_ROOT_PASSWORD}" $DB -e "
SELECT role, status, COUNT(*) as cantidad
FROM User
GROUP BY role, status;
"
```

#### Práctica 23: Verificación de Configuración

```bash
# Ver todas las variables de entorno del backend
docker exec inatrace-be-test-fortaleza env | sort | grep INATRACE

# Verificar conectividad externa (API de tasas de cambio)
docker exec inatrace-be-test-fortaleza curl -s https://api.exchangerate.host/latest

# Verificar configuración de correo
docker exec inatrace-be-test-fortaleza env | grep SPRING_MAIL
```

### 📝 Ejercicio Día 4: Guía de Troubleshooting

**Tarea:** Crear una guía rápida de resolución de problemas

Crear archivo: `/home/usuario/guia-troubleshooting-inatrace.md`

```markdown
# Guía Rápida de Troubleshooting INATrace

## Checklist Inicial (5 minutos)

- [ ] ¿Contenedores corriendo? `docker ps | grep inatrace`
- [ ] ¿Healthcheck OK? `curl http://localhost:8082/actuator/health`
- [ ] ¿Espacio en disco? `df -h /opt/inatrace`
- [ ] ¿Logs con errores? `docker logs inatrace-be-test-fortaleza --tail 50`

## Problemas Frecuentes

### 1. "No puedo acceder al sistema"
- Verificar que backend esté healthy
- Verificar que frontend esté desplegado
- Verificar firewall/proxy (Traefik/Nginx)

### 2. "Error al iniciar sesión"
- Ver logs: `docker logs inatrace-be-test-fortaleza | grep -i auth`
- Verificar usuario existe en BD
- Verificar contraseña no expirada

### 3. "El sistema está lento"
- Ver recursos: `docker stats inatrace-be-test-fortaleza`
- Ver conexiones MySQL: `SHOW PROCESSLIST`
- Considerar reinicio en horario de bajo uso

### 4. "Error al subir archivos"
- Verificar espacio: `df -h /opt/inatrace/uploads`
- Verificar permisos: `ls -ld /opt/inatrace/uploads`
- Ver logs de error específico

## Cuándo Escalar

Escalar a desarrollo si:
- Error persiste después de reinicio
- Error relacionado con lógica de negocio
- Necesitas modificar base de datos
- Necesitas cambiar configuración de aplicación
```

---

## 🚀 Día 5: Procedimientos de Mantenimiento y CI/CD

### Sesión 1: Despliegues con Jenkins (1.5 horas)

#### Práctica 24: Monitorear un Despliegue

**Acceder a Jenkins:**
```
URL: http://jenkins.servidor.com:8080
Usuario: [proporcionado por instructor]
```

**Pasos:**

1. **Ver historial de despliegues:**
   - Ir a job "Deploy-Backend-Fortaleza"
   - Ver últimos builds exitosos y fallidos

2. **Monitorear despliegue en progreso:**
   - Click en build #XX en progreso
   - Ver "Console Output"
   - Identificar etapas: Tests → Build → Push → Deploy

3. **Verificar post-despliegue:**
```bash
# En el servidor, verificar que se actualizó la imagen
docker images | grep backend-inatrace | head -3

# Ver que el contenedor usa la nueva imagen
docker inspect inatrace-be-test-fortaleza | grep Image

# Verificar logs de inicio
docker logs inatrace-be-test-fortaleza --tail 100 | grep "Started INATraceBackendApplication"
```

#### Práctica 25: Rollback Manual

**Escenario:** El último despliegue causó problemas. Necesitas volver a la versión anterior.

```bash
cd /opt/inatrace/backend/test/fortaleza

# Paso 1: Ver imágenes disponibles
docker images | grep backend-inatrace

# Paso 2: Identificar tag anterior (ejemplo: test-abc1234)
# Editar .env para cambiar TAG
nano .env
# Cambiar: TAG=test-xyz9999
# Por:     TAG=test-abc1234

# Paso 3: Recrear contenedor con imagen anterior
docker compose up -d --force-recreate backend

# Paso 4: Verificar
docker logs inatrace-be-test-fortaleza --tail 50
curl -s http://localhost:8082/actuator/health | jq
```

### Sesión 2: Mantenimiento Preventivo (1.5 horas)

#### Práctica 26: Limpieza de Sistema

**Script de mantenimiento mensual:**

```bash
#!/bin/bash
# Archivo: /usr/local/bin/maintenance-inatrace.sh

echo "=== MANTENIMIENTO PREVENTIVO INATRACE ==="
echo "Fecha: $(date)"
echo ""

# 1. Limpiar imágenes Docker antiguas
echo "1. Limpiando imágenes Docker antiguas..."
docker image prune -a -f --filter "until=720h"

# 2. Limpiar logs antiguos de Docker
echo "2. Rotando logs de contenedores..."
for container in $(docker ps -q --filter "name=inatrace"); do
  logfile=$(docker inspect --format='{{.LogPath}}' $container)
  if [ -f "$logfile" ]; then
    size=$(stat -f%z "$logfile" 2>/dev/null || stat -c%s "$logfile")
    if [ $size -gt 104857600 ]; then  # > 100MB
      echo "  Truncando log de $container ($(($size/1024/1024))MB)"
      truncate -s 0 "$logfile"
    fi
  fi
done

# 3. Verificar espacio en disco
echo "3. Espacio en disco:"
df -h /opt/inatrace | tail -1

# 4. Limpiar respaldos antiguos (>60 días)
echo "4. Limpiando respaldos antiguos..."
find /opt/inatrace/backups -name "backup-*.sql.gz" -mtime +60 -delete

# 5. Optimizar tablas MySQL (opcional, en ventana de mantenimiento)
# echo "5. Optimizando tablas MySQL..."
# docker exec inatrace-mysql-test-fortaleza mysqlcheck -u root -p"${MYSQL_ROOT_PASSWORD}" --optimize --all-databases

echo ""
echo "=== MANTENIMIENTO COMPLETADO ==="
```

#### Práctica 27: Actualización de Contenedores Base

**Actualizar imagen MySQL (en staging primero):**

```bash
cd /opt/inatrace/backend/test/fortaleza

# Paso 1: Backup completo
docker exec inatrace-mysql-test-fortaleza sh -c \
  'mysqldump -u root -p"${MYSQL_ROOT_PASSWORD}" --all-databases | gzip -9' > \
  /opt/inatrace/backups/backup-pre-mysql-update-$(date +%Y%m%d).sql.gz

# Paso 2: Detener servicios
docker compose down

# Paso 3: Actualizar versión en docker-compose.yml
nano docker-compose.yml
# Cambiar: image: mysql:8.0.35
# Por:     image: mysql:8.0.36

# Paso 4: Levantar con nueva versión
docker compose up -d

# Paso 5: Verificar
docker logs inatrace-mysql-test-fortaleza --tail 50
docker exec -it inatrace-mysql-test-fortaleza mysql --version
```

### Sesión 3: Documentación y Handover (1 hora)

#### Práctica 28: Crear Runbook Operativo

**Tarea:** Consolidar toda la información en un runbook

```markdown
# Runbook Operativo INATrace Backend

## Información de Contacto

### Equipo
- **Técnico Helpdesk**: [Nombre] - [Teléfono] - [Email]
- **Desarrollador Backend**: [Nombre] - [Email]
- **DevOps**: [Nombre] - [Email]

### Horarios de Soporte
- **Horario laboral**: Lunes a Viernes 8:00-17:00
- **Emergencias**: [Teléfono de guardia]

## Accesos

### Servidores
- **Staging**: ssh usuario@servidor-staging
- **Producción**: ssh administrador@10.10.102.26

### Aplicaciones
- **Jenkins**: http://jenkins.servidor.com:8080
- **Frontend Staging**: https://testinatrace.espam.edu.ec
- **Frontend Producción**: https://inatrace.espam.edu.ec

## Procedimientos Operativos Estándar (SOP)

### SOP-001: Verificación Diaria de Salud del Sistema
**Frecuencia**: Diaria, 9:00 AM
**Tiempo estimado**: 5 minutos

1. Ejecutar script: `/home/usuario/check-inatrace-health.sh`
2. Verificar que todos los contenedores estén "Up"
3. Verificar healthcheck retorna "UP"
4. Verificar espacio en disco > 20% libre
5. Documentar en bitácora

### SOP-002: Reinicio de Servicio Backend
**Cuándo**: Backend no responde o errores persistentes
**Tiempo estimado**: 5 minutos
**Downtime**: ~30 segundos

1. Notificar en canal de Slack/Teams
2. `cd /opt/inatrace/backend/[env]/fortaleza`
3. `docker compose restart backend`
4. Esperar 60 segundos
5. Verificar healthcheck
6. Confirmar funcionamiento en frontend
7. Documentar en bitácora

### SOP-003: Respaldo Manual de Emergencia
**Cuándo**: Antes de cambios críticos
**Tiempo estimado**: 5-10 minutos

1. `mkdir -p /opt/inatrace/backups`
2. Ejecutar comando mysqldump (ver Día 3)
3. Verificar que archivo .sql.gz se creó
4. Documentar nombre del archivo

### SOP-004: Escalamiento a Desarrollo
**Cuándo**: Problema no resuelto en 30 minutos

1. Recopilar información:
   - Logs: `docker logs inatrace-be-test-fortaleza > /tmp/logs.txt`
   - Healthcheck: `curl http://localhost:8082/actuator/health`
   - Estado contenedores: `docker ps -a`
2. Crear ticket con información
3. Notificar por email/Slack
4. Incluir pasos de reproducción

## Bitácora de Mantenimiento

| Fecha | Actividad | Responsable | Resultado | Observaciones |
|-------|-----------|-------------|-----------|---------------|
| 2025-01-15 | Reinicio backend | Juan P. | Exitoso | Backend lento, reinicio resolvió |
| 2025-01-16 | Backup manual | María G. | Exitoso | Pre-actualización MySQL |
```

### 📝 Ejercicio Final: Simulación de Incidente

**Escenario:** Es viernes a las 4:00 PM. Recibes reporte de que usuarios no pueden iniciar sesión.

**Tarea:** Resolver el incidente siguiendo los procedimientos aprendidos.

**Pasos a seguir:**

1. **Verificación inicial** (2 min)
   - Healthcheck
   - Estado contenedores
   - Logs recientes

2. **Diagnóstico** (5 min)
   - Identificar error específico en logs
   - Verificar conectividad MySQL
   - Verificar espacio en disco

3. **Acción correctiva** (5 min)
   - Aplicar solución apropiada
   - Reiniciar si es necesario
   - Verificar resolución

4. **Documentación** (3 min)
   - Registrar en bitácora
   - Notificar resolución
   - Identificar causa raíz

5. **Seguimiento** (5 min)
   - Monitorear por 15 minutos
   - Confirmar estabilidad
   - Cerrar incidente

---

## 📊 Evaluación y Certificación

### Criterios de Evaluación

El técnico debe demostrar competencia en:

1. ✅ **Operaciones Básicas (40%)**
   - Verificar estado del sistema
   - Reiniciar servicios correctamente
   - Consultar logs efectivamente

2. ✅ **Respaldos y Restauración (30%)**
   - Crear respaldos manuales
   - Verificar respaldos automáticos
   - Restaurar base de datos (en staging)

3. ✅ **Troubleshooting (20%)**
   - Diagnosticar problemas comunes
   - Aplicar soluciones documentadas
   - Escalar apropiadamente

4. ✅ **Documentación (10%)**
   - Mantener bitácora actualizada
   - Seguir procedimientos
   - Comunicar efectivamente

### Examen Práctico (2 horas)

**Parte 1: Operaciones (30 min)**
- Verificar estado del sistema
- Reiniciar backend
- Consultar logs y exportar

**Parte 2: Respaldos (30 min)**
- Crear respaldo manual
- Verificar integridad
- Simular restauración

**Parte 3: Troubleshooting (45 min)**
- Resolver 3 escenarios de problemas
- Documentar acciones
- Explicar causa raíz

**Parte 4: Documentación (15 min)**
- Completar bitácora
- Crear reporte de incidente
- Actualizar runbook

---

## 📚 Recursos Adicionales

### Documentación del Proyecto

- `README.md`: Guía de desarrollo
- `README-es.md`: Guía de inicio rápido en español
- `TROUBLESHOOTING-es.md`: Solución de problemas comunes
- `ci/README.md`: Documentación de despliegue
- `docs/MIGRACIONES_FLYWAY.md`: Gestión de migraciones

### Comandos de Referencia Rápida

```bash
# Estado del sistema
docker ps | grep inatrace
curl http://localhost:8082/actuator/health | jq

# Logs
docker logs inatrace-be-test-fortaleza --tail 100
docker logs -f inatrace-be-test-fortaleza

# Reinicio
docker compose restart backend
docker compose restart mysql

# Respaldo
docker exec inatrace-mysql-test-fortaleza sh -c \
  'mysqldump -u root -p"${MYSQL_ROOT_PASSWORD}" "${MYSQL_DATABASE}" | gzip' > backup.sql.gz

# Restauración
zcat backup.sql.gz | docker exec -i inatrace-mysql-test-fortaleza \
  mysql -u root -p"${MYSQL_ROOT_PASSWORD}" "${MYSQL_DATABASE}"

# Limpieza
docker image prune -a -f
docker system df
```

### Enlaces Útiles

- **Repositorio Backend**: https://github.com/Atijaguar-ec/backend
- **Docker Docs**: https://docs.docker.com/
- **MySQL 8.0 Docs**: https://dev.mysql.com/doc/refman/8.0/en/
- **Spring Boot Actuator**: https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html

---

## 📝 Checklist de Finalización

Al completar la capacitación, el técnico debe tener:

- [ ] Acceso SSH a servidores staging y producción
- [ ] Acceso a Jenkins
- [ ] Scripts de verificación instalados
- [ ] Runbook operativo documentado
- [ ] Procedimientos de respaldo configurados
- [ ] Contactos de escalamiento actualizados
- [ ] Bitácora de mantenimiento iniciada
- [ ] Certificado de capacitación firmado

---

## 🎓 Certificación

**Certifico que [NOMBRE DEL TÉCNICO] ha completado satisfactoriamente la capacitación de Mantenimiento y Operaciones del Backend INATrace, demostrando competencia en:**

- Verificación y monitoreo del sistema
- Reinicio y gestión de servicios
- Respaldos y restauración de base de datos
- Diagnóstico y resolución de problemas comunes
- Documentación y procedimientos operativos

**Instructor**: ___________________________  
**Fecha**: ___________________________  
**Firma**: ___________________________

---

**Versión**: 1.0  
**Fecha de creación**: Enero 2025  
**Última actualización**: Enero 2025  
**Autor**: Equipo DevOps INATrace
