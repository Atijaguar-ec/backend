-- ============================================================================
-- V8__configure_unocace_quota_alert_threshold.sql
-- UNOCACE: Configurar umbral de alerta de cupo al 80% (quotaAlertThresholdPercent: 80)
-- y asegurar activas las opciones de cupo (enableQuotaBalance) y comprobante (enableDeliveryReceipt)
-- para TODAS las organizaciones y asociaciones de UNOCACE.
-- Fortaleza del Valle (FV) permanece estrictamente excluida y limpia de estas configuraciones.
-- ============================================================================

-- 1. Habilitar quotaAlertThresholdPercent (80), enableQuotaBalance y enableDeliveryReceipt
-- en todas las empresas de UNOCACE (excluyendo Fortaleza del Valle)
UPDATE public.company
SET configuration = COALESCE(configuration, '{}'::jsonb) || '{"enableQuotaBalance": true, "quotaAlertThresholdPercent": 80, "enableDeliveryReceipt": true, "simplifySemiProductToCacao": true}'::jsonb
WHERE NOT (
    upper(name) LIKE '%FORTALEZA%'
    OR upper(abbreviation) LIKE '%FORTALEZA%'
    OR upper(abbreviation) LIKE '%FDV%'
    OR upper(abbreviation) LIKE '%CFV%'
);

-- 2. Asegurar que Fortaleza del Valle NO tenga ninguna de estas opciones activas
UPDATE public.company
SET configuration = configuration - 'quotaAlertThresholdPercent' - 'enableQuotaBalance' - 'enableDeliveryReceipt' - 'simplifySemiProductToCacao' - 'deliveryReceiptSequence' - 'genericCacaoDisplay'
WHERE upper(name) LIKE '%FORTALEZA%'
   OR upper(abbreviation) LIKE '%FORTALEZA%'
   OR upper(abbreviation) LIKE '%FDV%'
   OR upper(abbreviation) LIKE '%CFV%';

-- 3. Asegurar que las instalaciones de UNOCACE muestren saldo de cupo
UPDATE public.facility
SET displayquotabalance = true
WHERE company_id IS NULL OR company_id NOT IN (
    SELECT id FROM public.company
    WHERE upper(name) LIKE '%FORTALEZA%'
       OR upper(abbreviation) LIKE '%FORTALEZA%'
       OR upper(abbreviation) LIKE '%FDV%'
       OR upper(abbreviation) LIKE '%CFV%'
);

-- 4. Asegurar que las instalaciones de Fortaleza del Valle NO muestren saldo de cupo
UPDATE public.facility
SET displayquotabalance = false
WHERE company_id IN (
    SELECT id FROM public.company
    WHERE upper(name) LIKE '%FORTALEZA%'
       OR upper(abbreviation) LIKE '%FORTALEZA%'
       OR upper(abbreviation) LIKE '%FDV%'
       OR upper(abbreviation) LIKE '%CFV%'
);
