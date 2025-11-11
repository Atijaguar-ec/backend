-- Add is_laboratory field to facility table
-- This field indicates if the facility is a laboratory (specific for shrimp value chain)

ALTER TABLE Facility ADD COLUMN isLaboratory BIT(1) DEFAULT 0;
