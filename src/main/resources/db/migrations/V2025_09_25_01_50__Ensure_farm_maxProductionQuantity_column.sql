-- Ensure farm_maxProductionQuantity column exists on UserCustomer
-- Aligns UserCustomer.farm (FarmInformation) embedded column with Hibernate expectations

SET @farmColumnExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'UserCustomer'
    AND COLUMN_NAME = 'farm_max_production_quantity'
);

SET @legacyColumnExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'UserCustomer'
    AND COLUMN_NAME = 'max_production_quantity'
);

SET @wrongNameExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'UserCustomer'
    AND COLUMN_NAME = 'farm_maxProductionQuantity'
);

SET @ddl :=
  IF(@farmColumnExists > 0,
    'SELECT ''farm_max_production_quantity already exists''',
    IF(@wrongNameExists > 0,
      'ALTER TABLE UserCustomer CHANGE COLUMN farm_maxProductionQuantity farm_max_production_quantity DECIMAL(19,2) NULL COMMENT ''Maximum production quantity in quintals (qq)''',
      IF(@legacyColumnExists > 0,
        'ALTER TABLE UserCustomer CHANGE COLUMN max_production_quantity farm_max_production_quantity DECIMAL(19,2) NULL COMMENT ''Maximum production quantity in quintals (qq)''',
        'ALTER TABLE UserCustomer ADD COLUMN farm_max_production_quantity DECIMAL(19,2) NULL COMMENT ''Maximum production quantity in quintals (qq)'''
      )
    )
  );

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
