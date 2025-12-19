-- ============================================================================
-- Migración: Agregar campos de procesos de camarón (corte, tratamiento, túnel, lavado) a StockOrder
-- Fecha: 2025-11-19
-- Descripción: Campos de salida de procesamiento para órdenes de stock de camarón
-- ============================================================================

SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'StockOrder'
      AND column_name = 'cuttingType'
);
SET @sql = IF(
    @col_exists = 0,
    'ALTER TABLE StockOrder ADD COLUMN cuttingType VARCHAR(100) NULL COMMENT ''Tipo de corte (camarón)''',
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
      AND column_name = 'cuttingEntryDate'
);
SET @sql = IF(
    @col_exists = 0,
    'ALTER TABLE StockOrder ADD COLUMN cuttingEntryDate DATE NULL COMMENT ''Fecha de ingreso al área de corte (camarón)''',
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
      AND column_name = 'cuttingExitDate'
);
SET @sql = IF(
    @col_exists = 0,
    'ALTER TABLE StockOrder ADD COLUMN cuttingExitDate DATE NULL COMMENT ''Fecha de salida del área de corte (camarón)''',
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
      AND column_name = 'cuttingTemperatureControl'
);
SET @sql = IF(
    @col_exists = 0,
    'ALTER TABLE StockOrder ADD COLUMN cuttingTemperatureControl VARCHAR(255) NULL COMMENT ''Control de temperatura en corte (camarón)''',
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
      AND column_name = 'treatmentType'
);
SET @sql = IF(
    @col_exists = 0,
    'ALTER TABLE StockOrder ADD COLUMN treatmentType VARCHAR(100) NULL COMMENT ''Tipo de tratamiento (camarón)''',
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
      AND column_name = 'treatmentEntryDate'
);
SET @sql = IF(
    @col_exists = 0,
    'ALTER TABLE StockOrder ADD COLUMN treatmentEntryDate DATE NULL COMMENT ''Fecha de ingreso a tratamiento (camarón)''',
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
      AND column_name = 'treatmentExitDate'
);
SET @sql = IF(
    @col_exists = 0,
    'ALTER TABLE StockOrder ADD COLUMN treatmentExitDate DATE NULL COMMENT ''Fecha de salida de tratamiento (camarón)''',
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
      AND column_name = 'treatmentTemperatureControl'
);
SET @sql = IF(
    @col_exists = 0,
    'ALTER TABLE StockOrder ADD COLUMN treatmentTemperatureControl VARCHAR(255) NULL COMMENT ''Control de temperatura en tratamiento (camarón)''',
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
      AND column_name = 'treatmentChemicalUsed'
);
SET @sql = IF(
    @col_exists = 0,
    'ALTER TABLE StockOrder ADD COLUMN treatmentChemicalUsed VARCHAR(255) NULL COMMENT ''Químico utilizado en tratamiento (camarón)''',
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
      AND column_name = 'tunnelProductionDate'
);
SET @sql = IF(
    @col_exists = 0,
    'ALTER TABLE StockOrder ADD COLUMN tunnelProductionDate DATE NULL COMMENT ''Fecha de producción en túnel (camarón)''',
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
      AND column_name = 'tunnelExpirationDate'
);
SET @sql = IF(
    @col_exists = 0,
    'ALTER TABLE StockOrder ADD COLUMN tunnelExpirationDate DATE NULL COMMENT ''Fecha de expiración de túnel (camarón)''',
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
      AND column_name = 'tunnelNetWeight'
);
SET @sql = IF(
    @col_exists = 0,
    'ALTER TABLE StockOrder ADD COLUMN tunnelNetWeight DECIMAL(38,2) NULL COMMENT ''Peso neto en túnel (camarón)''',
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
      AND column_name = 'tunnelSupplier'
);
SET @sql = IF(
    @col_exists = 0,
    'ALTER TABLE StockOrder ADD COLUMN tunnelSupplier VARCHAR(255) NULL COMMENT ''Proveedor de túnel (camarón)''',
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
      AND column_name = 'tunnelFreezingType'
);
SET @sql = IF(
    @col_exists = 0,
    'ALTER TABLE StockOrder ADD COLUMN tunnelFreezingType VARCHAR(100) NULL COMMENT ''Tipo de congelación en túnel (camarón)''',
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
      AND column_name = 'tunnelEntryDate'
);
SET @sql = IF(
    @col_exists = 0,
    'ALTER TABLE StockOrder ADD COLUMN tunnelEntryDate DATE NULL COMMENT ''Fecha de ingreso a túnel (camarón)''',
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
      AND column_name = 'tunnelExitDate'
);
SET @sql = IF(
    @col_exists = 0,
    'ALTER TABLE StockOrder ADD COLUMN tunnelExitDate DATE NULL COMMENT ''Fecha de salida de túnel (camarón)''',
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
      AND column_name = 'washingWaterTemperature'
);
SET @sql = IF(
    @col_exists = 0,
    'ALTER TABLE StockOrder ADD COLUMN washingWaterTemperature VARCHAR(100) NULL COMMENT ''Temperatura del agua de lavado (camarón)''',
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
      AND column_name = 'washingShrimpTemperatureControl'
);
SET @sql = IF(
    @col_exists = 0,
    'ALTER TABLE StockOrder ADD COLUMN washingShrimpTemperatureControl VARCHAR(255) NULL COMMENT ''P.C. / control de temperatura del camarón en lavado (camarón)''',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

