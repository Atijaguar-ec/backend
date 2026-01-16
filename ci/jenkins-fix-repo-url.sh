#!/bin/bash
# ============================================================================
# Script de Corrección Repository URL - Deploy-Backend Job
# Autor: Alvaro Sanchez
# Propósito: Corregir Repository URL en config.xml de manera quirúrgica
# ============================================================================

set -e

JOB_NAME="Deploy-Backend"
JENKINS_HOME="/var/lib/jenkins"
JOB_CONFIG="${JENKINS_HOME}/jobs/${JOB_NAME}/config.xml"
BACKUP_DIR="${JENKINS_HOME}/config-backups"

EXPECTED_REPO="https://github.com/Atijaguar-ec/backend.git"
EXPECTED_CREDENTIAL="github-pat"

echo "═══════════════════════════════════════════════════════════════"
echo "  CORRECCIÓN REPOSITORY URL - JOB: ${JOB_NAME}"
echo "═══════════════════════════════════════════════════════════════"

# Verificar permisos
if [ "$(whoami)" != "root" ]; then
  echo "❌ Este script debe ejecutarse como root"
  echo "Ejecuta: sudo bash $0"
  exit 1
fi

# Verificar que el job existe
if [ ! -f "${JOB_CONFIG}" ]; then
  echo "❌ ERROR: Job '${JOB_NAME}' no encontrado en: ${JOB_CONFIG}"
  exit 1
fi

# Crear directorio de backups
mkdir -p "${BACKUP_DIR}"

# Mostrar configuración actual
echo ""
echo "📋 Configuración ACTUAL:"
echo "─────────────────────────────────────────────────────────────────"
CURRENT_URL=$(grep -A1 "<url>" "${JOB_CONFIG}" | grep -v "<url>" | sed 's/^[[:space:]]*//' | sed 's/<[^>]*>//g' | head -1)
CURRENT_CRED=$(grep "<credentialsId>" "${JOB_CONFIG}" | sed 's/.*<credentialsId>\(.*\)<\/credentialsId>.*/\1/' | head -1)
echo "Repository URL: ${CURRENT_URL}"
echo "Credential ID: ${CURRENT_CRED}"

echo ""
echo "📋 Configuración OBJETIVO:"
echo "─────────────────────────────────────────────────────────────────"
echo "Repository URL: ${EXPECTED_REPO}"
echo "Credential ID: ${EXPECTED_CREDENTIAL}"

# Confirmar cambio
echo ""
read -p "¿Aplicar corrección? (y/N): " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
  echo "❌ Cancelado por el usuario"
  exit 0
fi

# Crear backup
BACKUP_FILE="${BACKUP_DIR}/${JOB_NAME}_config_$(date +%Y%m%d_%H%M%S).xml"
echo ""
echo "📦 Creando backup en: ${BACKUP_FILE}"
cp "${JOB_CONFIG}" "${BACKUP_FILE}"
echo "✅ Backup creado"

# Detener Jenkins
echo ""
echo "🛑 Deteniendo Jenkins..."
systemctl stop jenkins
echo "✅ Jenkins detenido"

# Aplicar corrección
echo ""
echo "🔧 Corrigiendo Repository URL..."

# Estrategia: reemplazar el bloque completo de userRemoteConfigs
# Buscar el patrón y reemplazarlo de forma segura

# Crear archivo temporal
TMP_CONFIG="/tmp/jenkins_config_temp.xml"
cp "${JOB_CONFIG}" "${TMP_CONFIG}"

# Usar sed para hacer el reemplazo
# Reemplazar la URL incorrecta por la correcta
sed -i "s|<url>ghcr-credentials</url>|<url>${EXPECTED_REPO}</url>|g" "${TMP_CONFIG}"

# Si la credencial también está incorrecta, corregirla
if [ "${CURRENT_CRED}" != "${EXPECTED_CREDENTIAL}" ]; then
  sed -i "s|<credentialsId>${CURRENT_CRED}</credentialsId>|<credentialsId>${EXPECTED_CREDENTIAL}</credentialsId>|g" "${TMP_CONFIG}"
  echo "  → Repository URL corregido"
  echo "  → Credential ID corregido a: ${EXPECTED_CREDENTIAL}"
else
  echo "  → Repository URL corregido"
  echo "  → Credential ID ya era correcto: ${EXPECTED_CREDENTIAL}"
fi

# Verificar que el cambio se aplicó
if grep -q "<url>${EXPECTED_REPO}</url>" "${TMP_CONFIG}"; then
  echo "✅ Cambio validado en archivo temporal"
  
  # Copiar archivo temporal al config definitivo
  cp "${TMP_CONFIG}" "${JOB_CONFIG}"
  
  # Ajustar permisos
  chown jenkins:jenkins "${JOB_CONFIG}"
  chmod 644 "${JOB_CONFIG}"
  echo "✅ Permisos ajustados"
else
  echo "❌ ERROR: No se pudo validar el cambio"
  systemctl start jenkins
  exit 1
fi

# Limpiar archivo temporal
rm -f "${TMP_CONFIG}"

# Iniciar Jenkins
echo ""
echo "🚀 Iniciando Jenkins..."
systemctl start jenkins
echo "✅ Jenkins iniciado"

# Esperar a que Jenkins esté listo
echo ""
echo "⏳ Esperando a que Jenkins esté listo..."
sleep 10

for i in {1..30}; do
  if systemctl is-active --quiet jenkins; then
    if curl -s -o /dev/null -w "%{http_code}" http://localhost:8080 | grep -q "200\|403"; then
      echo "✅ Jenkins está listo"
      break
    fi
  fi
  echo "   Intento $i/30..."
  sleep 2
done

# Validar configuración final
echo ""
echo "═══════════════════════════════════════════════════════════════"
echo "  VALIDACIÓN FINAL"
echo "═══════════════════════════════════════════════════════════════"

FINAL_URL=$(grep -A1 "<url>" "${JOB_CONFIG}" | grep -v "<url>" | sed 's/^[[:space:]]*//' | sed 's/<[^>]*>//g' | head -1)
FINAL_CRED=$(grep "<credentialsId>" "${JOB_CONFIG}" | sed 's/.*<credentialsId>\(.*\)<\/credentialsId>.*/\1/' | head -1)

echo "Repository URL: ${FINAL_URL}"
echo "Credential ID: ${FINAL_CRED}"

if [ "${FINAL_URL}" = "${EXPECTED_REPO}" ]; then
  echo "✅ Repository URL correcto"
else
  echo "❌ Repository URL aún incorrecto"
fi

if [ "${FINAL_CRED}" = "${EXPECTED_CREDENTIAL}" ]; then
  echo "✅ Credential ID correcto"
else
  echo "⚠️  Credential ID: ${FINAL_CRED} (verifica que esta credencial exista)"
fi

echo ""
echo "═══════════════════════════════════════════════════════════════"
echo "  CORRECCIÓN COMPLETADA"
echo "═══════════════════════════════════════════════════════════════"
echo ""
echo "💾 Backup guardado en: ${BACKUP_FILE}"
echo ""
echo "📋 Próximos pasos:"
echo "  1. Verificar que la credencial '${EXPECTED_CREDENTIAL}' existe:"
echo "     Jenkins → Manage Jenkins → Credentials → System → Global"
echo ""
echo "  2. Ejecutar build de prueba:"
echo "     Deploy-Backend → Build with Parameters"
echo "     BRANCH: main"
echo "     SKIP_TESTS: true"
echo "     SKIP_DB_BACKUP: true"
echo ""
echo "  3. Verificar logs iniciales del build:"
echo "     ✅ Debe mostrar: 'Obtained ci/Jenkinsfile from git ${EXPECTED_REPO}'"
echo "     ✅ NO debe aparecer: 'NoSuchMethodError: sshagent'"
echo ""
