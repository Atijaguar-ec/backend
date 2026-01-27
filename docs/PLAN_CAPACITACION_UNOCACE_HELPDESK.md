# Plan de Capacitación: Mantenimiento INATrace Backend - UNOCACE
## Técnico Helpdesk e Infraestructura

---

## 📋 Información General

**Cliente:** UNOCACE (Unión de Organizaciones Campesinas Cacaoteras del Ecuador)  
**Producto:** Cacao (COCOA)  
**Duración:** 10 horas (2 días de 5 horas o distribución flexible)  
**Modalidad:** Práctica intensiva con ejercicios reales en ambiente UNOCACE  
**Nivel:** Técnico con conocimientos básicos de Linux y Docker

---

## 🎯 Objetivos de Aprendizaje

Al finalizar, el técnico de UNOCACE podrá:

1. ✅ Verificar estado de salud del sistema completo (frontend + backend + BD)
2. ✅ Reiniciar servicios de forma segura (frontend y backend)
3. ✅ Realizar y verificar respaldos de base de datos
4. ✅ Consultar logs para diagnóstico básico (frontend y backend)
5. ✅ Resolver problemas comunes documentados
6. ✅ Verificar conectividad entre frontend y backend
7. ✅ Escalar problemas complejos con información adecuada

---

## 🌐 Infraestructura UNOCACE

### Ambientes Desplegados

| Ambiente | URL | Frontend | Backend | MySQL | Puertos |
|----------|-----|----------|---------|-------|---------|
| **Test** | https://inatrace-test.unocace.com | inatrace-fe-test-unocace | inatrace-be-test-unocace | inatrace-mysql | FE:8081, BE:8080 |
| **Producción** | https://inatrace.unocace.com | inatrace-fe-prod-unocace | inatrace-be-prod-unocace | inatrace-mysql-prod-unocace | FE:80, BE:8080 |

### Rutas Importantes
```bash
# Backend
/opt/inatrace/backend/          # Configuración .env y docker-compose.yml
/opt/inatrace/uploads/          # Archivos subidos por usuarios
/opt/inatrace/documents/        # Documentos del sistema
/opt/inatrace/mysql/            # Datos de MySQL
/opt/inatrace/backups/mysql/    # Respaldos automáticos
/opt/inatrace/import/           # Archivos de importación

# Frontend
/opt/inatrace/frontend/test/unocace/     # Frontend Test
/opt/inatrace/frontend/prod/unocace/     # Frontend Producción
```

### Base de Datos
- **Test**: `inatrace_test_unocace`
- **Producción**: `inatrace_prod_unocace`
- **Producto**: COCOA (Cacao)

---

## 📅 Programa de Capacitación (10 horas)

### **Día 1: Fundamentos y Operaciones Básicas (5 horas)**

#### Sesión 1: Introducción y Arquitectura (1 hora)

**Conceptos clave:**
- INATrace para trazabilidad de cacao UNOCACE
- Arquitectura: Frontend (Angular) + Backend (Java/Spring Boot) + MySQL
- Despliegue con Docker y GitHub Actions

**Componentes UNOCACE:**
```
Usuario → Navegador
           ↓ HTTPS
Frontend Angular (inatrace-fe-prod-unocace:80)
    Nginx sirve archivos estáticos
           ↓ Proxy /api/*
Backend Spring Boot (inatrace-be-prod-unocace:8080)
    API REST + Actuator
           ↓ JDBC
MySQL 8.0 (inatrace-mysql-prod-unocace:3306)
    Base de datos inatrace_prod_unocace
```

**Práctica 1: Acceso y Verificación Inicial**

```bash
# Conectarse al servidor UNOCACE (proporcionado por instructor)
ssh [usuario]@[servidor-unocace]

# Ver contenedores corriendo
docker ps | grep inatrace

# Salida esperada:
# inatrace-fe-prod-unocace    Up X hours    0.0.0.0:80->80/tcp
# inatrace-be-prod-unocace    Up X hours    0.0.0.0:8080->8080/tcp
# inatrace-mysql-prod-unocace Up X hours    0.0.0.0:3306->3306/tcp

# Verificar healthcheck backend
curl -s http://localhost:8080/actuator/health | jq
# Salida esperada: {"status":"UP"}

# Verificar healthcheck frontend
curl -s http://localhost:80/health
# Salida esperada: healthy
```

---

#### Sesión 2: Verificación de Estado del Sistema (1.5 horas)

**Práctica 2: Script de Verificación Diaria**

Crear script de verificación para UNOCACE:

```bash
#!/bin/bash
# Archivo: /home/usuario/check-unocace-daily.sh

echo "=== VERIFICACIÓN DIARIA INATRACE UNOCACE - $(date) ==="

echo -e "\n1. CONTENEDORES:"
docker ps --filter "name=inatrace" --format "{{.Names}}: {{.Status}}"

echo -e "\n2. HEALTHCHECK BACKEND:"
curl -s http://localhost:8080/actuator/health | jq -r '.status' || echo "❌ ERROR"

echo -e "\n3. HEALTHCHECK FRONTEND:"
curl -s http://localhost:80/health || echo "❌ ERROR"

echo -e "\n4. ESPACIO EN DISCO:"
df -h /opt/inatrace | tail -1

echo -e "\n5. ÚLTIMO RESPALDO:"
ls -lth /opt/inatrace/backups/mysql/*.sql.gz 2>/dev/null | head -1 || echo "No hay respaldos"

echo -e "\n6. ERRORES BACKEND (últimas 24h):"
docker logs inatrace-be-prod-unocace --since 24h 2>&1 | grep -i "error" | wc -l

echo -e "\n7. ERRORES FRONTEND (últimas 24h):"
docker logs inatrace-fe-prod-unocace --since 24h 2>&1 | grep -i "error" | wc -l

echo -e "\n8. BASE DE DATOS:"
docker exec inatrace-mysql-prod-unocace mysql -u root -p"${MYSQL_ROOT_PASSWORD}" \
  -e "SELECT 'Usuarios:' as Info, COUNT(*) as Total FROM inatrace_prod_unocace.User
      UNION ALL
      SELECT 'Productores:', COUNT(*) FROM inatrace_prod_unocace.UserCustomer
      UNION ALL
      SELECT 'Órdenes:', COUNT(*) FROM inatrace_prod_unocace.StockOrder;" 2>/dev/null || echo "❌ No se pudo conectar a MySQL"

echo -e "\n=== FIN VERIFICACIÓN ===\n"
```

**Ejecutar:**
```bash
chmod +x /home/usuario/check-unocace-daily.sh
./check-unocace-daily.sh
```

**Práctica 3: Consultar Logs**

```bash
# Ver últimas 50 líneas del backend
docker logs inatrace-be-prod-unocace --tail 50

# Ver logs con timestamps
docker logs inatrace-be-prod-unocace --timestamps --tail 30

# Buscar errores
docker logs inatrace-be-prod-unocace --since 1h | grep -i "error\|exception"

# Seguir logs en tiempo real (Ctrl+C para salir)
docker logs -f inatrace-be-prod-unocace

# Exportar logs para análisis
docker logs inatrace-be-prod-unocace > /tmp/backend-logs-$(date +%Y%m%d).log
```

---

#### Sesión 3: Reinicio de Servicios (1.5 horas)

**Práctica 4: Reinicio Seguro del Backend**

```bash
# Paso 1: Ir al directorio de despliegue
cd /opt/inatrace/backend

# Paso 2: Verificar estado actual
docker compose ps

# Paso 3: Reiniciar solo el backend (30 segundos downtime)
docker compose restart backend

# Paso 4: Esperar y verificar
sleep 60
curl -s http://localhost:8080/actuator/health | jq

# Paso 5: Ver logs de inicio
docker logs inatrace-be-prod-unocace --tail 100 | grep "Started INATraceBackendApplication"
```

**Señales de inicio exitoso:**
```
Started INATraceBackendApplication in X.XXX seconds
Tomcat started on port(s): 8080 (http)
```

**Práctica 5: Reinicio Completo (Solo en Emergencia)**

⚠️ **ADVERTENCIA:** Causa 2-3 minutos de downtime. Solo usar en emergencias.

```bash
cd /opt/inatrace/backend

# Detener todos los servicios
docker compose down

# Verificar que se detuvieron
docker ps | grep inatrace

# Levantar servicios
docker compose up -d

# Monitorear inicio
docker compose logs -f backend
# Esperar mensaje "Started INATraceBackendApplication"
# Presionar Ctrl+C

# Verificar healthcheck
curl -s http://localhost:8080/actuator/health | jq
```

**Práctica 6: Manejo de Contenedor Problemático**

```bash
# Ver estado detallado del contenedor
docker inspect inatrace-be-prod-unocace | jq '.[0].State'

# Si está "unhealthy" o "restarting", ver logs
docker logs inatrace-be-prod-unocace --tail 200 | grep -i error

# Forzar recreación
cd /opt/inatrace/backend
docker compose up -d --force-recreate backend

# Verificar variables de entorno (sin mostrar contraseñas)
docker exec inatrace-be-prod-unocace env | grep INATRACE | grep -v PASSWORD
```

---

#### Sesión 4: Monitoreo de Recursos (1 hora)

**Práctica 7: Verificar Uso de Recursos**

```bash
# Ver uso de CPU y memoria en tiempo real
docker stats inatrace-be-prod-unocace inatrace-mysql-prod-unocace

# Ver uso de disco
df -h /opt/inatrace

# Ver tamaño de volúmenes
docker system df -v | grep inatrace

# Ver procesos dentro del contenedor backend
docker top inatrace-be-prod-unocace
```

**Práctica 8: Limpieza de Recursos**

```bash
# Limpiar imágenes no usadas (libera espacio)
docker image prune -a -f

# Ver espacio recuperado
docker system df

# Limpiar logs grandes de Docker (si es necesario)
# Verificar tamaño del log
docker inspect --format='{{.LogPath}}' inatrace-be-prod-unocace | xargs ls -lh

# Si es > 100MB, truncar (hacer con precaución)
# truncate -s 0 $(docker inspect --format='{{.LogPath}}' inatrace-be-prod-unocace)
```

---

#### Sesión 4.5: Operaciones del Frontend (45 minutos)

**Conceptos clave:**
- Frontend Angular servido por Nginx
- Archivos estáticos compilados en `/app` dentro del contenedor
- Nginx como proxy reverso para `/api/*` → backend
- Healthcheck endpoint `/health`

**Práctica 8.5: Verificación del Frontend**

```bash
# Ver estado del contenedor frontend
docker ps | grep inatrace-fe

# Healthcheck del frontend
curl -s http://localhost:80/health
# Salida esperada: healthy

# Verificar que el frontend está accesible
curl -I http://localhost:80/
# Salida esperada: HTTP/1.1 200 OK

# Ver logs del frontend (Nginx)
docker logs inatrace-fe-prod-unocace --tail 50

# Ver logs de acceso (últimas peticiones)
docker logs inatrace-fe-prod-unocace --tail 100 | grep "GET\|POST"

# Ver errores del frontend
docker logs inatrace-fe-prod-unocace 2>&1 | grep -i "error\|warn" | tail -20
```

**Práctica 8.6: Reinicio del Frontend**

```bash
# Ir al directorio del frontend
cd /opt/inatrace/frontend/prod/unocace

# Ver configuración actual
cat .env

# Reiniciar solo el frontend (5 segundos downtime)
docker compose restart inatrace-frontend

# Verificar que levantó correctamente
sleep 10
curl -s http://localhost:80/health

# Ver logs de inicio
docker logs inatrace-fe-prod-unocace --tail 30
```

**Práctica 8.7: Problemas Comunes del Frontend**

**Problema: Frontend muestra página en blanco**

```bash
# Diagnóstico
curl -I http://localhost:80/

# Ver logs
docker logs inatrace-fe-prod-unocace --tail 50

# Verificar archivos dentro del contenedor
docker exec inatrace-fe-prod-unocace ls -la /app/

# Solución: Reiniciar
cd /opt/inatrace/frontend/prod/unocace
docker compose restart inatrace-frontend
```

**Problema: Error "Cannot connect to backend"**

```bash
# Verificar que backend está corriendo
curl http://localhost:8080/actuator/health

# Verificar configuración de proxy en Nginx
docker exec inatrace-fe-prod-unocace cat /etc/nginx/nginx.conf | grep -A 10 "location /api"

# Ver logs de Nginx para errores de proxy
docker logs inatrace-fe-prod-unocace 2>&1 | grep "upstream\|proxy"

# Solución: Verificar que ambos contenedores están en la misma red
docker network inspect inatrace-backend-network | grep -A 5 "inatrace-fe\|inatrace-be"
```

**Práctica 8.8: Verificar Conectividad Frontend-Backend**

```bash
# Desde el contenedor frontend, hacer ping al backend
docker exec inatrace-fe-prod-unocace ping -c 3 inatrace-be-prod-unocace

# Verificar que el proxy funciona
curl -X POST http://localhost:80/api/user/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"test"}' -v

# Debe devolver JSON del backend (no HTML)
```

**Notas importantes del frontend:**
- El frontend NO tiene base de datos propia
- Los archivos estáticos están dentro del contenedor en `/app`
- Nginx escucha en puerto 80 (producción) o 8081 (test)
- Las peticiones a `/api/*` se redirigen automáticamente al backend
- El frontend se actualiza mediante GitHub Actions (despliegue automático)

---

### **Día 2: Respaldos, Troubleshooting y Procedimientos (5 horas)**

#### Sesión 5: Respaldos de Base de Datos (2 horas)

**Práctica 9: Crear Respaldo Manual**

```bash
# Crear directorio de respaldos si no existe
mkdir -p /opt/inatrace/backups/mysql

# Respaldo completo de la base de datos UNOCACE
docker exec inatrace-mysql-prod-unocace sh -c \
  'mysqldump -u root -p"${MYSQL_ROOT_PASSWORD}" \
  --single-transaction \
  --quick \
  --lock-tables=false \
  inatrace_prod_unocace | gzip -9' > \
  /opt/inatrace/backups/mysql/backup-manual-$(date +%Y%m%d-%H%M%S).sql.gz

# Verificar que se creó
ls -lh /opt/inatrace/backups/mysql/backup-manual-*.sql.gz | tail -1

# Ver tamaño
du -h /opt/inatrace/backups/mysql/backup-manual-*.sql.gz | tail -1
```

**Práctica 10: Verificar Respaldos Automáticos**

Los respaldos automáticos se crean antes de cada despliegue a producción (GitHub Actions).

```bash
# Ver últimos 5 respaldos
ls -lth /opt/inatrace/backups/mysql/*.sql.gz | head -5

# Ver respaldos de hoy
find /opt/inatrace/backups/mysql -name "*.sql.gz" -mtime -1 -ls

# Tamaño total de respaldos
du -sh /opt/inatrace/backups/mysql
```

**Práctica 11: Verificar Integridad de Respaldo**

```bash
# Descomprimir y ver primeras líneas (sin restaurar)
zcat /opt/inatrace/backups/mysql/backup-manual-*.sql.gz | head -n 50

# Verificar que contiene CREATE TABLE
zcat /opt/inatrace/backups/mysql/backup-manual-*.sql.gz | grep -c "CREATE TABLE"

# Verificar que contiene INSERT INTO
zcat /opt/inatrace/backups/mysql/backup-manual-*.sql.gz | grep -c "INSERT INTO"
```

**⚠️ IMPORTANTE: Restauración**

La restauración de base de datos en producción **SOLO** debe hacerse:
- Con autorización explícita del supervisor
- Después de crear respaldo de seguridad
- En ventana de mantenimiento programada

**Procedimiento de restauración (SOLO PRACTICAR EN TEST):**

```bash
# 1. Detener backend
cd /opt/inatrace/backend
docker compose stop backend

# 2. Crear respaldo de seguridad
docker exec inatrace-mysql sh -c \
  'mysqldump -u root -p"${MYSQL_ROOT_PASSWORD}" inatrace_test_unocace | gzip' > \
  /opt/inatrace/backups/mysql/backup-pre-restore-$(date +%Y%m%d-%H%M%S).sql.gz

# 3. Restaurar
zcat /opt/inatrace/backups/mysql/backup-YYYYMMDD-HHMMSS.sql.gz | \
  docker exec -i inatrace-mysql \
  mysql -u root -p"${MYSQL_ROOT_PASSWORD}" inatrace_test_unocace

# 4. Verificar
docker exec -it inatrace-mysql mysql \
  -u root -p"${MYSQL_ROOT_PASSWORD}" inatrace_test_unocace \
  -e "SELECT COUNT(*) FROM User; SELECT COUNT(*) FROM UserCustomer;"

# 5. Reiniciar backend
docker compose start backend
sleep 30
curl -s http://localhost:8080/actuator/health | jq
```

---

#### Sesión 6: Troubleshooting - Problemas Comunes (2 horas)

**Problema 1: Backend No Responde**

**Síntomas:**
- Frontend muestra errores
- Healthcheck falla

**Diagnóstico:**
```bash
# Ver estado del contenedor
docker ps | grep inatrace-be

# Ver healthcheck
curl http://localhost:8080/actuator/health

# Ver logs recientes
docker logs inatrace-be-prod-unocace --tail 50
```

**Solución:**
```bash
cd /opt/inatrace/backend
docker compose restart backend
sleep 60
curl -s http://localhost:8080/actuator/health | jq
```

---

**Problema 2: Error de Conexión a MySQL**

**Síntomas en logs:**
```
Communications link failure
Access denied for user
```

**Diagnóstico:**
```bash
# Verificar que MySQL esté corriendo
docker ps | grep mysql

# Verificar conectividad desde backend
docker exec inatrace-be-prod-unocace ping -c 3 inatrace-mysql-prod-unocace

# Ver logs de MySQL
docker logs inatrace-mysql-prod-unocace --tail 50
```

**Solución:**
```bash
# Reiniciar MySQL primero
cd /opt/inatrace/backend
docker compose restart mysql
sleep 15

# Luego reiniciar backend
docker compose restart backend
sleep 60
curl -s http://localhost:8080/actuator/health | jq
```

---

**Problema 3: Error de Migraciones Flyway**

**Síntomas en logs:**
```
FlywayException
Migration checksum mismatch
```

**Diagnóstico:**
```bash
# Ver error específico
docker logs inatrace-be-prod-unocace | grep "FlywayException"

# Ver estado de migraciones en BD
docker exec -it inatrace-mysql-prod-unocace mysql \
  -u root -p"${MYSQL_ROOT_PASSWORD}" inatrace_prod_unocace \
  -e "SELECT version, description, success FROM schema_version ORDER BY installed_rank DESC LIMIT 10;"
```

**Solución:**
```bash
# GitHub Actions ejecuta repair automáticamente
# Si persiste, contactar a desarrollo con logs completos

# Exportar logs
docker logs inatrace-be-prod-unocace > /tmp/flyway-error-$(date +%Y%m%d).log
```

---

**Problema 4: Sistema Lento**

**Diagnóstico:**
```bash
# Ver uso de recursos
docker stats inatrace-be-prod-unocace inatrace-mysql-prod-unocace --no-stream

# Ver espacio en disco
df -h /opt/inatrace

# Ver conexiones MySQL
docker exec inatrace-mysql-prod-unocace mysql \
  -u root -p"${MYSQL_ROOT_PASSWORD}" \
  -e "SHOW PROCESSLIST;" | wc -l
```

**Solución:**
```bash
# Si CPU/Memoria > 90%: Reiniciar
docker compose restart backend

# Si disco > 90%: Limpiar
docker image prune -a -f

# Si muchas conexiones MySQL (>100): Reiniciar MySQL
docker compose restart mysql
sleep 10
docker compose restart backend
```

---

**Problema 5: Error al Subir Archivos**

**Diagnóstico:**
```bash
# Verificar espacio
df -h /opt/inatrace/uploads

# Ver permisos
ls -ld /opt/inatrace/uploads

# Ver error en logs
docker logs inatrace-be-prod-unocace | grep -i "upload\|file" | tail -20
```

**Solución:**
```bash
# Si espacio lleno: Limpiar archivos antiguos (CON APROBACIÓN)
# Si permisos incorrectos:
sudo chown -R 1001:1001 /opt/inatrace/uploads
docker compose restart backend
```

---

**Problema 6: Frontend No Carga o Muestra Errores**

**Síntomas:**
- Página en blanco
- Error "Cannot connect to server"
- Errores 502 Bad Gateway

**Diagnóstico:**
```bash
# Verificar estado del contenedor frontend
docker ps | grep inatrace-fe

# Healthcheck frontend
curl http://localhost:80/health

# Ver logs
docker logs inatrace-fe-prod-unocace --tail 50

# Verificar backend está corriendo
curl http://localhost:8080/actuator/health
```

**Solución:**
```bash
# Si frontend está caído: Reiniciar
cd /opt/inatrace/frontend/prod/unocace
docker compose restart inatrace-frontend
sleep 10
curl http://localhost:80/health

# Si backend está caído: Reiniciar backend primero
cd /opt/inatrace/backend
docker compose restart backend
sleep 60

# Luego reiniciar frontend
cd /opt/inatrace/frontend/prod/unocace
docker compose restart inatrace-frontend
```

---

**Problema 7: Fallo en Envío de Correos (SMTP)**

**Síntomas:**
- No llegan correos de confirmación o recuperación de contraseña
- Logs con errores SMTP

**Diagnóstico:**
```bash
# Ver configuración actual en el contenedor
docker exec inatrace-be-prod-unocace env | grep SPRING_MAIL

# Probar conectividad SMTP desde el contenedor (puerto 587)
docker exec inatrace-be-prod-unocace curl -4 -v telnet://mail.unocace.com:587 --max-time 5

# Ver logs de correo
docker logs inatrace-be-prod-unocace --since 30m | grep -i "mail\|smtp" | tail -50
```

**Solución (UNOCACE / SiteGround):**
```bash
# Ajustar configuración SMTP (STARTTLS)
cd /opt/inatrace/backend
nano .env

SPRING_MAIL_PORT=587
SPRING_MAIL_SMTP_SSL_ENABLE=false
SPRING_MAIL_STARTTLS_ENABLE=true
INATRACE_MAIL_SENDING_ENABLED=true

# Re-crear contenedor para aplicar variables
docker compose down
docker compose up -d

# Verificar variables en el contenedor
docker exec inatrace-be-prod-unocace env | grep SPRING_MAIL
```

**Referencia:** `docs/TROUBLESHOOTING_CORREO_UNOCACE.md`

---

#### Sesión 7: Procedimientos y Documentación (1 hora)

**Práctica 12: Crear Bitácora de Mantenimiento**

Crear archivo: `/home/usuario/bitacora-unocace.md`

```markdown
# Bitácora de Mantenimiento INATrace UNOCACE

## [FECHA: YYYY-MM-DD]

### Verificación Diaria
- **Hora**: HH:MM
- **Contenedores**: ✅ OK / ❌ ERROR
- **Healthcheck Backend**: ✅ UP / ❌ DOWN
- **Healthcheck Frontend**: ✅ UP / ❌ DOWN
- **Espacio disco**: XX% usado
- **Último respaldo**: YYYY-MM-DD HH:MM
- **Observaciones**: [Notas]

### Incidentes
- **Ninguno** / [Descripción del incidente]

### Acciones Realizadas
- [Lista de acciones]

---
```

**Práctica 13: Procedimiento de Escalamiento**

**Cuándo escalar a desarrollo:**

✅ **Escalar INMEDIATAMENTE si:**
- Producción caída > 5 minutos
- Pérdida de datos reportada
- Error desconocido que no puedes resolver

✅ **Escalar en horario laboral si:**
- Problema persiste después de reinicio
- Errores recurrentes (>3 veces en 24h)
- Necesitas modificar base de datos

❌ **NO escalar si:**
- Problema resuelto con procedimientos estándar
- Es problema conocido con solución documentada

**Información a recopilar antes de escalar:**

```bash
# 1. Exportar logs backend
docker logs inatrace-be-prod-unocace > /tmp/backend-logs-$(date +%Y%m%d-%H%M%S).log

# 2. Exportar logs frontend
docker logs inatrace-fe-prod-unocace > /tmp/frontend-logs-$(date +%Y%m%d-%H%M%S).log

# 3. Estado del sistema
docker ps -a > /tmp/containers-status.txt
curl -s http://localhost:8080/actuator/health > /tmp/healthcheck-backend.json
curl -s http://localhost:80/health > /tmp/healthcheck-frontend.txt

# 4. Espacio en disco
df -h > /tmp/disk-usage.txt

# 4. Crear reporte
cat > /tmp/reporte-incidente-$(date +%Y%m%d).txt <<EOF
INCIDENTE INATRACE UNOCACE
Fecha/Hora: $(date)
Reportado por: [Tu nombre]

SÍNTOMAS:
[Descripción del problema]

ACCIONES REALIZADAS:
1. [Acción 1]
2. [Acción 2]

ARCHIVOS ADJUNTOS:
- backend-logs-*.log
- containers-status.txt
- healthcheck.json
- disk-usage.txt
EOF
```

---

## 📝 Evaluación Práctica (30 minutos)

### Escenario 1: Verificación Matutina
**Tarea:** Ejecutar verificación diaria y documentar resultados
- Ejecutar script de verificación
- Interpretar resultados
- Documentar en bitácora

### Escenario 2: Backend No Responde
**Tarea:** Diagnosticar y resolver
- Verificar healthcheck
- Consultar logs
- Reiniciar servicio
- Verificar resolución

### Escenario 3: Crear Respaldo
**Tarea:** Crear respaldo manual y verificar
- Ejecutar comando de respaldo
- Verificar que se creó correctamente
- Verificar integridad

---

## 📚 Comandos de Referencia Rápida UNOCACE

### Verificación
```bash
# Estado contenedores
docker ps | grep inatrace

# Healthcheck backend
curl -s http://localhost:8080/actuator/health | jq

# Healthcheck frontend
curl -s http://localhost:80/health

# Logs backend
docker logs inatrace-be-prod-unocace --tail 50

# Logs frontend
docker logs inatrace-fe-prod-unocace --tail 50

# Espacio disco
df -h /opt/inatrace
```

### Reinicio
```bash
# Solo backend (30 seg downtime)
cd /opt/inatrace/backend
docker compose restart backend

# Solo frontend (5 seg downtime)
cd /opt/inatrace/frontend/prod/unocace
docker compose restart inatrace-frontend

# Sistema completo (2 min downtime - EMERGENCIA)
cd /opt/inatrace/backend
docker compose down && docker compose up -d
cd /opt/inatrace/frontend/prod/unocace
docker compose restart inatrace-frontend
```

### Respaldos
```bash
# Crear respaldo manual
docker exec inatrace-mysql-prod-unocace sh -c \
  'mysqldump -u root -p"${MYSQL_ROOT_PASSWORD}" \
  --single-transaction --quick --lock-tables=false \
  inatrace_prod_unocace | gzip -9' > \
  /opt/inatrace/backups/mysql/backup-manual-$(date +%Y%m%d-%H%M%S).sql.gz

# Ver respaldos
ls -lth /opt/inatrace/backups/mysql/*.sql.gz | head -5
```

### Diagnóstico
```bash
# Ver errores
docker logs inatrace-be-prod-unocace --since 1h | grep -i error

# Ver recursos
docker stats inatrace-be-prod-unocace --no-stream

# Ver BD
docker exec -it inatrace-mysql-prod-unocace mysql \
  -u root -p"${MYSQL_ROOT_PASSWORD}" inatrace_prod_unocace
```

---

## 🔐 Información de Contacto UNOCACE

### URLs
- **Producción**: https://inatrace.unocace.com
- **Test**: https://inatrace.test.unocace.com
- **GitHub**: https://github.com/Atijaguar-ec/backend

### Contenedores Producción
- **Frontend**: `inatrace-fe-prod-unocace` (puerto 80)
- **Backend**: `inatrace-be-prod-unocace` (puerto 8080)
- **MySQL**: `inatrace-mysql-prod-unocace` (puerto 3306)
- **Backend Test**: `inatrace-be-test-unocace`
- **MySQL Test**: `inatrace-mysql`

### Equipo de Soporte
- **Técnico UNOCACE**: [Nombre] - [Email] - [Teléfono]
- **Desarrollo**: [Contacto desarrollo]
- **DevOps**: [Contacto DevOps]
- **Emergencias**: [Teléfono guardia]

---

## ✅ Checklist de Finalización

Al completar la capacitación, el técnico debe:

- [ ] Tener acceso SSH al servidor UNOCACE
- [ ] Script de verificación diaria instalado y probado
- [ ] Haber ejecutado al menos 1 reinicio de backend
- [ ] Haber ejecutado al menos 1 reinicio de frontend
- [ ] Haber verificado conectividad frontend-backend
- [ ] Haber creado al menos 1 respaldo manual
- [ ] Conocer procedimiento de escalamiento
- [ ] Tener contactos de soporte actualizados
- [ ] Bitácora de mantenimiento iniciada

---

## 🎓 Certificación

**Certifico que [NOMBRE DEL TÉCNICO] ha completado satisfactoriamente la capacitación de Mantenimiento del Backend INATrace para UNOCACE, demostrando competencia en:**

- Verificación y monitoreo del sistema
- Reinicio de servicios
- Creación y verificación de respaldos
- Diagnóstico de problemas comunes
- Procedimientos de escalamiento

**Instructor**: ___________________________  
**Fecha**: ___________________________  
**Firma**: ___________________________

---

**Versión**: 1.0 UNOCACE  
**Fecha**: Enero 2025  
**Producto**: Cacao (COCOA)  
**Autor**: Equipo DevOps INATrace
