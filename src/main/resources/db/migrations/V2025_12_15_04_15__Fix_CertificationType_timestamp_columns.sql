-- Fix CertificationType and CertificationTypeTranslation timestamp column names
-- to match TimestampEntity (creationTimestamp/updateTimestamp instead of createdAt/updatedAt)

-- Check if CertificationType table exists and has createdAt column, then rename
SET @table_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.TABLES 
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'CertificationType');

SET @col_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'CertificationType' AND COLUMN_NAME = 'createdAt');

SET @sql = IF(@table_exists > 0 AND @col_exists > 0, 
    'ALTER TABLE CertificationType CHANGE COLUMN createdAt creationTimestamp DATETIME(6) DEFAULT NULL',
    'SELECT "CertificationType.createdAt column not found or table missing" as Info');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'CertificationType' AND COLUMN_NAME = 'updatedAt');

SET @sql = IF(@table_exists > 0 AND @col_exists > 0, 
    'ALTER TABLE CertificationType CHANGE COLUMN updatedAt updateTimestamp DATETIME(6) DEFAULT NULL',
    'SELECT "CertificationType.updatedAt column not found or table missing" as Info');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Fix CertificationTypeTranslation table
SET @table_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.TABLES 
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'CertificationTypeTranslation');

SET @col_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'CertificationTypeTranslation' AND COLUMN_NAME = 'createdAt');

SET @sql = IF(@table_exists > 0 AND @col_exists > 0, 
    'ALTER TABLE CertificationTypeTranslation CHANGE COLUMN createdAt creationTimestamp DATETIME(6) DEFAULT NULL',
    'SELECT "CertificationTypeTranslation.createdAt column not found or table missing" as Info');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'CertificationTypeTranslation' AND COLUMN_NAME = 'updatedAt');

SET @sql = IF(@table_exists > 0 AND @col_exists > 0, 
    'ALTER TABLE CertificationTypeTranslation CHANGE COLUMN updatedAt updateTimestamp DATETIME(6) DEFAULT NULL',
    'SELECT "CertificationTypeTranslation.updatedAt column not found or table missing" as Info');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
