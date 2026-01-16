-- =====================================================
-- Migration: Restore LaboratoryAnalysis table
-- Date: 2026-01-16
-- Description: Restores shrimp-related LaboratoryAnalysis table if it was removed in non-shrimp deployments.
-- =====================================================

SET @table_exists := (
  SELECT COUNT(*)
  FROM information_schema.TABLES
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'LaboratoryAnalysis'
);

SET @sql := IF(
  @table_exists = 0,
  'CREATE TABLE `LaboratoryAnalysis` (
    `id` bigint NOT NULL AUTO_INCREMENT,

    `creationTimestamp` datetime(6) DEFAULT NULL,
    `updateTimestamp` datetime(6) DEFAULT NULL,
    `createdBy_id` bigint NOT NULL,
    `updatedBy_id` bigint DEFAULT NULL,

    `stockOrder_id` bigint NOT NULL,
    `destinationStockOrder_id` bigint DEFAULT NULL,

    `analysisType` enum(\'SENSORIAL\',\'MICROBIOLOGICAL\',\'CHEMICAL\',\'PCR\') NOT NULL DEFAULT \'SENSORIAL\',
    `analysisDate` datetime(6) DEFAULT NULL,

    `sensorialRawOdor` varchar(255) DEFAULT NULL,
    `sensorial_raw_odor_intensity` varchar(20) DEFAULT NULL,
    `sensorialRawTaste` varchar(255) DEFAULT NULL,
    `sensorial_raw_taste_intensity` varchar(20) DEFAULT NULL,
    `sensorialRawColor` varchar(255) DEFAULT NULL,

    `sensorialCookedOdor` varchar(255) DEFAULT NULL,
    `sensorial_cooked_odor_intensity` varchar(20) DEFAULT NULL,
    `sensorialCookedTaste` varchar(255) DEFAULT NULL,
    `sensorial_cooked_taste_intensity` varchar(20) DEFAULT NULL,
    `sensorialCookedColor` varchar(255) DEFAULT NULL,

    `qualityNotes` longtext,
    `metabisulfiteLevelAcceptable` tinyint(1) DEFAULT NULL,
    `approvedForPurchase` tinyint(1) DEFAULT NULL,

    PRIMARY KEY (`id`),

    KEY `FK_laboratory_analysis_stock_order` (`stockOrder_id`),
    KEY `FK_laboratory_analysis_dest_stock_order` (`destinationStockOrder_id`),
    KEY `FK_laboratory_analysis_created_by` (`createdBy_id`),
    KEY `FK_laboratory_analysis_updated_by` (`updatedBy_id`),

    KEY `IDX_laboratory_analysis_type` (`analysisType`),
    KEY `IDX_laboratory_analysis_date` (`analysisDate`),
    KEY `IDX_lab_analysis_raw_odor_intensity` (`sensorial_raw_odor_intensity`),
    KEY `IDX_lab_analysis_raw_taste_intensity` (`sensorial_raw_taste_intensity`),

    CONSTRAINT `FK_laboratory_analysis_stock_order` FOREIGN KEY (`stockOrder_id`) REFERENCES `StockOrder` (`id`) ON DELETE CASCADE,
    CONSTRAINT `FK_laboratory_analysis_dest_stock_order` FOREIGN KEY (`destinationStockOrder_id`) REFERENCES `StockOrder` (`id`),
    CONSTRAINT `FK_laboratory_analysis_created_by` FOREIGN KEY (`createdBy_id`) REFERENCES `User` (`id`),
    CONSTRAINT `FK_laboratory_analysis_updated_by` FOREIGN KEY (`updatedBy_id`) REFERENCES `User` (`id`)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci',
  'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Ensure columns exist (idempotent) for databases where the table exists but is missing some columns.
SET @col_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'LaboratoryAnalysis'
    AND COLUMN_NAME = 'destinationStockOrder_id'
);
SET @sql := IF(
  @col_exists = 0,
  'ALTER TABLE `LaboratoryAnalysis` ADD COLUMN `destinationStockOrder_id` bigint NULL AFTER `stockOrder_id`',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'LaboratoryAnalysis'
    AND COLUMN_NAME = 'metabisulfiteLevelAcceptable'
);
SET @sql := IF(
  @col_exists = 0,
  'ALTER TABLE `LaboratoryAnalysis` ADD COLUMN `metabisulfiteLevelAcceptable` TINYINT(1) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'LaboratoryAnalysis'
    AND COLUMN_NAME = 'approvedForPurchase'
);
SET @sql := IF(
  @col_exists = 0,
  'ALTER TABLE `LaboratoryAnalysis` ADD COLUMN `approvedForPurchase` TINYINT(1) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'LaboratoryAnalysis'
    AND COLUMN_NAME = 'sensorial_raw_odor_intensity'
);
SET @sql := IF(
  @col_exists = 0,
  'ALTER TABLE `LaboratoryAnalysis` ADD COLUMN `sensorial_raw_odor_intensity` VARCHAR(20) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'LaboratoryAnalysis'
    AND COLUMN_NAME = 'sensorial_raw_taste_intensity'
);
SET @sql := IF(
  @col_exists = 0,
  'ALTER TABLE `LaboratoryAnalysis` ADD COLUMN `sensorial_raw_taste_intensity` VARCHAR(20) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'LaboratoryAnalysis'
    AND COLUMN_NAME = 'sensorial_cooked_odor_intensity'
);
SET @sql := IF(
  @col_exists = 0,
  'ALTER TABLE `LaboratoryAnalysis` ADD COLUMN `sensorial_cooked_odor_intensity` VARCHAR(20) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'LaboratoryAnalysis'
    AND COLUMN_NAME = 'sensorial_cooked_taste_intensity'
);
SET @sql := IF(
  @col_exists = 0,
  'ALTER TABLE `LaboratoryAnalysis` ADD COLUMN `sensorial_cooked_taste_intensity` VARCHAR(20) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Ensure indexes exist (idempotent)
SET @idx_exists := (
  SELECT COUNT(*)
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'LaboratoryAnalysis'
    AND INDEX_NAME = 'IDX_lab_analysis_raw_odor_intensity'
);
SET @sql := IF(
  @idx_exists = 0,
  'CREATE INDEX `IDX_lab_analysis_raw_odor_intensity` ON `LaboratoryAnalysis` (`sensorial_raw_odor_intensity`)',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists := (
  SELECT COUNT(*)
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'LaboratoryAnalysis'
    AND INDEX_NAME = 'IDX_lab_analysis_raw_taste_intensity'
);
SET @sql := IF(
  @idx_exists = 0,
  'CREATE INDEX `IDX_lab_analysis_raw_taste_intensity` ON `LaboratoryAnalysis` (`sensorial_raw_taste_intensity`)',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Ensure foreign key for destinationStockOrder_id exists (idempotent)
SET @fk_exists := (
  SELECT COUNT(*)
  FROM information_schema.TABLE_CONSTRAINTS
  WHERE CONSTRAINT_SCHEMA = DATABASE()
    AND TABLE_NAME = 'LaboratoryAnalysis'
    AND CONSTRAINT_NAME = 'FK_laboratory_analysis_dest_stock_order'
    AND CONSTRAINT_TYPE = 'FOREIGN KEY'
);
SET @sql := IF(
  @fk_exists = 0,
  'ALTER TABLE `LaboratoryAnalysis` ADD CONSTRAINT `FK_laboratory_analysis_dest_stock_order` FOREIGN KEY (`destinationStockOrder_id`) REFERENCES `StockOrder` (`id`)',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
