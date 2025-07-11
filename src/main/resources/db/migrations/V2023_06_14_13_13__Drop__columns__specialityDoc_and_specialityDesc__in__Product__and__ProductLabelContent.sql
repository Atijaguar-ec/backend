-- Eliminar clave foránea FK5414co2yomrmr1wbiahodoi6j de Product solo si existe
SET @fk_exists := (SELECT COUNT(*) 
                  FROM information_schema.TABLE_CONSTRAINTS 
                  WHERE CONSTRAINT_SCHEMA = DATABASE() 
                  AND TABLE_NAME = 'Product' 
                  AND CONSTRAINT_NAME = 'FK5414co2yomrmr1wbiahodoi6j'
                  AND CONSTRAINT_TYPE = 'FOREIGN KEY');
SET @sql = IF(@fk_exists > 0, 'ALTER TABLE Product DROP FOREIGN KEY FK5414co2yomrmr1wbiahodoi6j;', 'SELECT 1;');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
-- Eliminar specialityDocument_id de Product solo si existe
SET @col_exists := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'Product'
    AND COLUMN_NAME = 'specialityDocument_id'
);
SET @sql := IF(@col_exists > 0, 'ALTER TABLE Product DROP COLUMN specialityDocument_id;', 'SELECT 1;');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
-- Eliminar specialityDescription de Product solo si existe
SET @col_exists := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'Product'
    AND COLUMN_NAME = 'specialityDescription'
);
SET @sql := IF(@col_exists > 0, 'ALTER TABLE Product DROP COLUMN specialityDescription;', 'SELECT 1;');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Eliminar clave foránea FKo78331b0piyfvhsjhibs5ckmo de ProductLabelContent solo si existe
SET @fk_exists := (SELECT COUNT(*) 
                  FROM information_schema.TABLE_CONSTRAINTS 
                  WHERE CONSTRAINT_SCHEMA = DATABASE() 
                  AND TABLE_NAME = 'ProductLabelContent' 
                  AND CONSTRAINT_NAME = 'FKo78331b0piyfvhsjhibs5ckmo'
                  AND CONSTRAINT_TYPE = 'FOREIGN KEY');
SET @sql = IF(@fk_exists > 0, 'ALTER TABLE ProductLabelContent DROP FOREIGN KEY FKo78331b0piyfvhsjhibs5ckmo;', 'SELECT 1;');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
-- Eliminar specialityDocument_id de ProductLabelContent solo si existe
SET @col_exists := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'ProductLabelContent'
    AND COLUMN_NAME = 'specialityDocument_id'
);
SET @sql := IF(@col_exists > 0, 'ALTER TABLE ProductLabelContent DROP COLUMN specialityDocument_id;', 'SELECT 1;');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
-- Eliminar specialityDescription de ProductLabelContent solo si existe
SET @col_exists := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'ProductLabelContent'
    AND COLUMN_NAME = 'specialityDescription'
);
SET @sql := IF(@col_exists > 0, 'ALTER TABLE ProductLabelContent DROP COLUMN specialityDescription;', 'SELECT 1;');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
