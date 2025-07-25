#!/bin/bash

# Script de inicialización automática de usuario administrador
# Se ejecuta después del despliegue del contenedor backend

set -e

echo "=== Inicialización de Usuario Administrador ==="
echo "Fecha: $(date)"

# Variables de configuración
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-inatrace}"
DB_USER="${DB_USER:-inatrace}"
DB_PASS="${DB_PASS:-inatrace}"

# Email del administrador (puede ser sobrescrito por variable de entorno)
ADMIN_EMAIL="${ADMIN_EMAIL:-admin@inatrace.com}"
ADMIN_NAME="${ADMIN_NAME:-System}"
ADMIN_SURNAME="${ADMIN_SURNAME:-Administrator}"

# Hash BCrypt para contraseña 'admin123'
ADMIN_PASSWORD_HASH='$2a$10$N.zmdr9k7uOsaVQoQvdOde7FZmYnZAhHkOmMlGGKJNM.jO2LJWLHy'

echo "Configuración:"
echo "  - Base de datos: ${DB_HOST}:${DB_PORT}/${DB_NAME}"
echo "  - Usuario DB: ${DB_USER}"
echo "  - Email admin: ${ADMIN_EMAIL}"

# Función para esperar que MySQL esté disponible
wait_for_mysql() {
    echo "Esperando que MySQL esté disponible..."
    local max_attempts=30
    local attempt=1
    
    while [ $attempt -le $max_attempts ]; do
        if mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" -e "SELECT 1;" > /dev/null 2>&1; then
            echo "MySQL está disponible."
            return 0
        fi
        
        echo "Intento $attempt/$max_attempts: MySQL no disponible, esperando 2 segundos..."
        sleep 2
        attempt=$((attempt + 1))
    done
    
    echo "ERROR: MySQL no está disponible después de $max_attempts intentos."
    exit 1
}

# Función para verificar si las tablas existen
check_tables() {
    echo "Verificando que las tablas existan..."
    
    local table_count=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" -D"$DB_NAME" -sN -e "
        SELECT COUNT(*) 
        FROM information_schema.tables 
        WHERE table_schema = '$DB_NAME' AND table_name = 'User';
    ")
    
    if [ "$table_count" -eq 0 ]; then
        echo "ERROR: La tabla 'User' no existe. Las migraciones de Flyway pueden no haberse ejecutado."
        exit 1
    fi
    
    echo "Tabla 'User' encontrada."
}

# Función para crear usuario administrador
create_admin_user() {
    echo "Verificando usuarios existentes..."
    
    local user_count=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" -D"$DB_NAME" -sN -e "
        SELECT COUNT(*) FROM User;
    ")
    
    echo "Usuarios existentes en el sistema: $user_count"
    
    if [ "$user_count" -eq 0 ]; then
        echo "No hay usuarios en el sistema. Creando usuario administrador..."
        
        mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" -D"$DB_NAME" -e "
            INSERT INTO User (email, password, name, surname, role, status, creationTimestamp) 
            VALUES ('$ADMIN_EMAIL', '$ADMIN_PASSWORD_HASH', '$ADMIN_NAME', '$ADMIN_SURNAME', 'SYSTEM_ADMIN', 'ACTIVE', NOW());
        "
        
        echo "✅ Usuario administrador creado exitosamente:"
        echo "   Email: $ADMIN_EMAIL"
        echo "   Password: admin123"
        echo "   Rol: SYSTEM_ADMIN"
        echo "   Estado: ACTIVE"
        
    else
        echo "⚠️  Ya existen $user_count usuarios en el sistema."
        echo "   No se creará usuario administrador adicional."
    fi
}

# Función para mostrar usuarios existentes
show_users() {
    echo ""
    echo "=== Usuarios en el sistema ==="
    mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" -D"$DB_NAME" -e "
        SELECT id, email, name, surname, role, status, creationTimestamp 
        FROM User 
        ORDER BY creationTimestamp ASC;
    "
}

# Función principal
main() {
    echo "Iniciando proceso de inicialización..."
    
    wait_for_mysql
    check_tables
    create_admin_user
    show_users
    
    echo ""
    echo "=== Inicialización completada ==="
    echo "Fecha: $(date)"
}

# Ejecutar función principal
main "$@"
