#!/bin/bash
# ============================================================================
# Script de Corrección Automática Jenkins - Deploy-Backend Job
# Autor: Alvaro Sanchez
# Propósito: Corregir configuración del job para usar ci/Jenkinsfile desde SCM
# ============================================================================

set -e

JOB_NAME="Deploy-Backend"
JENKINS_HOME="/var/lib/jenkins"
JOB_CONFIG="${JENKINS_HOME}/jobs/${JOB_NAME}/config.xml"
BACKUP_DIR="${JENKINS_HOME}/config-backups"

echo "═══════════════════════════════════════════════════════════════"
echo "  CORRECCIÓN AUTOMÁTICA JENKINS - JOB: ${JOB_NAME}"
echo "═══════════════════════════════════════════════════════════════"

# Verificar permisos
if [ "$(whoami)" != "root" ]; then
  echo "❌ Este script debe ejecutarse como root"
  echo "Ejecuta: sudo $0"
  exit 1
fi

# Verificar que el job existe
if [ ! -f "${JOB_CONFIG}" ]; then
  echo "❌ ERROR: Job '${JOB_NAME}' no encontrado en: ${JOB_CONFIG}"
  exit 1
fi

# Crear directorio de backups
mkdir -p "${BACKUP_DIR}"

# Crear backup
BACKUP_FILE="${BACKUP_DIR}/${JOB_NAME}_config_$(date +%Y%m%d_%H%M%S).xml"
echo "📦 Creando backup en: ${BACKUP_FILE}"
cp "${JOB_CONFIG}" "${BACKUP_FILE}"
echo "✅ Backup creado"

# Detener Jenkins
echo ""
echo "🛑 Deteniendo Jenkins..."
systemctl stop jenkins
echo "✅ Jenkins detenido"

# Crear nuevo config.xml
echo ""
echo "🔧 Generando nueva configuración..."

cat > "${JOB_CONFIG}" << 'EOF_CONFIG'
<?xml version='1.1' encoding='UTF-8'?>
<flow-definition plugin="workflow-job@1400.v7fd111b_ec82f">
  <actions/>
  <description>Pipeline de despliegue para backend Fortaleza del Valle (staging y producción)</description>
  <keepDependencies>false</keepDependencies>
  <properties>
    <hudson.model.ParametersDefinitionProperty>
      <parameterDefinitions>
        <hudson.model.ChoiceParameterDefinition>
          <name>BRANCH</name>
          <description>Rama a desplegar (staging→test, main→prod)</description>
          <choices class="java.util.Arrays$ArrayList">
            <a class="string-array">
              <string>staging</string>
              <string>main</string>
            </a>
          </choices>
        </hudson.model.ChoiceParameterDefinition>
        <hudson.model.BooleanParameterDefinition>
          <name>SKIP_TESTS</name>
          <description>⚠️ Omitir pruebas (no recomendado para producción)</description>
          <defaultValue>false</defaultValue>
        </hudson.model.BooleanParameterDefinition>
        <hudson.model.BooleanParameterDefinition>
          <name>SKIP_DB_BACKUP</name>
          <description>⚠️ Omitir backup de BD en producción (no recomendado para producción)</description>
          <defaultValue>false</defaultValue>
        </hudson.model.BooleanParameterDefinition>
        <hudson.model.StringParameterDefinition>
          <name>STAGING_HEALTH_URL</name>
          <description>URL de healthcheck del backend en staging</description>
          <defaultValue>https://testinatrace.espam.edu.ec/api/actuator/health</defaultValue>
          <trim>false</trim>
        </hudson.model.StringParameterDefinition>
        <hudson.model.StringParameterDefinition>
          <name>PROD_HOST_PRIMARY</name>
          <description>IP del servidor de producción ESPAM/CEDIA</description>
          <defaultValue>190.15.143.192</defaultValue>
          <trim>false</trim>
        </hudson.model.StringParameterDefinition>
        <hudson.model.StringParameterDefinition>
          <name>PROD_USER_PRIMARY</name>
          <description>Usuario SSH para el servidor de producción (usa credencial usuario-prod-ssh)</description>
          <defaultValue>administrador</defaultValue>
          <trim>false</trim>
        </hudson.model.StringParameterDefinition>
        <hudson.model.StringParameterDefinition>
          <name>PROD_TARGET_PRIMARY</name>
          <description>Directorio remoto donde se desplegará la app en el servidor principal</description>
          <defaultValue>/opt/inatrace/backend/prod/fortaleza</defaultValue>
          <trim>false</trim>
        </hudson.model.StringParameterDefinition>
        <hudson.model.StringParameterDefinition>
          <name>PROD_HEALTH_PORT_PRIMARY</name>
          <description>Puerto local donde expone healthcheck el backend en el servidor principal</description>
          <defaultValue>8082</defaultValue>
          <trim>false</trim>
        </hudson.model.StringParameterDefinition>
        <hudson.model.StringParameterDefinition>
          <name>PROD_HEALTH_URL_PRIMARY</name>
          <description>URL externa para validar healthcheck del servidor principal</description>
          <defaultValue>http://localhost:8082/actuator/health</defaultValue>
          <trim>false</trim>
        </hudson.model.StringParameterDefinition>
        <hudson.model.StringParameterDefinition>
          <name>PROD_DB_CONTAINER_NAME</name>
          <description>Nombre del contenedor MySQL en producción para backups</description>
          <defaultValue>inatrace-mysql-prod-fortaleza</defaultValue>
          <trim>false</trim>
        </hudson.model.StringParameterDefinition>
      </parameterDefinitions>
    </hudson.model.ParametersDefinitionProperty>
    <org.jenkinsci.plugins.workflow.job.properties.DisableConcurrentBuildsJobProperty>
      <abortPrevious>false</abortPrevious>
    </org.jenkinsci.plugins.workflow.job.properties.DisableConcurrentBuildsJobProperty>
  </properties>
  <definition class="org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition" plugin="workflow-cps@3837.v305192405b_c0">
    <scm class="hudson.plugins.git.GitSCM" plugin="git@5.2.2">
      <configVersion>2</configVersion>
      <userRemoteConfigs>
        <hudson.plugins.git.UserRemoteConfig>
          <url>https://github.com/Atijaguar-ec/backend.git</url>
          <credentialsId>github-pat</credentialsId>
        </hudson.plugins.git.UserRemoteConfig>
      </userRemoteConfigs>
      <branches>
        <hudson.plugins.git.BranchSpec>
          <name>*/${BRANCH}</name>
        </hudson.plugins.git.BranchSpec>
      </branches>
      <doGenerateSubmoduleConfigurations>false</doGenerateSubmoduleConfigurations>
      <submoduleCfg class="empty-list"/>
      <extensions/>
    </scm>
    <scriptPath>ci/Jenkinsfile</scriptPath>
    <lightweight>true</lightweight>
  </definition>
  <triggers/>
  <disabled>false</disabled>
</flow-definition>
EOF_CONFIG

echo "✅ Nueva configuración generada"

# Ajustar permisos
chown jenkins:jenkins "${JOB_CONFIG}"
chmod 644 "${JOB_CONFIG}"
echo "✅ Permisos ajustados"

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
    if curl -s http://localhost:8080 > /dev/null 2>&1; then
      echo "✅ Jenkins está listo"
      break
    fi
  fi
  echo "   Intento $i/30..."
  sleep 2
done

echo ""
echo "═══════════════════════════════════════════════════════════════"
echo "  CORRECCIÓN COMPLETADA"
echo "═══════════════════════════════════════════════════════════════"
echo ""
echo "✅ Job '${JOB_NAME}' reconfigurado para usar ci/Jenkinsfile desde SCM"
echo ""
echo "Configuración aplicada:"
echo "  - Definition: Pipeline script from SCM"
echo "  - SCM: Git"
echo "  - Repository: https://github.com/Atijaguar-ec/backend.git"
echo "  - Credential: github-pat"
echo "  - Branch: */${BRANCH}"
echo "  - Script Path: ci/Jenkinsfile"
echo ""
echo "📋 Próximos pasos:"
echo "  1. Verificar que la credencial 'github-pat' existe en Jenkins"
echo "  2. Ejecutar build de prueba con BRANCH=main, SKIP_TESTS=true"
echo "  3. Verificar que NO aparece error 'NoSuchMethodError: sshagent'"
echo ""
echo "💾 Backup guardado en: ${BACKUP_FILE}"
echo ""
