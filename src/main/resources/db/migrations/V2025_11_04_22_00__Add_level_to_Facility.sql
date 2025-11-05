-- Add level column to Facility for custom ordering
SET @facilityTableExists := (
  SELECT COUNT(*)
  FROM information_schema.TABLES
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'Facility'
);

SET @levelColumnExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'Facility'
    AND COLUMN_NAME = 'level'
);

SET @addLevelColumn := IF(
  @facilityTableExists = 1 AND @levelColumnExists = 0,
  'ALTER TABLE Facility ADD COLUMN level INT DEFAULT NULL',
  'SELECT 1'
);

PREPARE addLevelStmt FROM @addLevelColumn;
EXECUTE addLevelStmt;
DEALLOCATE PREPARE addLevelStmt;

-- Initialize facility levels with the  facility type order when missing
SET @updateLevels := IF(
  @facilityTableExists = 1,
  'UPDATE Facility f LEFT JOIN FacilityType ft ON f.facilityType_id = ft.id SET f.level = ft.`order` WHERE f.level IS NULL',
  'SELECT 1'
);

PREPARE updateLevelStmt FROM @updateLevels;
EXECUTE updateLevelStmt;
DEALLOCATE PREPARE updateLevelStmt;
