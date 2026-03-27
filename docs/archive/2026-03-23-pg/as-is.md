# AS-IS: Capa de Datos — inatrace-backend

> Documento que describe el estado actual de la infraestructura de base de datos del backend antes de cualquier migración a PostgreSQL.
> Fecha de análisis: 2026-03-23

---

## 1. Stack de Base de Datos

| Componente | Valor actual |
|---|---|
| Motor de base de datos | **MySQL** |
| Driver Java | `com.mysql:mysql-connector-j` (managed por Spring Boot 3.3.3) |
| ORM | **Spring Data JPA + Hibernate 6** |
| Dialecto Hibernate | `CustomMySQLDialect` (extiende `org.hibernate.dialect.MySQLDialect`) |
| Auditoría | **Hibernate Envers** vía `spring-data-envers` |
| Migraciones de esquema | **Flyway** (`flyway-core` + `flyway-mysql`) |
| Pool de conexiones | **HikariCP** (incluido en Spring Boot) |
| Query builder JPQL | **TorpedoQuery** (`org.torpedoquery.jakarta` v1.0.1) |

---

## 2. Configuración de Conexión

Archivo de referencia: `src/main/resources/application.properties.template`

```properties
# Parámetros de conexión
INATrace.database.name     =
INATrace.database.hostname = localhost
INATrace.database.port     = 3306

spring.datasource.url      = jdbc:mysql://${INATrace.database.hostname}:${INATrace.database.port}/${INATrace.database.name}?autoReconnect=true&allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC
spring.datasource.username =
spring.datasource.password =

# Dialecto personalizado MySQL
spring.jpa.properties.hibernate.dialect = com.abelium.inatrace.configuration.CustomMySQLDialect

# DDL auto-manage
spring.jpa.hibernate.ddl-auto                 = update
spring.jpa.properties.hibernate.hbm2ddl.auto  = update

# Naming strategies
spring.jpa.hibernate.naming.implicit-strategy = org.hibernate.boot.model.naming.ImplicitNamingStrategyComponentPathImpl
spring.jpa.hibernate.naming.physical-strategy = org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl

# HikariCP
spring.datasource.hikari.maximumPoolSize = 20
spring.datasource.hikari.minimumIdle     = 10
spring.datasource.hikari.idleTimeout     = 30000
spring.datasource.hikari.maxLifetime     = 30000
```

---

## 3. Dialecto Personalizado

Archivo: `src/main/java/com/abelium/inatrace/configuration/CustomMySQLDialect.java`

```java
public class CustomMySQLDialect extends MySQLDialect {
    @Override
    public void initializeFunctionRegistry(FunctionContributions functionContributions) {
        super.initializeFunctionRegistry(functionContributions);
        functionContributions.getFunctionRegistry()
            .register("GROUP_CONCAT", new StandardSQLFunction("group_concat", StandardBasicTypes.STRING));
    }
}
```

**Propósito**: Registrar la función `GROUP_CONCAT` de MySQL para poder usarla en queries JPQL. Esta función **no existe en PostgreSQL** — su equivalente es `string_agg`.

---

## 4. Sistema de Migraciones (Flyway)

### 4.1 Configuración

```properties
spring.flyway.baseline-on-migrate    = true
spring.flyway.out-of-order           = true
spring.flyway.validate-on-migrate    = false
spring.flyway.check-location         = false
spring.flyway.table                  = schema_version
spring.flyway.ignore-missing-migrations = true
spring.flyway.locations              = com.abelium.inatrace.db.migrations,classpath:/db/migrations
```

### 4.2 Fuentes de migración (dual)

| Fuente | Ubicación | Descripción |
|---|---|---|
| SQL puro | `src/main/resources/db/migrations/*.sql` | DDL estándar (ALTER TABLE, DROP COLUMN, DROP TABLE) — 20+ archivos |
| Java/JPQL | `src/main/java/com/abelium/inatrace/db/migrations/*.java` | Migraciones de datos usando EntityManager/JPQL |

### 4.3 Orden de inicialización customizado

Flyway se inicializa **después** del `EntityManagerFactory` para permitir las migraciones Java con JPA:

```
MigrationsConfiguration
├── flywayInitializer          → no hace nada (sobreescribe el default)
└── delayedFlywayInitializer   → ejecuta Flyway post-JPA init via JpaMigrationStrategy
```

Clases involucradas:
- `MigrationsConfiguration.java`
- `DelayedFlywayMigrationInitializer.java`
- `JpaMigrationStrategy.java`

### 4.4 Historial de migraciones SQL (archivos en `classpath:/db/migrations`)

Todas las migraciones son del año 2023 y son DDL estándar:
- `DROP COLUMN` en tablas `Product`, `ProductLabelContent`, `Process`, `Responsability`, `ProductSettings`
- `DROP TABLE` en `ProcessStandard`, `ProcessDocument`, `ResponsibilityFarmerPicture`
- `DELETE` de campos en `ProductLabel_fields`

> ⚠️ **Pendiente de auditoría**: verificar que ninguna contenga sintaxis MySQL-específica (backticks, `ENGINE=INNODB`, `TINYINT`, etc.)

---

## 5. Modelo de Entidades JPA

### 5.1 Base entity

Archivo: `src/main/java/com/abelium/inatrace/db/base/BaseEntity.java`

```java
@MappedSuperclass
public class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // ← compatible con PG SERIAL
    @Access(AccessType.PROPERTY)
    private Long id;
}
```

Estrategia `IDENTITY` es compatible con PostgreSQL (usa `SERIAL` / `BIGSERIAL` implícito).

### 5.2 Jerarquía de base entities

| Clase | Propósito |
|---|---|
| `BaseEntity` | Solo `id` auto-generado |
| `TimestampEntity` | `id` + `creationTimestamp` + `updateTimestamp` |
| `CreationTimestampEntity` | `id` + `creationTimestamp` |
| `CodebookBaseEntity` | Para tablas de codebook |
| `TranslatedEntity` | Entidades con soporte multiidioma |

### 5.3 Dominios del modelo (directorios en `db/entities/`)

```
db/entities/
├── analytics/
├── auth/
├── codebook/
├── common/
├── company/
├── currencies/
├── facility/
├── payment/
├── process/
├── processingaction/
├── processingorder/
├── product/
├── productorder/
├── stockorder/
└── value_chain/
```

### 5.4 Converters JPA

| Converter | Mapeo | Notas |
|---|---|---|
| `MarkerListConverter` | `List<JourneyMarker>` ↔ `String` (delimitado por `;`) | Serialización custom, no depende de MySQL |

---

## 6. Queries con funciones MySQL-específicas

### 6.1 GROUP_CONCAT en JPQL

Archivo: `src/main/java/com/abelium/inatrace/components/groupstockorder/GroupStockOrderService.java`

```java
"SELECT new com.abelium.inatrace...ApiGroupStockOrder(" +
"GROUP_CONCAT(SO.id), " +              // ← función MySQL-específica en JPQL
"SO.productionDate AS date, ..."
```

Este es el **único uso** de `GROUP_CONCAT` en todo el codebase. Se usa mediante JPQL (no SQL nativo), habilitado por el registro en `CustomMySQLDialect`.

> **Equivalente en PostgreSQL**: `string_agg(cast(SO.id as text), ',')`

### 6.2 Queries nativas

**No existen** — ningún `@Query(nativeQuery = true)` en todo el proyecto.

### 6.3 TorpedoQuery

Se utiliza para construir queries JPQL de manera programática type-safe. No genera SQL nativo — es agnóstico de base de datos.

---

## 7. Auditoría (Hibernate Envers)

- Configurado vía `spring-data-envers`
- Crea automáticamente tablas `*_AUD` para auditar cambios
- Inicialización controlada por `spring.jpa.properties.hibernate.hbm2ddl.auto = update`
- Depende del dialecto Hibernate activo — requiere validación con dialecto PG

---

## 8. Resumen de Acoplamiento MySQL

| Elemento | Archivo | Severidad |
|---|---|---|
| Driver JDBC MySQL | `pom.xml` | 🔴 Bloqueante |
| `flyway-mysql` dependency | `pom.xml` | 🔴 Bloqueante |
| Dialect MySQL | `application.properties.template` | 🔴 Bloqueante |
| `CustomMySQLDialect` (GROUP_CONCAT) | `configuration/CustomMySQLDialect.java` | 🟡 Medio |
| `GROUP_CONCAT` en JPQL | `GroupStockOrderService.java` | 🟡 Medio |
| JDBC URL MySQL | `application.properties.template` | 🔴 Bloqueante |
| SQL migrations (audit pendiente) | `db/migrations/*.sql` | 🟡 Por confirmar |
| `GenerationType.IDENTITY` | `BaseEntity.java` | 🟢 Compatible con PG |
| No hay `nativeQuery = true` | todo el proyecto | 🟢 Favorable |
| Converters JPA | `MarkerListConverter.java` | 🟢 Agnóstico |
