-- =====================================================
-- Migration: Add isDeheadingProcess flag to Facility
-- Date: 2025-11-28
-- Description: Adds support for shrimp deheading (descabezado) process flag on facilities
-- =====================================================

-- 1. Add isDeheadingProcess to Facility table (if not present)
SET @facilityTableExists := (
  SELECT COUNT(*)
  FROM information_schema.TABLES
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'Facility'
);

SET @isDeheadingProcessColumnExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'Facility'
    AND COLUMN_NAME = 'isDeheadingProcess'
);

SET @facilityDdl := IF(
  @facilityTableExists = 1 AND @isDeheadingProcessColumnExists = 0,
  'ALTER TABLE Facility ADD COLUMN isDeheadingProcess BIT(1) DEFAULT 0 COMMENT ''Indicates if this facility performs a deheading process (shrimp-specific, descabezado)''',
  'SELECT 1'
);

PREPARE stmt FROM @facilityDdl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
