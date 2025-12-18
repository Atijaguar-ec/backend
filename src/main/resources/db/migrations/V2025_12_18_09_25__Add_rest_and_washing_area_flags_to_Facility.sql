SET @facilityTableExists := (
  SELECT COUNT(*)
  FROM information_schema.TABLES
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'Facility'
);

SET @isRestAreaColumnExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'Facility'
    AND COLUMN_NAME = 'isRestArea'
);

SET @facilityRestAreaDdl := IF(
  @facilityTableExists = 1 AND @isRestAreaColumnExists = 0,
  'ALTER TABLE Facility ADD COLUMN isRestArea BIT(1) DEFAULT 0',
  'SELECT 1'
);

PREPARE facilityRestAreaStmt FROM @facilityRestAreaDdl;
EXECUTE facilityRestAreaStmt;
DEALLOCATE PREPARE facilityRestAreaStmt;

SET @isWashingAreaColumnExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'Facility'
    AND COLUMN_NAME = 'isWashingArea'
);

SET @facilityWashingAreaDdl := IF(
  @facilityTableExists = 1 AND @isWashingAreaColumnExists = 0,
  'ALTER TABLE Facility ADD COLUMN isWashingArea BIT(1) DEFAULT 0',
  'SELECT 1'
);

PREPARE facilityWashingAreaStmt FROM @facilityWashingAreaDdl;
EXECUTE facilityWashingAreaStmt;
DEALLOCATE PREPARE facilityWashingAreaStmt;
