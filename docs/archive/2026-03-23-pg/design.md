# Design: postgres-migration — inatrace-backend

> Fecha: 2026-03-23 | Change: `postgres-migration`

---

## Technical Approach

Swap mínimo de infraestructura (driver, dialecto, Flyway module) sin tocar el modelo de dominio ni la lógica de negocio. La única adaptación de código es el registro del dialecto y la query JPQL de `GroupStockOrderService`.

---

## Architecture Decisions

### Decision 1: `string_agg` como función JPQL registrada en dialecto

| Opción | Tradeoff | Decisión |
|--------|----------|----------|
| Registrar `STRING_AGG` en `CustomPostgreSQLDialect` | Sin cambio en la query JPQL salvo el nombre de función | ✅ Elegida |
| Reescribir la query con SQL nativo | Pierde type-safety de JPQL, acopla más al motor | ❌ Rechazada |
| Usar `LISTAGG` (ANSI SQL 2016) vía Hibernate 6 | No soportado en Hibernate 6 + PG sin extensión | ❌ Rechazada |

**Rationale**: Hibernate 6 permite registrar funciones SQL arbitrarias en el dialecto. `string_agg(cast(expr as text), ',')` es el equivalente PG de `group_concat`. Registrar la función como `STRING_AGG` mantiene el JPQL casi idéntico.

### Decision 2: `GenerationType.IDENTITY` — sin cambio

| Opción | Tradeoff | Decisión |
|--------|----------|----------|
| Mantener `IDENTITY` | Se mapea a `BIGSERIAL` en PG; cero cambio en entidades | ✅ Elegida |
| Migrar a `SEQUENCE` | Más eficiente en PG pero rompe todas las entidades | ❌ Rechazada |

**Rationale**: `GenerationType.IDENTITY` es compatible con PostgreSQL. Refactorizar a `SEQUENCE` sería Out of Scope.

### Decision 3: `ddl-auto = validate` en producción

| Opción | Tradeoff | Decisión |
|--------|----------|----------|
| Mantener `ddl-auto = update` | Hibernate modifica el schema en vivo — riesgoso en PG | ⚠️ Solo para dev |
| Cambiar a `validate` en producción | Flyway controla el schema; Hibernate solo valida | ✅ Para prod |

**Rationale**: Flyway ya gestiona el schema. En PostgreSQL, `update` puede generar DDL inesperado. Producción debe usar `validate`.

### Decision 4: Mapeo de Claves Foráneas Sensibles a Mayúsculas

| Opción | Tradeoff | Decisión |
|--------|----------|----------|
| Mapear explícitamente en `@JoinColumn` | Forzar nombres en minúsculas (ej: `createdby_id`) previene conflictos con el lowercasing implícito de PostgreSQL | ✅ Elegida |
| Cambiar `PhysicalNamingStrategy` global | Afecta a TODAS las tablas de la base de datos y scripts Flyway existentes | ❌ Rechazada |

**Rationale**: Cuando una tabla destino está explícitamente entrecomillada (`"User"`), Hibernate 6 en PostgreSQL entrecomilla también la columna de clave foránea ("createdBy_id"). Dado que PostgreSQL crea las columnas sin comillas en minúsculas por defecto (`createdby_id`), las claves foráneas fallan. Especificar `@JoinColumn(name = "createdby_id")` resuelve la asimetría sin cambiar toda la estrategia global.

### Decision 5: Compatibilidad Flyway DDL en Esquema Limpio

| Opción | Tradeoff | Decisión |
|--------|----------|----------|
| Usar `IF EXISTS` en `DROP COLUMN/TABLE` | Permite que las migraciones históricas corran en un modelo vacío sin fallar inmediatamente | ✅ Elegida |
| Modificar el esquema base inicial | Requiere inyectar un script V1 complejo sintéticamente | ❌ Rechazada |

**Rationale**: Ya que la aplicación no cuenta con un archivo `V1__init.sql` completo y depende de la inicialización de Hibernate, los scripts `DROP COLUMN` fallaban al aplicarse sobre una BD vacía. Ignorar los checksums (`validate-on-migrate = false`) e insertar `IF EXISTS` permite compatibilidad universal.
---

## Data Flow

```
Startup:
  Spring Boot
    → HikariCP (jdbc:postgresql://...)
    → Flyway (flyway-database-postgresql)
        → ejecuta /db/migrations/*.sql
        → ejecuta Java migrations (JpaMigrationStrategy)
    → Hibernate SessionFactory (CustomPostgreSQLDialect)
    → Envers (tablas *_AUD auto-creadas vía ddl-auto)

Runtime (GroupStockOrder):
  HTTP GET /group-stock-orders
    → GroupStockOrderService.getGroupedStockOrderList()
    → JPQL: STRING_AGG(SO.id, ',')
        → CustomPostgreSQLDialect.initializeFunctionRegistry()
        → SQL: string_agg(cast(so.id as text), ',') ... GROUP BY ...
    → ApiGroupStockOrder (DTO construido por constructor JPQL)
```

---

## File Changes

| Archivo | Acción | Descripción |
|---------|--------|-------------|
| `pom.xml` | Modificar | Swap `mysql-connector-j` → `postgresql`; `flyway-mysql` → `flyway-database-postgresql` |
| `application.properties.template` | Modificar | JDBC URL a `jdbc:postgresql://...`; dialecto; puerto 5432 |
| `configuration/CustomMySQLDialect.java` | Renombrar + Reescribir | Nueva clase `CustomPostgreSQLDialect extends PostgreSQLDialect`; registrar `STRING_AGG` |
| `components/groupstockorder/GroupStockOrderService.java` | Modificar | `GROUP_CONCAT(SO.id)` → `STRING_AGG(SO.id, ',')` en JPQL |
| `db/migrations/*.sql` (~20 archivos) | Auditar / Modificar | Corregir sintaxis incompatible si existe |
| `docker-compose*.yml` / `ci/` | Modificar | Imagen MySQL → PostgreSQL (si aplica) |

---

## Interfaces / Contracts

**CustomPostgreSQLDialect** (nuevo):
```java
public class CustomPostgreSQLDialect extends PostgreSQLDialect {
    @Override
    public void initializeFunctionRegistry(FunctionContributions fc) {
        super.initializeFunctionRegistry(fc);
        fc.getFunctionRegistry().register(
            "STRING_AGG",
            new StandardSQLFunction("string_agg", StandardBasicTypes.STRING)
        );
    }
}
```

**GroupStockOrderService** (cambio en JPQL):
```java
// Antes:
"GROUP_CONCAT(SO.id), "
// Después:
"STRING_AGG(cast(SO.id as string), ','), "
```

---

## Testing Strategy

| Capa | Qué testear | Cómo |
|------|-------------|------|
| Unit | `CustomPostgreSQLDialect` registra `STRING_AGG` | Test que invoca `initializeFunctionRegistry` y verifica registro |
| Integration | Flyway aplica todas las migraciones en PG limpio | `@SpringBootTest` con `spring.datasource.url=jdbc:postgresql://localhost/test` |
| Integration | `GroupStockOrderService` retorna IDs agrupados correctamente | Test con datos de prueba en PG; verificar que el campo no sea null |
| Integration | Envers crea tablas `*_AUD` y registra cambios | Verificar que al modificar una entidad auditada el registro en `*_AUD` existe |

---

## Migration / Rollout

1. **Dev**: levantar PostgreSQL vía Docker (`docker run -e POSTGRES_DB=inatrace -p 5432:5432 postgres:16`)
2. **Validación local**: arrancar el backend con `application.properties` apuntando a PG; verificar logs de Flyway
3. **CI**: actualizar pipeline para usar `postgres:16` como servicio en tests
4. **Producción**: NO aplicar hasta haber migrado datos con `pgloader` o equivalente (Out of Scope)

---

## Open Questions

- [ ] ¿Existen scripts SQL de inicialización fuera de `db/migrations/` (en el directorio `import/`)? Revisar si contienen DDL MySQL-específico
- [ ] ¿El CI actual tiene tests de integración que arrancan con MySQL? Si es así, deben actualizarse para usar la imagen PG
