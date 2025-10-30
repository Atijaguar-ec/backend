-- Add parcel lot, variety and organic certification fields to stock_order table

ALTER TABLE stock_order ADD COLUMN parcel_lot VARCHAR(255);
ALTER TABLE stock_order ADD COLUMN variety VARCHAR(255);
ALTER TABLE stock_order ADD COLUMN organic_certification VARCHAR(255);
