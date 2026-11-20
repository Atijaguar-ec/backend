#!/usr/bin/env bash
# Version: 1.0.0
# Restores INATrace PostgreSQL, Keycloak database, and binary document storage
# from a timestamped backup created by 09-backup-inatrace.sh.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib/common.sh
. "${SCRIPT_DIR}/lib/common.sh"

ENV_FILE="/opt/inatrace/.env"
BACKUP_DIR=""
TIMESTAMP=""
USE_LATEST="false"
CONFIRMED="false"

usage() {
  cat <<'EOF'
Usage: 10-restore-backup.sh [OPTIONS]

Verifies SHA256SUMS, stops Backend and Keycloak services, restores PostgreSQL
(inatrace and keycloak databases via pg_restore --clean --if-exists) plus binary
file_storage/documents archives, and restarts the full Docker Compose stack.

Options:
  --env-file <path>       Path to .env file (default: /opt/inatrace/.env)
  --backup-dir <path>     Backup root directory (default: <BASE_DIR>/backups)
  --timestamp <stamp>     Backup folder timestamp (YYYYMMDD-HHMMSS)
  --latest                Restore the most recent timestamp folder in --backup-dir
  --yes                   Confirm destructive overwrite of live database and files
  -h, --help              Show this help message and exit

Example:
  sudo ./10-restore-backup.sh --latest --yes
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --env-file)   [[ $# -ge 2 ]] || { log_error "--env-file requires a value"; exit 1; }; ENV_FILE="$2"; shift 2 ;;
    --backup-dir) [[ $# -ge 2 ]] || { log_error "--backup-dir requires a value"; exit 1; }; BACKUP_DIR="$2"; shift 2 ;;
    --timestamp)  [[ $# -ge 2 ]] || { log_error "--timestamp requires a value"; exit 1; }; TIMESTAMP="$2"; shift 2 ;;
    --latest)     USE_LATEST="true"; shift ;;
    --yes)        CONFIRMED="true"; shift ;;
    -h|--help)    usage; exit 0 ;;
    *)            log_error "Unknown option: $1"; usage >&2; exit 1 ;;
  esac
done

require_root
require_cmd docker tar curl sha256sum
[[ "${CONFIRMED}" == "true" ]] || {
  log_error "Restore overwrites live database and files. Pass --yes to confirm."
  exit 1
}

load_env_file "${ENV_FILE}"
BASE_DIR="${BASE_DIR:-/opt/inatrace}"
BACKUP_DIR="${BACKUP_DIR:-${BASE_DIR}/backups}"
COMPOSE_FILE="${BASE_DIR}/docker-compose.yml"
BACKEND_PORT="${BACKEND_PORT:-8083}"

if [[ "${USE_LATEST}" == "true" ]]; then
  TIMESTAMP="$(ls -1 "${BACKUP_DIR}" 2>/dev/null | sort -r | head -n 1 || true)"
fi
[[ -n "${TIMESTAMP}" ]] || { log_error "Specify --timestamp <YYYYMMDD-HHMMSS> or --latest."; exit 1; }

SOURCE_DIR="${BACKUP_DIR}/${TIMESTAMP}"
[[ -d "${SOURCE_DIR}" ]] || { log_error "Backup directory not found: ${SOURCE_DIR}"; exit 1; }
[[ -f "${SOURCE_DIR}/inatrace.dump" && -f "${SOURCE_DIR}/keycloak.dump" ]] || {
  log_error "Incomplete backup in ${SOURCE_DIR} (missing .dump files)."
  exit 1
}

if [[ -f "${SOURCE_DIR}/SHA256SUMS" ]]; then
  log_info "Verifying backup SHA256SUMS integrity..."
  (cd "${SOURCE_DIR}" && sha256sum -c SHA256SUMS)
  log_ok "Checksums verified."
fi

log_info "Stopping Backend and Keycloak containers before restore..."
compose_run "${ENV_FILE}" "${COMPOSE_FILE}" stop inatrace-backend inatrace-keycloak

log_info "Ensuring PostgreSQL container is running..."
compose_run "${ENV_FILE}" "${COMPOSE_FILE}" up -d inatrace-postgres
until docker exec inatrace-postgres pg_isready -U "${POSTGRES_USER}" -d "${POSTGRES_DB}" >/dev/null 2>&1; do
  sleep 2
done

log_info "Restoring '${POSTGRES_DB}'..."
docker exec -i inatrace-postgres pg_restore -U "${POSTGRES_USER}" -d "${POSTGRES_DB}" \
  --clean --if-exists --no-owner --no-privileges < "${SOURCE_DIR}/inatrace.dump"

log_info "Restoring '${KC_DB_NAME}'..."
docker exec -i inatrace-postgres pg_restore -U "${POSTGRES_USER}" -d "${KC_DB_NAME}" \
  --clean --if-exists --no-owner --no-privileges < "${SOURCE_DIR}/keycloak.dump"

if [[ -f "${SOURCE_DIR}/file_storage.tar.gz" ]]; then
  log_info "Restoring file_storage..."
  tar -xzf "${SOURCE_DIR}/file_storage.tar.gz" -C "${BASE_DIR}"
fi
if [[ -f "${SOURCE_DIR}/documents.tar.gz" ]]; then
  log_info "Restoring documents..."
  tar -xzf "${SOURCE_DIR}/documents.tar.gz" -C "${BASE_DIR}"
fi

log_info "Starting full INATrace stack..."
compose_run "${ENV_FILE}" "${COMPOSE_FILE}" up -d

log_info "Waiting for Backend OpenAPI readiness..."
for _ in $(seq 1 45); do
  if curl -fsS "http://127.0.0.1:${BACKEND_PORT}/api/v3/api-docs" >/dev/null 2>&1; then
    log_ok "Disaster recovery restore from '${TIMESTAMP}' completed successfully."
    exit 0
  fi
  sleep 4
done

log_error "Backend did not respond after restore; check 'docker logs inatrace-backend'."
exit 1
