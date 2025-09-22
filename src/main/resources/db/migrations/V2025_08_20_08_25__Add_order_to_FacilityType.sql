-- Add order field to FacilityType table
-- This enables dynamic ordering of facility types in the processing view

-- Check if FacilityType table exists
SET @table_exists = (
    SELECT COUNT(1)
    FROM information_schema.tables
    WHERE table_schema = DATABASE()
      AND table_name = 'FacilityType'
);

-- Check if order column already exists
SET @column_exists = (
    SELECT COUNT(1)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'FacilityType'
      AND column_name = 'order'
);

-- Add order column only if it doesn't exist
SET @sql = IF(@table_exists = 1 AND @column_exists = 0, 
    'ALTER TABLE FacilityType ADD COLUMN `order` INT DEFAULT 0 COMMENT "Order for displaying facility types (lower = first)"',
    'SELECT "Skipping: FacilityType table missing or column already exists" as message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Create index for better performance (only if it doesn't exist)
SET @index_exists = (
    SELECT COUNT(1) 
    FROM information_schema.statistics 
    WHERE table_schema = DATABASE() 
      AND table_name = 'FacilityType' 
      AND index_name = 'idx_facility_type_order'
);

SET @sql = IF(@table_exists = 1 AND @index_exists = 0, 
    'CREATE INDEX idx_facility_type_order ON FacilityType (`order`)',
    'SELECT "Skipping: FacilityType table missing or index already exists" as message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Initialize order values for existing facility types if the table exists
SET @sql = IF(@table_exists = 1,
    'UPDATE FacilityType SET `order` = id WHERE `order` IS NULL OR `order` = 0',
    'SELECT "Skipping: FacilityType table missing, nothing to update" as message'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
