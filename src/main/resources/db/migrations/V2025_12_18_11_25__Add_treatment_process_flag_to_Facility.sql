SET @facilityTableExists := (
  SELECT COUNT(*)
  FROM information_schema.TABLES
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'Facility'
);

SET @isTreatmentProcessColumnExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'Facility'
    AND COLUMN_NAME = 'isTreatmentProcess'
);

SET @facilityDdl := IF(
  @facilityTableExists = 1 AND @isTreatmentProcessColumnExists = 0,
  'ALTER TABLE Facility ADD COLUMN isTreatmentProcess BIT(1) DEFAULT 0 COMMENT ''Indicates if this facility performs a treatment process (shrimp-specific)''',
  'SELECT 1'
);

PREPARE stmt FROM @facilityDdl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
