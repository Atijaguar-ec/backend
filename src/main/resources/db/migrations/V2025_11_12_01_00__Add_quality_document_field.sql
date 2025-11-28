-- Add quality document field to stock_order table for laboratory quality documentation
-- This field will store a reference to a PDF document uploaded for quality analysis

ALTER TABLE `StockOrder`
ADD COLUMN `quality_document_id` BIGINT NULL;

-- Add foreign key constraint to document table
ALTER TABLE `StockOrder`
ADD CONSTRAINT `FK_stock_order_quality_document`
    FOREIGN KEY (`quality_document_id`) REFERENCES `Document`(`id`) ON DELETE SET NULL;

-- Add index for better performance on quality document lookups
CREATE INDEX `IDX_stock_order_quality_document_id` ON `StockOrder`(`quality_document_id`);

-- Add comment for documentation
ALTER TABLE `StockOrder`
MODIFY COLUMN `quality_document_id` BIGINT NULL 
COMMENT 'Reference to PDF document for laboratory quality analysis';
