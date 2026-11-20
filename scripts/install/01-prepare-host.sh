#!/usr/bin/env bash
# Version: 1.1.0
# Prepares an Ubuntu 22.04/24.04 or Debian 12 x86_64 LTS server host for INATrace:
# base packages, Docker Engine + Compose v2, Nginx, Certbot, UFW firewall,
# Docker JSON log rotation, and optional Fail2ban SSH protection (systemd backend).
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib/common.sh
. "${SCRIPT_DIR}/lib/common.sh"

DOCKER_USER="${SUDO_USER:-root}"
SKIP_FAIL2BAN="false"
SKIP_DOCKER_LOGS="false"
SKIP_UFW="false"

usage() {
  cat <<'EOF'
Usage: 01-prepare-host.sh [OPTIONS]

Prepares an Ubuntu 22.04 / 24.04 x86_64 LTS host with Docker Engine, Compose v2,
Nginx, Certbot, UFW firewall (22/80/443), Docker log rotation, and Fail2ban.

Options:
  --user <username>    Linux user to add to the docker group (default: SUDO_USER or root)
  --skip-fail2ban      Skip Fail2ban installation and SSH jail configuration
  --skip-docker-logs   Skip writing /etc/docker/daemon.json log rotation limits
  --skip-ufw           Skip UFW firewall rule configuration
  -h, --help           Show this help message and exit

Example:
  sudo ./01-prepare-host.sh --user ubuntu
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --user)
      [[ $# -ge 2 ]] || { log_error "--user requires a value"; exit 1; }
      DOCKER_USER="$2"
      shift 2
      ;;
    --skip-fail2ban)
      SKIP_FAIL2BAN="true"
      shift
      ;;
    --skip-docker-logs)
      SKIP_DOCKER_LOGS="true"
      shift
      ;;
    --skip-ufw)
      SKIP_UFW="true"
      shift
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      log_error "Unknown option: $1"
      usage >&2
      exit 1
      ;;
  esac
done

require_root
require_x86_64

log_info "Updating APT indexes and installing base dependencies..."
export DEBIAN_FRONTEND=noninteractive
apt-get update -y
apt-get install -y \
  apt-transport-https \
  ca-certificates \
  curl \
  git \
  gnupg \
  jq \
  lsb-release \
  nginx \
  certbot \
  python3 \
  python3-certbot-nginx \
  openssl \
  ufw

if ! command -v docker >/dev/null 2>&1; then
  log_info "Installing Docker Engine + Docker Compose v2 plugin..."
  DISTRO_ID="$(. /etc/os-release && printf '%s' "${ID}")"
  case "${DISTRO_ID}" in
    ubuntu|debian) ;;
    *) log_error "Unsupported Linux distribution '${DISTRO_ID}' (supported: ubuntu, debian)."; exit 1 ;;
  esac
  install -m 0755 -d /etc/apt/keyrings
  if [[ ! -f /etc/apt/keyrings/docker.asc ]]; then
    curl -fsSL "https://download.docker.com/linux/${DISTRO_ID}/gpg" -o /etc/apt/keyrings/docker.asc
    chmod a+r /etc/apt/keyrings/docker.asc
  fi
  ARCH_DEB="$(dpkg --print-architecture)"
  CODENAME="$(. /etc/os-release && printf '%s' "${VERSION_CODENAME}")"
  printf 'deb [arch=%s signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/%s %s stable\n' \
    "${ARCH_DEB}" "${DISTRO_ID}" "${CODENAME}" > /etc/apt/sources.list.d/docker.list
  apt-get update -y
  apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
else
  log_ok "Docker already installed: $(docker --version)"
fi

systemctl enable --now docker

if id "${DOCKER_USER}" >/dev/null 2>&1 && [[ "${DOCKER_USER}" != "root" ]]; then
  usermod -aG docker "${DOCKER_USER}"
  log_ok "Added user '${DOCKER_USER}' to docker group."
fi

if [[ "${SKIP_DOCKER_LOGS}" == "false" ]]; then
  log_info "Configuring Docker JSON log rotation (10m x 3 files)..."
  install -d -m 755 /etc/docker
  if [[ ! -f /etc/docker/daemon.json ]]; then
    cat > /etc/docker/daemon.json <<'JSON_EOF'
{
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "10m",
    "max-file": "3"
  }
}
JSON_EOF
    systemctl restart docker
    log_ok "Docker daemon.json configured."
  else
    log_warn "/etc/docker/daemon.json already exists; leaving untouched."
  fi
fi

if [[ "${SKIP_UFW}" == "false" ]]; then
  log_info "Configuring UFW firewall (SSH 22, HTTP 80, HTTPS 443)..."
  ufw allow 22/tcp >/dev/null
  ufw allow 80/tcp >/dev/null
  ufw allow 443/tcp >/dev/null
  ufw --force enable >/dev/null
  log_ok "UFW active."
fi

if [[ "${SKIP_FAIL2BAN}" == "false" ]]; then
  log_info "Installing and enabling Fail2ban SSH jail (systemd backend)..."
  apt-get install -y fail2ban python3-systemd || apt-get install -y fail2ban
  cat > /etc/fail2ban/jail.d/sshd.local <<'F2B_EOF'
[sshd]
enabled  = true
port     = ssh
filter   = sshd
backend  = systemd
maxretry = 5
bantime  = 3600
findtime = 600
F2B_EOF
  systemctl enable --now fail2ban
  systemctl restart fail2ban
  log_ok "Fail2ban configured."
fi

log_ok "Host preparation completed."
