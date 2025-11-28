-- 🦐 Add freezing fields to StockOrder for shrimp processing
-- These fields are used in the general freezing section (isFreezingProcess)
-- Separate from tunnel freezing fields which already exist

-- Add freezingType column if not exists
SET @dbname = DATABASE();
SET @tablename = 'StockOrder';
SET @columnname = 'freezing_type';
SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
    'SELECT 1',
    CONCAT('ALTER TABLE `', @tablename, '` ADD COLUMN `', @columnname, '` VARCHAR(100) NULL COMMENT \'Freezing type (shrimp processing)\'')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Add freezingEntryDate column if not exists
SET @columnname = 'freezing_entry_date';
SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
    'SELECT 1',
    CONCAT('ALTER TABLE `', @tablename, '` ADD COLUMN `', @columnname, '` DATE NULL COMMENT \'Freezing entry date (shrimp processing)\'')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Add freezingExitDate column if not exists
SET @columnname = 'freezing_exit_date';
SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
    'SELECT 1',
    CONCAT('ALTER TABLE `', @tablename, '` ADD COLUMN `', @columnname, '` DATE NULL COMMENT \'Freezing exit date (shrimp processing)\'')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Add freezingTemperatureControl column if not exists
SET @columnname = 'freezing_temperature_control';
SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
    'SELECT 1',
    CONCAT('ALTER TABLE `', @tablename, '` ADD COLUMN `', @columnname, '` VARCHAR(255) NULL COMMENT \'Temperature control during freezing (shrimp processing)\'')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;
