-- ============================================================================
-- V10__add_public_delivery_receipt_config.sql
-- UNOCACE: Habilitar consulta pública de comprobantes de entrega vía código QR
-- (enablePublicDeliveryReceipt: true) para organizaciones de UNOCACE.
-- Fortaleza del Valle (FV) permanece privada y excluida por defecto.
-- ============================================================================

-- 1. Habilitar enablePublicDeliveryReceipt en todas las empresas de UNOCACE (excluyendo FV)
UPDATE public.company
SET configuration = COALESCE(configuration, '{}'::jsonb) || '{"enablePublicDeliveryReceipt": true}'::jsonb
WHERE NOT (
    upper(name) LIKE '%FORTALEZA%'
    OR upper(abbreviation) LIKE '%FORTALEZA%'
    OR upper(abbreviation) LIKE '%FDV%'
    OR upper(abbreviation) LIKE '%CFV%'
);

-- 2. Asegurar que Fortaleza del Valle mantenga la opción desactivada/limpia por defecto
UPDATE public.company
SET configuration = configuration - 'enablePublicDeliveryReceipt'
WHERE upper(name) LIKE '%FORTALEZA%'
   OR upper(abbreviation) LIKE '%FORTALEZA%'
   OR upper(abbreviation) LIKE '%FDV%'
   OR upper(abbreviation) LIKE '%CFV%';
