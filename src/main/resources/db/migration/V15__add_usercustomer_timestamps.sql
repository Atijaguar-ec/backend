-- ============================================================================
-- V15__add_usercustomer_timestamps.sql
--
-- Interoperabilidad Mieru Cacao & Sync Incremental (ADR-020):
-- 1. Añadir columnas de auditoría temporal en usercustomer (nullable para evitar errores de arranque)
-- 2. Replicar columnas en usercustomer_aud para validación de Hibernate Envers (ddl-auto=validate)
-- 3. Índices B-Tree para optimización de sync incremental (updated_since)
-- ============================================================================

ALTER TABLE public.usercustomer
    ADD COLUMN IF NOT EXISTS creationtimestamp timestamp(6) with time zone NULL,
    ADD COLUMN IF NOT EXISTS updatetimestamp timestamp(6) with time zone NULL;

UPDATE public.usercustomer
SET creationtimestamp = COALESCE(creationtimestamp, statusupdatetimestamp, NOW()),
    updatetimestamp = COALESCE(updatetimestamp, statusupdatetimestamp, NOW())
WHERE creationtimestamp IS NULL OR updatetimestamp IS NULL;

ALTER TABLE public.usercustomer_aud
    ADD COLUMN IF NOT EXISTS creationtimestamp timestamp(6) with time zone NULL,
    ADD COLUMN IF NOT EXISTS updatetimestamp timestamp(6) with time zone NULL;

CREATE INDEX IF NOT EXISTS idx_usercustomer_update_ts ON public.usercustomer (updatetimestamp ASC);
CREATE INDEX IF NOT EXISTS idx_stockorder_update_ts ON public.stockorder (updatetimestamp ASC);
CREATE INDEX IF NOT EXISTS idx_plot_lastupdated ON public.plot (lastupdated ASC);
