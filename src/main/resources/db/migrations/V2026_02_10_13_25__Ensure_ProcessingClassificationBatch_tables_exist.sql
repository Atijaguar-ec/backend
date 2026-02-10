-- =====================================================
-- Migration: Ensure ProcessingClassificationBatch tables exist
-- Date: 2026-02-10
-- Description: Recreates ProcessingClassificationBatch and
--              ProcessingClassificationBatchDetail tables if they
--              were accidentally dropped. Idempotent - safe to run
--              even if tables already exist.
-- =====================================================

-- =====================================================
-- 1. Create ProcessingClassificationBatch if not exists
-- =====================================================
SET @batchTableExists := (
  SELECT COUNT(*)
  FROM information_schema.TABLES
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'ProcessingClassificationBatch'
);

SET @batchDdl := IF(
  @batchTableExists = 0,
  CONCAT(
    'CREATE TABLE ProcessingClassificationBatch (',
    '  id BIGINT NOT NULL AUTO_INCREMENT,',
    '  entityVersion BIGINT DEFAULT NULL,',
    '  creationTimestamp DATETIME(6) DEFAULT NULL,',
    '  updateTimestamp DATETIME(6) DEFAULT NULL,',
    '  targetStockOrderId BIGINT NOT NULL,',
    '  startTime VARCHAR(10) NULL,',
    '  endTime VARCHAR(10) NULL,',
    '  productionOrder VARCHAR(100) NULL,',
    '  freezingType VARCHAR(50) NULL,',
    '  machine VARCHAR(50) NULL,',
    '  brandHeader VARCHAR(100) NULL,',
    '  providerName VARCHAR(200) NULL,',
    '  settlementNumber VARCHAR(50) NULL,',
    '  processType VARCHAR(30) NULL,',
    '  poundsReceived DECIMAL(12,2) NULL,',
    '  poundsWaste DECIMAL(12,2) NULL,',
    '  poundsNetReceived DECIMAL(12,2) NULL,',
    '  poundsProcessed DECIMAL(12,2) NULL,',
    '  yieldPercentage DECIMAL(5,2) NULL,',
    '  totalAmount DECIMAL(14,2) NULL,',
    '  averagePrice DECIMAL(10,4) NULL,',
    '  settlementStatus VARCHAR(20) NULL,',
    '  output_type VARCHAR(20) DEFAULT ''PROCESSED'',',
    '  pounds_rejected DECIMAL(12,2) NULL,',
    '  rejected_stock_order_id BIGINT NULL,',
    '  PRIMARY KEY (id),',
    '  CONSTRAINT fk_pcb_target_stock_order FOREIGN KEY (targetStockOrderId) REFERENCES StockOrder(id) ON DELETE CASCADE,',
    '  CONSTRAINT fk_pcb_rejected_stock_order FOREIGN KEY (rejected_stock_order_id) REFERENCES StockOrder(id) ON DELETE SET NULL',
    ') ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci'
  ),
  'SELECT 1'
);

PREPARE batchStmt FROM @batchDdl;
EXECUTE batchStmt;
DEALLOCATE PREPARE batchStmt;

-- =====================================================
-- 2. Create ProcessingClassificationBatchDetail if not exists
-- =====================================================
SET @detailTableExists := (
  SELECT COUNT(*)
  FROM information_schema.TABLES
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'ProcessingClassificationBatchDetail'
);

SET @detailDdl := IF(
  @detailTableExists = 0,
  CONCAT(
    'CREATE TABLE ProcessingClassificationBatchDetail (',
    '  id BIGINT NOT NULL AUTO_INCREMENT,',
    '  entityVersion BIGINT DEFAULT NULL,',
    '  creationTimestamp DATETIME(6) DEFAULT NULL,',
    '  updateTimestamp DATETIME(6) DEFAULT NULL,',
    '  batchId BIGINT NOT NULL,',
    '  brandDetail VARCHAR(100) NULL,',
    '  size VARCHAR(20) NULL,',
    '  boxes INT NULL,',
    '  classificationU DECIMAL(10,2) NULL,',
    '  classificationNumber DECIMAL(10,2) NULL,',
    '  weightPerBox DECIMAL(10,2) NULL,',
    '  weightFormat VARCHAR(10) DEFAULT ''LB'',',
    '  processType VARCHAR(20) DEFAULT ''SHELL_ON'',',
    '  qualityGrade VARCHAR(10) NULL,',
    '  presentationType VARCHAR(50) NULL,',
    '  pricePerPound DECIMAL(10,4) NULL,',
    '  lineTotal DECIMAL(12,2) NULL,',
    '  PRIMARY KEY (id),',
    '  CONSTRAINT fk_pcbd_batch FOREIGN KEY (batchId) REFERENCES ProcessingClassificationBatch(id) ON DELETE CASCADE',
    ') ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci'
  ),
  'SELECT 1'
);

PREPARE detailStmt FROM @detailDdl;
EXECUTE detailStmt;
DEALLOCATE PREPARE detailStmt;

-- =====================================================
-- 3. Create indexes if not exist
-- =====================================================

-- Index on targetStockOrderId
SET @idxExists := (SELECT COUNT(*) FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch' AND INDEX_NAME = 'idx_pcb_target_stock_order');
SET @sql := IF(@idxExists = 0,
  'CREATE INDEX idx_pcb_target_stock_order ON ProcessingClassificationBatch(targetStockOrderId)',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Index on output_type
SET @idxExists := (SELECT COUNT(*) FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch' AND INDEX_NAME = 'idx_pcb_output_type');
SET @sql := IF(@idxExists = 0,
  'CREATE INDEX idx_pcb_output_type ON ProcessingClassificationBatch(output_type)',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Index on batchId (detail)
SET @idxExists := (SELECT COUNT(*) FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatchDetail' AND INDEX_NAME = 'idx_pcbd_batch');
SET @sql := IF(@idxExists = 0,
  'CREATE INDEX idx_pcbd_batch ON ProcessingClassificationBatchDetail(batchId)',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Index on processType (detail)
SET @idxExists := (SELECT COUNT(*) FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatchDetail' AND INDEX_NAME = 'idx_classification_detail_process_type');
SET @sql := IF(@idxExists = 0,
  'CREATE INDEX idx_classification_detail_process_type ON ProcessingClassificationBatchDetail(processType)',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- =====================================================
-- Migration completed successfully
-- =====================================================
