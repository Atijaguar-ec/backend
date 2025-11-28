-- Add isFieldInspection column to facility table
-- This field indicates if the facility is a field inspection point for shrimp sensory testing

-- Use INFORMATION_SCHEMA to add the column only if it does not already exist
SET @column_exists := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'Facility'
    AND COLUMN_NAME = 'isFieldInspection'
);

SET @sql := IF(@column_exists = 0,
  'ALTER TABLE `Facility` ADD COLUMN `isFieldInspection` BIT(1) DEFAULT 0',
  'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
