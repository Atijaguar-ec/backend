-- Eliminar tabla ProcessStandard solo si existe
SET @table_exists := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'ProcessStandard'
);
SET @sql := IF(@table_exists > 0, 'DROP TABLE ProcessStandard;', 'SELECT 1;');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Eliminar tabla ProcessDocument solo si existe
SET @table_exists := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'ProcessDocument'
);
SET @sql := IF(@table_exists > 0, 'DROP TABLE ProcessDocument;', 'SELECT 1;');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
