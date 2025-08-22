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
CREATE INDEX IF NOT EXISTS idx_payment_recipient_type ON Payment (recipientType);
