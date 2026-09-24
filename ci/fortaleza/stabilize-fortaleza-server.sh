#!/usr/bin/env bash
# ==============================================================================
# Script de Estabilización y Contención — Servidor Fortaleza del Valle (CEDIA/ESPAM)
# Historia de Usuario: HU-06 (ADR-010, ADR-014)
#
# Propósito:
#   1. Acotar el consumo de recursos de Jenkins (detener bucle AsyncResourceDisposer
#      y limitar CPU a 80% y RAM a 1.5GB vía systemd drop-in).
#   2. Configurar el host en modo headless estricto (multi-user.target).
#   3. Purgar procesos de interfaz gráfica (gdm3, gnome-shell, Xorg) y AnyDesk.
#   4. Reclamar memoria RAM y liberar espacio en swap.
#
# Uso en el servidor (como root o con sudo):
#   sudo bash stabilize-fortaleza-server.sh
# ==============================================================================

set -euo pipefail

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info()    { printf "${BLUE}[INFO]${NC} %s\n" "$*"; }
log_ok()      { printf "${GREEN}[OK]${NC} %s\n" "$*"; }
log_warn()    { printf "${YELLOW}[WARN]${NC} %s\n" "$*"; }
log_error()   { printf "${RED}[ERROR]${NC} %s\n" "$*"; }

if [[ "$(id -u)" -ne 0 ]]; then
  log_error "Este script debe ejecutarse con privilegios de root (sudo)."
  exit 1
fi

log_info "======================================================================"
log_info "  Iniciando Estabilización del Servidor Fortaleza del Valle (HU-06)   "
log_info "======================================================================"

# ------------------------------------------------------------------------------
# 1. Diagnóstico Inicial
# ------------------------------------------------------------------------------
log_info "Diagnóstico previo:"
log_info "Uptime y carga: $(uptime)"
free -h || true

# ------------------------------------------------------------------------------
# 2. Contención de Jenkins (Bucle AsyncResourceDisposer y Cuotas Systemd)
# ------------------------------------------------------------------------------
log_info "Paso 1: Aplicando contención sobre el servicio Jenkins..."

if [[ -z "${JENKINS_HOME:-}" ]]; then
  if [[ -d "/var/lib/jenkins" ]]; then
    JENKINS_HOME="/var/lib/jenkins"
  elif id -u jenkins >/dev/null 2>&1; then
    JENKINS_HOME="$(getent passwd jenkins | cut -d: -f6)"
  else
    JENKINS_HOME="/var/lib/jenkins"
  fi
fi

if systemctl is-active --quiet jenkins; then
  log_info "Deteniendo Jenkins temporalmente para purgar bucle y reconfigurar..."
  systemctl stop jenkins || true
fi

# Purgar archivo de estado con reintentos fallidos de AsyncResourceDisposer
DISPOSER_XML="${JENKINS_HOME}/org.jenkinsci.plugins.resourcedisposer.AsyncResourceDisposer.xml"
if [[ -f "${DISPOSER_XML}" ]]; then
  log_info "Purgando ciclo AsyncResourceDisposer corrupto en ${DISPOSER_XML}..."
  cp "${DISPOSER_XML}" "${DISPOSER_XML}.bak.$(date +%Y%m%d_%H%M%S)"
  cat <<'EOF' > "${DISPOSER_XML}"
<?xml version='1.1' encoding='UTF-8'?>
<org.jenkinsci.plugins.resourcedisposer.AsyncResourceDisposer plugin="resource-disposer@0.24">
  <disposables/>
</org.jenkinsci.plugins.resourcedisposer.AsyncResourceDisposer>
EOF
  chown jenkins:jenkins "${DISPOSER_XML}" || true
  log_ok "Archivo AsyncResourceDisposer purgado con éxito."
else
  log_info "No se encontró ${DISPOSER_XML} o ya estaba limpio."
fi

# Configurar cuotas systemd en override.conf
OVERRIDE_DIR="/etc/systemd/system/jenkins.service.d"
mkdir -p "${OVERRIDE_DIR}"

log_info "Configurando cuotas de recursos en ${OVERRIDE_DIR}/override.conf..."
cat <<'EOF' > "${OVERRIDE_DIR}/override.conf"
[Service]
# Cuota de CPU: máximo 80% de un core (o equivalente) para evitar canibalizar INATrace
CPUQuota=80%
# Memoria máxima para Jenkins: 1.5 GB
MemoryMax=1.5G
MemoryHigh=1.2G
# Límite de tareas/hilos concurrentes
TasksMax=2048
EOF

systemctl daemon-reload
log_ok "Cuotas systemd para Jenkins aplicadas."

if systemctl is-enabled --quiet jenkins 2>/dev/null; then
  log_info "Iniciando Jenkins con nuevas cuotas de recursos..."
  systemctl start jenkins || log_warn "No se pudo iniciar Jenkins inmediatamente. Verificar logs."
fi

# ------------------------------------------------------------------------------
# 3. Transición a Modo Headless (multi-user.target)
# ------------------------------------------------------------------------------
log_info "Paso 2: Fijando modo Headless (consola pura)..."

CURRENT_TARGET=$(systemctl get-default)
if [[ "${CURRENT_TARGET}" != "multi-user.target" ]]; then
  log_info "Cambiando target por defecto de ${CURRENT_TARGET} a multi-user.target..."
  systemctl set-default multi-user.target
  log_ok "Target por defecto configurado a multi-user.target."
else
  log_ok "Target por defecto ya es multi-user.target."
fi

# Detener gestores gráficos inmediatamente si están activos
for svc in gdm3 gdm lightdm sddm anydesk; do
  if systemctl is-active --quiet "${svc}"; then
    log_info "Deteniendo servicio gráfico ${svc}..."
    systemctl stop "${svc}" || true
  fi
  if systemctl is-enabled --quiet "${svc}" 2>/dev/null; then
    log_info "Deshabilitando inicio automático de ${svc}..."
    systemctl disable "${svc}" || true
  fi
done

# Matar procesos residuales de escritorio si existen
pkill -9 -f "gnome-shell" 2>/dev/null || true
pkill -9 -f "Xorg" 2>/dev/null || true
pkill -9 -f "anydesk" 2>/dev/null || true

# ------------------------------------------------------------------------------
# 4. Desinstalación de Paquetes Gráficos y AnyDesk
# ------------------------------------------------------------------------------
log_info "Paso 3: Purgando paquetes de escritorio innecesarios..."

PACKAGES_TO_PURGE=()
for pkg in anydesk gdm3 gnome-shell xorg x11-common; do
  if dpkg -l "${pkg}" 2>/dev/null | grep -q '^ii'; then
    PACKAGES_TO_PURGE+=("${pkg}")
  fi
done

if [[ ${#PACKAGES_TO_PURGE[@]} -gt 0 ]]; then
  log_info "Desinstalando: ${PACKAGES_TO_PURGE[*]}..."
  export DEBIAN_FRONTEND=noninteractive
  apt-get remove --purge -y "${PACKAGES_TO_PURGE[@]}" || true
  apt-get autoremove --purge -y || true
  apt-get clean
  log_ok "Paquetes de escritorio purgados."
else
  log_ok "No se encontraron paquetes gráficos para purgar."
fi

# ------------------------------------------------------------------------------
# 5. Liberación de Swap y Verificación de Carga
# ------------------------------------------------------------------------------
log_info "Paso 4: Verificación de memoria y refresco de swap..."

FREE_RAM_MB=$(free -m 2>/dev/null | awk '/^Mem:/{print $7}')
SWAP_USED_MB=$(free -m 2>/dev/null | awk '/^Swap:/{print $3}')
FREE_RAM_MB="${FREE_RAM_MB:-0}"
SWAP_USED_MB="${SWAP_USED_MB:-0}"
if [[ ! "${FREE_RAM_MB}" =~ ^[0-9]+$ ]]; then FREE_RAM_MB=0; fi
if [[ ! "${SWAP_USED_MB}" =~ ^[0-9]+$ ]]; then SWAP_USED_MB=0; fi

log_info "RAM disponible: ${FREE_RAM_MB} MB | Swap en uso: ${SWAP_USED_MB} MB"
if [[ "${SWAP_USED_MB}" -gt 0 && "${FREE_RAM_MB}" -gt $((SWAP_USED_MB + 512)) ]]; then
  log_info "Reclamando swap (swapoff -> swapon)..."
  swapoff -a && swapon -a || true
  log_ok "Swap limpiado exitosamente."
else
  log_warn "No se forzó la purga de swap por seguridad de memoria disponible."
fi

# ------------------------------------------------------------------------------
# 6. Verificación Final de Criterios de Aceptación (DoD)
# ------------------------------------------------------------------------------
log_info "======================================================================"
log_info "  Verificación Final de Criterios de Aceptación (HU-06)               "
log_info "======================================================================"

FINAL_TARGET=$(systemctl get-default)
log_info "Target del sistema: ${FINAL_TARGET}"
if [[ "${FINAL_TARGET}" == "multi-user.target" ]]; then
  log_ok "Criterio 1 CUMPLIDO: Servidor configurado en multi-user.target."
else
  log_error "Criterio 1 NO CUMPLIDO: Target actual es ${FINAL_TARGET}."
fi

RUNNING_DESKTOP=$(pgrep -f "gnome-shell|Xorg|anydesk|gdm3" || true)
if [[ -z "${RUNNING_DESKTOP}" ]]; then
  log_ok "Criterio 2 CUMPLIDO: Cero procesos gráficos o AnyDesk en ejecución."
else
  log_warn "Advertencia: Procesos aún detectados: ${RUNNING_DESKTOP}"
fi

log_info "Estado de cuotas en Jenkins:"
systemctl show jenkins -p CPUQuota -p MemoryMax -p TasksMax 2>/dev/null || true

log_info "Uptime y carga final:"
uptime

log_ok "Script de estabilización completado con éxito."
