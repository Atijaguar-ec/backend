-- ============================================================================
-- V16__sync_postgresql_identity_sequences.sql
-- Synchronizes all PostgreSQL IDENTITY/SERIAL sequences and Hibernate Envers
-- revinfo_seq with current table maxima (prevents duplicate-key
-- ConstraintViolationException after MySQL -> PostgreSQL data migrations).
-- ============================================================================

DO $$
DECLARE
    rec RECORD;
    max_id BIGINT;
    seq_last BIGINT;
    seq_called BOOLEAN;
BEGIN
    FOR rec IN
        SELECT c.relname AS table_name,
               a.attname AS col_name,
               pg_get_serial_sequence(format('%I.%I', n.nspname, c.relname), a.attname) AS seq_name
        FROM pg_class c
        JOIN pg_namespace n ON n.oid = c.relnamespace
        JOIN pg_attribute a ON a.attrelid = c.oid
        WHERE n.nspname = 'public'
          AND c.relkind = 'r'
          AND a.attnum > 0
          AND NOT a.attisdropped
          AND pg_get_serial_sequence(format('%I.%I', n.nspname, c.relname), a.attname) IS NOT NULL
    LOOP
        EXECUTE format('SELECT COALESCE(MAX(%I), 0) FROM public.%I', rec.col_name, rec.table_name)
            INTO max_id;
        IF max_id > 0 THEN
            EXECUTE format('SELECT last_value, is_called FROM %s', rec.seq_name)
                INTO seq_last, seq_called;
            IF (NOT seq_called AND seq_last <= max_id) OR (seq_called AND seq_last < max_id) THEN
                PERFORM setval(rec.seq_name, max_id + 1, false);
            END IF;
        END IF;
    END LOOP;

    IF to_regclass('public.revinfo') IS NOT NULL AND to_regclass('public.revinfo_seq') IS NOT NULL THEN
        SELECT COALESCE(MAX(rev), 0) INTO max_id FROM public.revinfo;
        IF max_id > 0 THEN
            SELECT last_value, is_called INTO seq_last, seq_called FROM public.revinfo_seq;
            IF (NOT seq_called AND seq_last <= max_id) OR (seq_called AND seq_last < max_id + 50) THEN
                PERFORM setval('public.revinfo_seq', max_id + 50, false);
            END IF;
        END IF;
    END IF;
END $$;
