-- 🦐 Create shrimp classification catalogs: FreezingType, Machine, Brand
-- These catalogs are used in the classification table rows

-- ═══════════════════════════════════════════════════════════════════════════
-- ShrimpFreezingType - Types of freezing (IQF, BLOCK, SEMI_IQF)
-- ═══════════════════════════════════════════════════════════════════════════
CREATE TABLE IF NOT EXISTS `ShrimpFreezingType` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `creationTimestamp` datetime(6) DEFAULT NULL,
  `updateTimestamp` datetime(6) DEFAULT NULL,
  `code` varchar(50) NOT NULL,
  `label` varchar(100) NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  `displayOrder` int DEFAULT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE',
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_shrimp_freezing_type_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `ShrimpFreezingTypeTranslation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `creationTimestamp` datetime(6) DEFAULT NULL,
  `updateTimestamp` datetime(6) DEFAULT NULL,
  `shrimp_freezing_type_id` bigint NOT NULL,
  `language` varchar(5) NOT NULL,
  `label` varchar(100) DEFAULT NULL,
  `description` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDX_sfrt_translation_type` (`shrimp_freezing_type_id`),
  CONSTRAINT `FK_sfrt_translation_type` FOREIGN KEY (`shrimp_freezing_type_id`) REFERENCES `ShrimpFreezingType` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ═══════════════════════════════════════════════════════════════════════════
-- ShrimpMachine - Processing machines
-- ═══════════════════════════════════════════════════════════════════════════
CREATE TABLE IF NOT EXISTS `ShrimpMachine` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `creationTimestamp` datetime(6) DEFAULT NULL,
  `updateTimestamp` datetime(6) DEFAULT NULL,
  `code` varchar(50) NOT NULL,
  `label` varchar(100) NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  `machineType` varchar(50) DEFAULT NULL,
  `displayOrder` int DEFAULT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE',
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_shrimp_machine_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `ShrimpMachineTranslation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `creationTimestamp` datetime(6) DEFAULT NULL,
  `updateTimestamp` datetime(6) DEFAULT NULL,
  `shrimp_machine_id` bigint NOT NULL,
  `language` varchar(5) NOT NULL,
  `label` varchar(100) DEFAULT NULL,
  `description` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDX_smt_translation_machine` (`shrimp_machine_id`),
  CONSTRAINT `FK_smt_translation_machine` FOREIGN KEY (`shrimp_machine_id`) REFERENCES `ShrimpMachine` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ═══════════════════════════════════════════════════════════════════════════
-- ShrimpBrand - Brands with weight per box and measure unit
-- ═══════════════════════════════════════════════════════════════════════════
CREATE TABLE IF NOT EXISTS `ShrimpBrand` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `creationTimestamp` datetime(6) DEFAULT NULL,
  `updateTimestamp` datetime(6) DEFAULT NULL,
  `code` varchar(50) NOT NULL,
  `label` varchar(100) NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  `weightPerBox` decimal(10,2) DEFAULT NULL,
  `measureUnit` varchar(10) DEFAULT NULL,
  `displayOrder` int DEFAULT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE',
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_shrimp_brand_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `ShrimpBrandTranslation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `creationTimestamp` datetime(6) DEFAULT NULL,
  `updateTimestamp` datetime(6) DEFAULT NULL,
  `shrimp_brand_id` bigint NOT NULL,
  `language` varchar(5) NOT NULL,
  `label` varchar(100) DEFAULT NULL,
  `description` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDX_sbt_translation_brand` (`shrimp_brand_id`),
  CONSTRAINT `FK_sbt_translation_brand` FOREIGN KEY (`shrimp_brand_id`) REFERENCES `ShrimpBrand` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ═══════════════════════════════════════════════════════════════════════════
-- Insert initial data for ShrimpFreezingType
-- ═══════════════════════════════════════════════════════════════════════════
INSERT INTO `ShrimpFreezingType` (`code`, `label`, `description`, `displayOrder`, `status`, `creationTimestamp`, `updateTimestamp`) 
VALUES 
  ('IQF', 'IQF', 'Individual Quick Frozen - Congelado individual rápido', 1, 'ACTIVE', NOW(), NOW()),
  ('BLOCK', 'Bloque', 'Block Frozen - Congelado en bloque', 2, 'ACTIVE', NOW(), NOW()),
  ('SEMI_IQF', 'Semi-IQF', 'Semi Individual Quick Frozen', 3, 'ACTIVE', NOW(), NOW())
ON DUPLICATE KEY UPDATE `updateTimestamp` = NOW();

-- Translations for FreezingType
INSERT INTO `ShrimpFreezingTypeTranslation` (`shrimp_freezing_type_id`, `language`, `label`, `description`, `creationTimestamp`, `updateTimestamp`)
SELECT ft.id, 'es', 
  CASE ft.code 
    WHEN 'IQF' THEN 'IQF (Individual)'
    WHEN 'BLOCK' THEN 'Bloque'
    WHEN 'SEMI_IQF' THEN 'Semi-IQF'
  END,
  CASE ft.code 
    WHEN 'IQF' THEN 'Congelado individual rápido'
    WHEN 'BLOCK' THEN 'Congelado en bloque compacto'
    WHEN 'SEMI_IQF' THEN 'Congelado semi-individual'
  END,
  NOW(), NOW()
FROM `ShrimpFreezingType` ft
WHERE NOT EXISTS (
  SELECT 1 FROM `ShrimpFreezingTypeTranslation` t 
  WHERE t.shrimp_freezing_type_id = ft.id AND t.language = 'es'
);

INSERT INTO `ShrimpFreezingTypeTranslation` (`shrimp_freezing_type_id`, `language`, `label`, `description`, `creationTimestamp`, `updateTimestamp`)
SELECT ft.id, 'en', 
  CASE ft.code 
    WHEN 'IQF' THEN 'IQF (Individual)'
    WHEN 'BLOCK' THEN 'Block'
    WHEN 'SEMI_IQF' THEN 'Semi-IQF'
  END,
  CASE ft.code 
    WHEN 'IQF' THEN 'Individual Quick Frozen'
    WHEN 'BLOCK' THEN 'Block frozen compact'
    WHEN 'SEMI_IQF' THEN 'Semi-individual frozen'
  END,
  NOW(), NOW()
FROM `ShrimpFreezingType` ft
WHERE NOT EXISTS (
  SELECT 1 FROM `ShrimpFreezingTypeTranslation` t 
  WHERE t.shrimp_freezing_type_id = ft.id AND t.language = 'en'
);

-- ═══════════════════════════════════════════════════════════════════════════
-- Insert initial data for ShrimpMachine
-- ═══════════════════════════════════════════════════════════════════════════
INSERT INTO `ShrimpMachine` (`code`, `label`, `description`, `machineType`, `displayOrder`, `status`, `creationTimestamp`, `updateTimestamp`) 
VALUES 
  ('MACH_01', 'Máquina 1', 'Clasificadora principal', 'CLASSIFIER', 1, 'ACTIVE', NOW(), NOW()),
  ('MACH_02', 'Máquina 2', 'Clasificadora secundaria', 'CLASSIFIER', 2, 'ACTIVE', NOW(), NOW()),
  ('MACH_03', 'Máquina 3', 'Clasificadora de respaldo', 'CLASSIFIER', 3, 'ACTIVE', NOW(), NOW()),
  ('GLAZER_01', 'Glaseadora 1', 'Glaseadora principal', 'GLAZER', 4, 'ACTIVE', NOW(), NOW()),
  ('FREEZER_01', 'Túnel 1', 'Túnel de congelación 1', 'FREEZER', 5, 'ACTIVE', NOW(), NOW()),
  ('FREEZER_02', 'Túnel 2', 'Túnel de congelación 2', 'FREEZER', 6, 'ACTIVE', NOW(), NOW())
ON DUPLICATE KEY UPDATE `updateTimestamp` = NOW();

-- Translations for Machine (Spanish)
INSERT INTO `ShrimpMachineTranslation` (`shrimp_machine_id`, `language`, `label`, `description`, `creationTimestamp`, `updateTimestamp`)
SELECT m.id, 'es', m.label, m.description, NOW(), NOW()
FROM `ShrimpMachine` m
WHERE NOT EXISTS (
  SELECT 1 FROM `ShrimpMachineTranslation` t 
  WHERE t.shrimp_machine_id = m.id AND t.language = 'es'
);

-- Translations for Machine (English)
INSERT INTO `ShrimpMachineTranslation` (`shrimp_machine_id`, `language`, `label`, `description`, `creationTimestamp`, `updateTimestamp`)
SELECT m.id, 'en', 
  CASE m.code 
    WHEN 'MACH_01' THEN 'Machine 1'
    WHEN 'MACH_02' THEN 'Machine 2'
    WHEN 'MACH_03' THEN 'Machine 3'
    WHEN 'GLAZER_01' THEN 'Glazer 1'
    WHEN 'FREEZER_01' THEN 'Tunnel 1'
    WHEN 'FREEZER_02' THEN 'Tunnel 2'
  END,
  CASE m.code 
    WHEN 'MACH_01' THEN 'Main classifier'
    WHEN 'MACH_02' THEN 'Secondary classifier'
    WHEN 'MACH_03' THEN 'Backup classifier'
    WHEN 'GLAZER_01' THEN 'Main glazer'
    WHEN 'FREEZER_01' THEN 'Freezing tunnel 1'
    WHEN 'FREEZER_02' THEN 'Freezing tunnel 2'
  END,
  NOW(), NOW()
FROM `ShrimpMachine` m
WHERE NOT EXISTS (
  SELECT 1 FROM `ShrimpMachineTranslation` t 
  WHERE t.shrimp_machine_id = m.id AND t.language = 'en'
);

-- ═══════════════════════════════════════════════════════════════════════════
-- Insert initial data for ShrimpBrand (Dufer brands with weight per box)
-- ═══════════════════════════════════════════════════════════════════════════
INSERT INTO `ShrimpBrand` (`code`, `label`, `description`, `weightPerBox`, `measureUnit`, `displayOrder`, `status`, `creationTimestamp`, `updateTimestamp`) 
VALUES 
  ('DUFER_2KG', 'Dufer 2 Kg', 'Marca Dufer - Caja de 2 kilogramos', 2.00, 'KG', 1, 'ACTIVE', NOW(), NOW()),
  ('DUFER_4LB', 'Dufer 4 Lb', 'Marca Dufer - Caja de 4 libras', 4.00, 'LB', 2, 'ACTIVE', NOW(), NOW()),
  ('DUFER_5LB', 'Dufer 5 Lb', 'Marca Dufer - Caja de 5 libras', 5.00, 'LB', 3, 'ACTIVE', NOW(), NOW()),
  ('OCEAN_GOLD_2KG', 'Ocean Gold 2 Kg', 'Marca Ocean Gold - Caja de 2 kilogramos', 2.00, 'KG', 4, 'ACTIVE', NOW(), NOW()),
  ('OCEAN_GOLD_5LB', 'Ocean Gold 5 Lb', 'Marca Ocean Gold - Caja de 5 libras', 5.00, 'LB', 5, 'ACTIVE', NOW(), NOW()),
  ('CUSTOM', 'Personalizado', 'Peso personalizado', NULL, NULL, 99, 'ACTIVE', NOW(), NOW())
ON DUPLICATE KEY UPDATE `updateTimestamp` = NOW();

-- Translations for Brand (Spanish)
INSERT INTO `ShrimpBrandTranslation` (`shrimp_brand_id`, `language`, `label`, `description`, `creationTimestamp`, `updateTimestamp`)
SELECT b.id, 'es', b.label, b.description, NOW(), NOW()
FROM `ShrimpBrand` b
WHERE NOT EXISTS (
  SELECT 1 FROM `ShrimpBrandTranslation` t 
  WHERE t.shrimp_brand_id = b.id AND t.language = 'es'
);

-- Translations for Brand (English)
INSERT INTO `ShrimpBrandTranslation` (`shrimp_brand_id`, `language`, `label`, `description`, `creationTimestamp`, `updateTimestamp`)
SELECT b.id, 'en', 
  CASE b.code 
    WHEN 'DUFER_2KG' THEN 'Dufer 2 Kg'
    WHEN 'DUFER_4LB' THEN 'Dufer 4 Lb'
    WHEN 'DUFER_5LB' THEN 'Dufer 5 Lb'
    WHEN 'OCEAN_GOLD_2KG' THEN 'Ocean Gold 2 Kg'
    WHEN 'OCEAN_GOLD_5LB' THEN 'Ocean Gold 5 Lb'
    WHEN 'CUSTOM' THEN 'Custom'
  END,
  CASE b.code 
    WHEN 'DUFER_2KG' THEN 'Dufer brand - 2 kilogram box'
    WHEN 'DUFER_4LB' THEN 'Dufer brand - 4 pound box'
    WHEN 'DUFER_5LB' THEN 'Dufer brand - 5 pound box'
    WHEN 'OCEAN_GOLD_2KG' THEN 'Ocean Gold brand - 2 kilogram box'
    WHEN 'OCEAN_GOLD_5LB' THEN 'Ocean Gold brand - 5 pound box'
    WHEN 'CUSTOM' THEN 'Custom weight'
  END,
  NOW(), NOW()
FROM `ShrimpBrand` b
WHERE NOT EXISTS (
  SELECT 1 FROM `ShrimpBrandTranslation` t 
  WHERE t.shrimp_brand_id = b.id AND t.language = 'en'
);
