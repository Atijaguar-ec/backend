-- Eliminar storage de Process solo si existe
SET @col_exists := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'Process'
    AND COLUMN_NAME = 'storage'
);
SET @sql := IF(@col_exists > 0, 'ALTER TABLE Process DROP COLUMN storage;', 'SELECT 1;');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
-- Eliminar codesOfConduct de Process solo si existe
SET @col_exists := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'Process'
    AND COLUMN_NAME = 'codesOfConduct'
);
SET @sql := IF(@col_exists > 0, 'ALTER TABLE Process DROP COLUMN codesOfConduct;', 'SELECT 1;');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
