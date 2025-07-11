-- Eliminar nutritionalValue de Product solo si existe
SET @col_exists := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'Product'
    AND COLUMN_NAME = 'nutritionalValue'
);
SET @sql := IF(@col_exists > 0, 'ALTER TABLE Product DROP COLUMN nutritionalValue;', 'SELECT 1;');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Eliminar nutritionalValue de ProductLabelContent solo si existe
SET @col_exists := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'ProductLabelContent'
    AND COLUMN_NAME = 'nutritionalValue'
);
SET @sql := IF(@col_exists > 0, 'ALTER TABLE ProductLabelContent DROP COLUMN nutritionalValue;', 'SELECT 1;');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
