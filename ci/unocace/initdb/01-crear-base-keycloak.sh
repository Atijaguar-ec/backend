#!/bin/bash
# ============================================================================
# Crea la base de datos de Keycloak junto a la de INATrace.
#
# El contenedor de postgres solo crea la base de POSTGRES_DB. Keycloak usa una
# aparte (KC_DB_NAME) y, si no existe, entra en bucle de reinicio con:
#   FATAL: database "inatrace_keycloak_unocace" does not exist
# Detectado en la prueba de humo del 2026-10-18, donde Keycloak se reinicio
# 10 veces antes de que mirasemos el log.
#
# Los scripts de /docker-entrypoint-initdb.d/ solo corren cuando el directorio
# de datos esta vacio, es decir en el primer arranque. Para una base ya
# existente, crearla a mano:
#   docker exec <pg> psql -U <usuario> -d postgres -c 'CREATE DATABASE ...'
# ============================================================================
set -e

KC_DB_NAME="${KC_DB_NAME:-inatrace_keycloak_unocace}"

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname postgres <<SQL
SELECT 'CREATE DATABASE "${KC_DB_NAME}" OWNER "${POSTGRES_USER}"'
WHERE NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = '${KC_DB_NAME}')\gexec
SQL

echo "initdb: base de Keycloak '${KC_DB_NAME}' lista."
