# Guía Rápida de Operaciones - INATrace Backend
## Para Técnico Helpdesk e Infraestructura

---

## 🚨 Comandos de Emergencia (Copiar y Pegar)

### Verificación Rápida del Sistema
```bash
# Estado de contenedores
docker ps | grep inatrace

# Healthcheck backend
curl -s http://localhost:8082/actuator/health | jq

# Logs recientes (últimos 50 líneas)
docker logs inatrace-be-test-fortaleza --tail 50

# Espacio en disco
df -h /opt/inatrace
```

### Reinicio de Emergencia
```bash
# Solo backend (30 segundos downtime)
cd /opt/inatrace/backend/test/fortaleza
docker compose restart backend

# Sistema completo (2 minutos downtime) - SOLO EN EMERGENCIA
cd /opt/inatrace/backend/test/fortaleza
docker compose down && docker compose up -d
```

### Respaldo de Emergencia
```bash
# Crear respaldo inmediato
docker exec inatrace-mysql-test-fortaleza sh -c \
  'mysqldump -u root -p"${MYSQL_ROOT_PASSWORD}" \
  --single-transaction --quick --lock-tables=false \
  "${MYSQL_DATABASE}" | gzip -9' > \
  /opt/inatrace/backups/backup-emergency-$(date +%Y%m%d-%H%M%S).sql.gz

# Verificar que se creó
ls -lh /opt/inatrace/backups/backup-emergency-*.sql.gz | tail -1
```

---

## 📊 Checklist Diario (9:00 AM)

```bash
#!/bin/bash
# Copiar este script a: /home/usuario/check-daily.sh

echo "=== VERIFICACIÓN DIARIA INATRACE - $(date) ==="

echo -e "\n1. CONTENEDORES:"
docker ps --filter "name=inatrace" --format "{{.Names}}: {{.Status}}" || echo "❌ ERROR"

echo -e "\n2. HEALTHCHECK:"
curl -s http://localhost:8082/actuator/health | jq -r '.status' || echo "❌ ERROR"

echo -e "\n3. ESPACIO EN DISCO:"
df -h /opt/inatrace | tail -1 | awk '{print $5 " usado de " $2}'

echo -e "\n4. ÚLTIMO RESPALDO:"
ls -lth /opt/inatrace/backups/*.sql.gz | head -1 | awk '{print $9 " - " $6 " " $7 " " $8}'

echo -e "\n5. ERRORES RECIENTES (últimas 24h):"
docker logs inatrace-be-test-fortaleza --since 24h 2>&1 | grep -i "error" | wc -l | awk '{print $1 " errores encontrados"}'

echo -e "\n=== FIN VERIFICACIÓN ===\n"
```

**Ejecutar:**
```bash
chmod +x /home/usuario/check-daily.sh
./check-daily.sh
```

**✅ TODO OK si:**
- Contenedores: "Up" 
- Healthcheck: "UP"
- Disco: < 80% usado
- Respaldo: < 24 horas
- Errores: < 10

---

## 🔥 Problemas Frecuentes y Soluciones Rápidas

### Problema 1: "No puedo acceder al sistema"

**Diagnóstico rápido:**
```bash
# ¿Backend corriendo?
docker ps | grep inatrace-be

# ¿Backend healthy?
curl http://localhost:8082/actuator/health

# ¿Errores en logs?
docker logs inatrace-be-test-fortaleza --tail 20
```

**Solución:**
```bash
# Reiniciar backend
cd /opt/inatrace/backend/test/fortaleza
docker compose restart backend

# Esperar 60 segundos
sleep 60

# Verificar
curl http://localhost:8082/actuator/health | jq
```

---

### Problema 2: "Error al iniciar sesión"

**Diagnóstico:**
```bash
# Ver errores de autenticación
docker logs inatrace-be-test-fortaleza | grep -i "auth\|login" | tail -20
```

**Verificar usuario en BD:**
```bash
docker exec -it inatrace-mysql-test-fortaleza mysql \
  -u root -p"${MYSQL_ROOT_PASSWORD}" "${MYSQL_DATABASE}" \
  -e "SELECT id, email, status, role FROM User WHERE email='usuario@ejemplo.com';"
```

**Solución común:**
- Si usuario no existe → Crear usuario (escalar a desarrollo)
- Si status = 'DISABLED' → Activar usuario (escalar a desarrollo)
- Si error persiste → Reiniciar backend

---

### Problema 3: "Sistema muy lento"

**Diagnóstico:**
```bash
# Ver uso de recursos
docker stats inatrace-be-test-fortaleza inatrace-mysql-test-fortaleza --no-stream

# Ver espacio en disco
df -h /opt/inatrace

# Ver conexiones MySQL
docker exec inatrace-mysql-test-fortaleza mysql \
  -u root -p"${MYSQL_ROOT_PASSWORD}" \
  -e "SHOW PROCESSLIST;" | wc -l
```

**Solución:**
```bash
# Si CPU/Memoria > 90%: Reiniciar
docker compose restart backend

# Si disco > 90%: Limpiar
docker image prune -a -f
docker system prune -f

# Si muchas conexiones MySQL (>100): Reiniciar MySQL
docker compose restart mysql
sleep 10
docker compose restart backend
```

---

### Problema 4: "Error al subir archivos"

**Diagnóstico:**
```bash
# Verificar espacio en volumen de uploads
df -h /opt/inatrace/uploads

# Ver permisos
ls -ld /opt/inatrace/uploads

# Ver error específico en logs
docker logs inatrace-be-test-fortaleza | grep -i "upload\|file" | tail -20
```

**Solución:**
```bash
# Si espacio lleno: Limpiar archivos antiguos (con aprobación)
# Si permisos incorrectos:
sudo chown -R 1001:1001 /opt/inatrace/uploads
docker compose restart backend
```

---

### Problema 5: "Backend no inicia después de despliegue"

**Diagnóstico:**
```bash
# Ver logs completos
docker logs inatrace-be-test-fortaleza --tail 200

# Buscar error específico
docker logs inatrace-be-test-fortaleza 2>&1 | grep -i "error\|exception\|failed"
```

**Errores comunes:**

**A) Error de migración Flyway:**
```bash
# Síntoma en logs: "FlywayException" o "Migration failed"

# Solución: Activar repair automático
cd /opt/inatrace/backend/test/fortaleza
echo "INATRACE_FLYWAY_REPAIR_ON_STARTUP=true" >> .env
docker compose restart backend
```

**B) Error de conexión MySQL:**
```bash
# Síntoma en logs: "Communications link failure"

# Solución: Reiniciar MySQL primero
docker compose restart mysql
sleep 15
docker compose restart backend
```

**C) Puerto en uso:**
```bash
# Síntoma en logs: "Port 8080 already in use"

# Solución: Eliminar contenedor duplicado
docker ps -a | grep 8080
docker rm -f [CONTAINER_ID_CONFLICTIVO]
docker compose up -d backend
```

---

## 💾 Respaldos

### Respaldo Manual Rápido
```bash
# Crear respaldo ahora
docker exec inatrace-mysql-test-fortaleza sh -c \
  'mysqldump -u root -p"${MYSQL_ROOT_PASSWORD}" \
  --single-transaction --quick --lock-tables=false \
  "${MYSQL_DATABASE}" | gzip -9' > \
  /opt/inatrace/backups/backup-manual-$(date +%Y%m%d-%H%M%S).sql.gz

# Verificar
ls -lh /opt/inatrace/backups/backup-manual-*.sql.gz | tail -1
```

### Verificar Respaldos Automáticos
```bash
# Ver últimos 5 respaldos
ls -lth /opt/inatrace/backups/*.sql.gz | head -5

# Ver respaldos de hoy
find /opt/inatrace/backups -name "*.sql.gz" -mtime -1 -ls

# Tamaño total de respaldos
du -sh /opt/inatrace/backups
```

### Restaurar Respaldo (SOLO EN STAGING)
```bash
# ⚠️ NUNCA EN PRODUCCIÓN SIN AUTORIZACIÓN

# 1. Detener backend
cd /opt/inatrace/backend/test/fortaleza
docker compose stop backend

# 2. Backup de seguridad
docker exec inatrace-mysql-test-fortaleza sh -c \
  'mysqldump -u root -p"${MYSQL_ROOT_PASSWORD}" "${MYSQL_DATABASE}" | gzip' > \
  /opt/inatrace/backups/backup-pre-restore-$(date +%Y%m%d-%H%M%S).sql.gz

# 3. Restaurar
zcat /opt/inatrace/backups/backup-YYYYMMDD-HHMMSS.sql.gz | \
  docker exec -i inatrace-mysql-test-fortaleza \
  mysql -u root -p"${MYSQL_ROOT_PASSWORD}" "${MYSQL_DATABASE}"

# 4. Reiniciar backend
docker compose start backend

# 5. Verificar
sleep 30
curl http://localhost:8082/actuator/health | jq
```

---

## 📋 Información de Contacto

### Servidores

| Entorno | Servidor | Usuario | Puerto Backend | URL |
|---------|----------|---------|----------------|-----|
| **Staging** | servidor-staging | usuario | 8082 | https://testinatrace.espam.edu.ec |
| **Producción** | 10.10.102.26 | administrador | 8082 | https://inatrace.espam.edu.ec |

### Contenedores

| Entorno | Backend | MySQL | Red |
|---------|---------|-------|-----|
| **Staging** | inatrace-be-test-fortaleza | inatrace-mysql-test-fortaleza | inatrace-backend-network |
| **Producción** | inatrace-be-prod-fortaleza | inatrace-mysql-prod-fortaleza | inatrace-backend-network |

### Rutas Importantes

```bash
# Staging
/opt/inatrace/backend/test/fortaleza/     # Despliegue
/opt/inatrace/uploads/                    # Archivos subidos
/opt/inatrace/mysql/                      # Datos MySQL
/opt/inatrace/backups/                    # Respaldos

# Producción (misma estructura)
/opt/inatrace/backend/prod/fortaleza/
```

### Equipo de Soporte

- **Helpdesk**: [Tu nombre] - [teléfono] - [email]
- **Desarrollo Backend**: [Nombre dev] - [email]
- **DevOps**: [Nombre devops] - [email]
- **Emergencias**: [Teléfono guardia]

---

## 🔍 Comandos Útiles de Diagnóstico

### Docker
```bash
# Ver todos los contenedores
docker ps -a

# Ver logs en tiempo real
docker logs -f inatrace-be-test-fortaleza

# Ver últimas 100 líneas con timestamps
docker logs --timestamps --tail 100 inatrace-be-test-fortaleza

# Buscar errores en logs
docker logs inatrace-be-test-fortaleza 2>&1 | grep -i "error\|exception"

# Ver uso de recursos
docker stats --no-stream

# Inspeccionar contenedor
docker inspect inatrace-be-test-fortaleza | jq

# Ejecutar comando dentro del contenedor
docker exec -it inatrace-be-test-fortaleza bash

# Ver variables de entorno
docker exec inatrace-be-test-fortaleza env | sort
```

### MySQL
```bash
# Conectarse a MySQL
docker exec -it inatrace-mysql-test-fortaleza mysql \
  -u root -p"${MYSQL_ROOT_PASSWORD}" "${MYSQL_DATABASE}"

# Consultas útiles (dentro de MySQL):
SHOW DATABASES;
USE inatrace_test_fortaleza;
SHOW TABLES;
SELECT COUNT(*) FROM User;
SELECT COUNT(*) FROM Company;
SHOW PROCESSLIST;
SELECT version, description, success FROM schema_version ORDER BY installed_rank DESC LIMIT 5;
EXIT;
```

### Sistema
```bash
# Espacio en disco
df -h

# Uso de memoria
free -h

# Procesos que más consumen
top -b -n 1 | head -20

# Conexiones de red
netstat -tuln | grep -E "8080|8082|3306"

# Ver puertos en uso
lsof -i :8082
lsof -i :3306
```

---

## 📝 Plantilla de Reporte de Incidente

```markdown
# Incidente INATrace - [FECHA]

## Información Básica
- **Fecha/Hora**: YYYY-MM-DD HH:MM
- **Entorno**: Staging / Producción
- **Reportado por**: [Nombre/Email]
- **Severidad**: Crítica / Alta / Media / Baja

## Descripción del Problema
[Descripción detallada del problema reportado]

## Síntomas Observados
- [ ] Backend no responde
- [ ] Errores en frontend
- [ ] Sistema lento
- [ ] Error al iniciar sesión
- [ ] Otro: [especificar]

## Diagnóstico Realizado
```bash
# Comandos ejecutados y resultados
docker ps | grep inatrace
# [pegar salida]

curl http://localhost:8082/actuator/health
# [pegar salida]

docker logs inatrace-be-test-fortaleza --tail 50
# [pegar errores relevantes]
```

## Acciones Tomadas
1. [Acción 1]
2. [Acción 2]
3. [Acción 3]

## Resultado
- [ ] Problema resuelto
- [ ] Problema parcialmente resuelto
- [ ] Escalado a: [equipo/persona]

## Causa Raíz Identificada
[Si se identificó]

## Prevención Futura
[Recomendaciones para evitar recurrencia]

## Tiempo de Resolución
- **Inicio**: HH:MM
- **Fin**: HH:MM
- **Total**: XX minutos

## Archivos Adjuntos
- Logs: /tmp/logs-incidente-YYYYMMDD.txt
- Screenshots: [si aplica]
```

---

## ⚠️ Cuándo Escalar

### Escalar INMEDIATAMENTE si:
- ❌ Producción completamente caída (>5 minutos)
- ❌ Pérdida de datos reportada
- ❌ Brecha de seguridad sospechada
- ❌ Error desconocido que no puedes resolver

### Escalar en HORARIO LABORAL si:
- ⚠️ Problema persiste después de reinicio
- ⚠️ Necesitas modificar base de datos
- ⚠️ Necesitas cambiar configuración de aplicación
- ⚠️ Errores recurrentes (>3 veces en 24h)

### NO escalar si:
- ✅ Problema resuelto con procedimientos estándar
- ✅ Es un problema conocido con solución documentada
- ✅ Afecta solo a ambiente de desarrollo

---

## 📚 Documentación Relacionada

- **Plan de Capacitación Completo**: `docs/PLAN_CAPACITACION_HELPDESK_INFRAESTRUCTURA.md`
- **Troubleshooting Detallado**: `TROUBLESHOOTING-es.md`
- **Guía de Despliegue**: `ci/README.md`
- **Migraciones Flyway**: `docs/MIGRACIONES_FLYWAY.md`
- **README en Español**: `README-es.md`

---

## 🎯 Tips y Mejores Prácticas

### ✅ Hacer SIEMPRE:
- Documentar todas las acciones en bitácora
- Crear respaldo antes de cambios importantes
- Verificar healthcheck después de reiniciar
- Revisar logs antes y después de acciones
- Notificar al equipo sobre cambios en producción

### ❌ NO hacer NUNCA:
- Modificar base de datos en producción sin respaldo
- Reiniciar producción sin notificar
- Eliminar respaldos sin verificar retención
- Compartir contraseñas en texto plano
- Ejecutar comandos sin entender qué hacen

### 💡 Consejos:
- Usa `tmux` o `screen` para sesiones SSH persistentes
- Crea aliases para comandos frecuentes
- Mantén un log personal de incidentes resueltos
- Practica en staging antes de tocar producción
- Pregunta si no estás seguro

---

## 🔐 Seguridad

### Contraseñas y Credenciales
- **NUNCA** compartir contraseñas por email/chat
- **NUNCA** commitear contraseñas a Git
- **SIEMPRE** usar variables de entorno
- **SIEMPRE** rotar contraseñas periódicamente

### Acceso SSH
```bash
# Usar llaves SSH en lugar de contraseñas
ssh-keygen -t ed25519 -C "tu_email@ejemplo.com"

# Copiar llave pública al servidor
ssh-copy-id usuario@servidor

# Verificar que funciona
ssh usuario@servidor
```

### Archivos Sensibles
```bash
# Verificar permisos de .env
ls -l /opt/inatrace/backend/test/fortaleza/.env
# Debe ser: -rw------- (600)

# Corregir si es necesario
chmod 600 /opt/inatrace/backend/test/fortaleza/.env
```

---

**Versión**: 1.0  
**Última actualización**: Enero 2025  
**Mantenido por**: Equipo DevOps INATrace

---

## 📞 Números de Emergencia

```
┌─────────────────────────────────────────┐
│   EMERGENCIA PRODUCCIÓN CAÍDA           │
│                                         │
│   1. Llamar: [TELÉFONO GUARDIA]       │
│   2. Email: emergencias@ejemplo.com    │
│   3. Slack: #inatrace-emergencias      │
│                                         │
│   Mientras esperas respuesta:          │
│   - Ejecutar check-daily.sh            │
│   - Exportar logs                      │
│   - Documentar síntomas                │
└─────────────────────────────────────────┘
```
