-- ============================================================================
-- Migración: Agregar campos específicos de camarón a StockOrder
-- Fecha: 2025-11-11
-- Descripción: Campos para entregas normales de camarón (NO laboratorio)
-- ============================================================================

SET @table_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'StockOrder');

SET @old_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'StockOrder' AND COLUMN_NAME = 'number_of_gavetas');
SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'StockOrder' AND COLUMN_NAME = 'numberOfGavetas');
SET @sql = IF(@table_exists > 0 AND @old_exists > 0 AND @new_exists = 0,
  'ALTER TABLE StockOrder CHANGE COLUMN number_of_gavetas numberOfGavetas INT NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'StockOrder' AND COLUMN_NAME = 'numberOfGavetas');
SET @sql = IF(@table_exists > 0 AND @new_exists = 0,
  'ALTER TABLE StockOrder ADD COLUMN numberOfGavetas INT NULL COMMENT ''N° de Gavetas (específico para camarón)''',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @old_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'StockOrder' AND COLUMN_NAME = 'number_of_bines');
SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'StockOrder' AND COLUMN_NAME = 'numberOfBines');
SET @sql = IF(@table_exists > 0 AND @old_exists > 0 AND @new_exists = 0,
  'ALTER TABLE StockOrder CHANGE COLUMN number_of_bines numberOfBines VARCHAR(50) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'StockOrder' AND COLUMN_NAME = 'numberOfBines');
SET @sql = IF(@table_exists > 0 AND @new_exists = 0,
  'ALTER TABLE StockOrder ADD COLUMN numberOfBines VARCHAR(50) NULL COMMENT ''N° de Bines (específico para camarón)''',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @old_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'StockOrder' AND COLUMN_NAME = 'number_of_piscinas');
SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'StockOrder' AND COLUMN_NAME = 'numberOfPiscinas');
SET @sql = IF(@table_exists > 0 AND @old_exists > 0 AND @new_exists = 0,
  'ALTER TABLE StockOrder CHANGE COLUMN number_of_piscinas numberOfPiscinas VARCHAR(50) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'StockOrder' AND COLUMN_NAME = 'numberOfPiscinas');
SET @sql = IF(@table_exists > 0 AND @new_exists = 0,
  'ALTER TABLE StockOrder ADD COLUMN numberOfPiscinas VARCHAR(50) NULL COMMENT ''N° de Piscinas (específico para camarón)''',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @old_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'StockOrder' AND COLUMN_NAME = 'guia_remision_number');
SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'StockOrder' AND COLUMN_NAME = 'guiaRemisionNumber');
SET @sql = IF(@table_exists > 0 AND @old_exists > 0 AND @new_exists = 0,
  'ALTER TABLE StockOrder CHANGE COLUMN guia_remision_number guiaRemisionNumber VARCHAR(100) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'StockOrder' AND COLUMN_NAME = 'guiaRemisionNumber');
SET @sql = IF(@table_exists > 0 AND @new_exists = 0,
  'ALTER TABLE StockOrder ADD COLUMN guiaRemisionNumber VARCHAR(100) NULL COMMENT ''N° de Guía de Remisión (específico para camarón)''',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
