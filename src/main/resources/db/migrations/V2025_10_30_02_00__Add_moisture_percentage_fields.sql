-- Add moisture percentage configuration to Facility table
SET @facilityTableExists := (
  SELECT COUNT(*)
  FROM information_schema.TABLES
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'Facility'
);

SET @facilityColumnExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'Facility'
    AND COLUMN_NAME = 'displayMoisturePercentage'
);

SET @facilityDdl := IF(
  @facilityTableExists = 1 AND @facilityColumnExists = 0,
  'ALTER TABLE Facility ADD COLUMN displayMoisturePercentage BIT(1) DEFAULT 0',
  'SELECT 1'
);

PREPARE facilityStmt FROM @facilityDdl;
EXECUTE facilityStmt;
DEALLOCATE PREPARE facilityStmt;

-- Add moisture percentage fields to StockOrder table
SET @stockOrderTableExists := (
  SELECT COUNT(*)
  FROM information_schema.TABLES
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'StockOrder'
);

SET @moisturePercentageColumnExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'StockOrder'
    AND COLUMN_NAME = 'moisturePercentage'
);

SET @moisturePercentageDdl := IF(
  @stockOrderTableExists = 1 AND @moisturePercentageColumnExists = 0,
  'ALTER TABLE StockOrder ADD COLUMN moisturePercentage DECIMAL(5,2) DEFAULT NULL',
  'SELECT 1'
);

PREPARE moisturePercentageStmt FROM @moisturePercentageDdl;
EXECUTE moisturePercentageStmt;
DEALLOCATE PREPARE moisturePercentageStmt;

SET @moistureWeightDeductionColumnExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'StockOrder'
    AND COLUMN_NAME = 'moistureWeightDeduction'
);

SET @moistureWeightDeductionDdl := IF(
  @stockOrderTableExists = 1 AND @moistureWeightDeductionColumnExists = 0,
  'ALTER TABLE StockOrder ADD COLUMN moistureWeightDeduction DECIMAL(38,2) DEFAULT NULL',
  'SELECT 1'
);

PREPARE moistureWeightDeductionStmt FROM @moistureWeightDeductionDdl;
EXECUTE moistureWeightDeductionStmt;
DEALLOCATE PREPARE moistureWeightDeductionStmt;
