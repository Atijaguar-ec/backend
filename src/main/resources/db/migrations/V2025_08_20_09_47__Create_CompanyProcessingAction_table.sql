-- Create CompanyProcessingAction table for company-specific processing action configuration
-- Allows companies to enable/disable and override order of global processing actions

CREATE TABLE IF NOT EXISTS CompanyProcessingAction (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entityVersion BIGINT NOT NULL DEFAULT 0,
    company_id BIGINT NOT NULL,
    processing_action_id BIGINT NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    order_override INT NULL,
    alias_label VARCHAR(255) NULL,
    creationTimestamp TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6),
    updateTimestamp TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    
    -- Foreign key constraints
    CONSTRAINT fk_company_processing_action_company 
        FOREIGN KEY (company_id) REFERENCES company(id) ON DELETE CASCADE,
    CONSTRAINT fk_company_processing_action_processing_action 
        FOREIGN KEY (processing_action_id) REFERENCES processing_action(id) ON DELETE CASCADE,
    
    -- Unique constraint to prevent duplicate entries per company-action pair
    CONSTRAINT uk_company_processing_action_company_action 
        UNIQUE (company_id, processing_action_id)
);

-- Create indexes for efficient querying
-- Note: These are also defined in the entity @Index annotations for consistency
CREATE INDEX IF NOT EXISTS idx_company_processing_action_company_enabled 
    ON CompanyProcessingAction (company_id, enabled, order_override);

CREATE INDEX IF NOT EXISTS idx_company_processing_action_processing_action 
    ON CompanyProcessingAction (processing_action_id);

-- Initialize CompanyProcessingAction for all existing company-processing action combinations
-- Set enabled=true and order_override=null (uses global sortOrder)
-- Only insert if the combination doesn't already exist to avoid conflicts
INSERT IGNORE INTO CompanyProcessingAction (company_id, processing_action_id, enabled, order_override, entityVersion)
SELECT c.id, pa.id, TRUE, NULL, 0
FROM company c
CROSS JOIN processing_action pa
WHERE EXISTS (SELECT 1 FROM company c2 WHERE c2.id = c.id)
  AND EXISTS (SELECT 1 FROM processing_action pa2 WHERE pa2.id = pa.id);
