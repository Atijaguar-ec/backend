-- ============================================================================
-- Migración: Agregar campos para entregas de laboratorio
-- Fecha: 2025-11-11
-- Descripción: Campos para N° de Muestra y Hora de recepción
-- ============================================================================

ALTER TABLE StockOrder 
ADD COLUMN sampleNumber VARCHAR(100) NULL COMMENT 'N° de Muestra (específico para laboratorio)',
ADD COLUMN receptionTime TIME NULL COMMENT 'Hora de recepción (específico para laboratorio)';
