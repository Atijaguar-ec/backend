#!/bin/bash

# Script de inicialización para contenedor Docker
# Se ejecuta automáticamente al iniciar el contenedor backend
# Espera a que el backend esté listo y luego crea el usuario administrador

set -e

echo "=== Docker Init Script - INATrace Backend ==="
echo "Fecha: $(date)"

# Variables de entorno del contenedor
BACKEND_HOST="${BACKEND_HOST:-localhost}"
BACKEND_PORT="${BACKEND_PORT:-8080}"
DB_HOST="${DB_HOST:-inatrace-mysql}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-inatrace}"
DB_USER="${DB_USER:-inatrace}"
DB_PASS="${DB_PASS:-inatrace}"

# Configuración del usuario administrador
ADMIN_EMAIL="${ADMIN_EMAIL:-admin@inatrace.com}"
ADMIN_NAME="${ADMIN_NAME:-System}"
ADMIN_SURNAME="${ADMIN_SURNAME:-Administrator}"
ADMIN_PASSWORD_HASH='$2a$10$N.zmdr9k7uOsaVQoQvdOde7FZmYnZAhHkOmMlGGKJNM.jO2LJWLHy'

echo "Configuración del contenedor:"
echo "  - Backend: ${BACKEND_HOST}:${BACKEND_PORT}"
echo "  - Base de datos: ${DB_HOST}:${DB_PORT}/${DB_NAME}"
echo "  - Usuario admin: ${ADMIN_EMAIL}"

# Función para esperar que el backend esté listo
wait_for_backend() {
    echo "Esperando que el backend esté disponible..."
    local max_attempts=60
    local attempt=1
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s -f "http://${BACKEND_HOST}:${BACKEND_PORT}/actuator/health" > /dev/null 2>&1; then
            echo "✅ Backend está disponible y saludable."
            return 0
        fi
        
        echo "Intento $attempt/$max_attempts: Backend no disponible, esperando 5 segundos..."
        sleep 5
        attempt=$((attempt + 1))
    done
    
    echo "⚠️  Backend no está disponible después de $max_attempts intentos."
    echo "Continuando con la inicialización de base de datos..."
}

# Función para esperar MySQL
wait_for_mysql() {
    echo "Esperando que MySQL esté disponible..."
    local max_attempts=30
    local attempt=1
    
    while [ $attempt -le $max_attempts ]; do
        if mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" -e "SELECT 1;" > /dev/null 2>&1; then
            echo "✅ MySQL está disponible."
            return 0
        fi
        
        echo "Intento $attempt/$max_attempts: MySQL no disponible, esperando 3 segundos..."
        sleep 3
        attempt=$((attempt + 1))
    done
    
    echo "❌ ERROR: MySQL no está disponible después de $max_attempts intentos."
    exit 1
}

# Función para esperar que las tablas existan
wait_for_tables() {
    echo "Esperando que las migraciones de Flyway se completen..."
    local max_attempts=20
    local attempt=1
    
    while [ $attempt -le $max_attempts ]; do
        local table_count=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" -D"$DB_NAME" -sN -e "
            SELECT COUNT(*) 
            FROM information_schema.tables 
            WHERE table_schema = '$DB_NAME' AND table_name = 'user';
        " 2>/dev/null || echo "0")
        
        if [ "$table_count" -eq 1 ]; then
            echo "✅ Tabla 'user' encontrada. Migraciones completadas."
            return 0
        fi
        
        echo "Intento $attempt/$max_attempts: Tabla 'user' no encontrada, esperando 5 segundos..."
        sleep 5
        attempt=$((attempt + 1))
    done
    
    echo "❌ ERROR: Tabla 'user' no encontrada después de $max_attempts intentos."
    echo "Las migraciones de Flyway pueden no haberse ejecutado correctamente."
    exit 1
}

# Función para crear usuario administrador
create_admin_user() {
    echo "Verificando usuarios existentes..."
    
    local user_count=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" -D"$DB_NAME" -sN -e "
        SELECT COUNT(*) FROM user;
    " 2>/dev/null || echo "0")
    
    echo "Usuarios existentes en el sistema: $user_count"
    
    if [ "$user_count" -eq 0 ]; then
        echo "No hay usuarios en el sistema. Creando usuario administrador..."
        
        mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" -D"$DB_NAME" -e "
            INSERT INTO user (email, password, name, surname, role, status, created) 
            VALUES ('$ADMIN_EMAIL', '$ADMIN_PASSWORD_HASH', '$ADMIN_NAME', '$ADMIN_SURNAME', 'SYSTEM_ADMIN', 'ACTIVE', NOW());
        " 2>/dev/null
        
        if [ $? -eq 0 ]; then
            echo ""
            echo "🎉 ¡Usuario administrador creado exitosamente!"
            echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
            echo "   📧 Email:    $ADMIN_EMAIL"
            echo "   🔑 Password: admin123"
            echo "   👤 Rol:      SYSTEM_ADMIN"
            echo "   ✅ Estado:   ACTIVE"
            echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
            echo ""
        else
            echo "❌ ERROR: No se pudo crear el usuario administrador."
            exit 1
        fi
        
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
        SELECT id, email, name, surname, role, status, DATE_FORMAT(created, '%Y-%m-%d %H:%i:%s') as created
        FROM user 
        ORDER BY created ASC;
    " 2>/dev/null || echo "No se pudieron obtener los usuarios."
}

# Función para crear archivo de log
create_log_entry() {
    local log_file="/tmp/inatrace-init.log"
    echo "$(date): Inicialización de usuario administrador completada" >> "$log_file"
    echo "$(date): Email: $ADMIN_EMAIL" >> "$log_file"
    echo "$(date): Estado: $1" >> "$log_file"
}

# Función principal
main() {
    echo "🚀 Iniciando proceso de inicialización automática..."
    echo ""
    
    # Esperar servicios
    wait_for_mysql
    wait_for_tables
    wait_for_backend
    
    echo ""
    echo "📋 Creando usuario administrador..."
    create_admin_user
    
    echo ""
    echo "👥 Listando usuarios..."
    show_users
    
    create_log_entry "SUCCESS"
    
    echo ""
    echo "✅ Inicialización completada exitosamente"
    echo "   Fecha: $(date)"
    echo "   Log: /tmp/inatrace-init.log"
    echo ""
}

# Manejo de errores
trap 'echo "❌ Error en línea $LINENO. Saliendo..."; create_log_entry "ERROR"; exit 1' ERR

# Ejecutar función principal
main "$@"
