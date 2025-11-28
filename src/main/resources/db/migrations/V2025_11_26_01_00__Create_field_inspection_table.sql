-- ============================================================================
-- Migration: Create FieldInspection table
-- Description: Stores field inspection (sensorial inspection at shrimp farms)
--              results that can be linked to plant deliveries.
-- Author: INATrace Team
-- Date: 2025-11-26
-- ============================================================================

CREATE TABLE IF NOT EXISTS FieldInspection (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- Timestamps
    creationTimestamp DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updateTimestamp DATETIME(6) NULL ON UPDATE CURRENT_TIMESTAMP(6),
    
    -- Audit
    created_by_id BIGINT NOT NULL,
    updated_by_id BIGINT NULL,
    
    -- Source: The stock order where the inspection was performed (at field inspection facility)
    source_stock_order_id BIGINT NOT NULL,
    
    -- Destination: The stock order where this inspection is used (at packing plant)
    -- NULL means the inspection is still available for linking
    destination_stock_order_id BIGINT NULL,
    
    -- Company reference for easy filtering
    company_id BIGINT NOT NULL,
    
    -- Inspection data
    inspection_date DATETIME(6) NOT NULL,
    inspection_time VARCHAR(10) NULL COMMENT 'Time in HH:mm format',
    
    -- Supplier/Producer info (denormalized for quick display)
    producer_user_customer_id BIGINT NULL,
    producerName VARCHAR(255) NULL COMMENT 'Cached producer name for display',
    
    -- Sensorial inspection results
    flavor_test_result VARCHAR(20) NOT NULL COMMENT 'NORMAL or DEFECT',
    flavor_defect_type_id BIGINT NULL COMMENT 'Reference to ShrimpFlavorDefect codebook',
    flavor_defect_type_code VARCHAR(50) NULL COMMENT 'Cached defect code',
    flavor_defect_type_label VARCHAR(255) NULL COMMENT 'Cached defect label',
    
    -- Recommendation
    purchase_recommended BOOLEAN NOT NULL DEFAULT TRUE,
    
    -- Notes
    inspection_notes LONGTEXT NULL,
    
    -- Reception data (optional, from source order)
    number_of_gavetas INT NULL,
    number_of_bines INT NULL,
    number_of_piscinas INT NULL,
    guia_remision_number VARCHAR(100) NULL,
    total_quantity DECIMAL(19,2) NULL COMMENT 'Quantity in units',
    
    -- Foreign keys
    CONSTRAINT fk_field_inspection_created_by
        FOREIGN KEY (created_by_id) REFERENCES User(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
        
    CONSTRAINT fk_field_inspection_updated_by
        FOREIGN KEY (updated_by_id) REFERENCES User(id)
        ON DELETE SET NULL ON UPDATE CASCADE,
        
    CONSTRAINT fk_field_inspection_source_stock_order
        FOREIGN KEY (source_stock_order_id) REFERENCES StockOrder(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
        
    CONSTRAINT fk_field_inspection_destination_stock_order
        FOREIGN KEY (destination_stock_order_id) REFERENCES StockOrder(id)
        ON DELETE SET NULL ON UPDATE CASCADE,
        
    CONSTRAINT fk_field_inspection_company
        FOREIGN KEY (company_id) REFERENCES Company(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
        
    CONSTRAINT fk_field_inspection_producer
        FOREIGN KEY (producer_user_customer_id) REFERENCES UserCustomer(id)
        ON DELETE SET NULL ON UPDATE CASCADE,
        
    -- Indexes for common queries
    INDEX idx_field_inspection_company (company_id),
    INDEX idx_field_inspection_source (source_stock_order_id),
    INDEX idx_field_inspection_destination (destination_stock_order_id),
    INDEX idx_field_inspection_available (company_id, destination_stock_order_id, purchase_recommended),
    INDEX idx_field_inspection_date (inspection_date)
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
    COMMENT='Field inspection records for shrimp sensorial tests at farms';

-- ============================================================================
-- Add reference from StockOrder to FieldInspection for reverse lookup (idempotent)
-- ============================================================================

-- Add column field_inspection_id only if it does not exist
SET @col_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'StockOrder'
      AND COLUMN_NAME = 'field_inspection_id'
);

SET @ddl_add_column := IF(
    @col_exists = 0,
    'ALTER TABLE StockOrder ADD COLUMN field_inspection_id BIGINT NULL COMMENT ''Reference to linked field inspection'' AFTER quality_document_id',
    'SELECT 1'
);

PREPARE stmt FROM @ddl_add_column;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add foreign key constraint only if it does not exist
SET @fk_exists := (
    SELECT COUNT(*)
    FROM information_schema.REFERENTIAL_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE()
      AND CONSTRAINT_NAME = 'fk_stock_order_field_inspection'
);

SET @ddl_add_fk := IF(
    @fk_exists = 0,
    'ALTER TABLE StockOrder ADD CONSTRAINT fk_stock_order_field_inspection FOREIGN KEY (field_inspection_id) REFERENCES FieldInspection(id) ON DELETE SET NULL ON UPDATE CASCADE',
    'SELECT 1'
);

PREPARE stmt FROM @ddl_add_fk;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add index on field_inspection_id only if it does not exist
SET @idx_exists := (
    SELECT COUNT(1)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'StockOrder'
      AND INDEX_NAME = 'idx_stock_order_field_inspection'
);

SET @ddl_add_index := IF(
    @idx_exists = 0,
    'CREATE INDEX idx_stock_order_field_inspection ON StockOrder(field_inspection_id)',
    'SELECT 1'
);

PREPARE stmt FROM @ddl_add_index;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
