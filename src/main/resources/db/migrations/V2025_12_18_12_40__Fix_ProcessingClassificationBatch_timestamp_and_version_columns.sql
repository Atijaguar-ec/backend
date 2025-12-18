SET @table_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch');

SET @old_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch' AND COLUMN_NAME = 'created');
SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch' AND COLUMN_NAME = 'creationTimestamp');
SET @sql = IF(@table_exists > 0 AND @old_exists > 0 AND @new_exists = 0,
  'ALTER TABLE ProcessingClassificationBatch CHANGE COLUMN created creationTimestamp DATETIME(6) DEFAULT NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch' AND COLUMN_NAME = 'creationTimestamp');
SET @sql = IF(@table_exists > 0 AND @new_exists = 0,
  'ALTER TABLE ProcessingClassificationBatch ADD COLUMN creationTimestamp DATETIME(6) DEFAULT NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @old_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch' AND COLUMN_NAME = 'created');
SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch' AND COLUMN_NAME = 'creationTimestamp');
SET @sql = IF(@table_exists > 0 AND @old_exists > 0 AND @new_exists > 0,
  'UPDATE ProcessingClassificationBatch SET creationTimestamp = created WHERE creationTimestamp IS NULL AND created IS NOT NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @old_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch' AND COLUMN_NAME = 'updated');
SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch' AND COLUMN_NAME = 'updateTimestamp');
SET @sql = IF(@table_exists > 0 AND @old_exists > 0 AND @new_exists = 0,
  'ALTER TABLE ProcessingClassificationBatch CHANGE COLUMN updated updateTimestamp DATETIME(6) DEFAULT NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch' AND COLUMN_NAME = 'updateTimestamp');
SET @sql = IF(@table_exists > 0 AND @new_exists = 0,
  'ALTER TABLE ProcessingClassificationBatch ADD COLUMN updateTimestamp DATETIME(6) DEFAULT NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @old_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch' AND COLUMN_NAME = 'updated');
SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch' AND COLUMN_NAME = 'updateTimestamp');
SET @sql = IF(@table_exists > 0 AND @old_exists > 0 AND @new_exists > 0,
  'UPDATE ProcessingClassificationBatch SET updateTimestamp = updated WHERE updateTimestamp IS NULL AND updated IS NOT NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @old_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch' AND COLUMN_NAME = 'version');
SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch' AND COLUMN_NAME = 'entityVersion');
SET @sql = IF(@table_exists > 0 AND @old_exists > 0 AND @new_exists = 0,
  'ALTER TABLE ProcessingClassificationBatch CHANGE COLUMN version entityVersion BIGINT DEFAULT NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch' AND COLUMN_NAME = 'entityVersion');
SET @sql = IF(@table_exists > 0 AND @new_exists = 0,
  'ALTER TABLE ProcessingClassificationBatch ADD COLUMN entityVersion BIGINT DEFAULT NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @old_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch' AND COLUMN_NAME = 'version');
SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch' AND COLUMN_NAME = 'entityVersion');
SET @sql = IF(@table_exists > 0 AND @old_exists > 0 AND @new_exists > 0,
  'UPDATE ProcessingClassificationBatch SET entityVersion = version WHERE entityVersion IS NULL AND version IS NOT NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @old_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch' AND COLUMN_NAME = 'settlement_number');
SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch' AND COLUMN_NAME = 'settlementNumber');
SET @sql = IF(@table_exists > 0 AND @old_exists > 0 AND @new_exists = 0,
  'ALTER TABLE ProcessingClassificationBatch CHANGE COLUMN settlement_number settlementNumber VARCHAR(50) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch' AND COLUMN_NAME = 'settlementNumber');
SET @sql = IF(@table_exists > 0 AND @new_exists = 0,
  'ALTER TABLE ProcessingClassificationBatch ADD COLUMN settlementNumber VARCHAR(50) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @old_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch' AND COLUMN_NAME = 'settlement_number');
SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch' AND COLUMN_NAME = 'settlementNumber');
SET @sql = IF(@table_exists > 0 AND @old_exists > 0 AND @new_exists > 0,
  'UPDATE ProcessingClassificationBatch SET settlementNumber = settlement_number WHERE settlementNumber IS NULL AND settlement_number IS NOT NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @old_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch' AND COLUMN_NAME = 'process_type');
SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch' AND COLUMN_NAME = 'processType');
SET @sql = IF(@table_exists > 0 AND @old_exists > 0 AND @new_exists = 0,
  'ALTER TABLE ProcessingClassificationBatch CHANGE COLUMN process_type processType VARCHAR(30) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch' AND COLUMN_NAME = 'processType');
SET @sql = IF(@table_exists > 0 AND @new_exists = 0,
  'ALTER TABLE ProcessingClassificationBatch ADD COLUMN processType VARCHAR(30) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @old_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch' AND COLUMN_NAME = 'process_type');
SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch' AND COLUMN_NAME = 'processType');
SET @sql = IF(@table_exists > 0 AND @old_exists > 0 AND @new_exists > 0,
  'UPDATE ProcessingClassificationBatch SET processType = process_type WHERE processType IS NULL AND process_type IS NOT NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @old_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch' AND COLUMN_NAME = 'pounds_received');
SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch' AND COLUMN_NAME = 'poundsReceived');
SET @sql = IF(@table_exists > 0 AND @old_exists > 0 AND @new_exists = 0,
  'ALTER TABLE ProcessingClassificationBatch CHANGE COLUMN pounds_received poundsReceived DECIMAL(12,2) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch' AND COLUMN_NAME = 'poundsReceived');
SET @sql = IF(@table_exists > 0 AND @new_exists = 0,
  'ALTER TABLE ProcessingClassificationBatch ADD COLUMN poundsReceived DECIMAL(12,2) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @old_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch' AND COLUMN_NAME = 'pounds_received');
SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch' AND COLUMN_NAME = 'poundsReceived');
SET @sql = IF(@table_exists > 0 AND @old_exists > 0 AND @new_exists > 0,
  'UPDATE ProcessingClassificationBatch SET poundsReceived = pounds_received WHERE poundsReceived IS NULL AND pounds_received IS NOT NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @old_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch' AND COLUMN_NAME = 'pounds_waste');
SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch' AND COLUMN_NAME = 'poundsWaste');
SET @sql = IF(@table_exists > 0 AND @old_exists > 0 AND @new_exists = 0,
  'ALTER TABLE ProcessingClassificationBatch CHANGE COLUMN pounds_waste poundsWaste DECIMAL(12,2) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch' AND COLUMN_NAME = 'poundsWaste');
SET @sql = IF(@table_exists > 0 AND @new_exists = 0,
  'ALTER TABLE ProcessingClassificationBatch ADD COLUMN poundsWaste DECIMAL(12,2) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @old_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch' AND COLUMN_NAME = 'pounds_waste');
SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch' AND COLUMN_NAME = 'poundsWaste');
SET @sql = IF(@table_exists > 0 AND @old_exists > 0 AND @new_exists > 0,
  'UPDATE ProcessingClassificationBatch SET poundsWaste = pounds_waste WHERE poundsWaste IS NULL AND pounds_waste IS NOT NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @old_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch' AND COLUMN_NAME = 'pounds_net_received');
SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch' AND COLUMN_NAME = 'poundsNetReceived');
SET @sql = IF(@table_exists > 0 AND @old_exists > 0 AND @new_exists = 0,
  'ALTER TABLE ProcessingClassificationBatch CHANGE COLUMN pounds_net_received poundsNetReceived DECIMAL(12,2) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch' AND COLUMN_NAME = 'poundsNetReceived');
SET @sql = IF(@table_exists > 0 AND @new_exists = 0,
  'ALTER TABLE ProcessingClassificationBatch ADD COLUMN poundsNetReceived DECIMAL(12,2) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @old_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch' AND COLUMN_NAME = 'pounds_net_received');
SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch' AND COLUMN_NAME = 'poundsNetReceived');
SET @sql = IF(@table_exists > 0 AND @old_exists > 0 AND @new_exists > 0,
  'UPDATE ProcessingClassificationBatch SET poundsNetReceived = pounds_net_received WHERE poundsNetReceived IS NULL AND pounds_net_received IS NOT NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @old_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch' AND COLUMN_NAME = 'pounds_processed');
SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch' AND COLUMN_NAME = 'poundsProcessed');
SET @sql = IF(@table_exists > 0 AND @old_exists > 0 AND @new_exists = 0,
  'ALTER TABLE ProcessingClassificationBatch CHANGE COLUMN pounds_processed poundsProcessed DECIMAL(12,2) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch' AND COLUMN_NAME = 'poundsProcessed');
SET @sql = IF(@table_exists > 0 AND @new_exists = 0,
  'ALTER TABLE ProcessingClassificationBatch ADD COLUMN poundsProcessed DECIMAL(12,2) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @old_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch' AND COLUMN_NAME = 'pounds_processed');
SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch' AND COLUMN_NAME = 'poundsProcessed');
SET @sql = IF(@table_exists > 0 AND @old_exists > 0 AND @new_exists > 0,
  'UPDATE ProcessingClassificationBatch SET poundsProcessed = pounds_processed WHERE poundsProcessed IS NULL AND pounds_processed IS NOT NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @old_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch' AND COLUMN_NAME = 'yield_percentage');
SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch' AND COLUMN_NAME = 'yieldPercentage');
SET @sql = IF(@table_exists > 0 AND @old_exists > 0 AND @new_exists = 0,
  'ALTER TABLE ProcessingClassificationBatch CHANGE COLUMN yield_percentage yieldPercentage DECIMAL(5,2) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch' AND COLUMN_NAME = 'yieldPercentage');
SET @sql = IF(@table_exists > 0 AND @new_exists = 0,
  'ALTER TABLE ProcessingClassificationBatch ADD COLUMN yieldPercentage DECIMAL(5,2) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @old_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch' AND COLUMN_NAME = 'yield_percentage');
SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch' AND COLUMN_NAME = 'yieldPercentage');
SET @sql = IF(@table_exists > 0 AND @old_exists > 0 AND @new_exists > 0,
  'UPDATE ProcessingClassificationBatch SET yieldPercentage = yield_percentage WHERE yieldPercentage IS NULL AND yield_percentage IS NOT NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @old_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch' AND COLUMN_NAME = 'total_amount');
SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch' AND COLUMN_NAME = 'totalAmount');
SET @sql = IF(@table_exists > 0 AND @old_exists > 0 AND @new_exists = 0,
  'ALTER TABLE ProcessingClassificationBatch CHANGE COLUMN total_amount totalAmount DECIMAL(14,2) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch' AND COLUMN_NAME = 'totalAmount');
SET @sql = IF(@table_exists > 0 AND @new_exists = 0,
  'ALTER TABLE ProcessingClassificationBatch ADD COLUMN totalAmount DECIMAL(14,2) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @old_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch' AND COLUMN_NAME = 'total_amount');
SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch' AND COLUMN_NAME = 'totalAmount');
SET @sql = IF(@table_exists > 0 AND @old_exists > 0 AND @new_exists > 0,
  'UPDATE ProcessingClassificationBatch SET totalAmount = total_amount WHERE totalAmount IS NULL AND total_amount IS NOT NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @old_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch' AND COLUMN_NAME = 'average_price');
SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch' AND COLUMN_NAME = 'averagePrice');
SET @sql = IF(@table_exists > 0 AND @old_exists > 0 AND @new_exists = 0,
  'ALTER TABLE ProcessingClassificationBatch CHANGE COLUMN average_price averagePrice DECIMAL(10,4) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch' AND COLUMN_NAME = 'averagePrice');
SET @sql = IF(@table_exists > 0 AND @new_exists = 0,
  'ALTER TABLE ProcessingClassificationBatch ADD COLUMN averagePrice DECIMAL(10,4) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @old_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch' AND COLUMN_NAME = 'average_price');
SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch' AND COLUMN_NAME = 'averagePrice');
SET @sql = IF(@table_exists > 0 AND @old_exists > 0 AND @new_exists > 0,
  'UPDATE ProcessingClassificationBatch SET averagePrice = average_price WHERE averagePrice IS NULL AND average_price IS NOT NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @old_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch' AND COLUMN_NAME = 'settlement_status');
SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch' AND COLUMN_NAME = 'settlementStatus');
SET @sql = IF(@table_exists > 0 AND @old_exists > 0 AND @new_exists = 0,
  'ALTER TABLE ProcessingClassificationBatch CHANGE COLUMN settlement_status settlementStatus VARCHAR(20) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch' AND COLUMN_NAME = 'settlementStatus');
SET @sql = IF(@table_exists > 0 AND @new_exists = 0,
  'ALTER TABLE ProcessingClassificationBatch ADD COLUMN settlementStatus VARCHAR(20) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @old_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch' AND COLUMN_NAME = 'settlement_status');
SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatch' AND COLUMN_NAME = 'settlementStatus');
SET @sql = IF(@table_exists > 0 AND @old_exists > 0 AND @new_exists > 0,
  'UPDATE ProcessingClassificationBatch SET settlementStatus = settlement_status WHERE settlementStatus IS NULL AND settlement_status IS NOT NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @table_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatchDetail');

SET @old_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatchDetail' AND COLUMN_NAME = 'created');
SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatchDetail' AND COLUMN_NAME = 'creationTimestamp');
SET @sql = IF(@table_exists > 0 AND @old_exists > 0 AND @new_exists = 0,
  'ALTER TABLE ProcessingClassificationBatchDetail CHANGE COLUMN created creationTimestamp DATETIME(6) DEFAULT NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatchDetail' AND COLUMN_NAME = 'creationTimestamp');
SET @sql = IF(@table_exists > 0 AND @new_exists = 0,
  'ALTER TABLE ProcessingClassificationBatchDetail ADD COLUMN creationTimestamp DATETIME(6) DEFAULT NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @old_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatchDetail' AND COLUMN_NAME = 'created');
SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatchDetail' AND COLUMN_NAME = 'creationTimestamp');
SET @sql = IF(@table_exists > 0 AND @old_exists > 0 AND @new_exists > 0,
  'UPDATE ProcessingClassificationBatchDetail SET creationTimestamp = created WHERE creationTimestamp IS NULL AND created IS NOT NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @old_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatchDetail' AND COLUMN_NAME = 'updated');
SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatchDetail' AND COLUMN_NAME = 'updateTimestamp');
SET @sql = IF(@table_exists > 0 AND @old_exists > 0 AND @new_exists = 0,
  'ALTER TABLE ProcessingClassificationBatchDetail CHANGE COLUMN updated updateTimestamp DATETIME(6) DEFAULT NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatchDetail' AND COLUMN_NAME = 'updateTimestamp');
SET @sql = IF(@table_exists > 0 AND @new_exists = 0,
  'ALTER TABLE ProcessingClassificationBatchDetail ADD COLUMN updateTimestamp DATETIME(6) DEFAULT NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @old_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatchDetail' AND COLUMN_NAME = 'updated');
SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatchDetail' AND COLUMN_NAME = 'updateTimestamp');
SET @sql = IF(@table_exists > 0 AND @old_exists > 0 AND @new_exists > 0,
  'UPDATE ProcessingClassificationBatchDetail SET updateTimestamp = updated WHERE updateTimestamp IS NULL AND updated IS NOT NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @old_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatchDetail' AND COLUMN_NAME = 'version');
SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatchDetail' AND COLUMN_NAME = 'entityVersion');
SET @sql = IF(@table_exists > 0 AND @old_exists > 0 AND @new_exists = 0,
  'ALTER TABLE ProcessingClassificationBatchDetail CHANGE COLUMN version entityVersion BIGINT DEFAULT NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatchDetail' AND COLUMN_NAME = 'entityVersion');
SET @sql = IF(@table_exists > 0 AND @new_exists = 0,
  'ALTER TABLE ProcessingClassificationBatchDetail ADD COLUMN entityVersion BIGINT DEFAULT NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @old_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatchDetail' AND COLUMN_NAME = 'version');
SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatchDetail' AND COLUMN_NAME = 'entityVersion');
SET @sql = IF(@table_exists > 0 AND @old_exists > 0 AND @new_exists > 0,
  'UPDATE ProcessingClassificationBatchDetail SET entityVersion = version WHERE entityVersion IS NULL AND version IS NOT NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @old_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatchDetail' AND COLUMN_NAME = 'quality_grade');
SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatchDetail' AND COLUMN_NAME = 'qualityGrade');
SET @sql = IF(@table_exists > 0 AND @old_exists > 0 AND @new_exists = 0,
  'ALTER TABLE ProcessingClassificationBatchDetail CHANGE COLUMN quality_grade qualityGrade VARCHAR(10) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatchDetail' AND COLUMN_NAME = 'qualityGrade');
SET @sql = IF(@table_exists > 0 AND @new_exists = 0,
  'ALTER TABLE ProcessingClassificationBatchDetail ADD COLUMN qualityGrade VARCHAR(10) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @old_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatchDetail' AND COLUMN_NAME = 'quality_grade');
SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatchDetail' AND COLUMN_NAME = 'qualityGrade');
SET @sql = IF(@table_exists > 0 AND @old_exists > 0 AND @new_exists > 0,
  'UPDATE ProcessingClassificationBatchDetail SET qualityGrade = quality_grade WHERE qualityGrade IS NULL AND quality_grade IS NOT NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @old_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatchDetail' AND COLUMN_NAME = 'presentation_type');
SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatchDetail' AND COLUMN_NAME = 'presentationType');
SET @sql = IF(@table_exists > 0 AND @old_exists > 0 AND @new_exists = 0,
  'ALTER TABLE ProcessingClassificationBatchDetail CHANGE COLUMN presentation_type presentationType VARCHAR(50) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatchDetail' AND COLUMN_NAME = 'presentationType');
SET @sql = IF(@table_exists > 0 AND @new_exists = 0,
  'ALTER TABLE ProcessingClassificationBatchDetail ADD COLUMN presentationType VARCHAR(50) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @old_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatchDetail' AND COLUMN_NAME = 'presentation_type');
SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatchDetail' AND COLUMN_NAME = 'presentationType');
SET @sql = IF(@table_exists > 0 AND @old_exists > 0 AND @new_exists > 0,
  'UPDATE ProcessingClassificationBatchDetail SET presentationType = presentation_type WHERE presentationType IS NULL AND presentation_type IS NOT NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @old_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatchDetail' AND COLUMN_NAME = 'price_per_pound');
SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatchDetail' AND COLUMN_NAME = 'pricePerPound');
SET @sql = IF(@table_exists > 0 AND @old_exists > 0 AND @new_exists = 0,
  'ALTER TABLE ProcessingClassificationBatchDetail CHANGE COLUMN price_per_pound pricePerPound DECIMAL(10,4) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatchDetail' AND COLUMN_NAME = 'pricePerPound');
SET @sql = IF(@table_exists > 0 AND @new_exists = 0,
  'ALTER TABLE ProcessingClassificationBatchDetail ADD COLUMN pricePerPound DECIMAL(10,4) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @old_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatchDetail' AND COLUMN_NAME = 'price_per_pound');
SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatchDetail' AND COLUMN_NAME = 'pricePerPound');
SET @sql = IF(@table_exists > 0 AND @old_exists > 0 AND @new_exists > 0,
  'UPDATE ProcessingClassificationBatchDetail SET pricePerPound = price_per_pound WHERE pricePerPound IS NULL AND price_per_pound IS NOT NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @old_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatchDetail' AND COLUMN_NAME = 'line_total');
SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatchDetail' AND COLUMN_NAME = 'lineTotal');
SET @sql = IF(@table_exists > 0 AND @old_exists > 0 AND @new_exists = 0,
  'ALTER TABLE ProcessingClassificationBatchDetail CHANGE COLUMN line_total lineTotal DECIMAL(12,2) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatchDetail' AND COLUMN_NAME = 'lineTotal');
SET @sql = IF(@table_exists > 0 AND @new_exists = 0,
  'ALTER TABLE ProcessingClassificationBatchDetail ADD COLUMN lineTotal DECIMAL(12,2) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @old_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatchDetail' AND COLUMN_NAME = 'line_total');
SET @new_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProcessingClassificationBatchDetail' AND COLUMN_NAME = 'lineTotal');
SET @sql = IF(@table_exists > 0 AND @old_exists > 0 AND @new_exists > 0,
  'UPDATE ProcessingClassificationBatchDetail SET lineTotal = line_total WHERE lineTotal IS NULL AND line_total IS NOT NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
