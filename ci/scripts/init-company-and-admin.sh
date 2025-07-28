#!/bin/bash

# Requiere que existan las funciones execute_sql y que las variables $ADMIN_EMAIL estén definidas

set -e

# Crear empresa si no existe
COMPANY_NAME="${COMPANY_NAME:-Empresa Principal}"
echo "🏢 Creando empresa '$COMPANY_NAME' si no existe..."
execute_sql "
INSERT INTO Company (name, status, entityVersion)
SELECT '$COMPANY_NAME', 'ACTIVE', 0
WHERE NOT EXISTS (SELECT 1 FROM Company WHERE name = '$COMPANY_NAME');
"

# Obtener IDs de empresa y usuario
echo "🔍 Obteniendo IDs de empresa y usuario..."
COMPANY_ID=$(execute_sql "SELECT id FROM Company WHERE name = '$COMPANY_NAME';" | grep -Eo '[0-9]+' | head -1)
USER_ID=$(execute_sql "SELECT id FROM User WHERE email = '$ADMIN_EMAIL';" | grep -Eo '[0-9]+' | head -1)

# Relacionar usuario con empresa como COMPANY_ADMIN si no existe
echo "🔗 Relacionando usuario administrador con la empresa principal..."
execute_sql "
INSERT INTO CompanyUser (company_id, user_id, role, entityVersion)
SELECT $COMPANY_ID, $USER_ID, 'COMPANY_ADMIN', 0
WHERE NOT EXISTS (
  SELECT 1 FROM CompanyUser WHERE company_id = $COMPANY_ID AND user_id = $USER_ID
);
"

echo "✅ Empresa y relación administrador-empresa aseguradas."
