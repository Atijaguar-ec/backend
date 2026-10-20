-- ============================================================================
-- V12__disable_quota_balance_for_fv.sql
--
-- Fortaleza del Valle (FV): Desactivar control de cupo, restituir parcelLotFreeText
-- y depurar cualquier configuración exclusiva de UNOCACE.
--
-- REGLA DE NEGOCIO:
-- El control y saldo de cupos anuales (enableQuotaBalance), la simplificación a 'Cacao'
-- (simplifySemiProductToCacao), opciones numéricas de variedad (numericVarietyOptions)
-- y displayquotabalance en centros de acopio son funcionalidades exclusivas de UNOCACE.
-- En Fortaleza del Valle los productores no gestionan cupos anuales en el sistema,
-- acopian 100% Cacao en Baba y las entregas operan con parcelas en modo texto libre
-- (parcelLotFreeText = true).
-- V11 erróneamente activó enableQuotaBalance y parcelLotFreeText = false para FV.
-- Esta migración sanea definitivamente la base de datos de FV.
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

    -- 1. Eliminar cualquier clave exclusiva de UNOCACE y asegurar parcelLotFreeText = true en TODA la base de FV
    UPDATE public.company
    SET configuration = (COALESCE(configuration, '{}'::jsonb)
        - 'enableQuotaBalance'
        - 'simplifySemiProductToCacao'
        - 'genericCacaoDisplay'
        - 'numericVarietyOptions'
        - 'quotaAlertThresholdPercent'
    ) || '{"parcelLotFreeText": true}'::jsonb;
    GET DIAGNOSTICS updated_companies = ROW_COUNT;

    -- 2. Desactivar displayquotabalance en todas las instalaciones de la base de FV
    UPDATE public.facility
    SET displayquotabalance = false;
    GET DIAGNOSTICS updated_facilities = ROW_COUNT;

    RAISE NOTICE 'V12: Base de Fortaleza del Valle saneada con éxito. % empresas actualizadas (claves UNOCACE depuradas, parcelLotFreeText=true), % instalaciones con displayquotabalance=false.',
        updated_companies, updated_facilities;
END $$;
