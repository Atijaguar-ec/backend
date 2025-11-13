-- =====================================================
-- Migration: Add Freezing Process Flag to Facility
-- Date: 2025-11-13
-- Description: Adds support for shrimp freezing process flag on facilities
-- =====================================================

-- 1. Add isFreezingProcess to Facility table (if not present)
SET @facilityTableExists := (
  SELECT COUNT(*)
  FROM information_schema.TABLES
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'Facility'
);

SET @isFreezingProcessColumnExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'Facility'
    AND COLUMN_NAME = 'isFreezingProcess'
);

SET @facilityDdl := IF(
  @facilityTableExists = 1 AND @isFreezingProcessColumnExists = 0,
  'ALTER TABLE Facility ADD COLUMN isFreezingProcess BIT(1) DEFAULT 0 COMMENT ''Indicates if this facility performs a freezing process (shrimp-specific)''',
  'SELECT 1'
);

PREPARE facilityStmt FROM @facilityDdl;
EXECUTE facilityStmt;
DEALLOCATE PREPARE facilityStmt;
