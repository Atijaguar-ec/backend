SET @schema := DATABASE();

SET @column_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema
      AND TABLE_NAME = 'StockOrder'
      AND COLUMN_NAME = 'organicCertification'
);
SET @sql := IF(@column_exists > 0,
    'SELECT "Column StockOrder.organicCertification already exists" as Info',
    'ALTER TABLE StockOrder ADD COLUMN organicCertification VARCHAR(255)'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @column_length := (
    SELECT CHARACTER_MAXIMUM_LENGTH
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema
      AND TABLE_NAME = 'StockOrder'
      AND COLUMN_NAME = 'organicCertification'
);
SET @sql := IF(@column_length IS NULL OR @column_length >= 255,
    'SELECT "Column StockOrder.organicCertification length OK" as Info',
    'ALTER TABLE StockOrder MODIFY COLUMN organicCertification VARCHAR(255)'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @legacy_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema
      AND TABLE_NAME = 'StockOrder'
      AND COLUMN_NAME = 'organicCert'
);
SET @column_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema
      AND TABLE_NAME = 'StockOrder'
      AND COLUMN_NAME = 'organicCertification'
);
SET @sql := IF(@legacy_exists > 0 AND @column_exists > 0,
    'UPDATE StockOrder SET organicCertification = COALESCE(organicCertification, organicCert) WHERE organicCert IS NOT NULL',
    'SELECT "No legacy organicCert column to migrate" as Info'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
