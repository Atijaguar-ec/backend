-- Add week_number column to stock_order table for cacao deliveries
-- This field stores the week number (1-53) for cacao traceability

SET @tableExists := (
  SELECT COUNT(*)
  FROM information_schema.TABLES
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'StockOrder'
);

SET @columnExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'StockOrder'
    AND COLUMN_NAME = 'weekNumber'
);

SET @ddl := IF(
  @tableExists = 1 AND @columnExists = 0,
  'ALTER TABLE StockOrder ADD COLUMN weekNumber INT NULL COMMENT ''Week number for cacao deliveries (1-53)''',
  'SELECT 1'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add index for better query performance when filtering by week number (only if column was added)
SET @indexExists := (
  SELECT COUNT(*)
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'StockOrder'
    AND INDEX_NAME = 'idx_stock_order_week_number'
);

SET @indexDdl := IF(
  @tableExists = 1 AND @indexExists = 0 AND @columnExists = 0,
  'CREATE INDEX idx_stock_order_week_number ON StockOrder(weekNumber)',
  'SELECT 1'
);

PREPARE indexStmt FROM @indexDdl;
EXECUTE indexStmt;
DEALLOCATE PREPARE indexStmt;

-- Add check constraint to ensure week number is within valid range (1-53)
SET @constraintExists := (
  SELECT COUNT(*)
  FROM information_schema.TABLE_CONSTRAINTS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'StockOrder'
    AND CONSTRAINT_NAME = 'chk_stock_order_week_number'
);

SET @constraintDdl := IF(
  @tableExists = 1 AND @constraintExists = 0 AND @columnExists = 0,
  'ALTER TABLE StockOrder ADD CONSTRAINT chk_stock_order_week_number CHECK (weekNumber IS NULL OR (weekNumber >= 1 AND weekNumber <= 53))',
  'SELECT 1'
);

PREPARE constraintStmt FROM @constraintDdl;
EXECUTE constraintStmt;
DEALLOCATE PREPARE constraintStmt;
