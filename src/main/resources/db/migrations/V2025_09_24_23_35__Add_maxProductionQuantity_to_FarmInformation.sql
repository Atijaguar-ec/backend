-- Add maxProductionQuantity field to farm information
-- This field stores the maximum production quantity in quintals (qq)

SET @columnExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'user_customer'
    AND COLUMN_NAME = 'max_production_quantity'
);

SET @ddl := IF(
  @columnExists = 0,
  'ALTER TABLE user_customer ADD COLUMN max_production_quantity DECIMAL(19,2) NULL COMMENT ''Maximum production quantity in quintals (qq)''',
  'SELECT 1'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;