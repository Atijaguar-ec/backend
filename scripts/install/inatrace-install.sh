#!/usr/bin/env bash
# Version: 1.1.0
# Single-command declarative orchestrator for INATrace Cacao clean-server deployment.
# Source-agnostic: works from any public repo, private/corporate fork, pre-cloned folder,
# or pre-built OCI container registry.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
# shellcheck source=lib/common.sh
. "${SCRIPT_DIR}/lib/common.sh"

CONFIG_FILE="/opt/inatrace/install.conf"
FROM_STEP="1"
ONLY_STEP=""
CHECK_QR="false"

usage() {
  cat <<'EOF'
Usage: inatrace-install.sh [OPTIONS]

Executes the full INATrace Cacao installation in a verified sequence from a
single declarative configuration file (install.conf).

Steps executed in order:
  1  Prepare host (Ubuntu 22.04/24.04 or Debian 12: Docker, Nginx, Certbot, UFW, Fail2ban)
  2  Resolve frontend source directory (use pre-cloned FRONTEND_DIR or clone FRONTEND_REPO)
  3  Build or pull Docker images (DEPLOY_MODE=build | pull)
  4  Initialize environment (.env 0600, docker-compose.yml, initdb, Keycloak theme)
  5  Configure Nginx reverse proxy and TLS (letsencrypt | lan-selfsigned)
  6  Start Compose stack and bootstrap Keycloak 26 realm + OIDC clients
  7  Create initial SYSTEM_ADMIN and apply idempotent Cacao catalog seed
  8  Run end-to-end verification (06-verify-installation.sh)

Options:
  --config <path>      Path to declarative install.conf (default: /opt/inatrace/install.conf)
  --from-step <1..8>   Resume execution starting at step N (default: 1)
  --only-step <1..8>   Execute only step N
  --check-qr           Include public QR probe in step 8
  -h, --help           Show this help message and exit

Security note:
  Set INATRACE_ADMIN_PASSWORD in the environment for unattended runs, or leave
  unset to enter the password interactively without terminal echo.

Example:
  sudo ./scripts/install/inatrace-install.sh --config /opt/inatrace/install.conf
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --config)    [[ $# -ge 2 ]] || { log_error "--config requires a value"; exit 1; }; CONFIG_FILE="$2"; shift 2 ;;
    --from-step) [[ $# -ge 2 ]] || { log_error "--from-step requires a value"; exit 1; }; FROM_STEP="$2"; shift 2 ;;
    --only-step) [[ $# -ge 2 ]] || { log_error "--only-step requires a value"; exit 1; }; ONLY_STEP="$2"; shift 2 ;;
    --check-qr)  CHECK_QR="true"; shift ;;
    -h|--help)   usage; exit 0 ;;
    *)           log_error "Unknown option: $1"; usage >&2; exit 1 ;;
  esac
done

require_root
require_x86_64

[[ -f "${CONFIG_FILE}" ]] || {
  log_error "Configuration file not found: ${CONFIG_FILE}"
  log_info  "Copy ${SCRIPT_DIR}/install.conf.example to ${CONFIG_FILE} and customize it first."
  exit 1
}

# shellcheck disable=SC1090
. "${CONFIG_FILE}"

# Defaults
DOMAIN_NAME="${DOMAIN_NAME:-}"
TLS_MODE="${TLS_MODE:-letsencrypt}"
TLS_EMAIL="${TLS_EMAIL:-}"
DEPLOY_MODE="${DEPLOY_MODE:-build}"
IMAGE_PREFIX="${IMAGE_PREFIX:-mi-organizacion}"
IMAGE_TAG="${IMAGE_TAG:-v1.0.0}"
FRONTEND_DIR="${FRONTEND_DIR:-/opt/src/inatrace-fe}"
FRONTEND_REPO="${FRONTEND_REPO:-}"
FRONTEND_REF="${FRONTEND_REF:-main}"
BASE_DIR="${BASE_DIR:-/opt/inatrace}"
DOCKER_USER="${DOCKER_USER:-${SUDO_USER:-root}}"
KEYCLOAK_REALM="${KEYCLOAK_REALM:-inatrace}"
SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-prod}"
SWAP_GB="${SWAP_GB:-4}"
WAIT_SECONDS="${WAIT_SECONDS:-240}"
PG_HOST_PORT="${PG_HOST_PORT:-5432}"
FRONTEND_PORT="${FRONTEND_PORT:-8081}"
KEYCLOAK_PORT="${KEYCLOAK_PORT:-8082}"
BACKEND_PORT="${BACKEND_PORT:-8083}"
ADMIN_EMAIL="${ADMIN_EMAIL:-}"
ADMIN_NAME="${ADMIN_NAME:-Admin}"
ADMIN_SURNAME="${ADMIN_SURNAME:-Sistema}"
ADMIN_LANG="${ADMIN_LANG:-ES}"
COUNTRY_CODE="${COUNTRY_CODE:-EC}"
COUNTRY_NAME="${COUNTRY_NAME:-Ecuador}"
COUNTRY_LAT="${COUNTRY_LAT:--1.8312}"
COUNTRY_LNG="${COUNTRY_LNG:--78.1834}"
CURRENCY_CODE="${CURRENCY_CODE:-USD}"
CURRENCY_LABEL="${CURRENCY_LABEL:-US Dollar}"
COMPANY_NAME="${COMPANY_NAME:-Mi Cooperativa Cacao}"
COMPANY_ABBR="${COMPANY_ABBR:-COOP}"
COMPANY_PROFILE="${COMPANY_PROFILE:-baba-organico}"
SKIP_SEED="${SKIP_SEED:-false}"
SKIP_UFW="${SKIP_UFW:-false}"
SKIP_FAIL2BAN="${SKIP_FAIL2BAN:-false}"
SKIP_DOCKER_LOGS="${SKIP_DOCKER_LOGS:-false}"

[[ -n "${DOMAIN_NAME}" ]] || { log_error "DOMAIN_NAME must be set in ${CONFIG_FILE}."; exit 1; }
if [[ "${TLS_MODE}" == "letsencrypt" && -z "${TLS_EMAIL}" ]]; then
  log_error "TLS_EMAIL is required in ${CONFIG_FILE} when TLS_MODE=letsencrypt."
  exit 1
fi
case "${DEPLOY_MODE}" in
  build|pull) ;;
  *) log_error "DEPLOY_MODE must be 'build' or 'pull' (got: ${DEPLOY_MODE})"; exit 1 ;;
esac

should_run() {
  local step="$1"
  if [[ -n "${ONLY_STEP}" ]]; then
    [[ "${step}" == "${ONLY_STEP}" ]]
  else
    [[ "${step}" -ge "${FROM_STEP}" ]]
  fi
}

if should_run 7 && [[ -n "${ADMIN_EMAIL}" && -z "${INATRACE_ADMIN_PASSWORD:-}" ]]; then
  if [[ -t 0 ]]; then
    read -r -s -p "Enter initial SYSTEM_ADMIN password for ${ADMIN_EMAIL}: " INATRACE_ADMIN_PASSWORD
    printf '\n'
    export INATRACE_ADMIN_PASSWORD
  else
    log_error "Set INATRACE_ADMIN_PASSWORD environment variable for non-interactive step 7."
    exit 1
  fi
fi

ENV_FILE="${BASE_DIR}/.env"

# Step 1: Prepare host
if should_run 1; then
  log_info "=== [Step 1/8] Preparing host OS, Docker, Nginx, Certbot, UFW & Fail2ban ==="
  PREP_ARGS=(--user "${DOCKER_USER}")
  [[ "${SKIP_UFW}" == "true" ]] && PREP_ARGS+=(--skip-ufw)
  [[ "${SKIP_FAIL2BAN}" == "true" ]] && PREP_ARGS+=(--skip-fail2ban)
  [[ "${SKIP_DOCKER_LOGS}" == "true" ]] && PREP_ARGS+=(--skip-docker-logs)
  "${SCRIPT_DIR}/01-prepare-host.sh" "${PREP_ARGS[@]}"
fi

# Step 2: Resolve frontend repository (pre-cloned folder OR auto-clone public/private URL)
if should_run 2; then
  log_info "=== [Step 2/8] Resolving frontend assets & source directory (${FRONTEND_DIR}) ==="
  if [[ -d "${FRONTEND_DIR}/scripts/install" ]]; then
    log_ok "Using existing frontend directory: ${FRONTEND_DIR}"
  elif [[ -n "${FRONTEND_REPO}" ]]; then
    require_cmd git
    mkdir -p "$(dirname "${FRONTEND_DIR}")"
    log_info "Cloning frontend repository (${FRONTEND_REF}) into ${FRONTEND_DIR}..."
    git clone --depth 1 --branch "${FRONTEND_REF}" "${FRONTEND_REPO}" "${FRONTEND_DIR}"
    log_ok "Frontend cloned at ${FRONTEND_DIR}"
  else
    log_error "FRONTEND_DIR (${FRONTEND_DIR}) does not contain scripts/install and FRONTEND_REPO is empty."
    log_error "Clone your frontend repository into FRONTEND_DIR first or set FRONTEND_REPO in ${CONFIG_FILE}."
    exit 1
  fi
fi

# Step 3: Build or Pull Docker images
if should_run 3; then
  log_info "=== [Step 3/8] Obtaining Docker images (DEPLOY_MODE=${DEPLOY_MODE}) ==="
  if [[ "${DEPLOY_MODE}" == "build" ]]; then
    "${SCRIPT_DIR}/02-build-backend.sh" --image-prefix "${IMAGE_PREFIX}" --tag "${IMAGE_TAG}"
    FE_BUILD_ARGS=(--image-prefix "${IMAGE_PREFIX}" --tag "${IMAGE_TAG}" --swap-gb "${SWAP_GB}")
    [[ "${SWAP_GB}" == "0" ]] && FE_BUILD_ARGS+=(--no-swap)
    "${FRONTEND_DIR}/scripts/install/build-frontend.sh" "${FE_BUILD_ARGS[@]}"
  else
    require_cmd docker
    docker pull "${IMAGE_PREFIX}/inatrace-backend:${IMAGE_TAG}"
    docker pull "${IMAGE_PREFIX}/inatrace-fe:${IMAGE_TAG}"
    log_ok "Pulled pre-built images for ${IMAGE_PREFIX} (${IMAGE_TAG})."
  fi
fi

# Step 4: Initialize environment, .env, docker-compose.yml & Keycloak theme
if should_run 4; then
  log_info "=== [Step 4/8] Initializing ${BASE_DIR}, .env (0600), Compose & Keycloak theme ==="
  "${SCRIPT_DIR}/03-init-environment.sh" \
    --domain "${DOMAIN_NAME}" \
    --realm "${KEYCLOAK_REALM}" \
    --base-dir "${BASE_DIR}" \
    --image-prefix "${IMAGE_PREFIX}" \
    --tag "${IMAGE_TAG}" \
    --spring-profile "${SPRING_PROFILES_ACTIVE}" \
    --tls-mode "${TLS_MODE}" \
    --pg-port "${PG_HOST_PORT}" \
    --frontend-port "${FRONTEND_PORT}" \
    --keycloak-port "${KEYCLOAK_PORT}" \
    --backend-port "${BACKEND_PORT}"
  "${FRONTEND_DIR}/scripts/install/install-keycloak-theme.sh" --env-file "${ENV_FILE}"
fi

# Step 5: Configure Nginx reverse proxy & TLS BEFORE starting full application stack
if should_run 5; then
  log_info "=== [Step 5/8] Configuring Nginx reverse proxy & TLS (${TLS_MODE}) ==="
  NGX_ARGS=(--env-file "${ENV_FILE}" --tls-mode "${TLS_MODE}")
  [[ -n "${TLS_EMAIL}" ]] && NGX_ARGS+=(--email "${TLS_EMAIL}")
  "${FRONTEND_DIR}/scripts/install/configure-nginx-tls.sh" "${NGX_ARGS[@]}"
fi

# Step 6: Start Compose stack & bootstrap Keycloak 26 realm + OIDC clients
if should_run 6; then
  log_info "=== [Step 6/8] Deploying Compose stack & bootstrapping Keycloak 26 ==="
  "${SCRIPT_DIR}/04-deploy-and-bootstrap-keycloak.sh" \
    --env-file "${ENV_FILE}" \
    --wait-seconds "${WAIT_SECONDS}"
fi

# Step 7: Create initial SYSTEM_ADMIN & apply Cacao catalog seed
if should_run 7; then
  log_info "=== [Step 7/8] Creating SYSTEM_ADMIN & loading Cacao catalog seed ==="
  if [[ -z "${ADMIN_EMAIL}" ]]; then
    log_warn "ADMIN_EMAIL is empty in ${CONFIG_FILE}; skipping step 7."
  else
    ADMIN_ARGS=(
      --env-file "${ENV_FILE}"
      --email "${ADMIN_EMAIL}"
      --name "${ADMIN_NAME}"
      --surname "${ADMIN_SURNAME}"
      --lang "${ADMIN_LANG}"
      --country-code "${COUNTRY_CODE}"
      --country-name "${COUNTRY_NAME}"
      --lat "${COUNTRY_LAT}"
      --lng "${COUNTRY_LNG}"
      --currency-code "${CURRENCY_CODE}"
      --currency-label "${CURRENCY_LABEL}"
      --company-name "${COMPANY_NAME}"
      --company-abbr "${COMPANY_ABBR}"
      --company-profile "${COMPANY_PROFILE}"
    )
    [[ "${SKIP_SEED}" == "true" ]] && ADMIN_ARGS+=(--skip-seed)
    INATRACE_ADMIN_PASSWORD="${INATRACE_ADMIN_PASSWORD}" \
      "${SCRIPT_DIR}/05-create-admin-and-seed.sh" "${ADMIN_ARGS[@]}"
  fi
fi

# Step 8: End-to-end automated verification
if should_run 8; then
  log_info "=== [Step 8/8] Running end-to-end installation verification ==="
  VER_ARGS=(--env-file "${ENV_FILE}")
  [[ "${CHECK_QR}" == "true" ]] && VER_ARGS+=(--check-qr)
  [[ "${TLS_MODE}" == "lan-selfsigned" ]] && VER_ARGS+=(--insecure-tls)
  "${SCRIPT_DIR}/06-verify-installation.sh" "${VER_ARGS[@]}"
fi

log_ok "INATrace installation workflow completed for https://${DOMAIN_NAME}"
