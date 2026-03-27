# Propuesta: Migración a PostgreSQL — inatrace-backend

> **Change**: postgres-migration  
> **Fecha**: 2026-03-23  
> **Estado**: En revisión

---

## Intent

Migrar la infraestructura de base de datos de **MySQL a PostgreSQL** para:
- Alinear el backend con estándares modernos de infraestructura
- Habilitar características avanzadas de PostgreSQL (JSONB, full-text search, etc.)
- Eliminar dependencia de un motor propietario/cerrado en entornos cloud

El codebase tiene **muy bajo acoplamiento** con MySQL (sin `nativeQuery`, un solo uso de `GROUP_CONCAT`), lo que hace la migración viable con esfuerzo Medium.

---

## Scope

### En el Alcance
- Sustitución de driver JDBC (`mysql-connector-j` → `postgresql`)
- Swap de dependencias Flyway (`flyway-mysql` → `flyway-database-postgresql`)
- Actualización de `application.properties.template` (JDBC URL, puerto, dialecto)
- Reemplazo de `CustomMySQLDialect` por `CustomPostgreSQLDialect`
- Adaptación de `GROUP_CONCAT(SO.id)` → `string_agg` en `GroupStockOrderService`
- Auditoría completa de los ~20 archivos SQL de migración Flyway
- Validación de Hibernate Envers con el nuevo dialecto

### Fuera del Alcance
- Migración física de datos de producción (plan separado con `pgloader` o DMS)
- Refactorización de lógica de negocio o del frontend
- Cambios en el modelo de entidades JPA (solo validación)
- Introducción de features PostgreSQL-específicas (JSONB, etc.) — se deja para después

---

## Approach

**Full Lift-and-Shift** en 4 pasos:

1. **Dependencias** — swap `pom.xml`: MySQL driver → PG driver, `flyway-mysql` → `flyway-database-postgresql`
2. **Configuración** — nueva JDBC URL `jdbc:postgresql://...`, nuevo dialectoo, puerto 5432
3. **Dialect + JPQL** — crear `CustomPostgreSQLDialect` con `string_agg` registrada; adaptar la query en `GroupStockOrderService`
4. **Migrations audit** — recorrer los 20+ SQL files, corregir sintaxis MySQL-específica (backticks, `TINYINT`, `ENGINE=INNODB`, etc.)

---

## Affected Areas

| Área | Impacto | Descripción |
|------|---------|-------------|
| `pom.xml` | Modificado | Swap de driver + Flyway module |
| `application.properties.template` | Modificado | JDBC URL, dialect, port |
| `configuration/CustomMySQLDialect.java` | Renombrado/Reescrito | Nueva clase `CustomPostgreSQLDialect` |
| `components/groupstockorder/GroupStockOrderService.java` | Modificado | `GROUP_CONCAT` → `string_agg` |
| `src/main/resources/db/migrations/*.sql` | Auditados | ~20 archivos DDL, corrección de sintaxis incompatible |
| Docker/CI (`Dockerfile`, `ci/`) | Modificado | Imagen de MySQL → PostgreSQL |

---

## Risks

| Riesgo | Probabilidad | Mitigación |
|--------|-------------|------------|
| SQL migrations con sintaxis MySQL (`ENGINE`, backticks) | Media | Auditoría manual previa a ejecución |
| `string_agg` con comportamiento distinto a `GROUP_CONCAT` | Baja | Test unitario de `GroupStockOrderService` con datos reales |
| Envers tablas `_AUD` con incompatibilidades de dialecto | Baja | Pruebas de integración con `ddl-auto=create-drop` en entorno limpio |
| Diferencias en `ddl-auto=update` entre dialectos | Baja | Probar con DB limpia antes de aplicar sobre schema existente |

---

## Rollback Plan

1. `git revert` o `git checkout <commit-anterior>` en `pom.xml`, `application.properties.template`, y archivos de configuración
2. Detener instancia PostgreSQL, levantar MySQL original
3. Las migraciones Flyway no se revierten — si se ejecutaron en PG no afectan MySQL
4. Si se llegó a modificar el schema en MySQL por error, restaurar backup previo

---

## Dependencies

- Instancia de PostgreSQL disponible en entorno de desarrollo (Docker Compose o local)
- Acceso para crear base de datos y usuario en la instancia PG

---

## Success Criteria

- [ ] El backend arranca correctamente conectado a PostgreSQL sin errores en consola
- [ ] Todas las migraciones Flyway se aplican sin errores sintácticos
- [ ] El endpoint que usa `GroupStockOrderService` devuelve resultados correctos
- [ ] Las tablas de auditoría (`*_AUD`) se crean y registran cambios correctamente
- [ ] Los tests de integración (si existen) pasan en CI con imagen PostgreSQL
