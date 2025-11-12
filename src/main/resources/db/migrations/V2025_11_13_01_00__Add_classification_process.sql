-- =====================================================
-- Migration: Add Classification Process Support
-- Date: 2025-11-13
-- Description: Adds support for shrimp classification process
-- =====================================================

-- =====================================================
-- 1. Add isClassificationProcess to Facility table
-- =====================================================
SET @facilityTableExists := (
  SELECT COUNT(*)
  FROM information_schema.TABLES
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'Facility'
);

SET @isClassificationProcessColumnExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'Facility'
    AND COLUMN_NAME = 'isClassificationProcess'
);

SET @facilityDdl := IF(
  @facilityTableExists = 1 AND @isClassificationProcessColumnExists = 0,
  'ALTER TABLE Facility ADD COLUMN isClassificationProcess BIT(1) DEFAULT 0 COMMENT ''Indicates if this facility performs shrimp classification''',
  'SELECT 1'
);

PREPARE facilityStmt FROM @facilityDdl;
EXECUTE facilityStmt;
DEALLOCATE PREPARE facilityStmt;

-- =====================================================
-- 2. Create ProcessingClassificationBatch table
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
    '  version BIGINT,',
    '  created DATETIME(6),',
    '  updated DATETIME(6),',
    '  targetStockOrderId BIGINT NOT NULL COMMENT ''Reference to target stock order (output)'',',
    '  startTime VARCHAR(10) COMMENT ''Hora de inicio (HH:MM)'',',
    '  endTime VARCHAR(10) COMMENT ''Hora termina (HH:MM)'',',
    '  productionOrder VARCHAR(100) COMMENT ''Orden de producción'',',
    '  freezingType VARCHAR(50) COMMENT ''Tipo de congelación'',',
    '  machine VARCHAR(50) COMMENT ''Máquina utilizada'',',
    '  brandHeader VARCHAR(100) COMMENT ''Marca (cabecera)'',',
    '  PRIMARY KEY (id),',
    '  CONSTRAINT fk_pcb_target_stock_order FOREIGN KEY (targetStockOrderId) REFERENCES StockOrder(id) ON DELETE CASCADE',
    ') ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci'
  ),
  'SELECT 1'
);

PREPARE batchStmt FROM @batchDdl;
EXECUTE batchStmt;
DEALLOCATE PREPARE batchStmt;

-- =====================================================
-- 3. Create ProcessingClassificationBatchDetail table
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
    '  version BIGINT,',
    '  created DATETIME(6),',
    '  updated DATETIME(6),',
    '  batchId BIGINT NOT NULL COMMENT ''Reference to classification batch'',',
    '  brandDetail VARCHAR(100) COMMENT ''Marca específica del detalle'',',
    '  size VARCHAR(20) COMMENT ''Tallas (e.g., 16/20, 21/25)'',',
    '  boxes INT COMMENT ''Número de cajas'',',
    '  classificationU DECIMAL(10,2) COMMENT ''Clasificación U'',',
    '  classificationNumber DECIMAL(10,2) COMMENT ''Clasificación #'',',
    '  weightPerBox DECIMAL(10,2) COMMENT ''Peso por caja'',',
    '  weightFormat VARCHAR(10) DEFAULT ''LB'' COMMENT ''Formato: LB o KG'',',
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
-- 4. Create indexes for performance
-- =====================================================
SET @batchIndexExists := (
  SELECT COUNT(*)
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'ProcessingClassificationBatch'
    AND INDEX_NAME = 'idx_pcb_target_stock_order'
);

SET @batchIndexDdl := IF(
  @batchIndexExists = 0 AND @batchTableExists = 1,
  'CREATE INDEX idx_pcb_target_stock_order ON ProcessingClassificationBatch(targetStockOrderId)',
  'SELECT 1'
);

PREPARE batchIndexStmt FROM @batchIndexDdl;
EXECUTE batchIndexStmt;
DEALLOCATE PREPARE batchIndexStmt;

SET @detailIndexExists := (
  SELECT COUNT(*)
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'ProcessingClassificationBatchDetail'
    AND INDEX_NAME = 'idx_pcbd_batch'
);

SET @detailIndexDdl := IF(
  @detailIndexExists = 0 AND @detailTableExists = 1,
  'CREATE INDEX idx_pcbd_batch ON ProcessingClassificationBatchDetail(batchId)',
  'SELECT 1'
);

PREPARE detailIndexStmt FROM @detailIndexDdl;
EXECUTE detailIndexStmt;
DEALLOCATE PREPARE detailIndexStmt;

-- =====================================================
-- Migration completed successfully
-- =====================================================
