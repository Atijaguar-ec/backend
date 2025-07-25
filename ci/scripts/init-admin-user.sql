-- Script de inicialización de usuario administrador
-- Se ejecuta automáticamente después de las migraciones de Flyway
-- Crea un usuario SYSTEM_ADMIN si no existe ningún usuario en el sistema

-- Verificar si ya existen usuarios en el sistema
SET @user_count = (SELECT COUNT(*) FROM user);

-- Solo crear usuario administrador si no hay usuarios existentes
INSERT INTO user (email, password, name, surname, role, status, created)
SELECT 
    'admin@inatrace.com',
    '$2a$10$N.zmdr9k7uOsaVQoQvdOde7FZmYnZAhHkOmMlGGKJNM.jO2LJWLHy',
    'System',
    'Administrator',
    'SYSTEM_ADMIN',
    'ACTIVE',
    NOW()
WHERE @user_count = 0;

-- Mostrar resultado
SELECT 
    CASE 
        WHEN @user_count = 0 THEN 'Usuario administrador creado exitosamente'
        ELSE CONCAT('Ya existen ', @user_count, ' usuarios en el sistema. No se creó usuario adicional.')
    END as resultado;

-- Mostrar usuarios existentes
SELECT id, email, name, surname, role, status, created 
FROM user 
ORDER BY created ASC;
