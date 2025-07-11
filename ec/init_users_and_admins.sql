-- Script para crear usuarios administradores y asociarlos a las compañías
-- Ajusta los nombres de tabla/campos según tu modelo real

-- DUFER
INSERT INTO user (username, password, full_name, created_at, updated_at)
VALUES ('admin_dufer', 'DUFER.2025', 'Administrador DUFER', NOW(), NOW());
-- Asociar usuario como admin a DUFER (supón id 1 para DUFER y 1 para el usuario)
INSERT INTO company_user (company_id, user_id, role)
VALUES (1, 1, 'COMPANY_ADMIN');

-- Fortaleza del Valle
INSERT INTO user (username, password, full_name, created_at, updated_at)
VALUES ('admin_fortaleza', 'FortalezaDelValle.2025', 'Administrador Fortaleza del Valle', NOW(), NOW());
-- Asociar usuario como admin a Fortaleza del Valle (supón id 2 para Fortaleza y 2 para el usuario)
INSERT INTO company_user (company_id, user_id, role)
VALUES (2, 2, 'COMPANY_ADMIN');

-- UNOCACE
INSERT INTO user (username, password, full_name, created_at, updated_at)
VALUES ('admin_unocace', 'UNOCACE.2025', 'Administrador UNOCACE', NOW(), NOW());
-- Asociar usuario como admin a UNOCACE (supón id 3 para UNOCACE y 3 para el usuario)
INSERT INTO company_user (company_id, user_id, role)
VALUES (3, 3, 'COMPANY_ADMIN');

-- NOTA: Ajusta los IDs si tu base de datos los genera automáticamente (usa LAST_INSERT_ID() o consulta el ID generado)
-- Cambia los nombres de tabla/campos según tu modelo real
