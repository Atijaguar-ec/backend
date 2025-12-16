-- Add person type and legal entity fields to UserCustomer

SET @columnExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'UserCustomer'
    AND COLUMN_NAME = 'personType'
);

SET @ddl := IF(
  @columnExists = 0,
  'ALTER TABLE UserCustomer ADD COLUMN personType ENUM(''NATURAL'',''LEGAL'') DEFAULT NULL COMMENT ''Person type: NATURAL (individual) or LEGAL (company)''',
  'SELECT 1'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add companyName column if it does not exist
SET @columnExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'UserCustomer'
    AND COLUMN_NAME = 'companyName'
);

SET @ddl := IF(
  @columnExists = 0,
  'ALTER TABLE UserCustomer ADD COLUMN companyName VARCHAR(255) DEFAULT NULL COMMENT ''Legal entity company name''',
  'SELECT 1'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add legalRepresentative column if it does not exist
SET @columnExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'UserCustomer'
    AND COLUMN_NAME = 'legalRepresentative'
);

SET @ddl := IF(
  @columnExists = 0,
  'ALTER TABLE UserCustomer ADD COLUMN legalRepresentative VARCHAR(255) DEFAULT NULL COMMENT ''Legal representative full name''',
  'SELECT 1'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
