-- Script de inicialización de usuario administrador
-- Se ejecuta automáticamente después de las migraciones de Flyway
-- Crea un usuario SYSTEM_ADMIN si no existe ningún usuario en el sistema

-- Verificar si ya existen usuarios en el sistema
SET @User_count = (SELECT COUNT(*) FROM User);

-- Solo crear usuario administrador si no hay usuarios existentes
INSERT INTO User (email, password, name, surname, role, status, creationTimestamp)
SELECT 
    'admin@inatrace.com',
    '$2a$10$N.zmdr9k7uOsaVQoQvdOde7FZmYnZAhHkOmMlGGKJNM.jO2LJWLHy',
    'System',
    'Administrator',
    'SYSTEM_ADMIN',
    'ACTIVE',
    NOW()
WHERE @User_count = 0;

-- Mostrar resultado
SELECT 
    CASE 
        WHEN @User_count = 0 THEN 'Usuario administrador creado exitosamente'
        ELSE CONCAT('Ya existen ', @User_count, ' usuarios en el sistema. No se creó usuario adicional.')
    END as resultado;

-- Mostrar usuarios existentes
SELECT id, email, name, surname, role, status, creationTimestamp 
FROM User 
ORDER BY creationTimestamp ASC;
