-- =====================================================
-- Migration: Add processType field to ProcessingClassificationBatchDetail
-- Purpose: Support separate liquidations for HEAD_ON (Entero) and SHELL_ON (Cola)
-- Date: 2025-11-28
-- Made idempotent to handle cases where columns already exist
-- =====================================================

-- Add processType column if not exists
SET @col_exists := (SELECT COUNT(*) FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatchDetail' AND COLUMN_NAME = 'processType');
SET @sql := IF(@col_exists = 0, 
    'ALTER TABLE ProcessingClassificationBatchDetail ADD COLUMN processType VARCHAR(20) DEFAULT ''SHELL_ON'' COMMENT ''Process type: HEAD_ON (Entero) or SHELL_ON (Cola)''', 
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Update existing records based on size prefix
UPDATE ProcessingClassificationBatchDetail
SET processType = 'HEAD_ON'
WHERE size LIKE 'WHOLE_%' AND (processType IS NULL OR processType != 'HEAD_ON');

UPDATE ProcessingClassificationBatchDetail
SET processType = 'SHELL_ON'
WHERE (size LIKE 'TAIL_%' OR size NOT LIKE 'WHOLE_%') AND (processType IS NULL OR processType = '');

-- Add index if not exists
SET @idx_exists := (SELECT COUNT(*) FROM information_schema.STATISTICS 
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatchDetail' AND INDEX_NAME = 'idx_classification_detail_process_type');
SET @sql := IF(@idx_exists = 0, 
    'CREATE INDEX idx_classification_detail_process_type ON ProcessingClassificationBatchDetail(processType)', 
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- =====================================================
-- Add providerName field to ProcessingClassificationBatch (Formato DUFER)
-- =====================================================
SET @col_exists := (SELECT COUNT(*) FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch' AND COLUMN_NAME = 'providerName');
SET @sql := IF(@col_exists = 0, 
    'ALTER TABLE ProcessingClassificationBatch ADD COLUMN providerName VARCHAR(200) COMMENT ''Provider/Supplier name (Formato DUFER)''', 
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
