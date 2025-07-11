-- Eliminar relationship de Responsibility solo si existe
SET @col_exists := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'Responsibility'
    AND COLUMN_NAME = 'relationship'
);
SET @sql := IF(@col_exists > 0, 'ALTER TABLE Responsibility DROP COLUMN relationship;', 'SELECT 1;');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
-- Eliminar farmer de Responsibility solo si existe
SET @col_exists := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'Responsibility'
    AND COLUMN_NAME = 'farmer'
);
SET @sql := IF(@col_exists > 0, 'ALTER TABLE Responsibility DROP COLUMN farmer;', 'SELECT 1;');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
-- Eliminar story de Responsibility solo si existe
SET @col_exists := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'Responsibility'
    AND COLUMN_NAME = 'story'
);
SET @sql := IF(@col_exists > 0, 'ALTER TABLE Responsibility DROP COLUMN story;', 'SELECT 1;');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
