-- Safely (re)create CompanyProcessingAction table if it is missing
-- Fixes previous migration issue where column name mismatch caused failure

-- 1) Create table if not exists with correct column names
CREATE TABLE IF NOT EXISTS `CompanyProcessingAction` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `entityVersion` BIGINT NOT NULL DEFAULT 0,
  `company_id` BIGINT NOT NULL,
  `processing_action_id` BIGINT NOT NULL,
  `enabled` BOOLEAN NOT NULL DEFAULT TRUE,
  `order_override` INT NULL,
  `alias_label` VARCHAR(255) NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_company_processing_action_company`
    FOREIGN KEY (`company_id`) REFERENCES `Company`(`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_company_processing_action_processing_action`
    FOREIGN KEY (`processing_action_id`) REFERENCES `ProcessingAction`(`id`) ON DELETE CASCADE,
  CONSTRAINT `uk_company_processing_action_company_action`
    UNIQUE (`company_id`, `processing_action_id`)
) ENGINE=InnoDB;

-- 2) Ensure indexes exist (idempotent)
SET @idx1 := (SELECT INDEX_NAME FROM INFORMATION_SCHEMA.STATISTICS
              WHERE TABLE_SCHEMA = DATABASE()
                AND TABLE_NAME = 'CompanyProcessingAction'
                AND INDEX_NAME = 'idx_company_processing_action_company_enabled' LIMIT 1);
SET @sql1 := IF(@idx1 IS NULL,
  'CREATE INDEX idx_company_processing_action_company_enabled ON CompanyProcessingAction (company_id, enabled, order_override)',
  'SELECT 1');
PREPARE s1 FROM @sql1; EXECUTE s1; DEALLOCATE PREPARE s1;

SET @idx2 := (SELECT INDEX_NAME FROM INFORMATION_SCHEMA.STATISTICS
              WHERE TABLE_SCHEMA = DATABASE()
                AND TABLE_NAME = 'CompanyProcessingAction'
                AND INDEX_NAME = 'idx_company_processing_action_processing_action' LIMIT 1);
SET @sql2 := IF(@idx2 IS NULL,
  'CREATE INDEX idx_company_processing_action_processing_action ON CompanyProcessingAction (processing_action_id)',
  'SELECT 1');
PREPARE s2 FROM @sql2; EXECUTE s2; DEALLOCATE PREPARE s2;

-- 3) Seed data only if Company and ProcessingAction tables exist
SET @company_exists := (SELECT COUNT(1) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'Company');
SET @pa_exists := (SELECT COUNT(1) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingAction');

SET @seed_sql := IF(@company_exists > 0 AND @pa_exists > 0,
  'INSERT IGNORE INTO CompanyProcessingAction (company_id, processing_action_id, enabled, order_override, entityVersion)\n   SELECT c.id, pa.id, TRUE, NULL, 0 FROM Company c CROSS JOIN ProcessingAction pa',
  'SELECT 1');
PREPARE s3 FROM @seed_sql; EXECUTE s3; DEALLOCATE PREPARE s3;
