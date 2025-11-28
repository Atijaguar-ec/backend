-- ============================================================================
-- Migración: Crear tabla LaboratoryAnalysis para resultados de laboratorio
-- Fecha: 2025-11-11
-- Descripción: Tabla separada para análisis de laboratorio (sensorial, microbiológico, químico)
--              Mantiene StockOrder limpio y permite múltiples análisis por orden
-- ============================================================================

DROP TABLE IF EXISTS `LaboratoryAnalysis`;
CREATE TABLE `LaboratoryAnalysis` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  
  -- Auditoría (siguiendo el patrón del sistema)
  `creationTimestamp` datetime(6) DEFAULT NULL,
  `updateTimestamp` datetime(6) DEFAULT NULL,
  `createdBy_id` bigint NOT NULL,
  `updatedBy_id` bigint DEFAULT NULL,
  
  -- Relación con StockOrder
  `stockOrder_id` bigint NOT NULL,
  
  -- Tipo de análisis (permite extensión futura)
  `analysisType` enum('SENSORIAL','MICROBIOLOGICAL','CHEMICAL','PCR') NOT NULL DEFAULT 'SENSORIAL',
  `analysisDate` datetime(6) DEFAULT NULL,
  
  -- Análisis Sensorial - Estado CRUDO
  `sensorialRawOdor` varchar(255) DEFAULT NULL,
  `sensorialRawTaste` varchar(255) DEFAULT NULL,
  `sensorialRawColor` varchar(255) DEFAULT NULL,
  
  -- Análisis Sensorial - Estado COCIDO
  `sensorialCookedOdor` varchar(255) DEFAULT NULL,
  `sensorialCookedTaste` varchar(255) DEFAULT NULL,
  `sensorialCookedColor` varchar(255) DEFAULT NULL,
  
  -- Observaciones generales de calidad
  `qualityNotes` longtext,
  
  PRIMARY KEY (`id`),
  KEY `FK_laboratory_analysis_stock_order` (`stockOrder_id`),
  KEY `FK_laboratory_analysis_created_by` (`createdBy_id`),
  KEY `FK_laboratory_analysis_updated_by` (`updatedBy_id`),
  KEY `IDX_laboratory_analysis_type` (`analysisType`),
  KEY `IDX_laboratory_analysis_date` (`analysisDate`),
  
  CONSTRAINT `FK_laboratory_analysis_stock_order` FOREIGN KEY (`stockOrder_id`) REFERENCES `StockOrder` (`id`) ON DELETE CASCADE,
  CONSTRAINT `FK_laboratory_analysis_created_by` FOREIGN KEY (`createdBy_id`) REFERENCES `User` (`id`),
  CONSTRAINT `FK_laboratory_analysis_updated_by` FOREIGN KEY (`updatedBy_id`) REFERENCES `User` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
