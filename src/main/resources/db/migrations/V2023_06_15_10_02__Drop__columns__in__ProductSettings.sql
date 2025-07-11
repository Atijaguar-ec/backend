-- Eliminar checkAuthenticity de ProductSettings solo si existe
SET @col_exists := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'ProductSettings'
    AND COLUMN_NAME = 'checkAuthenticity'
);
SET @sql := IF(@col_exists > 0, 'ALTER TABLE ProductSettings DROP COLUMN checkAuthenticity;', 'SELECT 1;');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
-- Eliminar traceOrigin de ProductSettings solo si existe
SET @col_exists := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'ProductSettings'
    AND COLUMN_NAME = 'traceOrigin'
);
SET @sql := IF(@col_exists > 0, 'ALTER TABLE ProductSettings DROP COLUMN traceOrigin;', 'SELECT 1;');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
-- Eliminar giveFeedback de ProductSettings solo si existe
SET @col_exists := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'ProductSettings'
    AND COLUMN_NAME = 'giveFeedback'
);
SET @sql := IF(@col_exists > 0, 'ALTER TABLE ProductSettings DROP COLUMN giveFeedback;', 'SELECT 1;');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
