#!/bin/bash
# ============================================================================
# Corrección Manual Jenkins Config.xml
# ============================================================================

set -e

JOB_CONFIG="/var/lib/jenkins/jobs/Deploy-Backend/config.xml"
BACKUP_FILE="/var/lib/jenkins/config-backups/Deploy-Backend_config_manual_$(date +%Y%m%d_%H%M%S).xml"

echo "═══════════════════════════════════════════════════════════════"
echo "  CORRECCIÓN MANUAL CONFIG.XML"
echo "═══════════════════════════════════════════════════════════════"

if [ "$(whoami)" != "root" ]; then
  echo "❌ Ejecutar como root: sudo bash $0"
  exit 1
fi

# Backup
mkdir -p /var/lib/jenkins/config-backups
cp "${JOB_CONFIG}" "${BACKUP_FILE}"
echo "✅ Backup: ${BACKUP_FILE}"

# Detener Jenkins
echo "🛑 Deteniendo Jenkins..."
systemctl stop jenkins

# Editar directamente el config.xml
cat > "${JOB_CONFIG}" << 'EOF'
<?xml version='1.1' encoding='UTF-8'?>
<flow-definition plugin="workflow-job@1400.v7fd111b_ec82f">
  <actions/>
  <description>Pipeline de despliegue para backend Fortaleza del Valle</description>
  <keepDependencies>false</keepDependencies>
  <properties>
    <hudson.model.ParametersDefinitionProperty>
      <parameterDefinitions>
        <hudson.model.ChoiceParameterDefinition>
          <name>BRANCH</name>
          <description>Rama a desplegar</description>
          <choices class="java.util.Arrays$ArrayList">
            <a class="string-array">
              <string>staging</string>
              <string>main</string>
            </a>
          </choices>
        </hudson.model.ChoiceParameterDefinition>
        <hudson.model.BooleanParameterDefinition>
          <name>SKIP_TESTS</name>
          <description>Omitir pruebas</description>
          <defaultValue>false</defaultValue>
        </hudson.model.BooleanParameterDefinition>
        <hudson.model.BooleanParameterDefinition>
          <name>SKIP_DB_BACKUP</name>
          <description>Omitir backup de BD</description>
          <defaultValue>false</defaultValue>
        </hudson.model.BooleanParameterDefinition>
        <hudson.model.StringParameterDefinition>
          <name>STAGING_HEALTH_URL</name>
          <defaultValue>https://testinatrace.espam.edu.ec/api/actuator/health</defaultValue>
          <trim>false</trim>
        </hudson.model.StringParameterDefinition>
        <hudson.model.StringParameterDefinition>
          <name>PROD_HOST_PRIMARY</name>
          <defaultValue>190.15.143.192</defaultValue>
          <trim>false</trim>
        </hudson.model.StringParameterDefinition>
        <hudson.model.StringParameterDefinition>
          <name>PROD_USER_PRIMARY</name>
          <defaultValue>administrador</defaultValue>
          <trim>false</trim>
        </hudson.model.StringParameterDefinition>
        <hudson.model.StringParameterDefinition>
          <name>PROD_TARGET_PRIMARY</name>
          <defaultValue>/opt/inatrace/backend/prod/fortaleza</defaultValue>
          <trim>false</trim>
        </hudson.model.StringParameterDefinition>
        <hudson.model.StringParameterDefinition>
          <name>PROD_HEALTH_PORT_PRIMARY</name>
          <defaultValue>8082</defaultValue>
          <trim>false</trim>
        </hudson.model.StringParameterDefinition>
        <hudson.model.StringParameterDefinition>
          <name>PROD_HEALTH_URL_PRIMARY</name>
          <defaultValue>http://localhost:8082/actuator/health</defaultValue>
          <trim>false</trim>
        </hudson.model.StringParameterDefinition>
        <hudson.model.StringParameterDefinition>
          <name>PROD_DB_CONTAINER_NAME</name>
          <defaultValue>inatrace-mysql-prod-fortaleza</defaultValue>
          <trim>false</trim>
        </hudson.model.StringParameterDefinition>
      </parameterDefinitions>
    </hudson.model.ParametersDefinitionProperty>
    <org.jenkinsci.plugins.workflow.job.properties.DisableConcurrentBuildsJobProperty/>
  </properties>
  <definition class="org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition" plugin="workflow-cps">
    <scm class="hudson.plugins.git.GitSCM" plugin="git">
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
EOF

chown jenkins:jenkins "${JOB_CONFIG}"
chmod 644 "${JOB_CONFIG}"

echo "✅ Config.xml actualizado"

# Iniciar Jenkins
echo "🚀 Iniciando Jenkins..."
systemctl start jenkins

echo ""
echo "⏳ Esperando Jenkins..."
sleep 15

for i in {1..20}; do
  if curl -s http://localhost:8080 > /dev/null 2>&1; then
    echo "✅ Jenkins listo"
    break
  fi
  sleep 3
done

echo ""
echo "═══════════════════════════════════════════════════════════════"
echo "✅ CORRECCIÓN APLICADA"
echo "═══════════════════════════════════════════════════════════════"
echo ""
echo "Repository URL: https://github.com/Atijaguar-ec/backend.git"
echo "Credential ID: github-pat"
echo "Script Path: ci/Jenkinsfile"
echo ""
echo "🧹 SIGUIENTE PASO: Limpiar workspace"
echo "sudo rm -rf /var/lib/jenkins/workspace/Deploy-Backend/*"
echo ""
echo "📋 Luego ejecutar build de prueba en Jenkins UI"
echo ""
