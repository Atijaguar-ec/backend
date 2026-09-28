-- ============================================================================
-- V4__add_delivery_receipt_and_unocace_config.sql
-- UNOCACE: Correlativo automático de comprobante de entrega ("0001", "0002", ...)
-- Gobernanza por company.configuration: UNOCACE activo, Fortaleza del Valle inactivo
-- ============================================================================

-- 1. Agregar columna deliveryreceipt en stockorder y stockorder_aud
ALTER TABLE public.stockorder ADD COLUMN IF NOT EXISTS deliveryreceipt character varying(255);
ALTER TABLE public.stockorder_aud ADD COLUMN IF NOT EXISTS deliveryreceipt character varying(255);

-- 2. Índice para acelerar búsquedas y correlativos por empresa
CREATE INDEX IF NOT EXISTS idx_stockorder_deliveryreceipt ON public.stockorder USING btree (company_id, deliveryreceipt);

-- 3. Habilitar enableDeliveryReceipt y simplifySemiProductToCacao en UNOCACE
UPDATE public.company
SET configuration = COALESCE(configuration, '{}'::jsonb) || '{"enableDeliveryReceipt": true, "simplifySemiProductToCacao": true}'::jsonb
WHERE upper(name) LIKE '%UNOCACE%' OR upper(abbreviation) LIKE '%UNOCACE%';

-- 4. Asegurar que Fortaleza del Valle NO tenga estas opciones activadas por defecto
UPDATE public.company
SET configuration = configuration - 'enableDeliveryReceipt' - 'simplifySemiProductToCacao' - 'deliveryReceiptSequence' - 'genericCacaoDisplay'
WHERE upper(name) LIKE '%FORTALEZA%' OR upper(abbreviation) LIKE '%FDV%' OR upper(abbreviation) LIKE '%CFV%';

-- 5. Poblar correlativos secuenciales ("0001", "0002", ...) para compras históricas de UNOCACE
WITH numbered AS (
    SELECT so.id,
           LPAD((ROW_NUMBER() OVER (PARTITION BY so.company_id ORDER BY so.id ASC))::text, 4, '0') AS seq
    FROM public.stockorder so
    JOIN public.company c ON c.id = so.company_id
    WHERE (upper(c.name) LIKE '%UNOCACE%' OR upper(c.abbreviation) LIKE '%UNOCACE%')
      AND so.ordertype = 'PURCHASE_ORDER'
)
UPDATE public.stockorder so
SET deliveryreceipt = numbered.seq
FROM numbered
WHERE so.id = numbered.id
  AND so.deliveryreceipt IS NULL;
