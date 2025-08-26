-- Create tables required by entities Document and ActivityProof
-- This migration is idempotent on fresh installs; uses IF NOT EXISTS on table creation.

-- Document table
CREATE TABLE IF NOT EXISTS `Document` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `creationTimestamp` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `storageKey` VARCHAR(255) DEFAULT NULL,
  `type` VARCHAR(40) NOT NULL DEFAULT 'GENERAL',
  `name` VARCHAR(255) DEFAULT NULL,
  `contentType` VARCHAR(128) DEFAULT NULL,
  `size` BIGINT DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_document_storageKey` (`storageKey`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ActivityProof table
CREATE TABLE IF NOT EXISTS `ActivityProof` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `creationTimestamp` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updateTimestamp` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  `formalCreationDate` DATETIME(6) DEFAULT NULL,
  `validUntil` DATETIME(6) DEFAULT NULL,
  `type` VARCHAR(255) DEFAULT NULL,
  `document_id` BIGINT DEFAULT NULL,
  `entityVersion` BIGINT DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_activityproof_document_id` (`document_id`),
  CONSTRAINT `fk_activityproof_document` FOREIGN KEY (`document_id`) REFERENCES `Document` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
