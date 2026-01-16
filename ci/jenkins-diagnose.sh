#!/bin/bash
# ============================================================================
# Script de Diagnóstico Jenkins - Deploy-Backend Job
# Autor: Alvaro Sanchez
# Propósito: Diagnosticar por qué Jenkins no usa ci/Jenkinsfile del repo
# ============================================================================

set -e

JOB_NAME="Deploy-Backend"
JENKINS_HOME="/var/lib/jenkins"
JOB_CONFIG="${JENKINS_HOME}/jobs/${JOB_NAME}/config.xml"

echo "═══════════════════════════════════════════════════════════════"
echo "  DIAGNÓSTICO JENKINS - JOB: ${JOB_NAME}"
echo "═══════════════════════════════════════════════════════════════"

# ============================================================================
# 1. Verificar que el job existe
# ============================================================================
echo ""
echo "1️⃣  Verificando existencia del job..."
if [ ! -f "${JOB_CONFIG}" ]; then
  echo "❌ ERROR: Job '${JOB_NAME}' no encontrado en: ${JOB_CONFIG}"
  echo ""
  echo "Jobs disponibles:"
  ls -1 "${JENKINS_HOME}/jobs/" 2>/dev/null || echo "No se puede acceder a ${JENKINS_HOME}/jobs/"
  exit 1
fi
echo "✅ Job encontrado: ${JOB_CONFIG}"

# ============================================================================
# 2. Extraer tipo de definición del pipeline
# ============================================================================
echo ""
echo "2️⃣  Analizando tipo de pipeline..."

if grep -q "<definition class=\"org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition\"" "${JOB_CONFIG}"; then
  echo "✅ Configuración CORRECTA: Pipeline script from SCM"
  PIPELINE_TYPE="SCM"
elif grep -q "<definition class=\"org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition\"" "${JOB_CONFIG}"; then
  echo "❌ Configuración INCORRECTA: Pipeline script (hardcoded)"
  PIPELINE_TYPE="INLINE"
else
  echo "⚠️  Tipo de pipeline desconocido"
  PIPELINE_TYPE="UNKNOWN"
fi

# ============================================================================
# 3. Mostrar detalles según tipo
# ============================================================================
echo ""
echo "3️⃣  Detalles de configuración:"
echo "─────────────────────────────────────────────────────────────────"

if [ "$PIPELINE_TYPE" = "SCM" ]; then
  # Extraer URL del repositorio
  REPO_URL=$(grep -A1 "<url>" "${JOB_CONFIG}" | grep -v "<url>" | sed 's/^[[:space:]]*//' | sed 's/<[^>]*>//g')
  echo "Repository URL: ${REPO_URL}"
  
  # Extraer credencial
  CREDENTIAL=$(grep "<credentialsId>" "${JOB_CONFIG}" | sed 's/.*<credentialsId>\(.*\)<\/credentialsId>.*/\1/')
  echo "Credential ID: ${CREDENTIAL}"
  
  # Extraer branch
  BRANCH=$(grep "<name>" "${JOB_CONFIG}" | grep -v "credentialsId" | sed 's/.*<name>\(.*\)<\/name>.*/\1/' | head -1)
  echo "Branch Specifier: ${BRANCH}"
  
  # Extraer script path
  SCRIPT_PATH=$(grep "<scriptPath>" "${JOB_CONFIG}" | sed 's/.*<scriptPath>\(.*\)<\/scriptPath>.*/\1/')
  echo "Script Path: ${SCRIPT_PATH}"
  
  # Validaciones
  echo ""
  echo "📊 Validaciones:"
  if [ "${REPO_URL}" = "https://github.com/Atijaguar-ec/backend.git" ]; then
    echo "✅ Repository URL correcto"
  else
    echo "❌ Repository URL incorrecto (esperado: https://github.com/Atijaguar-ec/backend.git)"
  fi
  
  if [ "${SCRIPT_PATH}" = "ci/Jenkinsfile" ]; then
    echo "✅ Script Path correcto"
  else
    echo "❌ Script Path incorrecto (esperado: ci/Jenkinsfile, actual: ${SCRIPT_PATH})"
  fi
  
  if [ -n "${CREDENTIAL}" ]; then
    echo "✅ Credencial configurada: ${CREDENTIAL}"
  else
    echo "⚠️  Sin credencial configurada (puede causar problemas en repos privados)"
  fi

elif [ "$PIPELINE_TYPE" = "INLINE" ]; then
  echo "❌ PROBLEMA DETECTADO: Pipeline hardcoded en el job"
  echo ""
  echo "El job tiene el script Groovy embebido directamente."
  echo "Por eso NO lee ci/Jenkinsfile del repositorio."
  echo ""
  echo "Primeras líneas del script embebido:"
  echo "─────────────────────────────────────────────────────────────────"
  sed -n '/<script>/,/<\/script>/p' "${JOB_CONFIG}" | head -20
  echo "─────────────────────────────────────────────────────────────────"
fi

# ============================================================================
# 4. Verificar si hay sshagent en la configuración
# ============================================================================
echo ""
echo "4️⃣  Buscando 'sshagent' en configuración del job..."

if grep -q "sshagent" "${JOB_CONFIG}"; then
  echo "❌ ENCONTRADO: 'sshagent' está presente en config.xml"
  echo ""
  echo "Líneas que contienen 'sshagent':"
  grep -n "sshagent" "${JOB_CONFIG}"
else
  echo "✅ 'sshagent' NO encontrado en config.xml (correcto)"
fi

# ============================================================================
# 5. Verificar Jenkinsfile en el repositorio
# ============================================================================
echo ""
echo "5️⃣  Verificando ci/Jenkinsfile en repositorio..."

REPO_DIR="/var/lib/jenkins/workspace/${JOB_NAME}"
JENKINSFILE="${REPO_DIR}/ci/Jenkinsfile"

if [ -f "${JENKINSFILE}" ]; then
  echo "✅ Jenkinsfile encontrado en workspace"
  
  if grep -q "sshagent" "${JENKINSFILE}"; then
    echo "❌ 'sshagent' encontrado en ci/Jenkinsfile del workspace"
  else
    echo "✅ 'sshagent' NO está en ci/Jenkinsfile (correcto)"
  fi
  
  if grep -q "withCredentials.*sshUserPrivateKey" "${JENKINSFILE}"; then
    echo "✅ 'withCredentials' con sshUserPrivateKey encontrado (correcto)"
  else
    echo "❌ 'withCredentials' con sshUserPrivateKey NO encontrado"
  fi
else
  echo "⚠️  Jenkinsfile no encontrado en workspace (puede no haberse ejecutado checkout)"
fi

# ============================================================================
# 6. Recomendaciones
# ============================================================================
echo ""
echo "═══════════════════════════════════════════════════════════════"
echo "  RECOMENDACIONES"
echo "═══════════════════════════════════════════════════════════════"

if [ "$PIPELINE_TYPE" = "INLINE" ]; then
  echo ""
  echo "❌ ACCIÓN REQUERIDA: Cambiar configuración del job"
  echo ""
  echo "Opciones:"
  echo ""
  echo "A) Reconfigurar via UI (Recomendado):"
  echo "   1. Jenkins → ${JOB_NAME} → Configure"
  echo "   2. Pipeline → Definition: Cambiar a 'Pipeline script from SCM'"
  echo "   3. SCM: Git"
  echo "   4. Repository URL: https://github.com/Atijaguar-ec/backend.git"
  echo "   5. Credentials: github-pat"
  echo "   6. Branch Specifier: */\${BRANCH}"
  echo "   7. Script Path: ci/Jenkinsfile"
  echo "   8. Save"
  echo ""
  echo "B) Modificar config.xml directamente:"
  echo "   sudo systemctl stop jenkins"
  echo "   sudo cp ${JOB_CONFIG} ${JOB_CONFIG}.backup"
  echo "   # Editar ${JOB_CONFIG}"
  echo "   # Cambiar <definition class> a CpsScmFlowDefinition"
  echo "   sudo systemctl start jenkins"
  echo ""
elif [ "$PIPELINE_TYPE" = "SCM" ]; then
  echo ""
  if [ "${SCRIPT_PATH}" != "ci/Jenkinsfile" ]; then
    echo "⚠️  Script Path incorrecto"
    echo "   Cambiar a: ci/Jenkinsfile"
  elif [ "${REPO_URL}" != "https://github.com/Atijaguar-ec/backend.git" ]; then
    echo "⚠️  Repository URL incorrecto"
    echo "   Cambiar a: https://github.com/Atijaguar-ec/backend.git"
  else
    echo "✅ Configuración parece correcta"
    echo ""
    echo "Posibles causas del problema:"
    echo "1. Jenkins tiene cache del pipeline anterior"
    echo "   Solución: Reiniciar Jenkins"
    echo ""
    echo "2. El workspace tiene código antiguo"
    echo "   Solución: Limpiar workspace y ejecutar nuevo build"
    echo ""
    echo "3. Plugin SSH Agent no instalado"
    echo "   Solución: No usar sshagent (ya corregido con withCredentials)"
  fi
fi

echo ""
echo "═══════════════════════════════════════════════════════════════"
echo "  FIN DEL DIAGNÓSTICO"
echo "═══════════════════════════════════════════════════════════════"
