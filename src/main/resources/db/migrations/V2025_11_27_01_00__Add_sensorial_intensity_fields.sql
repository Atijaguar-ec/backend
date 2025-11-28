-- Add intensity fields for sensorial analysis (Odor and Taste)
-- Color uses ShrimpColorGrade catalog without intensity
-- Made idempotent to handle cases where columns already exist

-- Add sensorial_raw_odor_intensity column if not exists
SET @col_exists := (SELECT COUNT(*) FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'LaboratoryAnalysis' AND COLUMN_NAME = 'sensorial_raw_odor_intensity');
SET @sql := IF(@col_exists = 0, 
    'ALTER TABLE `LaboratoryAnalysis` ADD COLUMN `sensorial_raw_odor_intensity` VARCHAR(20) NULL COMMENT ''Intensity of raw odor defect: LEVE, MODERADO, FUERTE''', 
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Add sensorial_raw_taste_intensity column if not exists
SET @col_exists := (SELECT COUNT(*) FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'LaboratoryAnalysis' AND COLUMN_NAME = 'sensorial_raw_taste_intensity');
SET @sql := IF(@col_exists = 0, 
    'ALTER TABLE `LaboratoryAnalysis` ADD COLUMN `sensorial_raw_taste_intensity` VARCHAR(20) NULL COMMENT ''Intensity of raw taste defect: LEVE, MODERADO, FUERTE''', 
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Add sensorial_cooked_odor_intensity column if not exists
SET @col_exists := (SELECT COUNT(*) FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'LaboratoryAnalysis' AND COLUMN_NAME = 'sensorial_cooked_odor_intensity');
SET @sql := IF(@col_exists = 0, 
    'ALTER TABLE `LaboratoryAnalysis` ADD COLUMN `sensorial_cooked_odor_intensity` VARCHAR(20) NULL COMMENT ''Intensity of cooked odor defect: LEVE, MODERADO, FUERTE''', 
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Add sensorial_cooked_taste_intensity column if not exists
SET @col_exists := (SELECT COUNT(*) FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'LaboratoryAnalysis' AND COLUMN_NAME = 'sensorial_cooked_taste_intensity');
SET @sql := IF(@col_exists = 0, 
    'ALTER TABLE `LaboratoryAnalysis` ADD COLUMN `sensorial_cooked_taste_intensity` VARCHAR(20) NULL COMMENT ''Intensity of cooked taste defect: LEVE, MODERADO, FUERTE''', 
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Add index IDX_lab_analysis_raw_odor_intensity if not exists
SET @idx_exists := (SELECT COUNT(*) FROM information_schema.STATISTICS 
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'LaboratoryAnalysis' AND INDEX_NAME = 'IDX_lab_analysis_raw_odor_intensity');
SET @sql := IF(@idx_exists = 0, 
    'CREATE INDEX `IDX_lab_analysis_raw_odor_intensity` ON `LaboratoryAnalysis` (`sensorial_raw_odor_intensity`)', 
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Add index IDX_lab_analysis_raw_taste_intensity if not exists
SET @idx_exists := (SELECT COUNT(*) FROM information_schema.STATISTICS 
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'LaboratoryAnalysis' AND INDEX_NAME = 'IDX_lab_analysis_raw_taste_intensity');
SET @sql := IF(@idx_exists = 0, 
    'CREATE INDEX `IDX_lab_analysis_raw_taste_intensity` ON `LaboratoryAnalysis` (`sensorial_raw_taste_intensity`)', 
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
