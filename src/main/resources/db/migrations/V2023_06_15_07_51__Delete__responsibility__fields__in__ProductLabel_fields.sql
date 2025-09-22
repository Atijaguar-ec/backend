-- Safe deletion: only if ProductLabel_fields exists
SET @tbl_exists := (
  SELECT COUNT(1)
  FROM information_schema.tables
  WHERE table_schema = DATABASE()
    AND table_name = 'ProductLabel_fields'
);

SET @sql := IF(
  @tbl_exists > 0,
  'DELETE FROM ProductLabel_fields WHERE fields_name IN (''responsibility.relationship'',''responsibility.pictures'',''responsibility.farmer'',''responsibility.story'')',
  'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
