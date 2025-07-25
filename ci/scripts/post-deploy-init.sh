#!/bin/bash

# Script de inicialización post-despliegue
# Ejecutar manualmente después del despliegue inicial del backend
# Uso: ./scripts/post-deploy-init.sh

set -e

echo "🚀 INATrace - Inicialización Post-Despliegue"
echo "=============================================="
echo "Fecha: $(date)"
echo ""

# Configuración por defecto (puede ser sobrescrita por variables de entorno)
CONTAINER_NAME="${CONTAINER_NAME:-inatrace-mysql}"
DB_NAME="${DB_NAME:-inatrace}"
DB_USER="${DB_USER:-inatrace}"
DB_PASS="${DB_PASS:-inatrace}"

# Configuración del usuario administrador
ADMIN_EMAIL="${ADMIN_EMAIL:-admin@inatrace.com}"
ADMIN_NAME="${ADMIN_NAME:-System}"
ADMIN_SURNAME="${ADMIN_SURNAME:-Administrator}"
ADMIN_PASSWORD_HASH='$2a$10$N.zmdr9k7uOsaVQoQvdOde7FZmYnZAhHkOmMlGGKJNM.jO2LJWLHy'

echo "📋 Configuración:"
echo "   Contenedor MySQL: $CONTAINER_NAME"
echo "   Base de datos: $DB_NAME"
echo "   Usuario admin: $ADMIN_EMAIL"
echo ""

# Verificar que el contenedor MySQL esté corriendo
if ! docker ps | grep -q "$CONTAINER_NAME"; then
    echo "❌ ERROR: El contenedor MySQL '$CONTAINER_NAME' no está corriendo."
    echo "   Asegúrate de que el backend esté desplegado correctamente."
    exit 1
fi

echo "✅ Contenedor MySQL encontrado y corriendo."

# Función para ejecutar comandos SQL
execute_sql() {
    docker exec -i "$CONTAINER_NAME" mysql -u"$DB_USER" -p"$DB_PASS" -D"$DB_NAME" -e "$1"
}

# Verificar que la tabla User existe
echo "🔍 Verificando estructura de base de datos..."
table_exists=$(execute_sql "
    SELECT COUNT(*) 
    FROM information_schema.tables 
    WHERE table_schema = '$DB_NAME' AND table_name = 'User';
" | tail -n 1)

if [ "$table_exists" -eq 0 ]; then
    echo "❌ ERROR: La tabla 'User' no existe."
    echo "   Las migraciones de Flyway pueden no haberse ejecutado."
    echo "   Verifica los logs del backend: docker logs inatrace-be"
    exit 1
fi

echo "✅ Tabla 'User' encontrada."

# Verificar usuarios existentes
echo "👥 Verificando usuarios existentes..."
user_count=$(execute_sql "SELECT COUNT(*) FROM User;" | tail -n 1)

echo "   Usuarios en el sistema: $user_count"

if [ "$user_count" -eq 0 ]; then
    echo ""
    echo "🔧 Creando usuario administrador..."
    
    execute_sql "
        INSERT INTO User (email, password, name, surname, role, status, creationTimestamp) 
        VALUES ('$ADMIN_EMAIL', '$ADMIN_PASSWORD_HASH', '$ADMIN_NAME', '$ADMIN_SURNAME', 'SYSTEM_ADMIN', 'ACTIVE', NOW());
    "
    
    echo ""
    echo "🎉 ¡Usuario administrador creado exitosamente!"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "   📧 Email:    $ADMIN_EMAIL"
    echo "   🔑 Password: admin123"
    echo "   👤 Rol:      SYSTEM_ADMIN"
    echo "   ✅ Estado:   ACTIVE"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    echo "💡 Ahora puedes acceder al sistema con estas credenciales."
    
else
    echo ""
    echo "⚠️  Ya existen $user_count usuarios en el sistema."
    echo "   No se creará usuario administrador adicional."
fi

# Mostrar usuarios existentes
echo ""
echo "👥 Usuarios actuales en el sistema:"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
execute_sql "
    SELECT 
        CONCAT('ID: ', id, ' | Email: ', email, ' | Nombre: ', name, ' ', surname, ' | Rol: ', role, ' | Estado: ', status) as usuario_info
    FROM User 
    ORDER BY creationTimestamp ASC;
"

echo ""
echo "✅ Inicialización completada"
echo "   Fecha: $(date)"
echo "   Para más información, consulta: docs/tecnico/creacion-usuarios.md"
echo ""
