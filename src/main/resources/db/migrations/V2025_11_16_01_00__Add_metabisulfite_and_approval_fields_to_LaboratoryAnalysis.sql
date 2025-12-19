-- ============================================================================
-- Migración: Añadir campos de Metabisulfito y aprobación a LaboratoryAnalysis
-- Fecha: 2025-11-16
-- Descripción: Agrega campos para registrar si el nivel de Metabisulfito es
--              aceptable y si el análisis está aprobado para la compra.
-- =========================================================================

SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'LaboratoryAnalysis'
      AND column_name = 'metabisulfiteLevelAcceptable'
);
SET @sql = IF(
    @col_exists = 0,
    'ALTER TABLE `LaboratoryAnalysis` ADD COLUMN `metabisulfiteLevelAcceptable` TINYINT(1) NULL COMMENT ''Metabisulfite level acceptable (1 = yes, 0 = no)'' AFTER `qualityNotes`',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'LaboratoryAnalysis'
      AND column_name = 'approvedForPurchase'
);
SET @sql = IF(
    @col_exists = 0,
    'ALTER TABLE `LaboratoryAnalysis` ADD COLUMN `approvedForPurchase` TINYINT(1) NULL COMMENT ''Overall analysis approved for purchase (1 = yes, 0 = no)'' AFTER `metabisulfiteLevelAcceptable`',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
