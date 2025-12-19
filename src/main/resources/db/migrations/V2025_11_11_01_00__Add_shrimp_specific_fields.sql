-- ============================================================================
-- Migración: Agregar campos específicos de camarón a StockOrder
-- Fecha: 2025-11-11
-- Descripción: Campos para entregas normales de camarón (NO laboratorio)
-- ============================================================================

SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'StockOrder'
      AND column_name = 'numberOfGavetas'
);
SET @sql = IF(
    @col_exists = 0,
    'ALTER TABLE StockOrder ADD COLUMN numberOfGavetas INT NULL COMMENT ''N° de Gavetas (específico para camarón)''',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'StockOrder'
      AND column_name = 'number_of_bines'
);
SET @sql = IF(
    @col_exists = 0,
    'ALTER TABLE StockOrder ADD COLUMN number_of_bines VARCHAR(50) NULL COMMENT ''N° de Bines (específico para camarón)''',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'StockOrder'
      AND column_name = 'numberOfPiscinas'
);
SET @sql = IF(
    @col_exists = 0,
    'ALTER TABLE StockOrder ADD COLUMN numberOfPiscinas VARCHAR(50) NULL COMMENT ''N° de Piscinas (específico para camarón)''',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'StockOrder'
      AND column_name = 'guiaRemisionNumber'
);
SET @sql = IF(
    @col_exists = 0,
    'ALTER TABLE StockOrder ADD COLUMN guiaRemisionNumber VARCHAR(100) NULL COMMENT ''N° de Guía de Remisión (específico para camarón)''',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
