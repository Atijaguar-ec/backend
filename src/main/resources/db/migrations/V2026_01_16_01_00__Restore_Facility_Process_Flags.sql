-- =====================================================
-- Migration: Restore Facility process flags
-- Date: 2026-01-16
-- Description: Restores Facility boolean flags that may have been removed in non-shrimp deployments.
-- =====================================================

SET @facilityTableExists := (
  SELECT COUNT(*)
  FROM information_schema.TABLES
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'Facility'
);

-- isFieldInspection
SET @isFieldInspectionExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'Facility'
    AND COLUMN_NAME = 'isFieldInspection'
);
SET @ddl := IF(
  @facilityTableExists = 1 AND @isFieldInspectionExists = 0,
  'ALTER TABLE Facility ADD COLUMN isFieldInspection BIT(1) DEFAULT 0',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- isLaboratory
SET @isLaboratoryExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'Facility'
    AND COLUMN_NAME = 'isLaboratory'
);
SET @ddl := IF(
  @facilityTableExists = 1 AND @isLaboratoryExists = 0,
  'ALTER TABLE Facility ADD COLUMN isLaboratory BIT(1) DEFAULT 0',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- isClassificationProcess
SET @isClassificationProcessExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'Facility'
    AND COLUMN_NAME = 'isClassificationProcess'
);
SET @ddl := IF(
  @facilityTableExists = 1 AND @isClassificationProcessExists = 0,
  'ALTER TABLE Facility ADD COLUMN isClassificationProcess BIT(1) DEFAULT 0',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- isFreezingProcess
SET @isFreezingProcessExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'Facility'
    AND COLUMN_NAME = 'isFreezingProcess'
);
SET @ddl := IF(
  @facilityTableExists = 1 AND @isFreezingProcessExists = 0,
  'ALTER TABLE Facility ADD COLUMN isFreezingProcess BIT(1) DEFAULT 0',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
