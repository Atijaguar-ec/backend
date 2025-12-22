-- ═══════════════════════════════════════════════════════════════
-- V2025_12_22_12_30: Simplificar ENUM language a EN/ES
-- ═══════════════════════════════════════════════════════════════
-- Reduce el ENUM de idioma de ('DE','EN','ES','RW') a ('EN','ES')
-- para reflejar los idiomas soportados en producción.
-- 
-- NOTA: Antes de ejecutar, asegúrate de que no existan registros
-- con language='DE' o language='RW'. Si existen, primero migrarlos.
-- ═══════════════════════════════════════════════════════════════

-- 1. CompanyTranslation
ALTER TABLE `CompanyTranslation` 
MODIFY COLUMN `language` ENUM('EN','ES') NOT NULL;

-- 2. FacilityTranslation
ALTER TABLE `FacilityTranslation` 
MODIFY COLUMN `language` ENUM('EN','ES') NOT NULL;

-- 3. ProcessingActionTranslation
ALTER TABLE `ProcessingActionTranslation` 
MODIFY COLUMN `language` ENUM('EN','ES') NOT NULL;

-- 4. ProcessingEvidenceFieldTranslation
ALTER TABLE `ProcessingEvidenceFieldTranslation` 
MODIFY COLUMN `language` ENUM('EN','ES') NOT NULL;

-- 5. ProcessingEvidenceTypeTranslation
ALTER TABLE `ProcessingEvidenceTypeTranslation` 
MODIFY COLUMN `language` ENUM('EN','ES') NOT NULL;

-- 6. ProductLabel
ALTER TABLE `ProductLabel` 
MODIFY COLUMN `language` ENUM('EN','ES') NOT NULL;

-- 7. ProductSettings
ALTER TABLE `ProductSettings` 
MODIFY COLUMN `language` ENUM('EN','ES') NOT NULL;

-- 8. ProductTypeTranslation
ALTER TABLE `ProductTypeTranslation` 
MODIFY COLUMN `language` ENUM('EN','ES') NOT NULL;

-- 9. SemiProductTranslation
ALTER TABLE `SemiProductTranslation` 
MODIFY COLUMN `language` ENUM('EN','ES') NOT NULL;

-- 10. User
ALTER TABLE `User` 
MODIFY COLUMN `language` ENUM('EN','ES') NOT NULL;

-- 11. User_AUD (tabla de auditoría - permite NULL)
ALTER TABLE `User_AUD` 
MODIFY COLUMN `language` ENUM('EN','ES') DEFAULT NULL;
