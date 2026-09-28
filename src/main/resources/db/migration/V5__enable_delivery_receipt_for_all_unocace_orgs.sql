-- ============================================================================
-- V5__enable_delivery_receipt_for_all_unocace_orgs.sql
-- UNOCACE: Habilitar correlativo secuencial ("0001", "0002", ...) y 'Cacao'
-- globalmente para TODAS las organizaciones y asociaciones de UNOCACE.
-- Fortaleza del Valle permanece excluida e inactiva por defecto.
-- ============================================================================

-- 1. Habilitar enableDeliveryReceipt y simplifySemiProductToCacao en todas las empresas de UNOCACE
UPDATE public.company
SET configuration = COALESCE(configuration, '{}'::jsonb) || '{"enableDeliveryReceipt": true, "simplifySemiProductToCacao": true}'::jsonb
WHERE NOT (
    upper(name) LIKE '%FORTALEZA%'
    OR upper(abbreviation) LIKE '%FORTALEZA%'
    OR upper(abbreviation) LIKE '%FDV%'
    OR upper(abbreviation) LIKE '%CFV%'
);

-- 2. Asegurar que Fortaleza del Valle NO tenga estas opciones activas
UPDATE public.company
SET configuration = configuration - 'enableDeliveryReceipt' - 'simplifySemiProductToCacao' - 'deliveryReceiptSequence' - 'genericCacaoDisplay'
WHERE upper(name) LIKE '%FORTALEZA%' OR upper(abbreviation) LIKE '%FORTALEZA%' OR upper(abbreviation) LIKE '%FDV%' OR upper(abbreviation) LIKE '%CFV%';

-- 3. Poblar correlativos secuenciales ("0001", "0002", ...) para todas las compras históricas
-- de todas las asociaciones y organizaciones de UNOCACE donde deliveryreceipt sea NULL
WITH numbered AS (
    SELECT so.id,
           LPAD((ROW_NUMBER() OVER (PARTITION BY so.company_id ORDER BY so.id ASC))::text, 4, '0') AS seq
    FROM public.stockorder so
    JOIN public.company c ON c.id = so.company_id
    WHERE NOT (
        upper(c.name) LIKE '%FORTALEZA%'
        OR upper(c.abbreviation) LIKE '%FORTALEZA%'
        OR upper(c.abbreviation) LIKE '%FDV%'
        OR upper(c.abbreviation) LIKE '%CFV%'
    )
    AND so.ordertype = 'PURCHASE_ORDER'
)
UPDATE public.stockorder so
SET deliveryreceipt = numbered.seq
FROM numbered
WHERE so.id = numbered.id
  AND (so.deliveryreceipt IS NULL OR so.deliveryreceipt = '');
