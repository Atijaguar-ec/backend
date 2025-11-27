-- Add intensity fields for sensorial analysis (Odor and Taste)
-- Color uses ShrimpColorGrade catalog without intensity

-- Add intensity fields to LaboratoryAnalysis table
ALTER TABLE `LaboratoryAnalysis`
    ADD COLUMN `sensorial_raw_odor_intensity` VARCHAR(20) NULL COMMENT 'Intensity of raw odor defect: LEVE, MODERADO, FUERTE',
    ADD COLUMN `sensorial_raw_taste_intensity` VARCHAR(20) NULL COMMENT 'Intensity of raw taste defect: LEVE, MODERADO, FUERTE',
    ADD COLUMN `sensorial_cooked_odor_intensity` VARCHAR(20) NULL COMMENT 'Intensity of cooked odor defect: LEVE, MODERADO, FUERTE',
    ADD COLUMN `sensorial_cooked_taste_intensity` VARCHAR(20) NULL COMMENT 'Intensity of cooked taste defect: LEVE, MODERADO, FUERTE';

-- Add indexes for common queries
CREATE INDEX `IDX_lab_analysis_raw_odor_intensity` ON `LaboratoryAnalysis` (`sensorial_raw_odor_intensity`);
CREATE INDEX `IDX_lab_analysis_raw_taste_intensity` ON `LaboratoryAnalysis` (`sensorial_raw_taste_intensity`);
