-- Add order field to FacilityType table
-- This enables dynamic ordering of facility types in the processing view

-- Add order column
ALTER TABLE `FacilityType` 
ADD COLUMN `order` INT DEFAULT 0 COMMENT 'Order for displaying facility types (lower = first)';

-- Set order based on current hardcoded arrangement
UPDATE `FacilityType` SET `order` = 1 WHERE code IN ('WASHING_STATION', 'DRYING_BED', 'BENEFICIO_HUMEDO', 'ACOPIO', 'PUNTODEVENTA');
UPDATE `FacilityType` SET `order` = 2 WHERE code IN ('STORAGE', 'ALMACEN');
UPDATE `FacilityType` SET `order` = 3 WHERE code IN ('HULLING_STATION', 'MAQUILADO_CAFE', 'BENEFICIO_SECO');
UPDATE `FacilityType` SET `order` = 4 WHERE code IN ('GREEN_COFFEE_STORAGE', 'ALMACEN_CAFE_ORO');
UPDATE `FacilityType` SET `order` = 5 WHERE code IN ('ROASTED_COFFEE_STORAGE');

-- Create index for better performance
CREATE INDEX idx_facility_type_order ON `FacilityType` (`order`);
