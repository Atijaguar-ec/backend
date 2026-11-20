#!/usr/bin/env bash
# Version: 1.0.0
# Applies a standardized Cacao operational profile to a company's JSONB configuration,
# optionally activates the company, and links its Cacao value chain.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib/common.sh
. "${SCRIPT_DIR}/lib/common.sh"

ENV_FILE="/opt/inatrace/.env"
COMPANY_ID=""
PROFILE=""
PROFILE_FILE=""
VALUECHAIN_ID=""
ACTIVATE="true"
TOKEN=""

usage() {
  cat <<'EOF'
Usage: 07-configure-cacao-company.sh [OPTIONS]

Applies a Cacao multi-tenant profile to company.configuration, activates the
company (unless --no-activate), and optionally links a value chain.

Options:
  --company-id <id>       Target INATrace company ID (required)
  --profile <name>        Built-in profile: seco-certificado | baba-organico | privado
  --profile-file <path>   Custom JSON configuration file (alternative to --profile)
  --valuechain-id <id>    Optional valuechain_id to ensure in public.companyvaluechain
  --token <jwt>           Optional Bearer JWT to update/activate via Backend REST API
  --no-activate           Skip setting company status to ACTIVE
  --env-file <path>       Path to .env file (default: /opt/inatrace/.env)
  -h, --help              Show this help message and exit

Examples:
  sudo ./07-configure-cacao-company.sh --company-id 1 --profile baba-organico --valuechain-id 1
  sudo ./07-configure-cacao-company.sh --company-id 2 --profile seco-certificado
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --company-id)    [[ $# -ge 2 ]] || { log_error "--company-id requires a value"; exit 1; }; COMPANY_ID="$2"; shift 2 ;;
    --profile)       [[ $# -ge 2 ]] || { log_error "--profile requires a value"; exit 1; }; PROFILE="$2"; shift 2 ;;
    --profile-file)  [[ $# -ge 2 ]] || { log_error "--profile-file requires a value"; exit 1; }; PROFILE_FILE="$2"; shift 2 ;;
    --valuechain-id) [[ $# -ge 2 ]] || { log_error "--valuechain-id requires a value"; exit 1; }; VALUECHAIN_ID="$2"; shift 2 ;;
    --token)         [[ $# -ge 2 ]] || { log_error "--token requires a value"; exit 1; }; TOKEN="$2"; shift 2 ;;
    --no-activate)   ACTIVATE="false"; shift ;;
    --env-file)      [[ $# -ge 2 ]] || { log_error "--env-file requires a value"; exit 1; }; ENV_FILE="$2"; shift 2 ;;
    -h|--help)       usage; exit 0 ;;
    *)               log_error "Unknown option: $1"; usage >&2; exit 1 ;;
  esac
done

[[ "${COMPANY_ID}" =~ ^[0-9]+$ ]] || { log_error "--company-id must be a positive integer."; exit 1; }

if [[ -n "${PROFILE}" && -n "${PROFILE_FILE}" ]]; then
  log_error "Specify either --profile or --profile-file, not both."
  exit 1
fi
if [[ -z "${PROFILE}" && -z "${PROFILE_FILE}" ]]; then
  log_error "One of --profile or --profile-file is required."
  exit 1
fi

if [[ -n "${PROFILE}" ]]; then
  PROFILE_FILE="${SCRIPT_DIR}/templates/profiles/${PROFILE}.json"
fi
[[ -f "${PROFILE_FILE}" ]] || { log_error "Profile JSON not found: ${PROFILE_FILE}"; exit 1; }

require_cmd python3
PROFILE_JSON="$(python3 -c 'import json,sys; print(json.dumps(json.load(open(sys.argv[1], encoding="utf-8"))))' "${PROFILE_FILE}")"

load_env_file "${ENV_FILE}"
BACKEND_PORT="${BACKEND_PORT:-8083}"

if [[ -n "${TOKEN}" ]]; then
  log_info "Updating company #${COMPANY_ID} via Backend REST API..."
  python3 - "${BACKEND_PORT}" "${COMPANY_ID}" "${PROFILE_JSON}" "${TOKEN}" "${ACTIVATE}" <<'PY_API_CFG'
import json
import sys
import urllib.error
import urllib.request

port, cid, prof_raw, token, activate = sys.argv[1:]
profile = json.loads(prof_raw)
hdrs = {"Authorization": f"Bearer {token}", "Content-Type": "application/json"}


def req_json(url, method="GET", body=None):
    data = json.dumps(body).encode("utf-8") if body is not None else None
    req = urllib.request.Request(url, data=data, headers=hdrs, method=method)
    try:
        with urllib.request.urlopen(req, timeout=20) as resp:
            raw = resp.read().decode("utf-8")
            return resp.status, (json.loads(raw) if raw else {})
    except urllib.error.HTTPError as exc:
        sys.exit(f"REST call failed {method} {url} (HTTP {exc.code}): {exc.read().decode('utf-8', errors='replace')}")


_, prof_resp = req_json(f"http://127.0.0.1:{port}/api/company/profile/{cid}")
company_data = prof_resp.get("data") or prof_resp
cfg = dict(company_data.get("configuration") or {})
cfg.update(profile)
company_data["configuration"] = cfg
req_json(f"http://127.0.0.1:{port}/api/company/profile", method="PUT", body=company_data)

if activate == "true":
    req_json(
        f"http://127.0.0.1:{port}/api/company/execute/ACTIVATE_COMPANY",
        method="POST",
        body={"companyId": int(cid)},
    )
print(f"[OK]    Company #{cid} configuration updated via REST API.")
PY_API_CFG
else
  require_root
  require_cmd docker
  EXISTS="$(psql_exec -tA -v cid="${COMPANY_ID}" <<'SQL_CHECK' | tr -d '[:space:]'
SELECT COUNT(*) FROM public.company WHERE id = :'cid'::bigint;
SQL_CHECK
)"
  [[ "${EXISTS}" == "1" ]] || { log_error "Company ID ${COMPANY_ID} does not exist in public.company."; exit 1; }

  log_info "Merging configuration profile into company #${COMPANY_ID}..."
  psql_exec -v cid="${COMPANY_ID}" -v cfg="${PROFILE_JSON}" -v act="${ACTIVATE}" <<'SQL_CFG'
BEGIN;
UPDATE public.company
SET configuration = COALESCE(configuration, '{}'::jsonb) || :'cfg'::jsonb
WHERE id = :'cid'::bigint;

UPDATE public.company
SET status = 'ACTIVE'
WHERE id = :'cid'::bigint AND :'act' = 'true';
COMMIT;
SQL_CFG
fi

if [[ -n "${VALUECHAIN_ID}" ]]; then
  [[ "${VALUECHAIN_ID}" =~ ^[0-9]+$ ]] || { log_error "--valuechain-id must be numeric."; exit 1; }
  require_root
  require_cmd docker
  psql_exec -v cid="${COMPANY_ID}" -v vcid="${VALUECHAIN_ID}" <<'SQL_VC'
INSERT INTO public.companyvaluechain (company_id, valuechain_id)
SELECT :'cid'::bigint, :'vcid'::bigint
WHERE NOT EXISTS (
  SELECT 1 FROM public.companyvaluechain
  WHERE company_id = :'cid'::bigint AND valuechain_id = :'vcid'::bigint
);
SELECT setval(pg_get_serial_sequence('public.companyvaluechain', 'id'), COALESCE((SELECT MAX(id) FROM public.companyvaluechain), 1));
SQL_VC
  log_ok "Ensured value chain #${VALUECHAIN_ID} linked to company #${COMPANY_ID}."
fi

if command -v docker >/dev/null 2>&1 && [[ "$(id -u)" -eq 0 ]]; then
  psql_exec -v cid="${COMPANY_ID}" <<'SQL_SHOW'
SELECT id, name, status, configuration FROM public.company WHERE id = :'cid'::bigint;
SQL_SHOW
fi
log_ok "Cacao company #${COMPANY_ID} configuration completed."
