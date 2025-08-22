-- Fix Payment.recipientType to avoid deadlocks with ENUM constraints
-- Change from ENUM to VARCHAR for better flexibility and performance

-- First, check if the column exists and what type it is
SET @column_exists = (
    SELECT COUNT(1) 
    FROM information_schema.columns 
    WHERE table_schema = DATABASE() 
      AND table_name = 'Payment' 
      AND column_name = 'recipientType'
);

-- Only modify if the column exists
SET @sql = IF(@column_exists > 0, 
    'ALTER TABLE Payment MODIFY COLUMN recipientType VARCHAR(32) NOT NULL COMMENT "Type of payment recipient: COMPANY or USER_CUSTOMER"',
    'SELECT "Column recipientType does not exist in Payment table" as message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add index for better query performance on recipientType
-- MySQL doesn't support CREATE INDEX IF NOT EXISTS, so we use conditional logic
SET @index_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.STATISTICS 
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'Payment' 
    AND INDEX_NAME = 'idx_payment_recipient_type');

SET @sql = IF(@index_exists = 0, 
    'CREATE INDEX idx_payment_recipient_type ON Payment (recipientType)', 
    'SELECT "Index idx_payment_recipient_type already exists" as Info');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
