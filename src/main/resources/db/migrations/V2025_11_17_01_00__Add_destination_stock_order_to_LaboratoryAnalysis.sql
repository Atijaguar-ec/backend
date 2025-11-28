-- ============================================================================
-- Migración: Añadir destino de uso a LaboratoryAnalysis
-- Fecha: 2025-11-17
-- Descripción: Permite enlazar un análisis de laboratorio aprobado con la
--              orden de stock donde se utiliza (para no reutilizarlo).
-- ============================================================================

ALTER TABLE `LaboratoryAnalysis`
  ADD COLUMN `destinationStockOrder_id` bigint NULL AFTER `stockOrder_id`,
  ADD KEY `FK_laboratory_analysis_dest_stock_order` (`destinationStockOrder_id`),
  ADD CONSTRAINT `FK_laboratory_analysis_dest_stock_order`
    FOREIGN KEY (`destinationStockOrder_id`) REFERENCES `StockOrder` (`id`);
