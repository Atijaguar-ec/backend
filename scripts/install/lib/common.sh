#!/usr/bin/env bash
# Shared helper library for INATrace backend installation scripts (v1.0.0)

log_info()  { printf '[INFO]  %s\n' "$*"; }
log_ok()    { printf '[OK]    %s\n' "$*"; }
log_warn()  { printf '[WARN]  %s\n' "$*"; }
log_error() { printf '[ERROR] %s\n' "$*" >&2; }

require_cmd() {
  local cmd
  for cmd in "$@"; do
    command -v "${cmd}" >/dev/null 2>&1 || {
      log_error "Required command not found: ${cmd}"
      exit 1
    }
  done
}

require_root() {
  if [[ "$(id -u)" -ne 0 ]]; then
    log_error "This operation must be executed as root (use sudo)."
    exit 1
  fi
}

require_x86_64() {
  local arch
  arch="$(uname -m)"
  if [[ "${arch}" != "x86_64" && "${arch}" != "amd64" ]]; then
    log_error "INATrace production containers target Linux x86_64/amd64 (detected: ${arch})."
    exit 1
  fi
}

load_env_file() {
  local env_file="$1"
  if [[ ! -f "${env_file}" ]]; then
    log_error "Environment file not found: ${env_file}"
    exit 1
  fi
  set -a
  # shellcheck disable=SC1090
  . "${env_file}"
  set +a
}

compose_run() {
  local env_file="$1"
  local compose_file="$2"
  shift 2
  if docker compose version >/dev/null 2>&1; then
    docker compose --env-file "${env_file}" -f "${compose_file}" "$@"
  elif command -v docker-compose >/dev/null 2>&1; then
    docker-compose --env-file "${env_file}" -f "${compose_file}" "$@"
  else
    log_error "Docker Compose plugin ('docker compose') is required."
    exit 1
  fi
}

psql_exec() {
  local pg_user="${POSTGRES_USER:-inatrace_admin}"
  local pg_db="${POSTGRES_DB:-inatrace}"
  docker exec -i inatrace-postgres psql -v ON_ERROR_STOP=1 -U "${pg_user}" -d "${pg_db}" "$@"
}
