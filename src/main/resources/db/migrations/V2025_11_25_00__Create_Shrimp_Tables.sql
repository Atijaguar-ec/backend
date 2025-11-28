-- Create core shrimp codebook tables for environments without ddl-auto=update
-- This migration is safe to run multiple times thanks to CREATE TABLE IF NOT EXISTS
-- It creates only the tables; data is inserted by JPA migrations
--   - V2025_11_25_01__Create_Shrimp_Catalogs (flavor, size, color, process)
--   - V2025_11_25_03__Complete_Shrimp_Catalogs (quality grade, treatment type, presentation additions)

-- ShrimpFlavorDefect
CREATE TABLE IF NOT EXISTS `ShrimpFlavorDefect` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `creationTimestamp` datetime(6) DEFAULT NULL,
  `updateTimestamp` datetime(6) DEFAULT NULL,
  `code` varchar(64) NOT NULL,
  `name` varchar(255) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `displayOrder` int DEFAULT NULL,
  `status` varchar(40) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_shrimp_flavor_defect_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ShrimpFlavorDefectTranslation
CREATE TABLE IF NOT EXISTS `ShrimpFlavorDefectTranslation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `creationTimestamp` datetime(6) DEFAULT NULL,
  `updateTimestamp` datetime(6) DEFAULT NULL,
  `shrimp_flavor_defect_id` bigint NOT NULL,
  `language` varchar(64) NOT NULL,
  `name` varchar(255) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_sfd_translation_defect_lang` (`shrimp_flavor_defect_id`,`language`),
  KEY `IDX_sfd_translation_defect` (`shrimp_flavor_defect_id`),
  CONSTRAINT `FK_sfd_translation_defect` FOREIGN KEY (`shrimp_flavor_defect_id`) REFERENCES `ShrimpFlavorDefect` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ShrimpSizeGrade
CREATE TABLE IF NOT EXISTS `ShrimpSizeGrade` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `creationTimestamp` datetime(6) DEFAULT NULL,
  `updateTimestamp` datetime(6) DEFAULT NULL,
  `code` varchar(64) NOT NULL,
  `label` varchar(255) NOT NULL,
  `sizeType` varchar(40) NOT NULL,
  `displayOrder` int DEFAULT NULL,
  `status` varchar(40) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_shrimp_size_grade_code` (`code`),
  KEY `IDX_shrimp_size_grade_type` (`sizeType`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ShrimpColorGrade
CREATE TABLE IF NOT EXISTS `ShrimpColorGrade` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `creationTimestamp` datetime(6) DEFAULT NULL,
  `updateTimestamp` datetime(6) DEFAULT NULL,
  `code` varchar(64) NOT NULL,
  `label` varchar(255) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `displayOrder` int DEFAULT NULL,
  `status` varchar(40) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_shrimp_color_grade_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ShrimpProcessType
CREATE TABLE IF NOT EXISTS `ShrimpProcessType` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `creationTimestamp` datetime(6) DEFAULT NULL,
  `updateTimestamp` datetime(6) DEFAULT NULL,
  `code` varchar(64) NOT NULL,
  `name` varchar(255) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `displayOrder` int DEFAULT NULL,
  `status` varchar(40) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_shrimp_process_type_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ShrimpProcessTypeTranslation
CREATE TABLE IF NOT EXISTS `ShrimpProcessTypeTranslation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `creationTimestamp` datetime(6) DEFAULT NULL,
  `updateTimestamp` datetime(6) DEFAULT NULL,
  `shrimp_process_type_id` bigint NOT NULL,
  `language` varchar(64) NOT NULL,
  `name` varchar(255) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_spt_translation_type_lang` (`shrimp_process_type_id`,`language`),
  KEY `IDX_spt_translation_type` (`shrimp_process_type_id`),
  CONSTRAINT `FK_spt_translation_type` FOREIGN KEY (`shrimp_process_type_id`) REFERENCES `ShrimpProcessType` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ShrimpQualityGrade
CREATE TABLE IF NOT EXISTS `ShrimpQualityGrade` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `creationTimestamp` datetime(6) DEFAULT NULL,
  `updateTimestamp` datetime(6) DEFAULT NULL,
  `code` varchar(10) NOT NULL,
  `label` varchar(100) NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  `displayOrder` int DEFAULT NULL,
  `status` varchar(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_shrimp_quality_grade_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ShrimpQualityGradeTranslation
CREATE TABLE IF NOT EXISTS `ShrimpQualityGradeTranslation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `creationTimestamp` datetime(6) DEFAULT NULL,
  `updateTimestamp` datetime(6) DEFAULT NULL,
  `shrimp_quality_grade_id` bigint NOT NULL,
  `language` varchar(5) NOT NULL,
  `label` varchar(100) DEFAULT NULL,
  `description` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDX_sqg_translation_grade` (`shrimp_quality_grade_id`),
  CONSTRAINT `FK_sqg_translation_grade` FOREIGN KEY (`shrimp_quality_grade_id`) REFERENCES `ShrimpQualityGrade` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ShrimpTreatmentType
CREATE TABLE IF NOT EXISTS `ShrimpTreatmentType` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `creationTimestamp` datetime(6) DEFAULT NULL,
  `updateTimestamp` datetime(6) DEFAULT NULL,
  `code` varchar(50) NOT NULL,
  `label` varchar(100) NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  `displayOrder` int DEFAULT NULL,
  `status` varchar(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_shrimp_treatment_type_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ShrimpTreatmentTypeTranslation
CREATE TABLE IF NOT EXISTS `ShrimpTreatmentTypeTranslation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `creationTimestamp` datetime(6) DEFAULT NULL,
  `updateTimestamp` datetime(6) DEFAULT NULL,
  `shrimp_treatment_type_id` bigint NOT NULL,
  `language` varchar(5) NOT NULL,
  `label` varchar(100) DEFAULT NULL,
  `description` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDX_stt_translation_type` (`shrimp_treatment_type_id`),
  CONSTRAINT `FK_stt_translation_type` FOREIGN KEY (`shrimp_treatment_type_id`) REFERENCES `ShrimpTreatmentType` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ShrimpPresentationType
CREATE TABLE IF NOT EXISTS `ShrimpPresentationType` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `creationTimestamp` datetime(6) DEFAULT NULL,
  `updateTimestamp` datetime(6) DEFAULT NULL,
  `code` varchar(64) NOT NULL,
  `label` varchar(255) NOT NULL,
  `category` varchar(40) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `displayOrder` int DEFAULT NULL,
  `status` varchar(40) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_shrimp_presentation_type_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ShrimpPresentationTypeTranslation
CREATE TABLE IF NOT EXISTS `ShrimpPresentationTypeTranslation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `creationTimestamp` datetime(6) DEFAULT NULL,
  `updateTimestamp` datetime(6) DEFAULT NULL,
  `shrimp_presentation_type_id` bigint NOT NULL,
  `language` varchar(64) NOT NULL,
  `label` varchar(255) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_spt_present_translation_type_lang` (`shrimp_presentation_type_id`,`language`),
  KEY `IDX_spt_present_translation_type` (`shrimp_presentation_type_id`),
  CONSTRAINT `FK_spt_present_translation_type` FOREIGN KEY (`shrimp_presentation_type_id`) REFERENCES `ShrimpPresentationType` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
