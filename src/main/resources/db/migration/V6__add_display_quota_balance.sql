-- ============================================================================
-- V6__add_display_quota_balance.sql
-- UNOCACE: Soporte para Saldo de Cupo en entregas (configuracion tipo Tara).
-- Habilita displayquotabalance en centros de acopio (facility) y enableQuotaBalance
-- en organizaciones de UNOCACE. Fortaleza del Valle permanece excluida.
-- ============================================================================

-- 1. Agregar columna displayquotabalance a facility y a facility_aud
ALTER TABLE public.facility
    ADD COLUMN IF NOT EXISTS displayquotabalance BOOLEAN DEFAULT false;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'facility_aud') THEN
        ALTER TABLE public.facility_aud
            ADD COLUMN IF NOT EXISTS displayquotabalance BOOLEAN;
    END IF;
END $$;

-- 2. Habilitar displayquotabalance para las instalaciones de UNOCACE
UPDATE public.facility
SET displayquotabalance = true
WHERE company_id IS NULL OR company_id NOT IN (
    SELECT id FROM public.company
    WHERE upper(name) LIKE '%FORTALEZA%'
       OR upper(abbreviation) LIKE '%FORTALEZA%'
       OR upper(abbreviation) LIKE '%FDV%'
       OR upper(abbreviation) LIKE '%CFV%'
);

-- 3. Habilitar enableQuotaBalance en la configuracion de las empresas de UNOCACE
UPDATE public.company
SET configuration = COALESCE(configuration, '{}'::jsonb) || '{"enableQuotaBalance": true}'::jsonb
WHERE NOT (
    upper(name) LIKE '%FORTALEZA%'
    OR upper(abbreviation) LIKE '%FORTALEZA%'
    OR upper(abbreviation) LIKE '%FDV%'
    OR upper(abbreviation) LIKE '%CFV%'
);

-- 4. Asegurar que Fortaleza del Valle NO tenga esta configuracion activa
UPDATE public.company
SET configuration = configuration - 'enableQuotaBalance'
WHERE upper(name) LIKE '%FORTALEZA%'
   OR upper(abbreviation) LIKE '%FORTALEZA%'
   OR upper(abbreviation) LIKE '%FDV%'
   OR upper(abbreviation) LIKE '%CFV%';

UPDATE public.facility
SET displayquotabalance = false
WHERE company_id IN (
    SELECT id FROM public.company
    WHERE upper(name) LIKE '%FORTALEZA%'
       OR upper(abbreviation) LIKE '%FORTALEZA%'
       OR upper(abbreviation) LIKE '%FDV%'
       OR upper(abbreviation) LIKE '%CFV%'
);
