-- Safely replace unique index on Transaction.inputMeasureUnitType_id with a non-unique index
-- Steps: drop FK -> drop unique index -> create non-unique index -> recreate FK

-- 1) Drop FK on `inputMeasureUnitType_id` if present
SET @fk_mut := (
  SELECT CONSTRAINT_NAME
  FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'Transaction'
    AND COLUMN_NAME = 'inputMeasureUnitType_id'
    AND REFERENCED_TABLE_NAME = 'MeasureUnitType'
  LIMIT 1
);
SET @sql_drop_fk_mut := IF(@fk_mut IS NOT NULL,
  CONCAT('ALTER TABLE `Transaction` DROP FOREIGN KEY `', @fk_mut, '`'),
  'SELECT 1'
);
PREPARE s1 FROM @sql_drop_fk_mut; EXECUTE s1; DEALLOCATE PREPARE s1;

-- 2) Find any unique index exclusively on inputMeasureUnitType_id and drop it
SET @uniq_idx := (
  SELECT INDEX_NAME
  FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'Transaction'
    AND NON_UNIQUE = 0
    AND INDEX_NAME <> 'PRIMARY'
  GROUP BY INDEX_NAME
  HAVING GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) = 'inputMeasureUnitType_id'
  LIMIT 1
);
SET @sql_drop_idx := IF(@uniq_idx IS NOT NULL,
  CONCAT('ALTER TABLE `Transaction` DROP INDEX `', @uniq_idx, '`'),
  'SELECT 1'
);
PREPARE s2 FROM @sql_drop_idx; EXECUTE s2; DEALLOCATE PREPARE s2;

-- 3) Ensure a non-unique index exists on inputMeasureUnitType_id
SET @has_nonuniq := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'Transaction'
    AND INDEX_NAME = 'idx_transaction_input_measure_unit_type'
);
SET @sql_create_idx := IF(@has_nonuniq = 0,
  'CREATE INDEX `idx_transaction_input_measure_unit_type` ON `Transaction` (`inputMeasureUnitType_id`)',
  'SELECT 1'
);
PREPARE s3 FROM @sql_create_idx; EXECUTE s3; DEALLOCATE PREPARE s3;

-- 4) Recreate the FK if missing
SET @fk_mut2 := (
  SELECT CONSTRAINT_NAME
  FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'Transaction'
    AND COLUMN_NAME = 'inputMeasureUnitType_id'
    AND REFERENCED_TABLE_NAME = 'MeasureUnitType'
  LIMIT 1
);
SET @sql_add_fk := IF(@fk_mut2 IS NULL,
  'ALTER TABLE `Transaction` ADD CONSTRAINT `FK_transaction_input_measure_unit_type` FOREIGN KEY (`inputMeasureUnitType_id`) REFERENCES `MeasureUnitType`(`id`)',
  'SELECT 1'
);
PREPARE s4 FROM @sql_add_fk; EXECUTE s4; DEALLOCATE PREPARE s4;
