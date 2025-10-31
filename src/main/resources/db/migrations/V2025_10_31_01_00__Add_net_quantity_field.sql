-- Add net_quantity field to stock_order table
-- This field stores the final net quantity after all deductions (tare, damaged weight, moisture)

-- Check if StockOrder table exists
SET @stockOrderTableExists := (
  SELECT COUNT(*)
  FROM information_schema.TABLES
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'StockOrder'
);

-- Check if netQuantity column already exists
SET @netQuantityColumnExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'StockOrder'
    AND COLUMN_NAME = 'netQuantity'
);

-- Add column only if table exists and column doesn't exist
SET @netQuantityDdl := IF(
  @stockOrderTableExists = 1 AND @netQuantityColumnExists = 0,
  'ALTER TABLE StockOrder ADD COLUMN netQuantity DECIMAL(38,2) DEFAULT NULL',
  'SELECT ''Column netQuantity already exists or table does not exist'''
);

PREPARE netQuantityStmt FROM @netQuantityDdl;
EXECUTE netQuantityStmt;
DEALLOCATE PREPARE netQuantityStmt;
