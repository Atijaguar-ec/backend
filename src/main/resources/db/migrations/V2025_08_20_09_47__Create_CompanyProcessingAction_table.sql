-- Create CompanyProcessingAction table for company-specific processing action configuration
-- Allows companies to enable/disable and override order of global processing actions

CREATE TABLE IF NOT EXISTS company_processing_action (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id BIGINT NOT NULL,
    processing_action_id BIGINT NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    order_override INT NULL,
    alias_label VARCHAR(255) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    CONSTRAINT fk_company_processing_action_company 
        FOREIGN KEY (company_id) REFERENCES company(id) ON DELETE CASCADE,
    CONSTRAINT fk_company_processing_action_processing_action 
        FOREIGN KEY (processing_action_id) REFERENCES processing_action(id) ON DELETE CASCADE,
    
    -- Unique constraint to prevent duplicate entries per company-action pair
    CONSTRAINT uk_company_processing_action_company_action 
        UNIQUE (company_id, processing_action_id)
);

-- Index for efficient querying by company and enabled status
-- Check if first index exists and create it if not
SET @index_exists = (SELECT COUNT(1) FROM information_schema.statistics 
                     WHERE table_schema = DATABASE() 
                     AND table_name = 'company_processing_action' 
                     AND index_name = 'idx_company_processing_action_company_enabled_order');

SET @sql = IF(@index_exists = 0, 'CREATE INDEX idx_company_processing_action_company_enabled_order ON company_processing_action (company_id, enabled, order_override)', 'SELECT 1');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Check if second index exists and create it if not
SET @index_exists = (SELECT COUNT(1) FROM information_schema.statistics 
                     WHERE table_schema = DATABASE() 
                     AND table_name = 'company_processing_action' 
                     AND index_name = 'idx_company_processing_action_processing_action');

SET @sql = IF(@index_exists = 0, 'CREATE INDEX idx_company_processing_action_processing_action ON company_processing_action (processing_action_id)', 'SELECT 1');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Initialize CompanyProcessingAction for all existing company-processing action combinations
-- Set enabled=true and order_override=null (uses global sortOrder)
INSERT INTO company_processing_action (company_id, processing_action_id, enabled, order_override)
SELECT c.id, pa.id, TRUE, NULL
FROM company c
CROSS JOIN processing_action pa
WHERE NOT EXISTS (
    SELECT 1 FROM company_processing_action cpa 
    WHERE cpa.company_id = c.id AND cpa.processing_action_id = pa.id
);
