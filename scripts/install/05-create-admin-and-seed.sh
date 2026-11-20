#!/usr/bin/env bash
# Version: 1.0.0
# Registers the initial SYSTEM_ADMIN user across INATrace and Keycloak and loads the
# idempotent base Cacao value-chain catalog seed into PostgreSQL.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib/common.sh
. "${SCRIPT_DIR}/lib/common.sh"

ENV_FILE="/opt/inatrace/.env"
ADMIN_EMAIL=""
ADMIN_PASSWORD="${INATRACE_ADMIN_PASSWORD:-}"
ADMIN_NAME="Admin"
ADMIN_SURNAME="Sistema"
ADMIN_LANG="ES"
COUNTRY_CODE="EC"
COUNTRY_NAME="Ecuador"
CURRENCY_CODE="USD"
CURRENCY_LABEL="US Dollar"
COUNTRY_LAT="-1.8312"
COUNTRY_LNG="-78.1834"
SKIP_SEED="false"

usage() {
  cat <<'EOF'
Usage: 05-create-admin-and-seed.sh [OPTIONS]

Creates and activates the initial SYSTEM_ADMIN account in both INATrace PostgreSQL
and Keycloak, then applies the idempotent Cacao base catalog seed.

Options:
  --email <email>          Administrator email / username (required)
  --password <password>    Administrator password meeting complexity policy (required)
  --name <name>            First name (default: Admin)
  --surname <surname>      Last name (default: Sistema)
  --lang <ES|EN|DE|RW>     UI language (default: ES)
  --country-code <iso>     Default ISO country code (default: EC)
  --country-name <name>    Default country name (default: Ecuador)
  --currency-code <code>   Default ISO currency code (default: USD)
  --currency-label <lbl>   Default currency label (default: US Dollar)
  --lat <latitude>         Country latitude (default: -1.8312)
  --lng <longitude>        Country longitude (default: -78.1834)
  --skip-seed              Only create/promote admin user without loading catalog seed
  --env-file <path>        Path to .env file (default: /opt/inatrace/.env)
  -h, --help               Show this help message and exit

Example:
  sudo ./05-create-admin-and-seed.sh \
    --email admin@mi-cooperativa.org \
    --password 'StrongPass!2026' \
    --name Maria --surname Mendoza --country-code EC
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --email)          [[ $# -ge 2 ]] || { log_error "--email requires a value"; exit 1; }; ADMIN_EMAIL="$2"; shift 2 ;;
    --password)       [[ $# -ge 2 ]] || { log_error "--password requires a value"; exit 1; }; ADMIN_PASSWORD="$2"; shift 2 ;;
    --name)           [[ $# -ge 2 ]] || { log_error "--name requires a value"; exit 1; }; ADMIN_NAME="$2"; shift 2 ;;
    --surname)        [[ $# -ge 2 ]] || { log_error "--surname requires a value"; exit 1; }; ADMIN_SURNAME="$2"; shift 2 ;;
    --lang)           [[ $# -ge 2 ]] || { log_error "--lang requires a value"; exit 1; }; ADMIN_LANG="$2"; shift 2 ;;
    --country-code)   [[ $# -ge 2 ]] || { log_error "--country-code requires a value"; exit 1; }; COUNTRY_CODE="$2"; shift 2 ;;
    --country-name)   [[ $# -ge 2 ]] || { log_error "--country-name requires a value"; exit 1; }; COUNTRY_NAME="$2"; shift 2 ;;
    --currency-code)  [[ $# -ge 2 ]] || { log_error "--currency-code requires a value"; exit 1; }; CURRENCY_CODE="$2"; shift 2 ;;
    --currency-label) [[ $# -ge 2 ]] || { log_error "--currency-label requires a value"; exit 1; }; CURRENCY_LABEL="$2"; shift 2 ;;
    --lat)            [[ $# -ge 2 ]] || { log_error "--lat requires a value"; exit 1; }; COUNTRY_LAT="$2"; shift 2 ;;
    --lng)            [[ $# -ge 2 ]] || { log_error "--lng requires a value"; exit 1; }; COUNTRY_LNG="$2"; shift 2 ;;
    --skip-seed)      SKIP_SEED="true"; shift ;;
    --env-file)       [[ $# -ge 2 ]] || { log_error "--env-file requires a value"; exit 1; }; ENV_FILE="$2"; shift 2 ;;
    -h|--help)        usage; exit 0 ;;
    *)                log_error "Unknown option: $1"; usage >&2; exit 1 ;;
  esac
done

require_root
require_cmd docker python3
[[ -n "${ADMIN_EMAIL}" ]] || {
  log_error "--email is required."
  exit 1
}
if [[ -z "${ADMIN_PASSWORD}" ]]; then
  if [[ -t 0 ]]; then
    read -r -s -p "Enter initial SYSTEM_ADMIN password (min 8 chars, upper, lower, digit, symbol): " ADMIN_PASSWORD
    printf '\n'
  fi
fi
[[ -n "${ADMIN_PASSWORD}" ]] || {
  log_error "Administrator password required via INATRACE_ADMIN_PASSWORD env var, interactive prompt, or --password."
  exit 1
}
case "${ADMIN_LANG}" in
  ES|EN|DE|RW) ;;
  *) log_error "Unsupported --lang '${ADMIN_LANG}' (allowed: ES, EN, DE, RW)"; exit 1 ;;
esac

load_env_file "${ENV_FILE}"
BACKEND_PORT="${BACKEND_PORT:-8083}"
KEYCLOAK_PORT="${KEYCLOAK_PORT:-8082}"

USER_COUNT="$(psql_exec -tA -v email="${ADMIN_EMAIL}" <<'SQL_CHECK' | tr -d '[:space:]'
SELECT COUNT(*) FROM public."User" WHERE lower(email) = lower(:'email');
SQL_CHECK
)"
if [[ "${USER_COUNT}" == "0" ]]; then
  log_info "Registering initial administrator '${ADMIN_EMAIL}' via Backend API..."
  python3 - "${BACKEND_PORT}" "${ADMIN_EMAIL}" "${ADMIN_PASSWORD}" "${ADMIN_NAME}" "${ADMIN_SURNAME}" "${ADMIN_LANG}" <<'PY_REG'
import json
import sys
import urllib.error
import urllib.request

port, email, password, name, surname, lang = sys.argv[1:]
payload = json.dumps({
    "email": email,
    "password": password,
    "name": name,
    "surname": surname,
    "language": lang,
}).encode("utf-8")

req = urllib.request.Request(
    f"http://127.0.0.1:{port}/api/user/register",
    data=payload,
    headers={"Content-Type": "application/json"},
    method="POST",
)
try:
    with urllib.request.urlopen(req, timeout=20) as resp:
        if resp.status not in (200, 201):
            sys.exit(f"Registration failed with HTTP {resp.status}")
except urllib.error.HTTPError as exc:
    body = exc.read().decode("utf-8", errors="replace")
    sys.exit(f"Registration failed (HTTP {exc.code}): {body}")
print("[OK]    User registered in INATrace.")
PY_REG
else
  log_warn "User '${ADMIN_EMAIL}' already exists in public.\"User\"; skipping /api/user/register."
fi

log_info "Activating user and granting SYSTEM_ADMIN role in PostgreSQL..."
psql_exec -v email="${ADMIN_EMAIL}" <<'SQL_ADMIN'
UPDATE public."User"
SET status = 'ACTIVE',
    role = 'SYSTEM_ADMIN',
    updatetimestamp = NOW()
WHERE lower(email) = lower(:'email');
SQL_ADMIN
log_ok "PostgreSQL SYSTEM_ADMIN role verified for '${ADMIN_EMAIL}'."

log_info "Synchronizing verified permanent credentials in Keycloak '${KEYCLOAK_REALM}'..."
ADMIN_EMAIL="${ADMIN_EMAIL}" ADMIN_PASSWORD="${ADMIN_PASSWORD}" \
ADMIN_NAME="${ADMIN_NAME}" ADMIN_SURNAME="${ADMIN_SURNAME}" \
python3 - <<'PY_KC_USER'
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
email = os.environ["ADMIN_EMAIL"]
password = os.environ["ADMIN_PASSWORD"]
first_name = os.environ["ADMIN_NAME"]
last_name = os.environ["ADMIN_SURNAME"]


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


st, tok = http_req(
    f"{kc_base}/realms/master/protocol/openid-connect/token",
    method="POST",
    data={"client_id": "admin-cli", "username": kc_admin, "password": kc_pass, "grant_type": "password"},
    form=True,
)
if st != 200:
    sys.exit(f"Keycloak admin login failed (HTTP {st}): {tok}")
hdr = {"Authorization": f"Bearer {tok['access_token']}"}

_, users = http_req(
    f"{kc_base}/admin/realms/{realm}/users?username={urllib.parse.quote(email)}&exact=true",
    headers=hdr,
)
user_payload = {
    "username": email,
    "email": email,
    "firstName": first_name,
    "lastName": last_name,
    "enabled": True,
    "emailVerified": True,
    "requiredActions": [],
}
if not users:
    st_c, res_c = http_req(f"{kc_base}/admin/realms/{realm}/users", method="POST", data=user_payload, headers=hdr)
    if st_c not in (201, 204):
        sys.exit(f"Failed creating Keycloak user (HTTP {st_c}): {res_c}")
    _, users = http_req(
        f"{kc_base}/admin/realms/{realm}/users?username={urllib.parse.quote(email)}&exact=true",
        headers=hdr,
    )

uid = users[0]["id"]
st_u, res_u = http_req(f"{kc_base}/admin/realms/{realm}/users/{uid}", method="PUT", data=user_payload, headers=hdr)
if st_u not in (200, 204):
    sys.exit(f"Failed updating Keycloak user (HTTP {st_u}): {res_u}")

st_p, res_p = http_req(
    f"{kc_base}/admin/realms/{realm}/users/{uid}/reset-password",
    method="PUT",
    data={"type": "password", "value": password, "temporary": False},
    headers=hdr,
)
if st_p not in (200, 204):
    sys.exit(f"Failed setting Keycloak user password (HTTP {st_p}): {res_p}")
print(f"[OK]    Keycloak user '{email}' verified with permanent credentials.")
PY_KC_USER

if [[ "${SKIP_SEED}" == "false" ]]; then
  log_info "Applying idempotent Cacao base catalog seed into PostgreSQL..."
  psql_exec \
    -v country_code="${COUNTRY_CODE}" \
    -v country_name="${COUNTRY_NAME}" \
    -v country_lat="${COUNTRY_LAT}" \
    -v country_lng="${COUNTRY_LNG}" \
    -v currency_code="${CURRENCY_CODE}" \
    -v currency_label="${CURRENCY_LABEL}" \
    -v admin_email="${ADMIN_EMAIL}" <<'SQL_SEED'
BEGIN;

INSERT INTO public.country (code, name, latitude, longitude)
SELECT :'country_code', :'country_name', :'country_lat'::double precision, :'country_lng'::double precision
WHERE NOT EXISTS (SELECT 1 FROM public.country WHERE code = :'country_code');

INSERT INTO public.currencytype (code, label, enabled)
SELECT :'currency_code', :'currency_label', true
WHERE NOT EXISTS (SELECT 1 FROM public.currencytype WHERE code = :'currency_code');

INSERT INTO public.measureunittype (code, label, weight, underlyingmeasurementunittype_id)
SELECT 'PESOLIBRA', 'Libra (lb)', 0.45, NULL
WHERE NOT EXISTS (SELECT 1 FROM public.measureunittype WHERE code = 'PESOLIBRA');

INSERT INTO public.measureunittype (code, label, weight, underlyingmeasurementunittype_id)
SELECT 'QUINTALES', 'Quintales (qq)', 45.36,
       (SELECT id FROM public.measureunittype WHERE code = 'PESOLIBRA' ORDER BY id LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM public.measureunittype WHERE code = 'QUINTALES');

INSERT INTO public.producttype (creationtimestamp, updatetimestamp, code, description, entityversion, name)
SELECT NOW(), NOW(), 'CACAO', 'Cadena de valor del cacao', 0, 'Cacao'
WHERE NOT EXISTS (SELECT 1 FROM public.producttype WHERE code = 'CACAO');

INSERT INTO public.producttypetranslation (language, description, entityversion, name, producttype_id)
SELECT 'ES', 'Cadena de valor del cacao', 0, 'Cacao',
       (SELECT id FROM public.producttype WHERE code = 'CACAO' ORDER BY id LIMIT 1)
WHERE NOT EXISTS (
  SELECT 1 FROM public.producttypetranslation ptt
  JOIN public.producttype pt ON pt.id = ptt.producttype_id
  WHERE pt.code = 'CACAO' AND ptt.language = 'ES'
);

INSERT INTO public.producttypetranslation (language, description, entityversion, name, producttype_id)
SELECT 'EN', 'Cacao value chain', 0, 'Cacao',
       (SELECT id FROM public.producttype WHERE code = 'CACAO' ORDER BY id LIMIT 1)
WHERE NOT EXISTS (
  SELECT 1 FROM public.producttypetranslation ptt
  JOIN public.producttype pt ON pt.id = ptt.producttype_id
  WHERE pt.code = 'CACAO' AND ptt.language = 'EN'
);

INSERT INTO public.semiproduct (creationtimestamp, updatetimestamp, description, isbuyable, issku, isskuendcustomer, name, measurementunittype_id)
SELECT NOW(), NOW(), 'Cacao fresco en baba cosechado en finca', true, false, false, 'Cacao en Baba',
       (SELECT id FROM public.measureunittype WHERE code = 'PESOLIBRA' ORDER BY id LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM public.semiproduct WHERE name = 'Cacao en Baba');

INSERT INTO public.semiproducttranslation (language, description, entityversion, name, semiproduct_id)
SELECT 'ES', 'Cacao fresco en baba cosechado en finca', 0, 'Cacao en Baba',
       (SELECT id FROM public.semiproduct WHERE name = 'Cacao en Baba' ORDER BY id LIMIT 1)
WHERE NOT EXISTS (
  SELECT 1 FROM public.semiproducttranslation st
  JOIN public.semiproduct sp ON sp.id = st.semiproduct_id
  WHERE sp.name = 'Cacao en Baba' AND st.language = 'ES'
);

INSERT INTO public.semiproducttranslation (language, description, entityversion, name, semiproduct_id)
SELECT 'EN', 'Fresh wet cacao beans harvested at farm', 0, 'Wet Cacao Beans',
       (SELECT id FROM public.semiproduct WHERE name = 'Cacao en Baba' ORDER BY id LIMIT 1)
WHERE NOT EXISTS (
  SELECT 1 FROM public.semiproducttranslation st
  JOIN public.semiproduct sp ON sp.id = st.semiproduct_id
  WHERE sp.name = 'Cacao en Baba' AND st.language = 'EN'
);

INSERT INTO public.facilitytype (code, label, "order")
SELECT 'COLLECTING', 'Centro de Acopio', 1
WHERE NOT EXISTS (SELECT 1 FROM public.facilitytype WHERE code = 'COLLECTING');

INSERT INTO public.valuechain (creationtimestamp, updatetimestamp, description, entityversion, name, valuechainstatus, createdby_id, producttype_id)
SELECT NOW(), NOW(), 'Cadena productiva de cacao', 0, 'Cacao', 'ENABLED',
       (SELECT id FROM public."User" WHERE lower(email) = lower(:'admin_email') ORDER BY id LIMIT 1),
       (SELECT id FROM public.producttype WHERE code = 'CACAO' ORDER BY id LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM public.valuechain WHERE name = 'Cacao');

INSERT INTO public.valuechainmeasureunittype (valuechain_id, measureunittype_id)
SELECT vc.id, mu.id
FROM public.valuechain vc, public.measureunittype mu
WHERE vc.name = 'Cacao' AND mu.code IN ('PESOLIBRA', 'QUINTALES')
ON CONFLICT (valuechain_id, measureunittype_id) DO NOTHING;

INSERT INTO public.valuechainsemiproduct (valuechain_id, semiproduct_id)
SELECT vc.id, sp.id
FROM public.valuechain vc, public.semiproduct sp
WHERE vc.name = 'Cacao' AND sp.name = 'Cacao en Baba'
ON CONFLICT (valuechain_id, semiproduct_id) DO NOTHING;

INSERT INTO public.valuechainfacilitytype (valuechain_id, facilitytype_id)
SELECT vc.id, ft.id
FROM public.valuechain vc, public.facilitytype ft
WHERE vc.name = 'Cacao' AND ft.code = 'COLLECTING'
ON CONFLICT (valuechain_id, facilitytype_id) DO NOTHING;

SELECT setval(pg_get_serial_sequence('public.country', 'id'), COALESCE((SELECT MAX(id) FROM public.country), 1));
SELECT setval(pg_get_serial_sequence('public.currencytype', 'id'), COALESCE((SELECT MAX(id) FROM public.currencytype), 1));
SELECT setval(pg_get_serial_sequence('public.measureunittype', 'id'), COALESCE((SELECT MAX(id) FROM public.measureunittype), 1));
SELECT setval(pg_get_serial_sequence('public.producttype', 'id'), COALESCE((SELECT MAX(id) FROM public.producttype), 1));
SELECT setval(pg_get_serial_sequence('public.producttypetranslation', 'id'), COALESCE((SELECT MAX(id) FROM public.producttypetranslation), 1));
SELECT setval(pg_get_serial_sequence('public.semiproduct', 'id'), COALESCE((SELECT MAX(id) FROM public.semiproduct), 1));
SELECT setval(pg_get_serial_sequence('public.semiproducttranslation', 'id'), COALESCE((SELECT MAX(id) FROM public.semiproducttranslation), 1));
SELECT setval(pg_get_serial_sequence('public.facilitytype', 'id'), COALESCE((SELECT MAX(id) FROM public.facilitytype), 1));
SELECT setval(pg_get_serial_sequence('public.valuechain', 'id'), COALESCE((SELECT MAX(id) FROM public.valuechain), 1));
SELECT setval(pg_get_serial_sequence('public.valuechainmeasureunittype', 'id'), COALESCE((SELECT MAX(id) FROM public.valuechainmeasureunittype), 1));
SELECT setval(pg_get_serial_sequence('public.valuechainsemiproduct', 'id'), COALESCE((SELECT MAX(id) FROM public.valuechainsemiproduct), 1));
SELECT setval(pg_get_serial_sequence('public.valuechainfacilitytype', 'id'), COALESCE((SELECT MAX(id) FROM public.valuechainfacilitytype), 1));

COMMIT;
SQL_SEED
  log_ok "Idempotent Cacao catalog seed applied successfully."
fi

log_ok "Administrator onboarding and initial catalog setup complete."
