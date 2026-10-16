-- ============================================================================
-- V11__align_fv_tenant_configuration.sql
--
-- REGLA DE AISLAMIENTO POR TENANT
-- UNOCACE y todas sus asociaciones filiales llevan la matriz de configuracion de
-- UNOCACE; Fortaleza del Valle (FV) y todas sus organizaciones llevan la matriz de
-- FV. Ninguna migracion de un tenant debe tocar al otro.
--
-- Correccion de V4, V5, V6, V8 y V10: esas migraciones decidian el tenant por el
-- nombre de la empresa ("todo lo que no se llame FORTALEZA es UNOCACE"). En la base
-- de FV eso produjo dos danos simetricos:
--   a) A las organizaciones de FV cuyo nombre no incluye "Fortaleza" (Valle del
--      Carrizal, Rio Grande, Aroma de Quiroga) les escribio claves exclusivas de
--      UNOCACE (simplifySemiProductToCacao, quotaAlertThresholdPercent,
--      numericVarietyOptions, parcelLotFreeText = true).
--   b) A las que si incluyen "Fortaleza" (FORTALEZA DEL VALLE, Corporacion Fortaleza
--      del Valle, Fortaleza de Guarumal) les BORRO las claves que el libro maestro de
--      FV exige (enableQuotaBalance, enableDeliveryReceipt, enablePublicDeliveryReceipt)
--      y apago displayquotabalance en sus centros de acopio.
--
-- Esta migracion solo actua en una base de FV (existe una empresa FV y NO existe una
-- empresa UNOCACE). En la base de UNOCACE no hace nada. No modifica V4-V10: Flyway
-- valida sus checksums. Es idempotente.
--
-- POR QUE SIGUE EN db/migration Y NO EN db/tenant/fv
-- De aqui en adelante, toda migracion de datos de un tenant va en db/tenant/<tenant>,
-- que entra en spring.flyway.locations solo con su perfil activo (ver db/tenant/README.md).
-- Esta es la excepcion: repara un dano ya presente en la base de FV y debe ejecutarse en
-- el proximo despliegue sin depender de que antes se active SPRING_PROFILES_ACTIVE=...,
-- fortaleza en el servidor. Por eso conserva la deteccion de tenant en tiempo de ejecucion.
-- Si la deteccion falla (p. ej. una empresa de prueba con "UNOCACE" en el nombre dentro de
-- la base de FV), la migracion se omite sin error: revisar el RAISE NOTICE en el log.
--
-- Referencias: docs/fv/REGLAS_OPERATIVAS_Y_TECNICAS_FORTALEZA_VALLE.md seccion 2
--              docs/unocace/REGLAS_OPERATIVAS_Y_TECNICAS_UNOCACE.md seccion 2
-- ============================================================================

DO $$
DECLARE
    is_fv_db boolean;
    is_unocace_db boolean;
    cleaned_companies integer := 0;
    aligned_companies integer := 0;
    aligned_facilities integer := 0;
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

    IF NOT is_fv_db OR is_unocace_db THEN
        RAISE NOTICE 'V11: base no identificada como FV (fv=%, unocace=%); sin cambios.', is_fv_db, is_unocace_db;
        RETURN;
    END IF;

    -- 1. Quitar de TODA la base de FV las claves exclusivas de UNOCACE.
    --    quotaAlertThresholdPercent se retira porque el umbral de FV es el default del
    --    codigo (DeliveryReceiptTools.DEFAULT_QUOTA_ALERT_THRESHOLD_PERCENT = 80.00),
    --    de modo que el comportamiento no cambia.
    UPDATE public.company
    SET configuration = configuration
        - 'simplifySemiProductToCacao'
        - 'genericCacaoDisplay'
        - 'numericVarietyOptions'
        - 'quotaAlertThresholdPercent'
    WHERE configuration IS NOT NULL
      AND configuration ?| ARRAY[
          'simplifySemiProductToCacao',
          'genericCacaoDisplay',
          'numericVarietyOptions',
          'quotaAlertThresholdPercent'
      ];
    GET DIAGNOSTICS cleaned_companies = ROW_COUNT;

    -- 2. Garantizar la matriz de FV en todas sus organizaciones (libro maestro, seccion 2).
    --    El operador || conserva el resto de la configuracion existente
    --    (p. ej. onlyOrganicProduction). parcelLotFreeText = false es el modo estricto,
    --    identico al comportamiento con la clave ausente.
    UPDATE public.company
    SET configuration = COALESCE(configuration, '{}'::jsonb) || '{
            "enableQuotaBalance": true,
            "enableDeliveryReceipt": true,
            "enablePublicDeliveryReceipt": true,
            "onlyNacionalVariety": true,
            "parcelLotFreeText": false
        }'::jsonb
    WHERE configuration IS NULL
       OR NOT (configuration @> '{
            "enableQuotaBalance": true,
            "enableDeliveryReceipt": true,
            "enablePublicDeliveryReceipt": true,
            "onlyNacionalVariety": true,
            "parcelLotFreeText": false
        }'::jsonb);
    GET DIAGNOSTICS aligned_companies = ROW_COUNT;

    -- 3. Restituir el saldo de cupo en los centros de acopio de FV. V6 y V8 lo apagaron
    --    en las empresas cuyo nombre coincidia con FORTALEZA. Si despues de esto algun
    --    centro no debe mostrarlo, se desactiva desde la configuracion de la instalacion.
    UPDATE public.facility
    SET displayquotabalance = true
    WHERE displayquotabalance IS DISTINCT FROM true
      AND company_id IN (
          SELECT id FROM public.company
          WHERE configuration->>'enableQuotaBalance' = 'true'
      );
    GET DIAGNOSTICS aligned_facilities = ROW_COUNT;

    RAISE NOTICE 'V11: base FV; % empresas limpiadas de claves UNOCACE, % empresas alineadas a la matriz FV, % instalaciones con saldo de cupo restituido.',
        cleaned_companies, aligned_companies, aligned_facilities;
END $$;
