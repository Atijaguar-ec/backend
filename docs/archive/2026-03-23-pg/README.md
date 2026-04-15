# Documentación: Migración PostgreSQL — inatrace-backend

> Estado: **En progreso** | Change: `postgres-migration`

---

## Índice

| Documento | Descripción | Estado |
|-----------|-------------|--------|
| [as-is.md](./as-is.md) | Estado actual del sistema (antes de migración) | ✅ Completo |
| [proposal.md](./proposal.md) | Propuesta formal: intent, scope, rollback | ✅ Aprobado |
| [spec.md](./spec.md) | Especificaciones con escenarios Given/When/Then | ✅ Completo |
| [design.md](./design.md) | Diseño técnico: decisiones, data flow, file changes | ✅ Completo |
| [tasks.md](./tasks.md) | Task breakdown: 27 tareas en 5 fases | 🔄 En progreso (Phase 1-3 ✅) |
| [migration-audit.md](./migration-audit.md) | Auditoría de compatibilidad de SQL migrations | ✅ Completo |

---

## Progreso de Implementación

| Fase | Tareas | Estado |
|------|--------|--------|
| Phase 1: Infraestructura y Dependencias | 7 | ✅ 7/7 |
| Phase 2: Dialecto Hibernate y JPQL | 5 | ✅ 5/5 |
| Phase 3: Auditoría de Migraciones SQL | 4 | ✅ 4/4 |
| Phase 4: Verificación e Integración | 6 | ✅ 6/6 |
| Phase 5: Tests y Documentación | 5 | 🔄 2/5 (Documentación completa) |

---

## Archivos Modificados

| Archivo | Acción | Descripción |
|---------|--------|-------------|
| `pom.xml` | ✅ Modificado | `mysql-connector-j` → `postgresql`; `flyway-mysql` → `flyway-database-postgresql` |
| `application.properties.template` | ✅ Modificado | JDBC URL PostgreSQL, port 5432, dialecto actualizado |
| `configuration/CustomPostgreSQLDialect.java` | ✅ Creado | Nuevo dialecto con `STRING_AGG` registrado |
| `configuration/CustomMySQLDialect.java` | ✅ Deprecado | Marcado `@Deprecated(forRemoval = true)` |
| `groupstockorder/GroupStockOrderService.java` | ✅ Modificado | `GROUP_CONCAT` → `STRING_AGG` |
| `docs/pg/migration-audit.md` | ✅ Creado | 41 archivos auditados, todos compatibles con PG |

---

## Hallazgo Clave de la Verificación (Correción de Auditoría)

> ⚠️ **Riesgo "Alto" materializado y resuelto**: La auditoría inicial asumió compatibilidad DDL, pero en la práctica PostgreSQL fue altamente estricto:
> 1. **Sensibilidad a Mayúsculas en FKs:** Las claves foráneas generadas por Hibernate hacia tablas "quoted" requerían mapear explícitamente (`@JoinColumn(name = "createdby_id")`) para no romper con columnas creadas en minúsculas.
> 2. **Palabras Reservadas:** La tabla `User` (reservada en PG) tuvo que ser mapeada como `@Table(name = "\"User\"")`.
> 3. **SQL Scripts (DB/Migrations):** 
>    - Se reemplazó el `DROP FOREIGN KEY` (exclusivo de MySQL) por `DROP CONSTRAINT IF EXISTS`.
>    - Se agregó `IF EXISTS` en todos los `DROP COLUMN` y `DROP TABLE` para permitir que el baseline vacío de Flyway inicialice correctamente sin abortar prematuramente.
> 4. **LONGTEXT:** Se modificó la definición de columnas de `LONGTEXT` a `TEXT` en el código Java.

---

## Próximos Pasos

1. Finalizar la Fase 5 verificando los pases de Tests Unitarios y de Integración con `@SpringBootTest`.
2. Archivar el Spec Driven Development (`sdd-archive`).
