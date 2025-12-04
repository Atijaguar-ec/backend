-- =====================================================
-- Migration: Add Rejected Output Support for Classification
-- Date: 2025-12-04
-- Description: Adds support for creating a secondary output stock order
--              for rejected shrimp that goes to deheading area.
--              
-- Business Logic:
--   - When classifying shrimp, some product may be rejected
--   - Rejected product goes to a deheading facility for reprocessing
--   - This creates TWO outputs from ONE processing order:
--     1. PRIMARY output (PROCESSED) - goes to freezing
--     2. SECONDARY output (REJECTED) - goes to deheading
-- =====================================================

SET @dbname = DATABASE();
SET @tablename = 'ProcessingClassificationBatch';

-- =====================================================
-- 1. Add outputType to ProcessingClassificationBatch
--    Values: 'PROCESSED' (primary), 'REJECTED' (secondary for deheading)
-- =====================================================
SET @columnname = 'output_type';
SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
    'SELECT 1',
    CONCAT('ALTER TABLE `', @tablename, '` ADD COLUMN `', @columnname, '` VARCHAR(20) DEFAULT ''PROCESSED'' COMMENT ''Output type: PROCESSED (primary) or REJECTED (secondary for deheading)''')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- =====================================================
-- 2. Add poundsRejected to ProcessingClassificationBatch
--    Stores the weight that was rejected during classification
-- =====================================================
SET @columnname = 'pounds_rejected';
SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
    'SELECT 1',
    CONCAT('ALTER TABLE `', @tablename, '` ADD COLUMN `', @columnname, '` DECIMAL(12,2) NULL COMMENT ''Pounds rejected (sent to deheading)''')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- =====================================================
-- 3. Add rejectedStockOrderId to ProcessingClassificationBatch
--    Links to the secondary output stock order (rejected product)
-- =====================================================
SET @columnname = 'rejected_stock_order_id';
SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
    'SELECT 1',
    CONCAT('ALTER TABLE `', @tablename, '` ADD COLUMN `', @columnname, '` BIGINT NULL COMMENT ''Reference to rejected output stock order''')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- =====================================================
-- 4. Add foreign key for rejectedStockOrderId (if not exists)
-- =====================================================
SET @fkname = 'fk_pcb_rejected_stock_order';
SET @fkExists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = @dbname 
    AND TABLE_NAME = @tablename 
    AND CONSTRAINT_NAME = @fkname
    AND CONSTRAINT_TYPE = 'FOREIGN KEY');

SET @preparedStatement = (SELECT IF(
    @fkExists > 0,
    'SELECT 1',
    CONCAT('ALTER TABLE `', @tablename, '` ADD CONSTRAINT `', @fkname, '` FOREIGN KEY (`rejected_stock_order_id`) REFERENCES `StockOrder`(`id`) ON DELETE SET NULL')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- =====================================================
-- 5. Add index on outputType for query performance
-- =====================================================
SET @indexname = 'idx_pcb_output_type';
SET @indexExists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = @dbname 
    AND TABLE_NAME = @tablename 
    AND INDEX_NAME = @indexname);

SET @preparedStatement = (SELECT IF(
    @indexExists > 0,
    'SELECT 1',
    CONCAT('CREATE INDEX `', @indexname, '` ON `', @tablename, '`(`output_type`)')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- =====================================================
-- Migration completed successfully
-- =====================================================
