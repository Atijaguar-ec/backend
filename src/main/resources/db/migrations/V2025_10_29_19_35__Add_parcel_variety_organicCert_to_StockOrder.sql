-- Add parcel lot, variety and organic certification fields to stock_order table

ALTER TABLE StockOrder ADD COLUMN parcelLot VARCHAR(8);
ALTER TABLE StockOrder ADD COLUMN variety VARCHAR(32);
ALTER TABLE StockOrder ADD COLUMN organicCertification VARCHAR(64);
