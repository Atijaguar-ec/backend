-- ============================================================================
-- V13__grant_mcp_ro_bi_schema.sql
--
-- Conceder permisos de lectura al usuario inatrace_mcp_ro sobre el esquema bi
-- para permitir la emision de reportes regulatorios Agrocalidad (ADR-BI-001).
-- ============================================================================

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'inatrace_mcp_ro') THEN
        GRANT USAGE ON SCHEMA bi TO inatrace_mcp_ro;
        GRANT SELECT ON ALL TABLES IN SCHEMA bi TO inatrace_mcp_ro;
        ALTER DEFAULT PRIVILEGES IN SCHEMA bi GRANT SELECT ON TABLES TO inatrace_mcp_ro;
        RAISE NOTICE 'V13: Privilegios sobre esquema bi concedidos exitosamente a inatrace_mcp_ro.';
    ELSE
        RAISE NOTICE 'V13: Rol inatrace_mcp_ro no existe en esta base; omitiendo grants.';
    END IF;
END $$;
