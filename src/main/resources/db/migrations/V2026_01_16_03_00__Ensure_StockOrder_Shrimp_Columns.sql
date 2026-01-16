-- Ensure StockOrder shrimp-specific columns exist
-- These columns were incorrectly dropped by cleanup migration but are still used by the Java entity

-- Check and add flavor_defect_type_id
SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'StockOrder'
      AND COLUMN_NAME = 'flavor_defect_type_id'
);
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `StockOrder` ADD COLUMN `flavor_defect_type_id` BIGINT NULL',
    'SELECT "Column flavor_defect_type_id already exists" as Info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Check and add flavorTestResult
SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'StockOrder'
      AND COLUMN_NAME = 'flavorTestResult'
);
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `StockOrder` ADD COLUMN `flavorTestResult` VARCHAR(20) NULL',
    'SELECT "Column flavorTestResult already exists" as Info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Check and add purchaseRecommended
SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'StockOrder'
      AND COLUMN_NAME = 'purchaseRecommended'
);
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `StockOrder` ADD COLUMN `purchaseRecommended` BIT(1) NULL',
    'SELECT "Column purchaseRecommended already exists" as Info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Check and add inspectionNotes
SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'StockOrder'
      AND COLUMN_NAME = 'inspectionNotes'
);
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `StockOrder` ADD COLUMN `inspectionNotes` VARCHAR(1000) NULL',
    'SELECT "Column inspectionNotes already exists" as Info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Check and add field_inspection_id
SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'StockOrder'
      AND COLUMN_NAME = 'field_inspection_id'
);
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `StockOrder` ADD COLUMN `field_inspection_id` BIGINT NULL',
    'SELECT "Column field_inspection_id already exists" as Info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Check and add sampleNumber
SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'StockOrder'
      AND COLUMN_NAME = 'sampleNumber'
);
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `StockOrder` ADD COLUMN `sampleNumber` VARCHAR(100) NULL',
    'SELECT "Column sampleNumber already exists" as Info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Check and add receptionTime
SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'StockOrder'
      AND COLUMN_NAME = 'receptionTime'
);
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `StockOrder` ADD COLUMN `receptionTime` TIME NULL',
    'SELECT "Column receptionTime already exists" as Info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Check and add quality_document_id
SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'StockOrder'
      AND COLUMN_NAME = 'quality_document_id'
);
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `StockOrder` ADD COLUMN `quality_document_id` BIGINT NULL',
    'SELECT "Column quality_document_id already exists" as Info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Check and add numberOfGavetas
SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'StockOrder'
      AND COLUMN_NAME = 'numberOfGavetas'
);
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `StockOrder` ADD COLUMN `numberOfGavetas` INT NULL',
    'SELECT "Column numberOfGavetas already exists" as Info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Check and add numberOfBines
SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'StockOrder'
      AND COLUMN_NAME = 'numberOfBines'
);
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `StockOrder` ADD COLUMN `numberOfBines` VARCHAR(50) NULL',
    'SELECT "Column numberOfBines already exists" as Info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Check and add numberOfPiscinas
SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'StockOrder'
      AND COLUMN_NAME = 'numberOfPiscinas'
);
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `StockOrder` ADD COLUMN `numberOfPiscinas` VARCHAR(50) NULL',
    'SELECT "Column numberOfPiscinas already exists" as Info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Check and add guiaRemisionNumber
SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'StockOrder'
      AND COLUMN_NAME = 'guiaRemisionNumber'
);
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `StockOrder` ADD COLUMN `guiaRemisionNumber` VARCHAR(100) NULL',
    'SELECT "Column guiaRemisionNumber already exists" as Info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Check and add cuttingType
SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'StockOrder'
      AND COLUMN_NAME = 'cuttingType'
);
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `StockOrder` ADD COLUMN `cuttingType` VARCHAR(100) NULL',
    'SELECT "Column cuttingType already exists" as Info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Check and add cuttingEntryDate
SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'StockOrder'
      AND COLUMN_NAME = 'cuttingEntryDate'
);
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `StockOrder` ADD COLUMN `cuttingEntryDate` DATE NULL',
    'SELECT "Column cuttingEntryDate already exists" as Info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Check and add cuttingExitDate
SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'StockOrder'
      AND COLUMN_NAME = 'cuttingExitDate'
);
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `StockOrder` ADD COLUMN `cuttingExitDate` DATE NULL',
    'SELECT "Column cuttingExitDate already exists" as Info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Check and add cuttingTemperatureControl
SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'StockOrder'
      AND COLUMN_NAME = 'cuttingTemperatureControl'
);
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `StockOrder` ADD COLUMN `cuttingTemperatureControl` VARCHAR(255) NULL',
    'SELECT "Column cuttingTemperatureControl already exists" as Info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Check and add treatmentType
SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'StockOrder'
      AND COLUMN_NAME = 'treatmentType'
);
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `StockOrder` ADD COLUMN `treatmentType` VARCHAR(100) NULL',
    'SELECT "Column treatmentType already exists" as Info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Check and add treatmentEntryDate
SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'StockOrder'
      AND COLUMN_NAME = 'treatmentEntryDate'
);
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `StockOrder` ADD COLUMN `treatmentEntryDate` DATE NULL',
    'SELECT "Column treatmentEntryDate already exists" as Info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Check and add treatmentExitDate
SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'StockOrder'
      AND COLUMN_NAME = 'treatmentExitDate'
);
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `StockOrder` ADD COLUMN `treatmentExitDate` DATE NULL',
    'SELECT "Column treatmentExitDate already exists" as Info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Check and add treatmentTemperatureControl
SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'StockOrder'
      AND COLUMN_NAME = 'treatmentTemperatureControl'
);
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `StockOrder` ADD COLUMN `treatmentTemperatureControl` VARCHAR(255) NULL',
    'SELECT "Column treatmentTemperatureControl already exists" as Info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Check and add treatmentChemicalUsed
SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'StockOrder'
      AND COLUMN_NAME = 'treatmentChemicalUsed'
);
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `StockOrder` ADD COLUMN `treatmentChemicalUsed` VARCHAR(255) NULL',
    'SELECT "Column treatmentChemicalUsed already exists" as Info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Check and add tunnelProductionDate
SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'StockOrder'
      AND COLUMN_NAME = 'tunnelProductionDate'
);
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `StockOrder` ADD COLUMN `tunnelProductionDate` DATE NULL',
    'SELECT "Column tunnelProductionDate already exists" as Info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Check and add tunnelExpirationDate
SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'StockOrder'
      AND COLUMN_NAME = 'tunnelExpirationDate'
);
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `StockOrder` ADD COLUMN `tunnelExpirationDate` DATE NULL',
    'SELECT "Column tunnelExpirationDate already exists" as Info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Check and add tunnelNetWeight
SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'StockOrder'
      AND COLUMN_NAME = 'tunnelNetWeight'
);
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `StockOrder` ADD COLUMN `tunnelNetWeight` DECIMAL(38,2) NULL',
    'SELECT "Column tunnelNetWeight already exists" as Info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Check and add tunnelSupplier
SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'StockOrder'
      AND COLUMN_NAME = 'tunnelSupplier'
);
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `StockOrder` ADD COLUMN `tunnelSupplier` VARCHAR(255) NULL',
    'SELECT "Column tunnelSupplier already exists" as Info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Check and add tunnelFreezingType
SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'StockOrder'
      AND COLUMN_NAME = 'tunnelFreezingType'
);
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `StockOrder` ADD COLUMN `tunnelFreezingType` VARCHAR(100) NULL',
    'SELECT "Column tunnelFreezingType already exists" as Info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Check and add tunnelEntryDate
SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'StockOrder'
      AND COLUMN_NAME = 'tunnelEntryDate'
);
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `StockOrder` ADD COLUMN `tunnelEntryDate` DATE NULL',
    'SELECT "Column tunnelEntryDate already exists" as Info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Check and add tunnelExitDate
SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'StockOrder'
      AND COLUMN_NAME = 'tunnelExitDate'
);
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `StockOrder` ADD COLUMN `tunnelExitDate` DATE NULL',
    'SELECT "Column tunnelExitDate already exists" as Info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Check and add washingWaterTemperature
SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'StockOrder'
      AND COLUMN_NAME = 'washingWaterTemperature'
);
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `StockOrder` ADD COLUMN `washingWaterTemperature` VARCHAR(100) NULL',
    'SELECT "Column washingWaterTemperature already exists" as Info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Check and add washingShrimpTemperatureControl
SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'StockOrder'
      AND COLUMN_NAME = 'washingShrimpTemperatureControl'
);
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `StockOrder` ADD COLUMN `washingShrimpTemperatureControl` VARCHAR(255) NULL',
    'SELECT "Column washingShrimpTemperatureControl already exists" as Info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
