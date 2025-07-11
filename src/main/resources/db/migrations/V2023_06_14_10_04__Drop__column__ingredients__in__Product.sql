SET @col_exists := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'Product'
    AND COLUMN_NAME = 'ingredients'
);

SET @sql := IF(@col_exists > 0, 'ALTER TABLE Product DROP COLUMN ingredients;', 'SELECT 1;');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
