-- Add finalPriceDiscount to StockOrder and displayFinalPriceDiscount to Facility
-- Keep migration idempotent for MySQL versions prior to 8.0

SET @schema := DATABASE();

-- Add StockOrder.finalPriceDiscount DECIMAL(38,2)
SET @column_missing := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema
      AND TABLE_NAME = 'StockOrder'
      AND COLUMN_NAME = 'finalPriceDiscount'
);
SET @sql := IF(@column_missing = 0,
    'SELECT "Column finalPriceDiscount already exists"',
    'ALTER TABLE StockOrder ADD COLUMN finalPriceDiscount DECIMAL(38,2)'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Add Facility.displayFinalPriceDiscount BIT(1)
SET @column_missing := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema
      AND TABLE_NAME = 'Facility'
      AND COLUMN_NAME = 'displayFinalPriceDiscount'
);
SET @sql := IF(@column_missing = 0,
    'SELECT "Column displayFinalPriceDiscount already exists"',
    'ALTER TABLE Facility ADD COLUMN displayFinalPriceDiscount BIT(1)'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
