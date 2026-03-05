#!/bin/bash
# Script: check-unocace-daily.sh
# Descripción: Verificación rápida diaria de salud del sistema INATrace UNOCACE
# Autor: Equipo DevOps INATrace
# Versión: 1.0

echo "===================================================="
echo "    VERIFICACIÓN DIARIA UNOCACE - $(date)"
echo "===================================================="

# 1. Verificar estado de los contenedores
echo -e "\n1. 🐳 ESTADO DE CONTENEDORES:"
docker ps --filter "name=inatrace" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

# 2. Verificar Healthcheck del Backend (Actuator)
echo -e "\n2. 🚀 HEALTHCHECK (API EXTERNA):"
HEALTH_STATUS=$(curl -s --connect-timeout 5 http://localhost:8080/actuator/health | jq -r '.status' 2>/dev/null)

if [ "$HEALTH_STATUS" == "UP" ]; then
    echo "✅ Backend: UP (Saludable)"
else
    echo "❌ Backend: $HEALTH_STATUS (REVISAR LOGS)"
fi

# 3. Verificar espacio en disco para el despliegue
echo -e "\n3. 💾 ESPACIO EN DISCO (/opt/inatrace):"
DISK_USAGE=$(df -h /opt/inatrace | tail -1 | awk '{print $5}')
DISK_FREE=$(df -h /opt/inatrace | tail -1 | awk '{print $4}')

echo "Uso: $DISK_USAGE (Libre: $DISK_FREE)"

# Alerta si el disco está muy lleno
USAGE_NUM=${DISK_USAGE%\%}
if [ "$USAGE_NUM" -gt 85 ]; then
    echo "⚠️ ALERTA: Poco espacio en disco!"
fi

# 4. Verificar último respaldo de MySQL
echo -e "\n4. 📦 ÚLTIMO RESPALDO GENERADO:"
LAST_BACKUP=$(ls -lth /opt/inatrace/backups/mysql/*.sql.gz 2>/dev/null | head -1)

if [ -n "$LAST_BACKUP" ]; then
    echo "$LAST_BACKUP"
else
    echo "❌ ERROR: No se encontraron respaldos recientes en /opt/inatrace/backups/mysql/"
fi

# 5. Conteo de errores en las últimas 24 horas
echo -e "\n5. ⚠️ ERRORES EN LOGS (ÚLTIMAS 24H):"
ERROR_COUNT=$(docker logs inatrace-be-prod-unocace --since 24h 2>&1 | grep -i "error" | wc -l)

if [ "$ERROR_COUNT" -gt 20 ]; then
    echo "⚠️ Se detectaron $ERROR_COUNT errores. Revisar con: docker logs --since 1h inatrace-be-prod-unocace"
else
    echo "✅ Errores bajo control: $ERROR_COUNT detectados."
fi

echo -e "\n===================================================="
echo "                FIN DE LA VERIFICACIÓN"
echo "===================================================="
