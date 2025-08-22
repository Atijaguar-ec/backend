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
CREATE INDEX IF NOT EXISTS idx_facility_type_order ON FacilityType (`order`);

-- Initialize order values for existing facility types if they are null
UPDATE FacilityType 
SET `order` = id 
WHERE `order` IS NULL OR `order` = 0;