-- ============================================================================
-- V12__disable_quota_balance_for_fv.sql
--
-- Fortaleza del Valle (FV): Desactivar control de cupo y restituir parcelLotFreeText.
--
-- REGLA DE NEGOCIO:
-- El control y saldo de cupos anuales (enableQuotaBalance) y displayquotabalance en
-- centros de acopio es una funcionalidad exclusiva de UNOCACE. En Fortaleza del Valle
-- los productores no gestionan cupos anuales en el sistema, y las entregas operan
-- con parcelas en modo texto libre (parcelLotFreeText = true).
-- V11 erróneamente activó enableQuotaBalance y parcelLotFreeText = false para FV.
-- Esta migración corrige dicha configuración en bases de FV.
-- ============================================================================

DO $$
DECLARE
    is_fv_db boolean;
    is_unocace_db boolean;
    updated_companies integer := 0;
    updated_facilities integer := 0;
BEGIN
    SELECT EXISTS (
        SELECT 1 FROM public.company
        WHERE upper(abbreviation) IN ('FDV', 'CFV')
           OR upper(name) LIKE '%FORTALEZA DEL VALLE%'
    ) INTO is_fv_db;

    SELECT EXISTS (
        SELECT 1 FROM public.company
        WHERE upper(name) LIKE '%UNOCACE%'
           OR upper(abbreviation) LIKE '%UNOCACE%'
           OR upper(abbreviation) = 'UNC'
    ) INTO is_unocace_db;

    -- Si no es base de FV o es base de UNOCACE, no aplicar cambios
    IF NOT is_fv_db OR is_unocace_db THEN
        RAISE NOTICE 'V12: base no identificada como FV exclusiva (fv=%, unocace=%); sin cambios.', is_fv_db, is_unocace_db;
        RETURN;
    END IF;

    -- 1. Eliminar enableQuotaBalance y establecer parcelLotFreeText = true para Fortaleza del Valle
    UPDATE public.company
    SET configuration = (COALESCE(configuration, '{}'::jsonb) - 'enableQuotaBalance') || '{"parcelLotFreeText": true}'::jsonb
    WHERE upper(name) LIKE '%FORTALEZA%'
       OR upper(abbreviation) IN ('FDV', 'CFV');
    GET DIAGNOSTICS updated_companies = ROW_COUNT;

    -- 2. Desactivar displayquotabalance en instalaciones de acopio de Fortaleza del Valle
    UPDATE public.facility
    SET displayquotabalance = false
    WHERE company_id IN (
        SELECT id FROM public.company
        WHERE upper(name) LIKE '%FORTALEZA%'
           OR upper(abbreviation) IN ('FDV', 'CFV')
    );
    GET DIAGNOSTICS updated_facilities = ROW_COUNT;

    RAISE NOTICE 'V12: Fortaleza del Valle configurada correctamente. % empresas actualizadas (enableQuotaBalance removido, parcelLotFreeText=true), % instalaciones con displayquotabalance=false.',
        updated_companies, updated_facilities;
END $$;
