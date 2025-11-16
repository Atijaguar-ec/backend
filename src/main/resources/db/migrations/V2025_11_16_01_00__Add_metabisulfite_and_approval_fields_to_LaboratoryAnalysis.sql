-- ============================================================================
-- Migración: Añadir campos de Metabisulfito y aprobación a LaboratoryAnalysis
-- Fecha: 2025-11-16
-- Descripción: Agrega campos para registrar si el nivel de Metabisulfito es
--              aceptable y si el análisis está aprobado para la compra.
-- =========================================================================

ALTER TABLE `LaboratoryAnalysis`
  ADD COLUMN `metabisulfiteLevelAcceptable` TINYINT(1) NULL COMMENT 'Metabisulfite level acceptable (1 = yes, 0 = no)' AFTER `qualityNotes`,
  ADD COLUMN `approvedForPurchase` TINYINT(1) NULL COMMENT 'Overall analysis approved for purchase (1 = yes, 0 = no)' AFTER `metabisulfiteLevelAcceptable`;
