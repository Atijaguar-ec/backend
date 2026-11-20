#!/usr/bin/env bash
# Version: 1.0.0
# Creates an atomic INATrace backup (PostgreSQL custom dumps + file_storage/documents
# + .env/docker-compose.yml + SHA256SUMS + retention pruning) or installs cron job.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib/common.sh
. "${SCRIPT_DIR}/lib/common.sh"

ENV_FILE="/opt/inatrace/.env"
BACKUP_DIR=""
RETENTION_DAYS="30"
INSTALL_CRON="false"
CRON_SCHEDULE="0 2 * * *"

usage() {
  cat <<'EOF'
Usage: 09-backup-inatrace.sh [OPTIONS]

Backs up INATrace and Keycloak PostgreSQL databases (pg_dump -Fc), file_storage,
documents, .env, and docker-compose.yml, writes SHA256SUMS, and prunes archives
older than --retention-days. Optionally registers a root cron job.

Options:
  --env-file <path>       Path to .env file (default: /opt/inatrace/.env)
  --backup-dir <path>     Backup root directory (default: <BASE_DIR>/backups)
  --retention-days <N>    Delete backups older than N days (default: 30)
  --install-cron          Install /etc/cron.d/inatrace-backup and exit
  --schedule "<cron>"     Cron schedule expression when --install-cron is set (default: "0 2 * * *")
  -h, --help              Show this help message and exit

Examples:
  sudo ./09-backup-inatrace.sh --env-file /opt/inatrace/.env
  sudo ./09-backup-inatrace.sh --install-cron --schedule "0 2 * * *"
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --env-file)       [[ $# -ge 2 ]] || { log_error "--env-file requires a value"; exit 1; }; ENV_FILE="$2"; shift 2 ;;
    --backup-dir)     [[ $# -ge 2 ]] || { log_error "--backup-dir requires a value"; exit 1; }; BACKUP_DIR="$2"; shift 2 ;;
    --retention-days) [[ $# -ge 2 ]] || { log_error "--retention-days requires a value"; exit 1; }; RETENTION_DAYS="$2"; shift 2 ;;
    --install-cron)   INSTALL_CRON="true"; shift ;;
    --schedule)       [[ $# -ge 2 ]] || { log_error "--schedule requires a value"; exit 1; }; CRON_SCHEDULE="$2"; shift 2 ;;
    -h|--help)        usage; exit 0 ;;
    *)                log_error "Unknown option: $1"; usage >&2; exit 1 ;;
  esac
done

require_root
[[ "${RETENTION_DAYS}" =~ ^[0-9]+$ ]] || { log_error "--retention-days must be numeric."; exit 1; }
load_env_file "${ENV_FILE}"

BASE_DIR="${BASE_DIR:-/opt/inatrace}"
BACKUP_DIR="${BACKUP_DIR:-${BASE_DIR}/backups}"

if [[ "${INSTALL_CRON}" == "true" ]]; then
  SCRIPT_SELF="${SCRIPT_DIR}/09-backup-inatrace.sh"
  mkdir -p /var/log/inatrace
  cat > /etc/cron.d/inatrace-backup <<CRON_EOF
SHELL=/bin/bash
PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin
${CRON_SCHEDULE} root "${SCRIPT_SELF}" --env-file "${ENV_FILE}" --backup-dir "${BACKUP_DIR}" --retention-days "${RETENTION_DAYS}" >> /var/log/inatrace/backup.log 2>&1
CRON_EOF
  chmod 644 /etc/cron.d/inatrace-backup
  log_ok "Installed scheduled backup cron at /etc/cron.d/inatrace-backup (${CRON_SCHEDULE})."
  exit 0
fi

require_cmd docker tar sha256sum

STAMP="$(date +%Y%m%d-%H%M%S)"
TARGET_DIR="${BACKUP_DIR}/${STAMP}"
install -d -m 700 "${TARGET_DIR}"

log_info "Dumping PostgreSQL database '${POSTGRES_DB}'..."
docker exec -i inatrace-postgres pg_dump -U "${POSTGRES_USER}" -Fc -d "${POSTGRES_DB}" \
  > "${TARGET_DIR}/inatrace.dump"

log_info "Dumping Keycloak database '${KC_DB_NAME}'..."
docker exec -i inatrace-postgres pg_dump -U "${POSTGRES_USER}" -Fc -d "${KC_DB_NAME}" \
  > "${TARGET_DIR}/keycloak.dump"

if [[ -d "${BASE_DIR}/file_storage" ]]; then
  tar -czf "${TARGET_DIR}/file_storage.tar.gz" -C "${BASE_DIR}" file_storage
fi
if [[ -d "${BASE_DIR}/documents" ]]; then
  tar -czf "${TARGET_DIR}/documents.tar.gz" -C "${BASE_DIR}" documents
fi

cp "${ENV_FILE}" "${TARGET_DIR}/env.backup"
chmod 600 "${TARGET_DIR}/env.backup"
if [[ -f "${BASE_DIR}/docker-compose.yml" ]]; then
  cp "${BASE_DIR}/docker-compose.yml" "${TARGET_DIR}/docker-compose.yml"
fi

(
  cd "${TARGET_DIR}"
  find . -maxdepth 1 -type f ! -name 'SHA256SUMS*' -exec sha256sum {} + > SHA256SUMS.tmp
  mv SHA256SUMS.tmp SHA256SUMS
)
chmod -R go-rwx "${TARGET_DIR}"

if [[ "${RETENTION_DAYS}" -gt 0 ]]; then
  find "${BACKUP_DIR}" -mindepth 1 -maxdepth 1 -type d -mtime +"${RETENTION_DAYS}" -exec rm -rf {} +
fi

log_ok "Backup completed successfully at ${TARGET_DIR}"
