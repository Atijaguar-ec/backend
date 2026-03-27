# Tasks: Migración PostgreSQL — inatrace-backend

> Change: `postgres-migration` | Fecha: 2026-03-23

---

## Phase 1: Infraestructura y Dependencias

- [x] 1.1 En `pom.xml`: reemplazar `com.mysql:mysql-connector-j` por `org.postgresql:postgresql` (sin versión — Spring Boot la gestiona)
- [x] 1.2 En `pom.xml`: reemplazar `org.flywaydb:flyway-mysql` por `org.flywaydb:flyway-database-postgresql`
- [ ] 1.3 Verificar que `mvn dependency:resolve` completa sin errores tras los swaps
- [x] 1.4 En `application.properties.template`: actualizar `spring.datasource.url` a `jdbc:postgresql://${INATrace.database.hostname}:${INATrace.database.port}/${INATrace.database.name}`
- [x] 1.5 En `application.properties.template`: cambiar port default de `3306` a `5432`
- [x] 1.6 En `application.properties.template`: actualizar `spring.jpa.properties.hibernate.dialect` a `com.abelium.inatrace.configuration.CustomPostgreSQLDialect`
- [ ] 1.7 Levantar instancia PostgreSQL local vía Docker: `docker run --name inatrace-pg -e POSTGRES_DB=inatrace -e POSTGRES_USER=inatrace -e POSTGRES_PASSWORD=inatrace -p 5432:5432 -d postgres:16`

---

## Phase 2: Dialecto Hibernate y Adaptación JPQL

- [x] 2.1 Crear `src/main/java/com/abelium/inatrace/configuration/CustomPostgreSQLDialect.java` extendiendo `org.hibernate.dialect.PostgreSQLDialect`
- [x] 2.2 En `CustomPostgreSQLDialect`: sobreescribir `initializeFunctionRegistry` y registrar `STRING_AGG` → `string_agg` (tipo `StandardBasicTypes.STRING`)
- [x] 2.3 En `GroupStockOrderService.java`: reemplazar `"GROUP_CONCAT(SO.id), "` por `"STRING_AGG(cast(SO.id as string), ','), "`
- [x] 2.4 Eliminar (o marcar como deprecated) `CustomMySQLDialect.java` — ya no referenciado
- [ ] 2.5 Verificar que `mvn compile` pasa sin errores

---

## Phase 3: Auditoría de Migraciones SQL

- [x] 3.1 Revisar cada uno de los ~20 archivos en `src/main/resources/db/migrations/*.sql` — ✅ LIMPIO (no backticks, no ENGINE=, no TINYINT, no AUTO_INCREMENT)
- [x] 3.2 Revisar el directorio `import/` — ✅ solo `countries.csv`, sin DDL
- [x] 3.3 Revisar migraciones Java en `db/migrations/*.java` — ✅ todas usan JPQL estándar o EntityManager, sin `createNativeQuery`
- [x] 3.4 Documentado en `docs/pg/migration-audit.md` — 41 archivos auditados, todos OK

---

## Phase 4: Verificación e Integración

- [ ] 4.1 Arrancar el backend con `application.properties` apuntando a PostgreSQL local (Phase 1.7); verificar que Flyway ejecuta todas las migraciones con estado `SUCCESS`
- [ ] 4.2 Verificar en logs que `CustomPostgreSQLDialect` se carga (buscar `Dialect: com.abelium.inatrace.configuration.CustomPostgreSQLDialect`)
- [ ] 4.3 Probar endpoint `GET /api/.../group-stock-orders` (o su equivalente); verificar que `ApiGroupStockOrder` devuelve IDs agrupados correctamente
- [ ] 4.4 Verificar que las tablas `*_AUD` de Envers se crean correctamente en PostgreSQL (inspeccionar el schema tras arrancar)
- [ ] 4.5 Cambiar `spring.jpa.hibernate.ddl-auto = validate` (y `hbm2ddl.auto = validate`) en el properties de prueba/producción — verificar que el backend arranca sin intentar modificar el schema
- [ ] 4.6 Actualizar imagen de base de datos en `docker-compose` o CI pipeline de `mysql` a `postgres:16` (si aplica)

---

## Phase 5: Tests y Documentación

- [ ] 5.1 Escribir test unitario para `CustomPostgreSQLDialect`: verificar que `STRING_AGG` está registrada en el `FunctionRegistry`
- [ ] 5.2 Escribir test de integración con `@SpringBootTest` + datasource PostgreSQL: verificar que Flyway completa todas las migraciones (REQ-FLY-02)
- [ ] 5.3 Escribir test de integración para `GroupStockOrderService.getGroupedStockOrderList`: insertar datos de prueba y verificar que el campo de IDs agrupados no es null (REQ-GSO-01)
- [ ] 5.4 Actualizar `docs/pg/as-is.md` → renombrar/archivar como estado histórico
- [x] 5.5 Crear `docs/pg/README.md` con índice del directorio de documentación de la migración
