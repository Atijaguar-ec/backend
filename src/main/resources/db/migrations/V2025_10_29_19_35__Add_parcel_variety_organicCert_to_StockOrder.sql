-- Add parcel lot, variety and organic certification fields to stock_order table
ALTER TABLE `StockOrder`
    ADD COLUMN IF NOT EXISTS `parcelLot` VARCHAR(8);
ALTER TABLE `StockOrder`
    ADD COLUMN IF NOT EXISTS `variety` VARCHAR(32);
ALTER TABLE `StockOrder`
    ADD COLUMN IF NOT EXISTS `organicCertification` VARCHAR(64);
