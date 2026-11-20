#!/usr/bin/env bash
# Version: 1.0.0
# Starts the INATrace Docker Compose stack, bootstraps the Keycloak 26 realm
# (institutional theme, browser flow without Organization step, confidential backend
# client with manage-users role, public PKCE S256 frontend client), and waits for health.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib/common.sh
. "${SCRIPT_DIR}/lib/common.sh"

ENV_FILE="/opt/inatrace/.env"
COMPOSE_FILE=""
WAIT_SECONDS="180"

usage() {
  cat <<'EOF'
Usage: 04-deploy-and-bootstrap-keycloak.sh [OPTIONS]

Deploys PostgreSQL 16 + Keycloak 26 + Backend + Frontend via Docker Compose and
idempotently configures the institutional Keycloak realm and OIDC clients.

Options:
  --env-file <path>      Path to .env file (default: /opt/inatrace/.env)
  --compose-file <path>  Path to docker-compose.yml (default: <BASE_DIR>/docker-compose.yml)
  --wait-seconds <N>     Timeout waiting for Keycloak and Backend readiness (default: 180)
  -h, --help             Show this help message and exit

Example:
  sudo ./04-deploy-and-bootstrap-keycloak.sh --env-file /opt/inatrace/.env
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --env-file)     [[ $# -ge 2 ]] || { log_error "--env-file requires a value"; exit 1; }; ENV_FILE="$2"; shift 2 ;;
    --compose-file) [[ $# -ge 2 ]] || { log_error "--compose-file requires a value"; exit 1; }; COMPOSE_FILE="$2"; shift 2 ;;
    --wait-seconds) [[ $# -ge 2 ]] || { log_error "--wait-seconds requires a value"; exit 1; }; WAIT_SECONDS="$2"; shift 2 ;;
    -h|--help)      usage; exit 0 ;;
    *)              log_error "Unknown option: $1"; usage >&2; exit 1 ;;
  esac
done

require_root
require_cmd docker curl python3
load_env_file "${ENV_FILE}"

COMPOSE_FILE="${COMPOSE_FILE:-${BASE_DIR:-/opt/inatrace}/docker-compose.yml}"
[[ -f "${COMPOSE_FILE}" ]] || { log_error "Compose file not found: ${COMPOSE_FILE}"; exit 1; }

KEYCLOAK_PORT="${KEYCLOAK_PORT:-8082}"
BACKEND_PORT="${BACKEND_PORT:-8083}"
FRONTEND_PORT="${FRONTEND_PORT:-8081}"
KC_BASE="http://127.0.0.1:${KEYCLOAK_PORT}/auth"

log_info "Starting PostgreSQL 16 and Keycloak 26 services..."
compose_run "${ENV_FILE}" "${COMPOSE_FILE}" up -d inatrace-postgres inatrace-keycloak

log_info "Waiting for Keycloak Admin API at ${KC_BASE}..."
DEADLINE=$(( $(date +%s) + WAIT_SECONDS ))
KC_READY="false"
while [[ $(date +%s) -lt ${DEADLINE} ]]; do
  if curl -fsS "${KC_BASE}/realms/master/.well-known/openid-configuration" >/dev/null 2>&1; then
    KC_READY="true"
    break
  fi
  sleep 3
done
[[ "${KC_READY}" == "true" ]] || { log_error "Keycloak did not become ready within ${WAIT_SECONDS}s."; exit 1; }
log_ok "Keycloak is responsive."

log_info "Bootstrapping Keycloak realm '${KEYCLOAK_REALM}' and OIDC clients..."
python3 - <<'PY_KC_BOOTSTRAP'
import json
import os
import sys
import urllib.error
import urllib.parse
import urllib.request

kc_base = f"http://127.0.0.1:{os.environ.get('KEYCLOAK_PORT', '8082')}/auth"
kc_admin = os.environ["KEYCLOAK_ADMIN"]
kc_pass = os.environ["KEYCLOAK_ADMIN_PASSWORD"]
realm = os.environ["KEYCLOAK_REALM"]
be_client_id = os.environ["KEYCLOAK_CLIENT_ID"]
be_client_secret = os.environ["KEYCLOAK_CLIENT_SECRET"]
domain = os.environ["DOMAIN_NAME"]


def http_req(url, method="GET", data=None, headers=None, form=False):
    hdrs = dict(headers or {})
    body = None
    if data is not None:
        if form:
            body = urllib.parse.urlencode(data).encode("utf-8")
            hdrs.setdefault("Content-Type", "application/x-www-form-urlencoded")
        else:
            body = json.dumps(data).encode("utf-8")
            hdrs.setdefault("Content-Type", "application/json")
    req = urllib.request.Request(url, data=body, headers=hdrs, method=method)
    try:
        with urllib.request.urlopen(req, timeout=20) as resp:
            raw = resp.read().decode("utf-8")
            return resp.status, (json.loads(raw) if raw else None)
    except urllib.error.HTTPError as exc:
        raw = exc.read().decode("utf-8", errors="replace")
        return exc.code, raw


status, tok_resp = http_req(
    f"{kc_base}/realms/master/protocol/openid-connect/token",
    method="POST",
    data={
        "client_id": "admin-cli",
        "username": kc_admin,
        "password": kc_pass,
        "grant_type": "password",
    },
    form=True,
)
if status != 200 or not isinstance(tok_resp, dict):
    sys.exit(f"Failed to get Keycloak master admin token (HTTP {status}): {tok_resp}")

auth_hdr = {"Authorization": f"Bearer {tok_resp['access_token']}"}

# 1. Create or update realm
realm_payload = {
    "realm": realm,
    "enabled": True,
    "loginTheme": "inatrace",
    "resetPasswordAllowed": True,
    "loginWithEmailAllowed": True,
    "bruteForceProtected": True,
}
st, res = http_req(f"{kc_base}/admin/realms/{realm}", headers=auth_hdr)
if st == 404:
    st_c, res_c = http_req(f"{kc_base}/admin/realms", method="POST", data=realm_payload, headers=auth_hdr)
    if st_c not in (201, 204):
        sys.exit(f"Failed to create realm {realm} (HTTP {st_c}): {res_c}")
    print(f"[OK]    Created Keycloak realm '{realm}'.")
elif st == 200:
    st_u, res_u = http_req(f"{kc_base}/admin/realms/{realm}", method="PUT", data=realm_payload, headers=auth_hdr)
    if st_u not in (200, 204):
        sys.exit(f"Failed to update realm {realm} (HTTP {st_u}): {res_u}")
    print(f"[OK]    Realm '{realm}' already exists; updated theme/security settings.")
else:
    sys.exit(f"Unexpected HTTP {st} checking realm '{realm}': {res}")

# 2. Clone browser flow as inatrace-browser-flow and disable Keycloak 26 Organization step
_, flows = http_req(f"{kc_base}/admin/realms/{realm}/authentication/flows", headers=auth_hdr)
if not any(f.get("alias") == "inatrace-browser-flow" for f in (flows or [])):
    st_f, res_f = http_req(
        f"{kc_base}/admin/realms/{realm}/authentication/flows/browser/copy",
        method="POST",
        data={"newName": "inatrace-browser-flow"},
        headers=auth_hdr,
    )
    if st_f not in (201, 204):
        sys.exit(f"Failed to copy browser flow (HTTP {st_f}): {res_f}")

st_e, execs = http_req(
    f"{kc_base}/admin/realms/{realm}/authentication/flows/inatrace-browser-flow/executions",
    headers=auth_hdr,
)
if st_e != 200:
    sys.exit(f"Failed to list executions for inatrace-browser-flow (HTTP {st_e}): {execs}")

for ex in execs or []:
    if "Organization" in ex.get("displayName", "") or ex.get("providerId") == "organization":
        if ex.get("requirement") != "DISABLED":
            ex["requirement"] = "DISABLED"
            st_ex, res_ex = http_req(
                f"{kc_base}/admin/realms/{realm}/authentication/flows/inatrace-browser-flow/executions",
                method="PUT",
                data=ex,
                headers=auth_hdr,
            )
            if st_ex not in (200, 202, 204):
                sys.exit(f"Failed to disable Organization execution (HTTP {st_ex}): {res_ex}")

http_req(
    f"{kc_base}/admin/realms/{realm}",
    method="PUT",
    data={"realm": realm, "browserFlow": "inatrace-browser-flow"},
    headers=auth_hdr,
)
print("[OK]    Configured browserFlow 'inatrace-browser-flow' (Organization step disabled).")

# 3. Confidential backend client + realm-management manage-users role
be_payload = {
    "clientId": be_client_id,
    "enabled": True,
    "serviceAccountsEnabled": True,
    "publicClient": False,
    "secret": be_client_secret,
}
_, existing_be = http_req(
    f"{kc_base}/admin/realms/{realm}/clients?clientId={urllib.parse.quote(be_client_id)}",
    headers=auth_hdr,
)
if not existing_be:
    st_bc, res_bc = http_req(f"{kc_base}/admin/realms/{realm}/clients", method="POST", data=be_payload, headers=auth_hdr)
    if st_bc not in (201, 204):
        sys.exit(f"Failed to create client {be_client_id} (HTTP {st_bc}): {res_bc}")
    _, existing_be = http_req(
        f"{kc_base}/admin/realms/{realm}/clients?clientId={urllib.parse.quote(be_client_id)}",
        headers=auth_hdr,
    )
else:
    be_uuid_cur = existing_be[0]["id"]
    http_req(f"{kc_base}/admin/realms/{realm}/clients/{be_uuid_cur}", method="PUT", data=be_payload, headers=auth_hdr)

be_uuid = existing_be[0]["id"]
_, sa_user = http_req(f"{kc_base}/admin/realms/{realm}/clients/{be_uuid}/service-account-user", headers=auth_hdr)
_, rm_list = http_req(f"{kc_base}/admin/realms/{realm}/clients?clientId=realm-management", headers=auth_hdr)
rm_uuid = rm_list[0]["id"]
_, manage_users_role = http_req(f"{kc_base}/admin/realms/{realm}/clients/{rm_uuid}/roles/manage-users", headers=auth_hdr)
_, assigned = http_req(
    f"{kc_base}/admin/realms/{realm}/users/{sa_user['id']}/role-mappings/clients/{rm_uuid}",
    headers=auth_hdr,
)
if not any(r.get("name") == "manage-users" for r in (assigned or [])):
    st_rm, res_rm = http_req(
        f"{kc_base}/admin/realms/{realm}/users/{sa_user['id']}/role-mappings/clients/{rm_uuid}",
        method="POST",
        data=[manage_users_role],
        headers=auth_hdr,
    )
    if st_rm not in (200, 204):
        sys.exit(f"Failed to assign manage-users role (HTTP {st_rm}): {res_rm}")
print(f"[OK]    Backend confidential client '{be_client_id}' ready with manage-users role.")

# 4. Public PKCE S256 client for Angular frontend
fe_payload = {
    "clientId": "inatrace-frontend",
    "enabled": True,
    "publicClient": True,
    "standardFlowEnabled": True,
    "directAccessGrantsEnabled": False,
    "redirectUris": [f"https://{domain}/*"],
    "webOrigins": [f"https://{domain}"],
    "attributes": {"pkce.code.challenge.method": "S256"},
}
_, existing_fe = http_req(f"{kc_base}/admin/realms/{realm}/clients?clientId=inatrace-frontend", headers=auth_hdr)
if not existing_fe:
    st_fe, res_fe = http_req(f"{kc_base}/admin/realms/{realm}/clients", method="POST", data=fe_payload, headers=auth_hdr)
    if st_fe not in (201, 204):
        sys.exit(f"Failed to create client inatrace-frontend (HTTP {st_fe}): {res_fe}")
else:
    http_req(
        f"{kc_base}/admin/realms/{realm}/clients/{existing_fe[0]['id']}",
        method="PUT",
        data=fe_payload,
        headers=auth_hdr,
    )
print(f"[OK]    Frontend PKCE S256 client 'inatrace-frontend' configured for https://{domain}.")

st_jwks, jwks = http_req(f"{kc_base}/realms/{realm}/protocol/openid-connect/certs")
if st_jwks != 200 or not isinstance(jwks, dict) or not jwks.get("keys"):
    sys.exit(f"JWKS endpoint check failed (HTTP {st_jwks})")
print("[OK]    Keycloak JWKS keys verified.")
PY_KC_BOOTSTRAP

log_info "Starting full INATrace stack (Backend Spring Boot + Angular Frontend)..."
compose_run "${ENV_FILE}" "${COMPOSE_FILE}" up -d

log_info "Waiting for Backend OpenAPI (/api/v3/api-docs) and Frontend readiness..."
DEADLINE=$(( $(date +%s) + WAIT_SECONDS ))
BE_READY="false"
while [[ $(date +%s) -lt ${DEADLINE} ]]; do
  if curl -fsS "http://127.0.0.1:${BACKEND_PORT}/api/v3/api-docs" >/dev/null 2>&1 \
     && curl -fsS "http://127.0.0.1:${FRONTEND_PORT}/" >/dev/null 2>&1; then
    BE_READY="true"
    break
  fi
  sleep 4
done
[[ "${BE_READY}" == "true" ]] || { log_error "Backend or Frontend failed to become ready within ${WAIT_SECONDS}s."; exit 1; }

log_ok "Full INATrace stack deployed and healthy."
