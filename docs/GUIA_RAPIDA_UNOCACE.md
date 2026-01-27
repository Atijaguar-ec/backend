# Guía Rápida de Operaciones - INATrace UNOCACE
## Tarjeta de Referencia para Técnico

---

## 🚨 COMANDOS DE EMERGENCIA

### Verificación Rápida
```bash
# Estado general
docker ps | grep inatrace
curl -s http://localhost:8080/actuator/health | jq
df -h /opt/inatrace

# Logs recientes
docker logs inatrace-be-prod-unocace --tail 50
```

### Reinicio Rápido (30 seg downtime)
```bash
cd /opt/inatrace/backend
docker compose restart backend
sleep 60
curl -s http://localhost:8080/actuator/health | jq
```

### Respaldo de Emergencia
```bash
docker exec inatrace-mysql-prod-unocace sh -c \
  'mysqldump -u root -p"${MYSQL_ROOT_PASSWORD}" \
  --single-transaction --quick --lock-tables=false \
  inatrace_prod_unocace | gzip -9' > \
  /opt/inatrace/backups/mysql/backup-emergency-$(date +%Y%m%d-%H%M%S).sql.gz
```

---

## 📊 VERIFICACIÓN DIARIA (9:00 AM)

```bash
#!/bin/bash
# Script: /home/usuario/check-unocace-daily.sh

echo "=== UNOCACE - $(date) ==="

echo -e "\n1. CONTENEDORES:"
docker ps --filter "name=inatrace" --format "{{.Names}}: {{.Status}}"

echo -e "\n2. HEALTHCHECK:"
curl -s http://localhost:8080/actuator/health | jq -r '.status'

echo -e "\n3. DISCO:"
df -h /opt/inatrace | tail -1

echo -e "\n4. ÚLTIMO RESPALDO:"
ls -lth /opt/inatrace/backups/mysql/*.sql.gz 2>/dev/null | head -1

echo -e "\n5. ERRORES (24h):"
docker logs inatrace-be-prod-unocace --since 24h 2>&1 | grep -i "error" | wc -l

echo -e "\n=== FIN ===\n"
```

**✅ TODO OK si:**
- Contenedores: "Up"
- Healthcheck: "UP"
- Disco: < 80%
- Respaldo: < 24h
- Errores: < 10

---

## 🔥 PROBLEMAS COMUNES

### 1. Backend No Responde

**Diagnóstico:**
```bash
curl http://localhost:8080/actuator/health
docker logs inatrace-be-prod-unocace --tail 20
```

**Solución:**
```bash
cd /opt/inatrace/backend
docker compose restart backend
sleep 60
curl -s http://localhost:8080/actuator/health | jq
```

---

### 2. Error Conexión MySQL

**Síntoma en logs:** `Communications link failure`

**Solución:**
```bash
cd /opt/inatrace/backend
docker compose restart mysql
sleep 15
docker compose restart backend
sleep 60
curl -s http://localhost:8080/actuator/health | jq
```

---

### 3. Sistema Lento

**Diagnóstico:**
```bash
docker stats inatrace-be-prod-unocace --no-stream
df -h /opt/inatrace
```

**Solución:**
```bash
# Si CPU/Mem > 90%
docker compose restart backend

# Si disco > 90%
docker image prune -a -f
```

---

### 4. Error al Subir Archivos

**Diagnóstico:**
```bash
df -h /opt/inatrace/uploads
ls -ld /opt/inatrace/uploads
```

**Solución:**
```bash
# Verificar permisos
sudo chown -R 1001:1001 /opt/inatrace/uploads
docker compose restart backend
```

---

## 💾 RESPALDOS

### Crear Respaldo Manual
```bash
docker exec inatrace-mysql-prod-unocace sh -c \
  'mysqldump -u root -p"${MYSQL_ROOT_PASSWORD}" \
  --single-transaction --quick --lock-tables=false \
  inatrace_prod_unocace | gzip -9' > \
  /opt/inatrace/backups/mysql/backup-manual-$(date +%Y%m%d-%H%M%S).sql.gz

# Verificar
ls -lh /opt/inatrace/backups/mysql/backup-manual-*.sql.gz | tail -1
```

### Ver Respaldos
```bash
# Últimos 5
ls -lth /opt/inatrace/backups/mysql/*.sql.gz | head -5

# De hoy
find /opt/inatrace/backups/mysql -name "*.sql.gz" -mtime -1 -ls
```

---

## 📋 INFORMACIÓN UNOCACE

### URLs
- **Producción**: https://inatrace.unocace.com
- **Test**: https://inatrace.test.unocace.com

### Contenedores Producción
- **Backend**: `inatrace-be-prod-unocace` (puerto 8080)
- **MySQL**: `inatrace-mysql-prod-unocace` (puerto 3306)

### Rutas Importantes
```
/opt/inatrace/backend/          # Configuración
/opt/inatrace/uploads/          # Archivos usuarios
/opt/inatrace/mysql/            # Datos MySQL
/opt/inatrace/backups/mysql/    # Respaldos
```

### Base de Datos
- **Producción**: `inatrace_prod_unocace`
- **Test**: `inatrace_test_unocace`
- **Producto**: COCOA (Cacao)

---

## ⚠️ CUÁNDO ESCALAR

### Escalar INMEDIATAMENTE:
- ❌ Producción caída > 5 minutos
- ❌ Pérdida de datos
- ❌ Error desconocido

### Escalar en horario laboral:
- ⚠️ Problema persiste después de reinicio
- ⚠️ Errores recurrentes (>3 en 24h)
- ⚠️ Necesitas modificar BD

### NO escalar:
- ✅ Problema resuelto con procedimientos
- ✅ Problema conocido documentado

---

## 📞 CONTACTOS

**Técnico UNOCACE**: [Nombre] - [Email] - [Teléfono]  
**Desarrollo**: [Contacto]  
**Emergencias**: [Teléfono guardia]

---

## 🔍 COMANDOS ÚTILES

```bash
# Ver logs en tiempo real
docker logs -f inatrace-be-prod-unocace

# Buscar errores
docker logs inatrace-be-prod-unocace --since 1h | grep -i error

# Ver recursos
docker stats inatrace-be-prod-unocace --no-stream

# Conectar a MySQL
docker exec -it inatrace-mysql-prod-unocace mysql \
  -u root -p"${MYSQL_ROOT_PASSWORD}" inatrace_prod_unocace

# Espacio en disco
df -h /opt/inatrace

# Limpiar imágenes
docker image prune -a -f
```

---

## 📝 ANTES DE ESCALAR

Recopilar información:

```bash
# Exportar logs
docker logs inatrace-be-prod-unocace > /tmp/backend-$(date +%Y%m%d).log

# Estado contenedores
docker ps -a > /tmp/containers.txt

# Healthcheck
curl -s http://localhost:8080/actuator/health > /tmp/health.json

# Espacio disco
df -h > /tmp/disk.txt
```

Enviar archivos + descripción del problema.

---

**Versión**: 1.0 UNOCACE  
**Producto**: Cacao  
**Última actualización**: Enero 2025
