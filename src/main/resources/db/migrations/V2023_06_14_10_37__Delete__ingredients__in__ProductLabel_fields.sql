-- Safe deletion: only if ProductLabel_fields exists in current schema
SET @tbl_exists := (
  SELECT COUNT(1)
  FROM information_schema.tables
  WHERE table_schema = DATABASE()
    AND table_name = 'ProductLabel_fields'
);

SET @sql := IF(
  @tbl_exists > 0,
  'DELETE FROM ProductLabel_fields WHERE fields_name = ''ingredients''',
  'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
