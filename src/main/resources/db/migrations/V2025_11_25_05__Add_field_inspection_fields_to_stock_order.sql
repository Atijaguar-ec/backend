-- Add field inspection (sensory testing) fields to StockOrder table
-- These fields are used when facility.isFieldInspection = true

ALTER TABLE stock_order ADD COLUMN flavor_test_result VARCHAR(20);
ALTER TABLE stock_order ADD COLUMN flavor_defect_type_id BIGINT;
ALTER TABLE stock_order ADD COLUMN purchase_recommended BIT(1);
ALTER TABLE stock_order ADD COLUMN inspection_notes VARCHAR(1000);

-- Add foreign key constraint for flavor_defect_type
ALTER TABLE stock_order ADD CONSTRAINT fk_stock_order_flavor_defect_type
    FOREIGN KEY (flavor_defect_type_id) REFERENCES shrimp_flavor_defect(id);

-- Add index for better query performance
CREATE INDEX idx_stock_order_flavor_defect_type ON stock_order(flavor_defect_type_id);
