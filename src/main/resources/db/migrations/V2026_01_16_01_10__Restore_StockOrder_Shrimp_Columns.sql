-- =====================================================
-- Migration: Restore shrimp columns in StockOrder
-- Date: 2026-01-16
-- Description: Restores shrimp-related StockOrder columns that may have been removed in non-shrimp deployments.
-- =====================================================

SET @stockOrderTableExists := (
  SELECT COUNT(*)
  FROM information_schema.TABLES
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'StockOrder'
);

-- flavor_defect_type_id
SET @colExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'StockOrder'
    AND COLUMN_NAME = 'flavor_defect_type_id'
);
SET @ddl := IF(
  @stockOrderTableExists = 1 AND @colExists = 0,
  'ALTER TABLE StockOrder ADD COLUMN flavor_defect_type_id BIGINT NULL',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- flavorTestResult
SET @colExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'StockOrder'
    AND COLUMN_NAME = 'flavorTestResult'
);
SET @ddl := IF(
  @stockOrderTableExists = 1 AND @colExists = 0,
  'ALTER TABLE StockOrder ADD COLUMN flavorTestResult VARCHAR(20) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- purchaseRecommended
SET @colExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'StockOrder'
    AND COLUMN_NAME = 'purchaseRecommended'
);
SET @ddl := IF(
  @stockOrderTableExists = 1 AND @colExists = 0,
  'ALTER TABLE StockOrder ADD COLUMN purchaseRecommended BIT(1) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- inspectionNotes
SET @colExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'StockOrder'
    AND COLUMN_NAME = 'inspectionNotes'
);
SET @ddl := IF(
  @stockOrderTableExists = 1 AND @colExists = 0,
  'ALTER TABLE StockOrder ADD COLUMN inspectionNotes VARCHAR(1000) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- field_inspection_id
SET @colExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'StockOrder'
    AND COLUMN_NAME = 'field_inspection_id'
);
SET @ddl := IF(
  @stockOrderTableExists = 1 AND @colExists = 0,
  'ALTER TABLE StockOrder ADD COLUMN field_inspection_id BIGINT NULL',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- sampleNumber
SET @colExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'StockOrder'
    AND COLUMN_NAME = 'sampleNumber'
);
SET @ddl := IF(
  @stockOrderTableExists = 1 AND @colExists = 0,
  'ALTER TABLE StockOrder ADD COLUMN sampleNumber VARCHAR(100) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- receptionTime
SET @colExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'StockOrder'
    AND COLUMN_NAME = 'receptionTime'
);
SET @ddl := IF(
  @stockOrderTableExists = 1 AND @colExists = 0,
  'ALTER TABLE StockOrder ADD COLUMN receptionTime TIME NULL',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- quality_document_id
SET @colExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'StockOrder'
    AND COLUMN_NAME = 'quality_document_id'
);
SET @ddl := IF(
  @stockOrderTableExists = 1 AND @colExists = 0,
  'ALTER TABLE StockOrder ADD COLUMN quality_document_id BIGINT NULL',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- numberOfGavetas
SET @colExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'StockOrder'
    AND COLUMN_NAME = 'numberOfGavetas'
);
SET @ddl := IF(
  @stockOrderTableExists = 1 AND @colExists = 0,
  'ALTER TABLE StockOrder ADD COLUMN numberOfGavetas INT NULL',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- numberOfBines
SET @colExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'StockOrder'
    AND COLUMN_NAME = 'numberOfBines'
);
SET @ddl := IF(
  @stockOrderTableExists = 1 AND @colExists = 0,
  'ALTER TABLE StockOrder ADD COLUMN numberOfBines VARCHAR(50) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- numberOfPiscinas
SET @colExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'StockOrder'
    AND COLUMN_NAME = 'numberOfPiscinas'
);
SET @ddl := IF(
  @stockOrderTableExists = 1 AND @colExists = 0,
  'ALTER TABLE StockOrder ADD COLUMN numberOfPiscinas VARCHAR(50) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- guiaRemisionNumber
SET @colExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'StockOrder'
    AND COLUMN_NAME = 'guiaRemisionNumber'
);
SET @ddl := IF(
  @stockOrderTableExists = 1 AND @colExists = 0,
  'ALTER TABLE StockOrder ADD COLUMN guiaRemisionNumber VARCHAR(100) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- cuttingType
SET @colExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'StockOrder'
    AND COLUMN_NAME = 'cuttingType'
);
SET @ddl := IF(
  @stockOrderTableExists = 1 AND @colExists = 0,
  'ALTER TABLE StockOrder ADD COLUMN cuttingType VARCHAR(100) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- cuttingEntryDate
SET @colExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'StockOrder'
    AND COLUMN_NAME = 'cuttingEntryDate'
);
SET @ddl := IF(
  @stockOrderTableExists = 1 AND @colExists = 0,
  'ALTER TABLE StockOrder ADD COLUMN cuttingEntryDate DATE NULL',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- cuttingExitDate
SET @colExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'StockOrder'
    AND COLUMN_NAME = 'cuttingExitDate'
);
SET @ddl := IF(
  @stockOrderTableExists = 1 AND @colExists = 0,
  'ALTER TABLE StockOrder ADD COLUMN cuttingExitDate DATE NULL',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- cuttingTemperatureControl
SET @colExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'StockOrder'
    AND COLUMN_NAME = 'cuttingTemperatureControl'
);
SET @ddl := IF(
  @stockOrderTableExists = 1 AND @colExists = 0,
  'ALTER TABLE StockOrder ADD COLUMN cuttingTemperatureControl VARCHAR(255) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- treatmentType
SET @colExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'StockOrder'
    AND COLUMN_NAME = 'treatmentType'
);
SET @ddl := IF(
  @stockOrderTableExists = 1 AND @colExists = 0,
  'ALTER TABLE StockOrder ADD COLUMN treatmentType VARCHAR(100) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- treatmentEntryDate
SET @colExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'StockOrder'
    AND COLUMN_NAME = 'treatmentEntryDate'
);
SET @ddl := IF(
  @stockOrderTableExists = 1 AND @colExists = 0,
  'ALTER TABLE StockOrder ADD COLUMN treatmentEntryDate DATE NULL',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- treatmentExitDate
SET @colExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'StockOrder'
    AND COLUMN_NAME = 'treatmentExitDate'
);
SET @ddl := IF(
  @stockOrderTableExists = 1 AND @colExists = 0,
  'ALTER TABLE StockOrder ADD COLUMN treatmentExitDate DATE NULL',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- treatmentTemperatureControl
SET @colExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'StockOrder'
    AND COLUMN_NAME = 'treatmentTemperatureControl'
);
SET @ddl := IF(
  @stockOrderTableExists = 1 AND @colExists = 0,
  'ALTER TABLE StockOrder ADD COLUMN treatmentTemperatureControl VARCHAR(255) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- treatmentChemicalUsed
SET @colExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'StockOrder'
    AND COLUMN_NAME = 'treatmentChemicalUsed'
);
SET @ddl := IF(
  @stockOrderTableExists = 1 AND @colExists = 0,
  'ALTER TABLE StockOrder ADD COLUMN treatmentChemicalUsed VARCHAR(255) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- tunnelProductionDate
SET @colExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'StockOrder'
    AND COLUMN_NAME = 'tunnelProductionDate'
);
SET @ddl := IF(
  @stockOrderTableExists = 1 AND @colExists = 0,
  'ALTER TABLE StockOrder ADD COLUMN tunnelProductionDate DATE NULL',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- tunnelExpirationDate
SET @colExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'StockOrder'
    AND COLUMN_NAME = 'tunnelExpirationDate'
);
SET @ddl := IF(
  @stockOrderTableExists = 1 AND @colExists = 0,
  'ALTER TABLE StockOrder ADD COLUMN tunnelExpirationDate DATE NULL',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- tunnelNetWeight
SET @colExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'StockOrder'
    AND COLUMN_NAME = 'tunnelNetWeight'
);
SET @ddl := IF(
  @stockOrderTableExists = 1 AND @colExists = 0,
  'ALTER TABLE StockOrder ADD COLUMN tunnelNetWeight DECIMAL(38,2) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- tunnelSupplier
SET @colExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'StockOrder'
    AND COLUMN_NAME = 'tunnelSupplier'
);
SET @ddl := IF(
  @stockOrderTableExists = 1 AND @colExists = 0,
  'ALTER TABLE StockOrder ADD COLUMN tunnelSupplier VARCHAR(255) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- tunnelFreezingType
SET @colExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'StockOrder'
    AND COLUMN_NAME = 'tunnelFreezingType'
);
SET @ddl := IF(
  @stockOrderTableExists = 1 AND @colExists = 0,
  'ALTER TABLE StockOrder ADD COLUMN tunnelFreezingType VARCHAR(100) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- tunnelEntryDate
SET @colExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'StockOrder'
    AND COLUMN_NAME = 'tunnelEntryDate'
);
SET @ddl := IF(
  @stockOrderTableExists = 1 AND @colExists = 0,
  'ALTER TABLE StockOrder ADD COLUMN tunnelEntryDate DATE NULL',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- tunnelExitDate
SET @colExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'StockOrder'
    AND COLUMN_NAME = 'tunnelExitDate'
);
SET @ddl := IF(
  @stockOrderTableExists = 1 AND @colExists = 0,
  'ALTER TABLE StockOrder ADD COLUMN tunnelExitDate DATE NULL',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- washingWaterTemperature
SET @colExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'StockOrder'
    AND COLUMN_NAME = 'washingWaterTemperature'
);
SET @ddl := IF(
  @stockOrderTableExists = 1 AND @colExists = 0,
  'ALTER TABLE StockOrder ADD COLUMN washingWaterTemperature VARCHAR(100) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- washingShrimpTemperatureControl
SET @colExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'StockOrder'
    AND COLUMN_NAME = 'washingShrimpTemperatureControl'
);
SET @ddl := IF(
  @stockOrderTableExists = 1 AND @colExists = 0,
  'ALTER TABLE StockOrder ADD COLUMN washingShrimpTemperatureControl VARCHAR(255) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
