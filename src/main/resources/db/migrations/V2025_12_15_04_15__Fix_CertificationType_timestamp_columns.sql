-- Fix CertificationType and CertificationTypeTranslation timestamp column names
-- to match TimestampEntity (creationTimestamp/updateTimestamp instead of createdAt/updatedAt)

-- Check if CertificationType table exists and has createdAt column, then rename
SET @table_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.TABLES 
    WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'certificationtype');

SET @old_col_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'certificationtype' AND LOWER(COLUMN_NAME) = 'createdat');
SET @new_col_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'certificationtype' AND LOWER(COLUMN_NAME) = 'creationtimestamp');

SET @sql = IF(@table_exists > 0 AND @old_col_exists > 0 AND @new_col_exists = 0, 
    'ALTER TABLE CertificationType RENAME COLUMN createdAt TO creationTimestamp',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @old_col_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'certificationtype' AND LOWER(COLUMN_NAME) = 'updatedat');
SET @new_col_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'certificationtype' AND LOWER(COLUMN_NAME) = 'updatetimestamp');

SET @sql = IF(@table_exists > 0 AND @old_col_exists > 0 AND @new_col_exists = 0, 
    'ALTER TABLE CertificationType RENAME COLUMN updatedAt TO updateTimestamp',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Fix CertificationTypeTranslation table
SET @table_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.TABLES 
    WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'certificationtypetranslation');

SET @old_col_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'certificationtypetranslation' AND LOWER(COLUMN_NAME) = 'createdat');
SET @new_col_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'certificationtypetranslation' AND LOWER(COLUMN_NAME) = 'creationtimestamp');

SET @sql = IF(@table_exists > 0 AND @old_col_exists > 0 AND @new_col_exists = 0, 
    'ALTER TABLE CertificationTypeTranslation RENAME COLUMN createdAt TO creationTimestamp',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @old_col_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'certificationtypetranslation' AND LOWER(COLUMN_NAME) = 'updatedat');
SET @new_col_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'certificationtypetranslation' AND LOWER(COLUMN_NAME) = 'updatetimestamp');

SET @sql = IF(@table_exists > 0 AND @old_col_exists > 0 AND @new_col_exists = 0, 
    'ALTER TABLE CertificationTypeTranslation RENAME COLUMN updatedAt TO updateTimestamp',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

