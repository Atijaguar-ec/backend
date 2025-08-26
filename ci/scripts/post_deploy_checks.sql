-- Post-deploy non-destructive verification checks
-- Environment: MySQL 8+
-- This script only runs reads (SELECT/SHOW) and does not modify any data.

/* =============================================
   CompanyProcessingAction — table, columns, timestamps, indexes, FKs
   ============================================= */

-- 1) Verify table exists
SELECT COUNT(*) AS table_exists
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'CompanyProcessingAction';

-- 2) Verify key columns and types
SELECT COLUMN_NAME, DATA_TYPE, COLUMN_TYPE, IS_NULLABLE, COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'CompanyProcessingAction'
  AND COLUMN_NAME IN (
    'id','entityVersion','entity_version',
    'company_id','processing_action_id',
    'enabled','order_override','alias_label',
    'creationTimestamp','updateTimestamp'
  )
ORDER BY FIELD(
  COLUMN_NAME,
  'id','entityVersion','entity_version',
  'company_id','processing_action_id',
  'enabled','order_override','alias_label',
  'creationTimestamp','updateTimestamp'
);

-- 3) Verify presence of timestamp columns expected by TimestampEntity
SELECT
  SUM(CASE WHEN COLUMN_NAME='creationTimestamp' THEN 1 ELSE 0 END) AS has_creationTimestamp,
  SUM(CASE WHEN COLUMN_NAME='updateTimestamp' THEN 1 ELSE 0 END)   AS has_updateTimestamp
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'CompanyProcessingAction'
  AND COLUMN_NAME IN ('creationTimestamp','updateTimestamp');

-- 4) Quick sample of timestamp values
SELECT id, creationTimestamp, updateTimestamp
FROM `CompanyProcessingAction`
ORDER BY id DESC
LIMIT 5;

-- 5) Verify expected non-unique indexes
SELECT
  INDEX_NAME,
  NON_UNIQUE,
  GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) AS columns
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'CompanyProcessingAction'
  AND INDEX_NAME IN (
    'idx_company_processing_action_company_enabled',
    'idx_company_processing_action_processing_action'
  )
GROUP BY INDEX_NAME, NON_UNIQUE;

-- 6) Verify UNIQUE constraint and FKs
SELECT
  tc.CONSTRAINT_NAME,
  tc.CONSTRAINT_TYPE,
  GROUP_CONCAT(kcu.COLUMN_NAME ORDER BY kcu.ORDINAL_POSITION) AS columns
FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS tc
JOIN INFORMATION_SCHEMA.KEY_COLUMN_USAGE kcu
  ON tc.CONSTRAINT_NAME = kcu.CONSTRAINT_NAME
  AND tc.TABLE_SCHEMA   = kcu.TABLE_SCHEMA
  AND tc.TABLE_NAME     = kcu.TABLE_NAME
WHERE tc.TABLE_SCHEMA = DATABASE()
  AND tc.TABLE_NAME = 'CompanyProcessingAction'
  AND tc.CONSTRAINT_TYPE IN ('UNIQUE','FOREIGN KEY')
GROUP BY tc.CONSTRAINT_NAME, tc.CONSTRAINT_TYPE
ORDER BY tc.CONSTRAINT_TYPE, tc.CONSTRAINT_NAME;

/* =============================================
   Transaction — final index and FK verification
   ============================================= */

-- 1) There should be NO unique index exclusively on these columns
SELECT INDEX_NAME,
       GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) AS columns
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'Transaction'
  AND NON_UNIQUE = 0
  AND INDEX_NAME <> 'PRIMARY'
GROUP BY INDEX_NAME
HAVING columns IN ('sourceStockOrder_id','inputMeasureUnitType_id');

-- 2) Expected non-unique indexes should exist with these names/columns
SELECT
  INDEX_NAME,
  NON_UNIQUE,
  GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) AS columns
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'Transaction'
  AND INDEX_NAME IN (
    'idx_transaction_source_stock_order',
    'idx_transaction_input_measure_unit_type'
  )
GROUP BY INDEX_NAME, NON_UNIQUE;

-- 3) Verify FKs on both columns
SELECT
  CONSTRAINT_NAME, COLUMN_NAME, REFERENCED_TABLE_NAME, REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'Transaction'
  AND COLUMN_NAME IN ('sourceStockOrder_id','inputMeasureUnitType_id')
  AND REFERENCED_TABLE_NAME IS NOT NULL
ORDER BY COLUMN_NAME, CONSTRAINT_NAME;

-- 4) Optional: legacy unique index should be absent
SELECT COUNT(*) AS legacy_uk_present
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'Transaction'
  AND INDEX_NAME = 'UKtqkhranfyfkv32rh6jaogmrwa';

-- 5) Optional: full table DDL for inspection
SHOW CREATE TABLE `Transaction`;
