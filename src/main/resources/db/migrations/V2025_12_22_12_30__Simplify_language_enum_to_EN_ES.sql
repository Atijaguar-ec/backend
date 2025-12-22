-- ═══════════════════════════════════════════════════════════════
-- V2025_12_22_12_30: Simplificar ENUM language a EN/ES
-- ═══════════════════════════════════════════════════════════════
-- Reduce el ENUM de idioma de ('DE','EN','ES','RW') a ('EN','ES')
-- para reflejar los idiomas soportados en producción.
-- 
-- NOTA: Antes de ejecutar, asegúrate de que no existan registros
-- con language='DE' o language='RW'. Si existen, primero migrarlos.
-- ═══════════════════════════════════════════════════════════════

-- 1. CompanyTranslation
SET @table_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.TABLES 
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'CompanyTranslation');
SET @sql = IF(@table_exists > 0, 
    'ALTER TABLE CompanyTranslation MODIFY COLUMN language ENUM(''EN'',''ES'') NOT NULL', 
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. FacilityTranslation
SET @table_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.TABLES 
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'FacilityTranslation');
SET @sql = IF(@table_exists > 0, 
    'ALTER TABLE FacilityTranslation MODIFY COLUMN language ENUM(''EN'',''ES'') NOT NULL', 
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3. ProcessingActionTranslation
SET @table_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.TABLES 
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingActionTranslation');
SET @sql = IF(@table_exists > 0, 
    'ALTER TABLE ProcessingActionTranslation MODIFY COLUMN language ENUM(''EN'',''ES'') NOT NULL', 
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 4. ProcessingEvidenceFieldTranslation
SET @table_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.TABLES 
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingEvidenceFieldTranslation');
SET @sql = IF(@table_exists > 0, 
    'ALTER TABLE ProcessingEvidenceFieldTranslation MODIFY COLUMN language ENUM(''EN'',''ES'') NOT NULL', 
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 5. ProcessingEvidenceTypeTranslation
SET @table_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.TABLES 
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingEvidenceTypeTranslation');
SET @sql = IF(@table_exists > 0, 
    'ALTER TABLE ProcessingEvidenceTypeTranslation MODIFY COLUMN language ENUM(''EN'',''ES'') NOT NULL', 
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 6. ProductLabel
SET @table_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.TABLES 
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProductLabel');
SET @sql = IF(@table_exists > 0, 
    'ALTER TABLE ProductLabel MODIFY COLUMN language ENUM(''EN'',''ES'') NOT NULL', 
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 7. ProductSettings
SET @table_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.TABLES 
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProductSettings');
SET @sql = IF(@table_exists > 0, 
    'ALTER TABLE ProductSettings MODIFY COLUMN language ENUM(''EN'',''ES'') NOT NULL', 
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 8. ProductTypeTranslation
SET @table_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.TABLES 
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProductTypeTranslation');
SET @sql = IF(@table_exists > 0, 
    'ALTER TABLE ProductTypeTranslation MODIFY COLUMN language ENUM(''EN'',''ES'') NOT NULL', 
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 9. SemiProductTranslation
SET @table_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.TABLES 
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'SemiProductTranslation');
SET @sql = IF(@table_exists > 0, 
    'ALTER TABLE SemiProductTranslation MODIFY COLUMN language ENUM(''EN'',''ES'') NOT NULL', 
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 10. User
SET @table_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.TABLES 
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'User');
SET @sql = IF(@table_exists > 0, 
    'ALTER TABLE User MODIFY COLUMN language ENUM(''EN'',''ES'') NOT NULL', 
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 11. User_AUD (tabla de auditoría - permite NULL)
SET @table_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.TABLES 
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'User_AUD');
SET @sql = IF(@table_exists > 0, 
    'ALTER TABLE User_AUD MODIFY COLUMN language ENUM(''EN'',''ES'') DEFAULT NULL', 
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
