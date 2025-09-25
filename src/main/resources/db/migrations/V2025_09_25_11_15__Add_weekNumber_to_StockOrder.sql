-- Add week_number column to stock_order table for cacao deliveries
-- This field stores the week number (1-53) for cacao traceability

SET @columnExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'stock_order'
    AND COLUMN_NAME = 'week_number'
);

SET @ddl := IF(
  @columnExists = 0,
  'ALTER TABLE stock_order ADD COLUMN week_number INT NULL COMMENT ''Week number for cacao deliveries (1-53)''',
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
    AND TABLE_NAME = 'stock_order'
    AND INDEX_NAME = 'idx_stock_order_week_number'
);

SET @indexDdl := IF(
  @indexExists = 0 AND @columnExists = 0,
  'CREATE INDEX idx_stock_order_week_number ON stock_order(week_number)',
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
    AND TABLE_NAME = 'stock_order'
    AND CONSTRAINT_NAME = 'chk_stock_order_week_number'
);

SET @constraintDdl := IF(
  @constraintExists = 0 AND @columnExists = 0,
  'ALTER TABLE stock_order ADD CONSTRAINT chk_stock_order_week_number CHECK (week_number IS NULL OR (week_number >= 1 AND week_number <= 53))',
  'SELECT 1'
);

PREPARE constraintStmt FROM @constraintDdl;
EXECUTE constraintStmt;
DEALLOCATE PREPARE constraintStmt;
