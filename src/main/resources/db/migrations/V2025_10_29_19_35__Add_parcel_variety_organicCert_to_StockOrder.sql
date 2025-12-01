-- MySQL versions prior to 8.0 do not support "ADD COLUMN IF NOT EXISTS"
-- Use conditional dynamic SQL to keep the migration idempotente

SET @schema := DATABASE();

SET @column_missing := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema
      AND TABLE_NAME = 'StockOrder'
      AND COLUMN_NAME = 'parcelLot'
);
SET @sql := IF(@column_missing = 0,
    'SELECT "Column parcelLot already exists"',
    'ALTER TABLE StockOrder ADD COLUMN parcelLot VARCHAR(8)'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @column_missing := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema
      AND TABLE_NAME = 'StockOrder'
      AND COLUMN_NAME = 'variety'
);
SET @sql := IF(@column_missing = 0,
    'SELECT "Column variety already exists"',
    'ALTER TABLE StockOrder ADD COLUMN variety VARCHAR(32)'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @column_missing := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema
      AND TABLE_NAME = 'StockOrder'
      AND COLUMN_NAME = 'organicCertification'
);
SET @sql := IF(@column_missing = 0,
    'SELECT "Column organicCertification already exists"',
    'ALTER TABLE StockOrder ADD COLUMN organicCertification VARCHAR(64)'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
