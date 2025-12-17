-- Fix missing columns that were not added due to inverted logic in previous migrations

SET @schema := DATABASE();

-- Add Facility.displayFinalPriceDiscount BIT(1) if missing
SET @column_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema
      AND TABLE_NAME = 'Facility'
      AND COLUMN_NAME = 'displayFinalPriceDiscount'
);
SET @sql := IF(@column_exists > 0,
    'SELECT "Column Facility.displayFinalPriceDiscount already exists" as Info',
    'ALTER TABLE Facility ADD COLUMN displayFinalPriceDiscount BIT(1)'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Add StockOrder.finalPriceDiscount DECIMAL(38,2) if missing
SET @column_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema
      AND TABLE_NAME = 'StockOrder'
      AND COLUMN_NAME = 'finalPriceDiscount'
);
SET @sql := IF(@column_exists > 0,
    'SELECT "Column StockOrder.finalPriceDiscount already exists" as Info',
    'ALTER TABLE StockOrder ADD COLUMN finalPriceDiscount DECIMAL(38,2)'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Add StockOrder.parcelLot VARCHAR(8) if missing
SET @column_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema
      AND TABLE_NAME = 'StockOrder'
      AND COLUMN_NAME = 'parcelLot'
);
SET @sql := IF(@column_exists > 0,
    'SELECT "Column StockOrder.parcelLot already exists" as Info',
    'ALTER TABLE StockOrder ADD COLUMN parcelLot VARCHAR(8)'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Add StockOrder.variety VARCHAR(32) if missing
SET @column_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema
      AND TABLE_NAME = 'StockOrder'
      AND COLUMN_NAME = 'variety'
);
SET @sql := IF(@column_exists > 0,
    'SELECT "Column StockOrder.variety already exists" as Info',
    'ALTER TABLE StockOrder ADD COLUMN variety VARCHAR(32)'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Add StockOrder.organicCertification VARCHAR(64) if missing
SET @column_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema
      AND TABLE_NAME = 'StockOrder'
      AND COLUMN_NAME = 'organicCertification'
);
SET @sql := IF(@column_exists > 0,
    'SELECT "Column StockOrder.organicCertification already exists" as Info',
    'ALTER TABLE StockOrder ADD COLUMN organicCertification VARCHAR(64)'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
