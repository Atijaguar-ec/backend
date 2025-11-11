-- ============================================================================
-- Migración: Agregar campos específicos de camarón a StockOrder
-- Fecha: 2025-11-11
-- Descripción: Campos para entregas normales de camarón (NO laboratorio)
-- ============================================================================

ALTER TABLE StockOrder 
ADD COLUMN numberOfGavetas INT NULL COMMENT 'N° de Gavetas (específico para camarón)',
ADD COLUMN numberOfBatea VARCHAR(50) NULL COMMENT 'N° de Batea (específico para camarón)',
ADD COLUMN numberOfPiscinas VARCHAR(50) NULL COMMENT 'N° de Piscinas (específico para camarón)',
ADD COLUMN guiaRemisionNumber VARCHAR(100) NULL COMMENT 'N° de Guía de Remisión (específico para camarón)';
