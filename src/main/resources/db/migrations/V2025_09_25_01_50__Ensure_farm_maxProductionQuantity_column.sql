-- Ensure farm_maxProductionQuantity column exists on UserCustomer
-- Aligns UserCustomer.farm (FarmInformation) embedded column with Hibernate expectations

SET @farmColumnExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'UserCustomer'
    AND COLUMN_NAME = 'farm_maxProductionQuantity'
);

SET @legacyColumnExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'UserCustomer'
    AND COLUMN_NAME = 'max_production_quantity'
);

SET @ddl :=
  IF(@farmColumnExists > 0,
    'SELECT ''farm_maxProductionQuantity already exists''',
    IF(@legacyColumnExists > 0,
      'ALTER TABLE UserCustomer CHANGE COLUMN max_production_quantity farm_maxProductionQuantity DECIMAL(19,2) NULL COMMENT ''Maximum production quantity in quintals (qq)''',
      'ALTER TABLE UserCustomer ADD COLUMN farm_maxProductionQuantity DECIMAL(19,2) NULL COMMENT ''Maximum production quantity in quintals (qq)'''
    )
  );

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
