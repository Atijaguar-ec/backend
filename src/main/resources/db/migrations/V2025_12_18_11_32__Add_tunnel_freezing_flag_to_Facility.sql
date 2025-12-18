SET @facilityTableExists := (
  SELECT COUNT(*)
  FROM information_schema.TABLES
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'Facility'
);

SET @isTunnelFreezingColumnExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'Facility'
    AND COLUMN_NAME = 'isTunnelFreezing'
);

SET @facilityDdl := IF(
  @facilityTableExists = 1 AND @isTunnelFreezingColumnExists = 0,
  'ALTER TABLE Facility ADD COLUMN isTunnelFreezing BIT(1) DEFAULT 0 COMMENT ''Indicates if this facility has a tunnel freezing area (shrimp-specific)''',
  'SELECT 1'
);

PREPARE stmt FROM @facilityDdl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
