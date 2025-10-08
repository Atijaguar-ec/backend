#!/bin/bash
# ═══════════════════════════════════════════════════════════════
# Script para generar V1__Initial_schema.sql desde base de desarrollo
# ═══════════════════════════════════════════════════════════════

set -euo pipefail

# Colores para output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${GREEN}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${GREEN}  Generador de V1__Initial_schema.sql${NC}"
echo -e "${GREEN}═══════════════════════════════════════════════════════════════${NC}"
echo

# Configuración (ajusta según tu entorno)
REMOTE_HOST="${REMOTE_HOST:-5.161.183.137}"
DOCKER_CONTAINER="${DOCKER_CONTAINER:-inatrace-mysql}"
DB_NAME="${DB_NAME:-inatrace}"
DB_USER="${DB_USER:-root}"
DB_PASSWORD="${DB_PASSWORD:-inatrace}"
SSH_USER="${SSH_USER:-root}"

echo -e "${YELLOW}Configuración:${NC}"
echo "  Servidor remoto: $REMOTE_HOST"
echo "  Usuario SSH: $SSH_USER"
echo "  Contenedor Docker: $DOCKER_CONTAINER"
echo "  Base de datos: $DB_NAME"
echo "  Usuario DB: $DB_USER"
echo

# Verificar conexión SSH
echo -e "${YELLOW}Verificando conexión SSH...${NC}"
if ! ssh -o ConnectTimeout=5 -o BatchMode=yes "$SSH_USER@$REMOTE_HOST" "echo 'Conexión OK'" &>/dev/null; then
    echo -e "${RED}Error: No se puede conectar al servidor $REMOTE_HOST${NC}"
    echo
    echo "Verifica:"
    echo "  1. Que tengas acceso SSH configurado"
    echo "  2. Que la clave SSH esté en ~/.ssh/authorized_keys del servidor"
    echo "  3. Ejecuta manualmente: ssh $SSH_USER@$REMOTE_HOST"
    echo
    echo "Para usar otro servidor/usuario:"
    echo "  REMOTE_HOST=tu_servidor SSH_USER=tu_usuario ./scripts/generate-v1-migration.sh"
    exit 1
fi

echo -e "${GREEN}✓ Conexión SSH establecida${NC}"

# Verificar que el contenedor exista y esté corriendo en el servidor remoto
echo -e "${YELLOW}Verificando contenedor Docker en servidor remoto...${NC}"
if ! ssh "$SSH_USER@$REMOTE_HOST" "docker ps --format '{{.Names}}' | grep -q '^${DOCKER_CONTAINER}$'"; then
    echo -e "${RED}Error: El contenedor '$DOCKER_CONTAINER' no está corriendo en $REMOTE_HOST${NC}"
    echo
    echo "Contenedores MySQL disponibles en el servidor:"
    ssh "$SSH_USER@$REMOTE_HOST" "docker ps --filter 'ancestor=mysql' --format '  - {{.Names}}'"
    echo
    echo "Para usar otro contenedor, ejecuta:"
    echo "  DOCKER_CONTAINER=nombre_contenedor ./scripts/generate-v1-migration.sh"
    exit 1
fi

echo -e "${GREEN}✓ Contenedor encontrado y corriendo en servidor remoto${NC}"

# Directorio de salida
OUTPUT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/src/main/resources/db/migrations"
OUTPUT_FILE="$OUTPUT_DIR/V1__Initial_schema.sql"
TEMP_FILE="/tmp/v1_migration_temp.sql"

echo -e "${YELLOW}Generando dump de esquema desde contenedor Docker remoto...${NC}"

# Generar dump usando docker exec en servidor remoto (solo estructura, sin datos)
ssh "$SSH_USER@$REMOTE_HOST" "docker exec $DOCKER_CONTAINER mysqldump \
  -u '$DB_USER' \
  -p'$DB_PASSWORD' \
  --no-data \
  --routines=false \
  --triggers=false \
  --single-transaction \
  --skip-add-locks \
  --skip-comments \
  --set-gtid-purged=OFF \
  --ignore-table='$DB_NAME.schema_version' \
  '$DB_NAME'" > "$TEMP_FILE"

if [ $? -ne 0 ]; then
    echo -e "${RED}Error al generar dump${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Dump generado${NC}"
echo

# Limpiar el archivo
echo -e "${YELLOW}Limpiando archivo...${NC}"

# Crear archivo limpio
cat > "$OUTPUT_FILE" <<'EOF'
-- ═══════════════════════════════════════════════════════════════
-- V1: Initial Schema - Base de datos INATrace
-- ═══════════════════════════════════════════════════════════════
-- Este archivo contiene el esquema base de la aplicación.
-- Generado automáticamente desde la base de desarrollo.
-- 
-- IMPORTANTE: Este archivo debe ejecutarse en una base de datos vacía.
-- Flyway se encarga de aplicarlo automáticamente en instalaciones limpias.
-- ═══════════════════════════════════════════════════════════════

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

EOF

# Filtrar y limpiar el dump
grep -v "^/\*" "$TEMP_FILE" | \
grep -v "^--" | \
grep -v "^$" | \
sed 's/AUTO_INCREMENT=[0-9]* //g' | \
sed 's/DEFINER[ ]*=[ ]*[^*]*\*/\*/g' >> "$OUTPUT_FILE"

# Añadir footer
cat >> "$OUTPUT_FILE" <<'EOF'

SET FOREIGN_KEY_CHECKS = 1;

-- ═══════════════════════════════════════════════════════════════
-- Fin de V1__Initial_schema.sql
-- ═══════════════════════════════════════════════════════════════
EOF

# Limpiar archivo temporal
rm -f "$TEMP_FILE"

echo -e "${GREEN}✓ Archivo limpiado${NC}"
echo

# Mostrar estadísticas
TABLES_COUNT=$(grep -c "CREATE TABLE" "$OUTPUT_FILE" || echo "0")
INDEXES_COUNT=$(grep -c "CREATE.*INDEX" "$OUTPUT_FILE" || echo "0")
FKS_COUNT=$(grep -c "FOREIGN KEY" "$OUTPUT_FILE" || echo "0")

echo -e "${GREEN}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${GREEN}  Migración V1 generada exitosamente${NC}"
echo -e "${GREEN}═══════════════════════════════════════════════════════════════${NC}"
echo
echo "Archivo: $OUTPUT_FILE"
echo "Estadísticas:"
echo "  - Tablas: $TABLES_COUNT"
echo "  - Índices: $INDEXES_COUNT"
echo "  - Foreign Keys: $FKS_COUNT"
echo
echo -e "${YELLOW}Próximos pasos:${NC}"
echo "1. Revisar el archivo generado"
echo "2. Verificar que no incluya CREATE DATABASE ni USE"
echo "3. Verificar que no incluya la tabla schema_version"
echo "4. Commit y push:"
echo "   git add $OUTPUT_FILE"
echo "   git commit -m 'feat(db): add V1 baseline migration from dev schema'"
echo "   git push"
echo
echo -e "${GREEN}¡Listo!${NC}"
