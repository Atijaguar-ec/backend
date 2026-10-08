-- ============================================================================
-- V9__add_stockorder_cancellation.sql
-- Soporte de anulacion de entregas / ordenes de stock (commit 4744616).
-- Agrega las columnas mapeadas en StockOrder (status, cancellationReason,
-- cancellationTimestamp, canceledBy) a stockorder y a su tabla de auditoria
-- Envers (stockorder_aud). Sin estas columnas Hibernate (ddl-auto=validate)
-- aborta el arranque del backend.
-- Migracion aditiva e idempotente: no altera datos existentes salvo marcar
-- como ACTIVE las ordenes historicas.
-- ============================================================================

-- 1. Columnas en stockorder
ALTER TABLE public.stockorder ADD COLUMN IF NOT EXISTS status character varying(40) DEFAULT 'ACTIVE';
ALTER TABLE public.stockorder ADD COLUMN IF NOT EXISTS cancellationreason character varying(255);
ALTER TABLE public.stockorder ADD COLUMN IF NOT EXISTS cancellationtimestamp timestamp(6) with time zone;
ALTER TABLE public.stockorder ADD COLUMN IF NOT EXISTS canceledby_id bigint;

-- 2. Ordenes historicas: estado ACTIVE explicito
UPDATE public.stockorder SET status = 'ACTIVE' WHERE status IS NULL;

-- 3. FK al usuario que anula + indices
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_stockorder_canceledby'
    ) THEN
        ALTER TABLE public.stockorder
            ADD CONSTRAINT fk_stockorder_canceledby
            FOREIGN KEY (canceledby_id) REFERENCES public."User"(id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_stockorder_canceledby_id ON public.stockorder USING btree (canceledby_id);
CREATE INDEX IF NOT EXISTS idx_stockorder_company_status ON public.stockorder USING btree (company_id, status);

-- 4. Columnas en la tabla de auditoria Envers (sin FK)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'stockorder_aud') THEN
        ALTER TABLE public.stockorder_aud ADD COLUMN IF NOT EXISTS status character varying(40);
        ALTER TABLE public.stockorder_aud ADD COLUMN IF NOT EXISTS cancellationreason character varying(255);
        ALTER TABLE public.stockorder_aud ADD COLUMN IF NOT EXISTS cancellationtimestamp timestamp(6) with time zone;
        ALTER TABLE public.stockorder_aud ADD COLUMN IF NOT EXISTS canceledby_id bigint;
    END IF;
END $$;
