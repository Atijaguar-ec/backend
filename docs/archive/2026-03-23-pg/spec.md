# Especificación: postgres-migration

> Delta spec — dominio: **Infraestructura de Base de Datos**  
> Change: `postgres-migration` | Fecha: 2026-03-23

---

## Dominio: Conexión y Driver JDBC

### MODIFIED — REQ-DB-01: Driver de Base de Datos

El sistema **MUST** utilizar el driver JDBC de PostgreSQL (`org.postgresql:postgresql`) en lugar del driver MySQL.

#### Scenario: Inicio de aplicación con PostgreSQL

- GIVEN que el driver `org.postgresql:postgresql` está en el classpath  
- WHEN la aplicación arranca  
- THEN el datasource establece conexión a PostgreSQL sin errores  
- AND el pool HikariCP reporta conexiones activas

#### Scenario: URL JDBC inválida

- GIVEN que la JDBC URL tiene formato incorrecto (`jdbc:postgres://` en vez de `jdbc:postgresql://`)  
- WHEN la aplicación intenta arrancar  
- THEN el arranque falla con error claro de conexión antes del health check

---

## Dominio: Dialecto Hibernate

### MODIFIED — REQ-DIALECT-01: CustomPostgreSQLDialect

El sistema **MUST** utilizar un dialecto Hibernate basado en `PostgreSQLDialect` en lugar de `MySQLDialect`.

El dialecto personalizado **MUST** registrar una función de agregación de strings compatible con PostgreSQL que pueda usarse desde JPQL.

#### Scenario: Función de agregación disponible en JPQL

- GIVEN que `CustomPostgreSQLDialect` extiende `PostgreSQLDialect`  
- WHEN Hibernate ejecuta una JPQL que usa `STRING_AGG`  
- THEN la función se traduce correctamente a SQL nativo de PostgreSQL  
- AND no se lanza excepción de función no registrada

---

## Dominio: Migraciones Flyway

### MODIFIED — REQ-FLY-01: Módulo Flyway para PostgreSQL

El sistema **MUST** usar `flyway-database-postgresql` en lugar de `flyway-mysql`.

### MODIFIED — REQ-FLY-02: Compatibilidad de scripts SQL

Todos los scripts existentes en `classpath:/db/migrations` **MUST** ejecutarse sin errores en PostgreSQL.

Los scripts **MUST NOT** contener sintaxis MySQL-específica (`ENGINE=INNODB`, backticks `` ` ``, `TINYINT(1)` como booleano sin anotación, etc.).

#### Scenario: Flyway aplica todas las migraciones en DB limpia

- GIVEN una instancia PostgreSQL vacía  
- WHEN Flyway ejecuta todas las migraciones (`baseline-on-migrate = true`)  
- THEN todas las migraciones pasan con estado `SUCCESS`  
- AND la tabla `schema_version` registra cada migración correctamente

#### Scenario: Script con sintaxis incompatible detectado

- GIVEN un script SQL con backticks o `ENGINE=INNODB`  
- WHEN Flyway intenta ejecutarlo sobre PostgreSQL  
- THEN Flyway falla con error sintáctico descriptivo  
- AND ninguna migración posterior se ejecuta (transacción revertida)

---

## Dominio: Agregación de Datos — GroupStockOrder

### MODIFIED — REQ-GSO-01: Agregación de IDs de StockOrder

El sistema **MUST** retornar los IDs de `StockOrder` agrupados como una cadena delimitada por comas dentro de `ApiGroupStockOrder`.

La implementación **MUST** usar la función registrada en el dialecto (no `GROUP_CONCAT` directamente).

#### Scenario: Recuperación de stock orders agrupados

- GIVEN órdenes de stock con el mismo `productionDate`, `internalLotNumber` y `orderType`  
- WHEN se llama a `GET /api/.../group-stock-orders`  
- THEN cada `ApiGroupStockOrder` contiene los IDs agrupados como string  
- AND el campo no es `null` ni vacío cuando hay órdenes

#### Scenario: Un solo stock order en el grupo

- GIVEN un solo `StockOrder` que cumple los criterios de agrupación  
- WHEN se llama al endpoint  
- THEN `ApiGroupStockOrder` contiene exactamente un ID en el campo de agregación

---

## Dominio: Auditoría (Envers)

### UNCHANGED — REQ-AUD-01: Tablas de Auditoría

El sistema **MUST** continuar creando tablas `*_AUD` para todas las entidades auditadas.

El comportamiento de auditoría **MUST NOT** cambiar desde la perspectiva del usuario final.

#### Scenario: Cambio auditado en entidad existente

- GIVEN que Hibernate Envers está activo con dialecto PostgreSQL  
- WHEN se modifica cualquier entidad auditada  
- THEN se registra una fila en la tabla `*_AUD` correspondiente  
- AND el campo `REV` apunta a la revisión correcta
