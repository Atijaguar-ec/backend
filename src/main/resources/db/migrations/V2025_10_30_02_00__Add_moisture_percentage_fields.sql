-- Add moisture percentage configuration to Facility table
ALTER TABLE `Facility`
    ADD COLUMN IF NOT EXISTS `display_moisture_percentage` bit(1) DEFAULT b'0';

-- Add moisture percentage fields to StockOrder table
ALTER TABLE `StockOrder`
    ADD COLUMN IF NOT EXISTS `moisture_percentage` decimal(5,2) DEFAULT NULL;

ALTER TABLE `StockOrder`
    ADD COLUMN IF NOT EXISTS `moisture_weight_deduction` decimal(38,2) DEFAULT NULL;
