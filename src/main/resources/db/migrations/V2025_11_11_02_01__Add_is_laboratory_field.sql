-- Add is_laboratory field to facility table
-- This field indicates if the facility is a laboratory (specific for shrimp value chain)

SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'Facility'
      AND column_name = 'isLaboratory'
);
SET @sql = IF(
    @col_exists = 0,
    'ALTER TABLE Facility ADD COLUMN isLaboratory BIT(1) DEFAULT 0',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
