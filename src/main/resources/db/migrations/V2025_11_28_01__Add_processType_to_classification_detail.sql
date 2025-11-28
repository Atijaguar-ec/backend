-- =====================================================
-- Migration: Add processType field to ProcessingClassificationBatchDetail
-- Purpose: Support separate liquidations for HEAD_ON (Entero) and SHELL_ON (Cola)
-- Date: 2025-11-28
-- =====================================================

-- Add processType column to distinguish between HEAD_ON and SHELL_ON details
ALTER TABLE ProcessingClassificationBatchDetail
ADD COLUMN processType VARCHAR(20) DEFAULT 'SHELL_ON' COMMENT 'Process type: HEAD_ON (Entero) or SHELL_ON (Cola)';

-- Update existing records based on size prefix
UPDATE ProcessingClassificationBatchDetail
SET processType = 'HEAD_ON'
WHERE size LIKE 'WHOLE_%';

UPDATE ProcessingClassificationBatchDetail
SET processType = 'SHELL_ON'
WHERE size LIKE 'TAIL_%' OR processType IS NULL;

-- Add index for filtering by process type
CREATE INDEX idx_classification_detail_process_type 
ON ProcessingClassificationBatchDetail(processType);

-- =====================================================
-- Add providerName field to ProcessingClassificationBatch (Formato DUFER)
-- =====================================================
ALTER TABLE ProcessingClassificationBatch
ADD COLUMN providerName VARCHAR(200) COMMENT 'Provider/Supplier name (Formato DUFER)';
