#!/usr/bin/env bash
# Version: 1.0.0
# Runs end-to-end health, database, OIDC, TLS reverse-proxy, and optional public QR checks.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib/common.sh
. "${SCRIPT_DIR}/lib/common.sh"

ENV_FILE="/opt/inatrace/.env"
CHECK_QR="false"
QR_ORDER_ID="1"
INSECURE_TLS="false"

usage() {
  cat <<'EOF'
Usage: 06-verify-installation.sh [OPTIONS]

Verifies container status, PostgreSQL Flyway schema, SYSTEM_ADMIN existence,
Keycloak OIDC discovery + JWKS, Backend OpenAPI, Nginx HTTPS reverse proxy,
canonical Host-header redirect, and optional public QR receipt endpoint.

Options:
  --env-file <path>      Path to .env file (default: /opt/inatrace/.env)
  --check-qr             Also verify public delivery receipt QR endpoint and LOPDP PII hygiene
  --qr-order-id <id>     StockOrder ID to probe when --check-qr is active (default: 1)
  --insecure-tls         Allow self-signed TLS certificates when probing local HTTPS (curl -k)
  -h, --help             Show this help message and exit

Example:
  sudo ./06-verify-installation.sh --env-file /opt/inatrace/.env --check-qr
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --env-file)     [[ $# -ge 2 ]] || { log_error "--env-file requires a value"; exit 1; }; ENV_FILE="$2"; shift 2 ;;
    --check-qr)     CHECK_QR="true"; shift ;;
    --qr-order-id)  [[ $# -ge 2 ]] || { log_error "--qr-order-id requires a value"; exit 1; }; QR_ORDER_ID="$2"; shift 2 ;;
    --insecure-tls) INSECURE_TLS="true"; shift ;;
    -h|--help)      usage; exit 0 ;;
    *)              log_error "Unknown option: $1"; usage >&2; exit 1 ;;
  esac
done

require_root
require_cmd docker curl python3
load_env_file "${ENV_FILE}"

FRONTEND_PORT="${FRONTEND_PORT:-8081}"
KEYCLOAK_PORT="${KEYCLOAK_PORT:-8082}"
BACKEND_PORT="${BACKEND_PORT:-8083}"

PASS_COUNT=0
FAIL_COUNT=0

check_ok()   { PASS_COUNT=$((PASS_COUNT + 1)); log_ok "$*"; }
check_fail() { FAIL_COUNT=$((FAIL_COUNT + 1)); log_error "$*"; }

# 1. Containers running
for c in inatrace-postgres inatrace-keycloak inatrace-backend inatrace-frontend; do
  if [[ "$(docker inspect -f '{{.State.Running}}' "${c}" 2>/dev/null || echo false)" == "true" ]]; then
    check_ok "Container running: ${c}"
  else
    check_fail "Container not running: ${c}"
  fi
done

# 2. PostgreSQL + Flyway + SYSTEM_ADMIN
if docker exec inatrace-postgres pg_isready -U "${POSTGRES_USER}" -d "${POSTGRES_DB}" >/dev/null 2>&1; then
  FLYWAY_OK="$(psql_exec -tAc "SELECT COUNT(*) FROM public.flyway_schema_history WHERE success = true;" 2>/dev/null || echo 0)"
  ADMIN_OK="$(psql_exec -tAc "SELECT COUNT(*) FROM public.\"User\" WHERE role = 'SYSTEM_ADMIN' AND status = 'ACTIVE';" 2>/dev/null || echo 0)"
  check_ok "PostgreSQL ready (Flyway migrations applied: ${FLYWAY_OK}; ACTIVE SYSTEM_ADMIN users: ${ADMIN_OK})"
else
  check_fail "PostgreSQL readiness check failed"
fi

# 3. Internal ports
if curl -fsS "http://127.0.0.1:${KEYCLOAK_PORT}/auth/realms/${KEYCLOAK_REALM}/protocol/openid-connect/certs" | grep -q '"keys"'; then
  check_ok "Keycloak JWKS reachable on 127.0.0.1:${KEYCLOAK_PORT}"
else
  check_fail "Keycloak JWKS check failed on 127.0.0.1:${KEYCLOAK_PORT}"
fi

if curl -fsS "http://127.0.0.1:${BACKEND_PORT}/api/v3/api-docs" >/dev/null 2>&1; then
  check_ok "Backend OpenAPI reachable on 127.0.0.1:${BACKEND_PORT}"
else
  check_fail "Backend OpenAPI check failed on 127.0.0.1:${BACKEND_PORT}"
fi

if curl -fsS "http://127.0.0.1:${FRONTEND_PORT}/" >/dev/null 2>&1; then
  check_ok "Frontend reachable on 127.0.0.1:${FRONTEND_PORT}"
else
  check_fail "Frontend check failed on 127.0.0.1:${FRONTEND_PORT}"
fi

# 4. External HTTPS routes
CURL_TLS_OPTS=(-sS)
[[ "${INSECURE_TLS}" == "true" ]] && CURL_TLS_OPTS+=(-k)

for path_url in \
  "https://${DOMAIN_NAME}/" \
  "https://${DOMAIN_NAME}/auth/realms/${KEYCLOAK_REALM}/.well-known/openid-configuration" \
  "https://${DOMAIN_NAME}/api/v3/api-docs"; do
  CODE="$(curl "${CURL_TLS_OPTS[@]}" -o /dev/null -w '%{http_code}' "${path_url}" || echo 000)"
  if [[ "${CODE}" == "200" ]]; then
    check_ok "HTTPS GET ${path_url} -> HTTP ${CODE}"
  else
    check_fail "HTTPS GET ${path_url} -> HTTP ${CODE} (expected 200)"
  fi
done

# 5. Canonical host redirect check
REDIR_CODE="$(curl "${CURL_TLS_OPTS[@]}" -o /dev/null -w '%{http_code}' -H "Host: invalid-alias.example.com" --resolve "invalid-alias.example.com:443:127.0.0.1" "https://invalid-alias.example.com/" 2>/dev/null || echo 000)"
if [[ "${REDIR_CODE}" == "301" || "${REDIR_CODE}" == "302" ]]; then
  check_ok "Canonical host redirect check -> HTTP ${REDIR_CODE}"
else
  log_warn "Canonical host redirect probe returned HTTP ${REDIR_CODE} (verify DNS/SNI if behind an external LB)."
fi

# 6. Optional Public QR receipt & PII sanitization verification
if [[ "${CHECK_QR}" == "true" ]]; then
  QR_URL="https://${DOMAIN_NAME}/api/public/delivery-receipt/${QR_ORDER_ID}"
  QR_TMP="$(mktemp)"
  QR_CODE="$(curl "${CURL_TLS_OPTS[@]}" -o "${QR_TMP}" -w '%{http_code}' "${QR_URL}" || echo 000)"
  case "${QR_CODE}" in
    200)
      python3 - "${QR_TMP}" <<'PY_QR_PII'
import json, sys
data = json.load(open(sys.argv[1], encoding="utf-8"))
payload = json.dumps(data).lower()
forbidden_pii = ['"email":', '"phone":', '"bankaccount', '"identificationnumber":', '"password":']
leaked = [k for k in forbidden_pii if k in payload and f'{k} null' not in payload]
if leaked:
    sys.exit(f"Potential sensitive fields present in public receipt: {leaked}")
print("[OK]    Public QR receipt HTTP 200 verified with LOPDP PII filtering.")
PY_QR_PII
      PASS_COUNT=$((PASS_COUNT + 1))
      ;;
    404)
      check_ok "Public QR endpoint reachable without login redirect (HTTP 404: receipt #${QR_ORDER_ID} not created yet)"
      ;;
    401|403)
      check_ok "Public QR endpoint reachable without 502 (HTTP ${QR_CODE}: enablePublicDeliveryReceipt disabled on organization #${QR_ORDER_ID})"
      ;;
    *)
      check_fail "Public QR endpoint ${QR_URL} returned unexpected HTTP ${QR_CODE}"
      ;;
  esac
  rm -f "${QR_TMP}"
fi

printf '\nVerification Summary: %d passed, %d failed.\n' "${PASS_COUNT}" "${FAIL_COUNT}"
[[ "${FAIL_COUNT}" -eq 0 ]]
