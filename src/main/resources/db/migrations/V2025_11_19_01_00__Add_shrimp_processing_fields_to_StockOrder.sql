-- ============================================================================
-- Migración: Agregar campos de procesos de camarón (corte, tratamiento, túnel, lavado) a StockOrder
-- Fecha: 2025-11-19
-- Descripción: Campos de salida de procesamiento para órdenes de stock de camarón
-- ============================================================================

ALTER TABLE StockOrder
    ADD COLUMN cuttingType VARCHAR(100) NULL COMMENT 'Tipo de corte (camarón)',
    ADD COLUMN cuttingEntryDate DATE NULL COMMENT 'Fecha de ingreso al área de corte (camarón)',
    ADD COLUMN cuttingExitDate DATE NULL COMMENT 'Fecha de salida del área de corte (camarón)',
    ADD COLUMN cuttingTemperatureControl VARCHAR(255) NULL COMMENT 'Control de temperatura en corte (camarón)',
    ADD COLUMN treatmentType VARCHAR(100) NULL COMMENT 'Tipo de tratamiento (camarón)',
    ADD COLUMN treatmentEntryDate DATE NULL COMMENT 'Fecha de ingreso a tratamiento (camarón)',
    ADD COLUMN treatmentExitDate DATE NULL COMMENT 'Fecha de salida de tratamiento (camarón)',
    ADD COLUMN treatmentTemperatureControl VARCHAR(255) NULL COMMENT 'Control de temperatura en tratamiento (camarón)',
    ADD COLUMN treatmentChemicalUsed VARCHAR(255) NULL COMMENT 'Químico utilizado en tratamiento (camarón)',
    ADD COLUMN tunnelProductionDate DATE NULL COMMENT 'Fecha de producción en túnel (camarón)',
    ADD COLUMN tunnelExpirationDate DATE NULL COMMENT 'Fecha de expiración de túnel (camarón)',
    ADD COLUMN tunnelNetWeight DECIMAL(38,2) NULL COMMENT 'Peso neto en túnel (camarón)',
    ADD COLUMN tunnelSupplier VARCHAR(255) NULL COMMENT 'Proveedor de túnel (camarón)',
    ADD COLUMN tunnelFreezingType VARCHAR(100) NULL COMMENT 'Tipo de congelación en túnel (camarón)',
    ADD COLUMN tunnelEntryDate DATE NULL COMMENT 'Fecha de ingreso a túnel (camarón)',
    ADD COLUMN tunnelExitDate DATE NULL COMMENT 'Fecha de salida de túnel (camarón)',
    ADD COLUMN washingWaterTemperature VARCHAR(100) NULL COMMENT 'Temperatura del agua de lavado (camarón)',
    ADD COLUMN washingShrimpTemperatureControl VARCHAR(255) NULL COMMENT 'P.C. / control de temperatura del camarón en lavado (camarón)';
