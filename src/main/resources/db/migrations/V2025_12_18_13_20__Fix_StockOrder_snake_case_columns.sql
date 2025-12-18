SET @table_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'StockOrder');

SET @old_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'StockOrder' AND COLUMN_NAME = 'freezing_type');
SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'StockOrder' AND COLUMN_NAME = 'freezingType');
SET @sql = IF(@table_exists > 0 AND @old_exists > 0 AND @new_exists = 0,
  'ALTER TABLE StockOrder CHANGE COLUMN freezing_type freezingType VARCHAR(100) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'StockOrder' AND COLUMN_NAME = 'freezingType');
SET @sql = IF(@table_exists > 0 AND @new_exists = 0,
  'ALTER TABLE StockOrder ADD COLUMN freezingType VARCHAR(100) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @old_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'StockOrder' AND COLUMN_NAME = 'freezing_type');
SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'StockOrder' AND COLUMN_NAME = 'freezingType');
SET @sql = IF(@table_exists > 0 AND @old_exists > 0 AND @new_exists > 0,
  'UPDATE StockOrder SET freezingType = freezing_type WHERE freezingType IS NULL AND freezing_type IS NOT NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @old_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'StockOrder' AND COLUMN_NAME = 'freezing_entry_date');
SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'StockOrder' AND COLUMN_NAME = 'freezingEntryDate');
SET @sql = IF(@table_exists > 0 AND @old_exists > 0 AND @new_exists = 0,
  'ALTER TABLE StockOrder CHANGE COLUMN freezing_entry_date freezingEntryDate DATE NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'StockOrder' AND COLUMN_NAME = 'freezingEntryDate');
SET @sql = IF(@table_exists > 0 AND @new_exists = 0,
  'ALTER TABLE StockOrder ADD COLUMN freezingEntryDate DATE NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @old_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'StockOrder' AND COLUMN_NAME = 'freezing_entry_date');
SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'StockOrder' AND COLUMN_NAME = 'freezingEntryDate');
SET @sql = IF(@table_exists > 0 AND @old_exists > 0 AND @new_exists > 0,
  'UPDATE StockOrder SET freezingEntryDate = freezing_entry_date WHERE freezingEntryDate IS NULL AND freezing_entry_date IS NOT NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @old_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'StockOrder' AND COLUMN_NAME = 'freezing_exit_date');
SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'StockOrder' AND COLUMN_NAME = 'freezingExitDate');
SET @sql = IF(@table_exists > 0 AND @old_exists > 0 AND @new_exists = 0,
  'ALTER TABLE StockOrder CHANGE COLUMN freezing_exit_date freezingExitDate DATE NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'StockOrder' AND COLUMN_NAME = 'freezingExitDate');
SET @sql = IF(@table_exists > 0 AND @new_exists = 0,
  'ALTER TABLE StockOrder ADD COLUMN freezingExitDate DATE NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @old_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'StockOrder' AND COLUMN_NAME = 'freezing_exit_date');
SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'StockOrder' AND COLUMN_NAME = 'freezingExitDate');
SET @sql = IF(@table_exists > 0 AND @old_exists > 0 AND @new_exists > 0,
  'UPDATE StockOrder SET freezingExitDate = freezing_exit_date WHERE freezingExitDate IS NULL AND freezing_exit_date IS NOT NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @old_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'StockOrder' AND COLUMN_NAME = 'freezing_temperature_control');
SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'StockOrder' AND COLUMN_NAME = 'freezingTemperatureControl');
SET @sql = IF(@table_exists > 0 AND @old_exists > 0 AND @new_exists = 0,
  'ALTER TABLE StockOrder CHANGE COLUMN freezing_temperature_control freezingTemperatureControl VARCHAR(255) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'StockOrder' AND COLUMN_NAME = 'freezingTemperatureControl');
SET @sql = IF(@table_exists > 0 AND @new_exists = 0,
  'ALTER TABLE StockOrder ADD COLUMN freezingTemperatureControl VARCHAR(255) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @old_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'StockOrder' AND COLUMN_NAME = 'freezing_temperature_control');
SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'StockOrder' AND COLUMN_NAME = 'freezingTemperatureControl');
SET @sql = IF(@table_exists > 0 AND @old_exists > 0 AND @new_exists > 0,
  'UPDATE StockOrder SET freezingTemperatureControl = freezing_temperature_control WHERE freezingTemperatureControl IS NULL AND freezing_temperature_control IS NOT NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @old_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'StockOrder' AND COLUMN_NAME = 'number_of_bines');
SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'StockOrder' AND COLUMN_NAME = 'numberOfBines');
SET @sql = IF(@table_exists > 0 AND @old_exists > 0 AND @new_exists = 0,
  'ALTER TABLE StockOrder CHANGE COLUMN number_of_bines numberOfBines VARCHAR(50) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'StockOrder' AND COLUMN_NAME = 'numberOfBines');
SET @sql = IF(@table_exists > 0 AND @new_exists = 0,
  'ALTER TABLE StockOrder ADD COLUMN numberOfBines VARCHAR(50) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @old_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'StockOrder' AND COLUMN_NAME = 'number_of_bines');
SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'StockOrder' AND COLUMN_NAME = 'numberOfBines');
SET @sql = IF(@table_exists > 0 AND @old_exists > 0 AND @new_exists > 0,
  'UPDATE StockOrder SET numberOfBines = number_of_bines WHERE numberOfBines IS NULL AND number_of_bines IS NOT NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
