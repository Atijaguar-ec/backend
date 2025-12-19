-- ============================================================================
-- Migración: Agregar campos para entregas de laboratorio
-- Fecha: 2025-11-11
-- Descripción: Campos para N° de Muestra y Hora de recepción
-- ============================================================================

SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'StockOrder'
      AND column_name = 'sampleNumber'
);
SET @sql = IF(
    @col_exists = 0,
    'ALTER TABLE StockOrder ADD COLUMN sampleNumber VARCHAR(100) NULL COMMENT ''N° de Muestra (específico para laboratorio)''',
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
      AND column_name = 'receptionTime'
);
SET @sql = IF(
    @col_exists = 0,
    'ALTER TABLE StockOrder ADD COLUMN receptionTime TIME NULL COMMENT ''Hora de recepción (específico para laboratorio)''',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
