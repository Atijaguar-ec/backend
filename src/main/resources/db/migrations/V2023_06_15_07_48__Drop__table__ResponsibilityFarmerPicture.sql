-- Eliminar tabla ResponsibilityFarmerPicture solo si existe
SET @table_exists := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'ResponsibilityFarmerPicture'
);
SET @sql := IF(@table_exists > 0, 'DROP TABLE ResponsibilityFarmerPicture;', 'SELECT 1;');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
