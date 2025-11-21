-- Fix Facility.displayFinalPriceDiscount column creation
-- Previous migration V2025_11_06_00_00__Add_final_price_discount.sql had inverted logic
-- and could skip adding the column even when it was missing.

SET @schema := DATABASE();

-- Ensure Facility.displayFinalPriceDiscount BIT(1) exists
SET @column_count := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema
      AND TABLE_NAME = 'Facility'
      AND COLUMN_NAME = 'displayFinalPriceDiscount'
);

SET @sql := IF(@column_count = 0,
    'ALTER TABLE Facility ADD COLUMN displayFinalPriceDiscount BIT(1)',
    'SELECT "Column displayFinalPriceDiscount already exists"'
);

PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
