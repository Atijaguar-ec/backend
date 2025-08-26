-- Drop the unique index created by previous @OneToOne mapping on Transaction.sourceStockOrder
-- If the index does not exist, this migration will no-op.

-- NOTE: The unique index `UKtqkhranfyfkv32rh6jaogmrwa` is on column `inputMeasureUnitType_id` and
-- may be used by the foreign key constraint. We must drop the FK first, then drop the index,
-- create a non-unique index, and finally recreate the FK.

-- 1) Drop FK on `inputMeasureUnitType_id` if it exists
SET @fk := (
  SELECT CONSTRAINT_NAME
  FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'Transaction'
    AND COLUMN_NAME = 'inputMeasureUnitType_id'
    AND REFERENCED_TABLE_NAME = 'MeasureUnitType'
  LIMIT 1
);
SET @sql_fk := IF(@fk IS NOT NULL,
  CONCAT('ALTER TABLE `Transaction` DROP FOREIGN KEY `', @fk, '`'),
  'SELECT 1'
);
PREPARE stmt FROM @sql_fk;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2) Drop the unique index if it exists
SET @idx := (
  SELECT INDEX_NAME
  FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'Transaction'
    AND INDEX_NAME = 'UKtqkhranfyfkv32rh6jaogmrwa'
  LIMIT 1
);
SET @sql_idx := IF(@idx IS NOT NULL,
  'ALTER TABLE `Transaction` DROP INDEX `UKtqkhranfyfkv32rh6jaogmrwa`',
  'SELECT 1'
);
PREPARE stmt FROM @sql_idx;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3) Ensure a non-unique index exists on the FK column
SET @has_idx := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'Transaction'
    AND INDEX_NAME = 'idx_transaction_input_measure_unit_type'
);
SET @sql_create_idx := IF(@has_idx = 0,
  'CREATE INDEX `idx_transaction_input_measure_unit_type` ON `Transaction` (`inputMeasureUnitType_id`)',
  'SELECT 1'
);
PREPARE stmt FROM @sql_create_idx;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 4) Recreate the FK if it does not exist
SET @fk2 := (
  SELECT CONSTRAINT_NAME
  FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'Transaction'
    AND COLUMN_NAME = 'inputMeasureUnitType_id'
    AND REFERENCED_TABLE_NAME = 'MeasureUnitType'
  LIMIT 1
);
SET @sql_fk_add := IF(@fk2 IS NULL,
  'ALTER TABLE `Transaction` ADD CONSTRAINT `FK_transaction_input_measure_unit_type` FOREIGN KEY (`inputMeasureUnitType_id`) REFERENCES `MeasureUnitType`(`id`)',
  'SELECT 1'
);
PREPARE stmt FROM @sql_fk_add;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
