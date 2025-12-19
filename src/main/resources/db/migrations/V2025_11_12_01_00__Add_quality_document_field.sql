-- Add quality document field to stock_order table for laboratory quality documentation
-- This field will store a reference to a PDF document uploaded for quality analysis

SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'StockOrder'
      AND column_name = 'quality_document_id'
);
SET @sql = IF(
    @col_exists = 0,
    'ALTER TABLE `StockOrder` ADD COLUMN `quality_document_id` BIGINT NULL COMMENT ''Reference to PDF document for laboratory quality analysis''',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add foreign key constraint to document table
SET @fk_exists = (
    SELECT COUNT(*)
    FROM information_schema.key_column_usage
    WHERE table_schema = DATABASE()
      AND table_name = 'StockOrder'
      AND column_name = 'quality_document_id'
      AND referenced_table_name = 'Document'
      AND referenced_column_name = 'id'
);
SET @sql = IF(
    @fk_exists = 0,
    'ALTER TABLE `StockOrder` ADD CONSTRAINT `FK_stock_order_quality_document` FOREIGN KEY (`quality_document_id`) REFERENCES `Document`(`id`) ON DELETE SET NULL',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add index for better performance on quality document lookups
SET @idx_exists = (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'StockOrder'
      AND index_name = 'IDX_stock_order_quality_document_id'
);
SET @sql = IF(
    @idx_exists = 0,
    'CREATE INDEX `IDX_stock_order_quality_document_id` ON `StockOrder`(`quality_document_id`)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add comment for documentation
SET @comment_ok = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'StockOrder'
      AND column_name = 'quality_document_id'
      AND column_comment = 'Reference to PDF document for laboratory quality analysis'
);
SET @sql = IF(
    @comment_ok = 0,
    'ALTER TABLE `StockOrder` MODIFY COLUMN `quality_document_id` BIGINT NULL COMMENT ''Reference to PDF document for laboratory quality analysis''',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
