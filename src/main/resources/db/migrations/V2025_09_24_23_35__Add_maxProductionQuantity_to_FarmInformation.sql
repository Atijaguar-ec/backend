-- Add maxProductionQuantity field to farm information
-- This field stores the maximum production quantity in quintals (qq)

-- Add column to UserCustomer table (where FarmInformation is embedded)
ALTER TABLE user_customer 
ADD COLUMN max_production_quantity DECIMAL(19,2) NULL;

-- Add comment for documentation
COMMENT ON COLUMN user_customer.max_production_quantity IS 'Maximum production quantity in quintals (qq)';
