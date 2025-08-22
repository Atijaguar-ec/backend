-- Add order field to FacilityType table
-- This enables dynamic ordering of facility types in the processing view

-- Check if order column already exists
SET @column_exists = (
    SELECT COUNT(1) 
    FROM information_schema.columns 
    WHERE table_schema = DATABASE() 
      AND table_name = 'FacilityType' 
      AND column_name = 'order'
);

-- Add order column only if it doesn't exist
SET @sql = IF(@column_exists = 0, 
    'ALTER TABLE FacilityType ADD COLUMN `order` INT DEFAULT 0 COMMENT "Order for displaying facility types (lower = first)"',
    'SELECT "Column order already exists in FacilityType" as message'
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

SET @sql = IF(@index_exists = 0, 
    'CREATE INDEX idx_facility_type_order ON FacilityType (`order`)',
    'SELECT "Index idx_facility_type_order already exists" as message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Initialize order values for existing facility types if they are null
UPDATE FacilityType 
SET `order` = id 
WHERE `order` IS NULL OR `order` = 0;