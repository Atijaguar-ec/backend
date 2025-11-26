-- Add field inspection (sensory testing) fields to StockOrder table
-- These fields are used when facility.isFieldInspection = true

-- Check if StockOrder table exists
SET @stockOrderTableExists := (
  SELECT COUNT(*)
  FROM information_schema.TABLES
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'StockOrder'
);

-- Check if columns already exist
SET @flavorTestResultExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'StockOrder'
    AND COLUMN_NAME = 'flavorTestResult'
);

SET @flavorDefectTypeIdExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'StockOrder'
    AND COLUMN_NAME = 'flavor_defect_type_id'
);

SET @purchaseRecommendedExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'StockOrder'
    AND COLUMN_NAME = 'purchaseRecommended'
);

SET @inspectionNotesExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'StockOrder'
    AND COLUMN_NAME = 'inspectionNotes'
);

-- Check if ShrimpFlavorDefect table exists (only present for SHRIMP deployments)
SET @shrimpFlavorDefectTableExists := (
  SELECT COUNT(*)
  FROM information_schema.TABLES
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'ShrimpFlavorDefect'
);

-- Add columns only if table exists and columns don't exist
SET @ddlFlavorTestResult := IF(
  @stockOrderTableExists = 1 AND @flavorTestResultExists = 0,
  'ALTER TABLE StockOrder ADD COLUMN flavorTestResult VARCHAR(20)',
  'SELECT 1'
);
PREPARE stmtFlavorTestResult FROM @ddlFlavorTestResult;
EXECUTE stmtFlavorTestResult;
DEALLOCATE PREPARE stmtFlavorTestResult;

SET @ddlFlavorDefectTypeId := IF(
  @stockOrderTableExists = 1 AND @flavorDefectTypeIdExists = 0,
  'ALTER TABLE StockOrder ADD COLUMN flavor_defect_type_id BIGINT',
  'SELECT 1'
);
PREPARE stmtFlavorDefectTypeId FROM @ddlFlavorDefectTypeId;
EXECUTE stmtFlavorDefectTypeId;
DEALLOCATE PREPARE stmtFlavorDefectTypeId;

SET @ddlPurchaseRecommended := IF(
  @stockOrderTableExists = 1 AND @purchaseRecommendedExists = 0,
  'ALTER TABLE StockOrder ADD COLUMN purchaseRecommended BIT(1)',
  'SELECT 1'
);
PREPARE stmtPurchaseRecommended FROM @ddlPurchaseRecommended;
EXECUTE stmtPurchaseRecommended;
DEALLOCATE PREPARE stmtPurchaseRecommended;

SET @ddlInspectionNotes := IF(
  @stockOrderTableExists = 1 AND @inspectionNotesExists = 0,
  'ALTER TABLE StockOrder ADD COLUMN inspectionNotes VARCHAR(1000)',
  'SELECT 1'
);
PREPARE stmtInspectionNotes FROM @ddlInspectionNotes;
EXECUTE stmtInspectionNotes;
DEALLOCATE PREPARE stmtInspectionNotes;

-- Recalculate flavor_defect_type_id existence after potential ALTER
SET @flavorDefectTypeIdExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'StockOrder'
    AND COLUMN_NAME = 'flavor_defect_type_id'
);

-- Add foreign key constraint for flavor_defect_type only if table and column exist and constraint is missing
SET @fkExists := (
  SELECT COUNT(*)
  FROM information_schema.REFERENTIAL_CONSTRAINTS
  WHERE CONSTRAINT_SCHEMA = DATABASE()
    AND CONSTRAINT_NAME = 'fk_stock_order_flavor_defect_type'
);

SET @ddlFk := IF(
  @stockOrderTableExists = 1 AND @flavorDefectTypeIdExists = 1 AND @shrimpFlavorDefectTableExists = 1 AND @fkExists = 0,
  'ALTER TABLE StockOrder ADD CONSTRAINT fk_stock_order_flavor_defect_type FOREIGN KEY (flavor_defect_type_id) REFERENCES ShrimpFlavorDefect(id)',
  'SELECT 1'
);
PREPARE stmtFk FROM @ddlFk;
EXECUTE stmtFk;
DEALLOCATE PREPARE stmtFk;

-- Add index for better query performance if missing
SET @indexExists := (
  SELECT COUNT(*)
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'StockOrder'
    AND INDEX_NAME = 'idx_stock_order_flavor_defect_type'
);

SET @ddlIndex := IF(
  @stockOrderTableExists = 1 AND @indexExists = 0,
  'CREATE INDEX idx_stock_order_flavor_defect_type ON StockOrder(flavor_defect_type_id)',
  'SELECT 1'
);
PREPARE stmtIndex FROM @ddlIndex;
EXECUTE stmtIndex;
DEALLOCATE PREPARE stmtIndex;
