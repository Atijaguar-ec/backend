-- ============================================================================
-- V14__backfill_final_product_qrcodetag.sql
--
-- EUDR (Reglamento UE 2023/1115) & TRACES-NT Traceability Contract:
-- Backfill automatico de qrcodetag (UUID v4) y qrcodetagfinalproduct_id
-- exclusivamente para lotes de Producto Final (finalproduct_id IS NOT NULL)
-- que carezcan de etiqueta QR activa.
-- Los lotes intermedios de semi-producto (finalproduct_id IS NULL) se mantienen
-- intactos para permitir mezcla libre de cajones de fermentacion y secado.
-- ============================================================================

UPDATE public.stockorder
SET qrcodetag = gen_random_uuid()::varchar,
    qrcodetagfinalproduct_id = COALESCE(qrcodetagfinalproduct_id, finalproduct_id)
WHERE finalproduct_id IS NOT NULL
  AND (qrcodetag IS NULL OR BTRIM(qrcodetag) = '')
  AND COALESCE(status, 'ACTIVE') <> 'CANCELED';
