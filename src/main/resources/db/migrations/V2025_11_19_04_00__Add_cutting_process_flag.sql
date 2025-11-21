-- =====================================================
-- Migration: Add isCuttingProcess flag to Facility
-- Date: 2025-11-19
-- Description: Adds support for shrimp cutting process flag on facilities
-- =====================================================

-- 1. Add isCuttingProcess to Facility table (if not present)
SET @facilityTableExists := (
  SELECT COUNT(*)
  FROM information_schema.TABLES
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'Facility'
);

SET @isCuttingProcessColumnExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'Facility'
    AND COLUMN_NAME = 'isCuttingProcess'
);

SET @facilityDdl := IF(
  @facilityTableExists = 1 AND @isCuttingProcessColumnExists = 0,
  'ALTER TABLE Facility ADD COLUMN isCuttingProcess BIT(1) DEFAULT 0 COMMENT ''Indicates if this facility performs a cutting process (shrimp-specific)''',
  'SELECT 1'
);

PREPARE stmt FROM @facilityDdl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
