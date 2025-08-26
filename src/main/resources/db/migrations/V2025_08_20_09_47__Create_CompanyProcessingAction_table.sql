-- Create CompanyProcessingAction table for company-specific processing action configuration
-- Allows companies to enable/disable and override order of global processing actions

CREATE TABLE IF NOT EXISTS CompanyProcessingAction (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entity_version BIGINT NOT NULL DEFAULT 0,
    company_id BIGINT NOT NULL,
    processing_action_id BIGINT NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    order_override INT NULL,
    alias_label VARCHAR(255) NULL,
    
    -- Foreign key constraints
    CONSTRAINT fk_company_processing_action_company 
        FOREIGN KEY (company_id) REFERENCES Company(id) ON DELETE CASCADE,
    CONSTRAINT fk_company_processing_action_processing_action 
        FOREIGN KEY (processing_action_id) REFERENCES ProcessingAction(id) ON DELETE CASCADE,
    
    -- Unique constraint to prevent duplicate entries per company-action pair
    CONSTRAINT uk_company_processing_action_company_action 
        UNIQUE (company_id, processing_action_id)
);

-- Create indexes for efficient querying
-- Note: These are also defined in the entity @Index annotations for consistency
-- MySQL doesn't support CREATE INDEX IF NOT EXISTS, so we use conditional logic

-- Check and create first index
SET @index_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.STATISTICS 
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'CompanyProcessingAction' 
    AND INDEX_NAME = 'idx_company_processing_action_company_enabled');

SET @sql = IF(@index_exists = 0, 
    'CREATE INDEX idx_company_processing_action_company_enabled ON CompanyProcessingAction (company_id, enabled, order_override)', 
    'SELECT "Index idx_company_processing_action_company_enabled already exists" as Info');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Check and create second index
SET @index_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.STATISTICS 
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'CompanyProcessingAction' 
    AND INDEX_NAME = 'idx_company_processing_action_processing_action');

SET @sql = IF(@index_exists = 0, 
    'CREATE INDEX idx_company_processing_action_processing_action ON CompanyProcessingAction (processing_action_id)', 
    'SELECT "Index idx_company_processing_action_processing_action already exists" as Info');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Initialize CompanyProcessingAction for all existing company-processing action combinations
-- Set enabled=true and order_override=null (uses global sortOrder)
-- Only insert if the combination doesn't already exist to avoid conflicts
-- Check if both tables exist before attempting to insert data

SET @company_table_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.TABLES 
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'Company');

SET @processing_action_table_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.TABLES 
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingAction');

SET @insert_sql = IF(@company_table_exists > 0 AND @processing_action_table_exists > 0,
    'INSERT IGNORE INTO CompanyProcessingAction (company_id, processing_action_id, enabled, order_override) SELECT c.id, pa.id, TRUE, NULL FROM Company c CROSS JOIN ProcessingAction pa WHERE EXISTS (SELECT 1 FROM Company c2 WHERE c2.id = c.id) AND EXISTS (SELECT 1 FROM ProcessingAction pa2 WHERE pa2.id = pa.id)',
    'SELECT "Skipping data initialization - Company or ProcessingAction tables do not exist yet" as Info');

PREPARE stmt FROM @insert_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
