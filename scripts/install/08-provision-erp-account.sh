#!/usr/bin/env bash
# Version: 1.0.0
# Provisions or updates a technical ERP/weighing-scale service account in INATrace
# (public."User" + public.companyuser) for Keycloak JWT service-account auth.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib/common.sh
. "${SCRIPT_DIR}/lib/common.sh"

ENV_FILE="/opt/inatrace/.env"
SA_EMAIL=""
COMPANY_ID=""
COMPANY_ROLE="COMPANY_ADMIN"
SA_NAME="Servicio"
SA_SURNAME="ERP Contable"
SA_LANG="ES"
KC_CLIENT_ID=""
KC_CLIENT_SECRET=""

usage() {
  cat <<'EOF'
Usage: 08-provision-erp-account.sh [OPTIONS]

Idempotently registers or updates a technical ERP / scale service-account user
in INATrace PostgreSQL (public."User" and public.companyuser) for OAuth2 Client
Credentials integration, and optionally creates its confidential client in Keycloak.

Options:
  --email <identifier>        JWT preferred_username or email claim (required, e.g. service-account-erp-contable)
  --company-id <id>           Target company ID in public.company (required)
  --role <role>               Company role: COMPANY_ADMIN | COMPANY_USER | MANAGER | ACCOUNTANT (default: COMPANY_ADMIN)
  --name <name>               Display first name (default: Servicio)
  --surname <surname>         Display last name (default: ERP Contable)
  --lang <ES|EN|DE|RW>        Language code (default: ES)
  --kc-client-id <clientId>   Optional confidential Keycloak clientId to provision alongside DB record
  --kc-client-secret <sec>    Optional client secret when --kc-client-id is passed (auto-generated if omitted)
  --env-file <path>           Path to .env file (default: /opt/inatrace/.env)
  -h, --help                  Show this help message and exit

Example:
  sudo ./08-provision-erp-account.sh --email service-account-erp-contable --company-id 1
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --email)            [[ $# -ge 2 ]] || { log_error "--email requires a value"; exit 1; }; SA_EMAIL="$2"; shift 2 ;;
    --company-id)       [[ $# -ge 2 ]] || { log_error "--company-id requires a value"; exit 1; }; COMPANY_ID="$2"; shift 2 ;;
    --role)             [[ $# -ge 2 ]] || { log_error "--role requires a value"; exit 1; }; COMPANY_ROLE="$2"; shift 2 ;;
    --name)             [[ $# -ge 2 ]] || { log_error "--name requires a value"; exit 1; }; SA_NAME="$2"; shift 2 ;;
    --surname)          [[ $# -ge 2 ]] || { log_error "--surname requires a value"; exit 1; }; SA_SURNAME="$2"; shift 2 ;;
    --lang)             [[ $# -ge 2 ]] || { log_error "--lang requires a value"; exit 1; }; SA_LANG="$2"; shift 2 ;;
    --kc-client-id)     [[ $# -ge 2 ]] || { log_error "--kc-client-id requires a value"; exit 1; }; KC_CLIENT_ID="$2"; shift 2 ;;
    --kc-client-secret) [[ $# -ge 2 ]] || { log_error "--kc-client-secret requires a value"; exit 1; }; KC_CLIENT_SECRET="$2"; shift 2 ;;
    --env-file)         [[ $# -ge 2 ]] || { log_error "--env-file requires a value"; exit 1; }; ENV_FILE="$2"; shift 2 ;;
    -h|--help)          usage; exit 0 ;;
    *)                  log_error "Unknown option: $1"; usage >&2; exit 1 ;;
  esac
done

require_root
require_cmd docker
[[ -n "${SA_EMAIL}" ]] || { log_error "--email is required."; exit 1; }
[[ "${COMPANY_ID}" =~ ^[0-9]+$ ]] || { log_error "--company-id must be a positive integer."; exit 1; }
case "${COMPANY_ROLE}" in
  COMPANY_ADMIN|COMPANY_USER|MANAGER|ACCOUNTANT) ;;
  *) log_error "Invalid --role '${COMPANY_ROLE}'."; exit 1 ;;
esac
case "${SA_LANG}" in
  ES|EN|DE|RW) ;;
  *) log_error "Invalid --lang '${SA_LANG}'."; exit 1 ;;
esac

load_env_file "${ENV_FILE}"

EXISTS="$(psql_exec -tA -v cid="${COMPANY_ID}" <<'SQL_CHECK' | tr -d '[:space:]'
SELECT COUNT(*) FROM public.company WHERE id = :'cid'::bigint;
SQL_CHECK
)"
[[ "${EXISTS}" == "1" ]] || { log_error "Company ID ${COMPANY_ID} not found in public.company."; exit 1; }

log_info "Provisioning service account '${SA_EMAIL}' for company #${COMPANY_ID} (${COMPANY_ROLE})..."
psql_exec \
  -v email="${SA_EMAIL}" \
  -v name="${SA_NAME}" \
  -v surname="${SA_SURNAME}" \
  -v lang="${SA_LANG}" \
  -v cid="${COMPANY_ID}" \
  -v crole="${COMPANY_ROLE}" <<'SQL_ERP'
BEGIN;

INSERT INTO public."User" (creationtimestamp, updatetimestamp, email, entityversion, language, name, role, status, surname)
SELECT NOW(), NOW(), :'email', 0, :'lang', :'name', 'USER', 'ACTIVE', :'surname'
WHERE NOT EXISTS (SELECT 1 FROM public."User" WHERE email = :'email');

UPDATE public."User"
SET status = 'ACTIVE',
    updatetimestamp = NOW()
WHERE email = :'email';

INSERT INTO public.companyuser (entityversion, role, company_id, user_id)
SELECT 0, :'crole', :'cid'::bigint, u.id
FROM public."User" u
WHERE u.email = :'email'
  AND NOT EXISTS (
    SELECT 1 FROM public.companyuser cu
    WHERE cu.company_id = :'cid'::bigint AND cu.user_id = u.id
  );

UPDATE public.companyuser cu
SET role = :'crole'
FROM public."User" u
WHERE cu.user_id = u.id
  AND u.email = :'email'
  AND cu.company_id = :'cid'::bigint;

SELECT setval(pg_get_serial_sequence('public."User"', 'id'), COALESCE((SELECT MAX(id) FROM public."User"), 1));
SELECT setval(pg_get_serial_sequence('public.companyuser', 'id'), COALESCE((SELECT MAX(id) FROM public.companyuser), 1));

COMMIT;
SQL_ERP

if [[ -n "${KC_CLIENT_ID}" ]]; then
  require_cmd python3 openssl
  if [[ -z "${KC_CLIENT_SECRET}" ]]; then
    KC_CLIENT_SECRET="$(openssl rand -hex 24)"
  fi
  log_info "Ensuring Keycloak confidential client '${KC_CLIENT_ID}' in realm '${KEYCLOAK_REALM}'..."
  KC_CLIENT_ID="${KC_CLIENT_ID}" KC_CLIENT_SECRET="${KC_CLIENT_SECRET}" python3 - <<'PY_KC_SA'
import json, os, sys, urllib.error, urllib.parse, urllib.request

kc_base = f"http://127.0.0.1:{os.environ.get('KEYCLOAK_PORT', '8082')}/auth"
realm = os.environ["KEYCLOAK_REALM"]
cid = os.environ["KC_CLIENT_ID"]
csec = os.environ["KC_CLIENT_SECRET"]


def req(url, method="GET", data=None, headers=None, form=False):
    hdrs = dict(headers or {})
    body = None
    if data is not None:
        if form:
            body = urllib.parse.urlencode(data).encode("utf-8")
            hdrs.setdefault("Content-Type", "application/x-www-form-urlencoded")
        else:
            body = json.dumps(data).encode("utf-8")
            hdrs.setdefault("Content-Type", "application/json")
    r = urllib.request.Request(url, data=body, headers=hdrs, method=method)
    try:
        with urllib.request.urlopen(r, timeout=20) as resp:
            raw = resp.read().decode("utf-8")
            return resp.status, (json.loads(raw) if raw else None)
    except urllib.error.HTTPError as exc:
        return exc.code, exc.read().decode("utf-8", errors="replace")


st, tok = req(
    f"{kc_base}/realms/master/protocol/openid-connect/token",
    method="POST",
    data={
        "client_id": "admin-cli",
        "username": os.environ["KEYCLOAK_ADMIN"],
        "password": os.environ["KEYCLOAK_ADMIN_PASSWORD"],
        "grant_type": "password",
    },
    form=True,
)
if st != 200:
    sys.exit(f"Keycloak admin login failed (HTTP {st}): {tok}")
hdr = {"Authorization": f"Bearer {tok['access_token']}"}
payload = {
    "clientId": cid,
    "enabled": True,
    "serviceAccountsEnabled": True,
    "publicClient": False,
    "directAccessGrantsEnabled": False,
    "secret": csec,
}
_, existing = req(f"{kc_base}/admin/realms/{realm}/clients?clientId={urllib.parse.quote(cid)}", headers=hdr)
if not existing:
    sc, rc = req(f"{kc_base}/admin/realms/{realm}/clients", method="POST", data=payload, headers=hdr)
    if sc not in (201, 204):
        sys.exit(f"Failed creating Keycloak client {cid} (HTTP {sc}): {rc}")
else:
    su, ru = req(f"{kc_base}/admin/realms/{realm}/clients/{existing[0]['id']}", method="PUT", data=payload, headers=hdr)
    if su not in (200, 204):
        sys.exit(f"Failed updating Keycloak client {cid} (HTTP {su}): {ru}")
print(f"[OK]    Keycloak service-account client '{cid}' ready (JWT preferred_username: service-account-{cid}).")
PY_KC_SA
  log_ok "Keycloak secret for '${KC_CLIENT_ID}': store securely (pass --kc-client-secret to set an explicit secret)."
fi

psql_exec -v email="${SA_EMAIL}" -v cid="${COMPANY_ID}" <<'SQL_VERIFY'
SELECT u.id AS user_id, u.email, u.status, cu.company_id, cu.role
FROM public."User" u
JOIN public.companyuser cu ON cu.user_id = u.id
WHERE u.email = :'email' AND cu.company_id = :'cid'::bigint;
SQL_VERIFY
log_ok "ERP service account '${SA_EMAIL}' linked to company #${COMPANY_ID}."
