-- Add missing timestamp columns expected by TimestampEntity
-- MySQL 8+ supports IF NOT EXISTS for ADD COLUMN, so this is idempotent

-- Use dynamic SQL to be compatible when IF NOT EXISTS is not supported

-- Add creationTimestamp if missing
SET @exists := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'CompanyProcessingAction'
    AND COLUMN_NAME = 'creationTimestamp'
);
SET @sql := IF(@exists = 0,
  'ALTER TABLE CompanyProcessingAction ADD COLUMN creationTimestamp datetime(6) NULL',
  'SELECT 1'
);
PREPARE s1 FROM @sql; EXECUTE s1; DEALLOCATE PREPARE s1;

-- Add updateTimestamp if missing
SET @exists2 := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'CompanyProcessingAction'
    AND COLUMN_NAME = 'updateTimestamp'
);
SET @sql2 := IF(@exists2 = 0,
  'ALTER TABLE CompanyProcessingAction ADD COLUMN updateTimestamp datetime(6) NULL',
  'SELECT 1'
);
PREPARE s2 FROM @sql2; EXECUTE s2; DEALLOCATE PREPARE s2;
