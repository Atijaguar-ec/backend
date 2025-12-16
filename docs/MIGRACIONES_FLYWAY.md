# Sistema de Migraciones Flyway - INATrace Backend

## Descripción General

El backend de INATrace utiliza **Flyway** para gestionar las migraciones de base de datos. El sistema soporta dos tipos de migraciones:

1. **Migraciones SQL** - Archivos `.sql` en `src/main/resources/db/migrations/`
2. **Migraciones Java (JPA)** - Clases Java que implementan `JpaMigration` en `src/main/java/com/abelium/inatrace/db/migrations/`

## Arquitectura de Migraciones

### Flujo de Ejecución

```
1. flywayInitializer          → Ejecuta migraciones SQL (crea tablas)
2. Hibernate                  → Valida el schema (ddl-auto=validate)
3. delayedFlywayInitializer   → Ejecuta migraciones Java (necesita EntityManager)
```

### Archivos Clave

| Archivo | Descripción |
|---------|-------------|
| `MigrationsConfiguration.java` | Configura los beans de Flyway |
| `JpaMigrationStrategy.java` | Estrategia para ejecutar migraciones Java |
| `JpaMigrationResolver.java` | Resuelve y escanea clases JpaMigration |
| `DelayedFlywayMigrationInitializer.java` | Ejecuta migraciones después de EntityManagerFactory |
| `JpaMigration.java` | Interfaz que deben implementar las migraciones Java |

## Convención de Nombres

### Migraciones SQL
```
V{año}_{mes}_{día}_{hora}_{minuto}__{Descripcion}.sql
Ejemplo: V2025_12_15_04_30__Fix_missing_Facility_columns.sql
```

### Migraciones Java
```
V{año}_{mes}_{día}_{hora}_{minuto}__{Descripcion}.java
Ejemplo: V2025_11_19_05_00__Prefill_SemiProducts.java
```

## Configuración de Producción

### application.properties

```properties
# Flyway habilitado
spring.flyway.enabled = true

# Configuración de migraciones
spring.flyway.baseline-on-migrate = true
spring.flyway.baseline-version = 0
spring.flyway.out-of-order = true
spring.flyway.validate-on-migrate = true
spring.flyway.ignore-missing-migrations = true
spring.flyway.table = schema_version
spring.flyway.locations = com.abelium.inatrace.db.migrations,classpath:/db/migrations

# Hibernate en modo validación (NO crea/modifica tablas)
spring.jpa.hibernate.ddl-auto = validate
spring.jpa.properties.hibernate.hbm2ddl.auto = validate
```

## Tipos de Producto Soportados

El sistema soporta los siguientes tipos de producto:

- **COCOA** - Cacao (configuración por defecto para UNOCACE)
- **COFFEE** - Café

La configuración se realiza mediante:
```properties
INATrace.product.type = ${INATRACE_PRODUCT_TYPE:COCOA}
```

## Migraciones Java Importantes

### Inicialización de Datos

| Migración | Descripción |
|-----------|-------------|
| `V2020_03_27_15_00__Prefill_Countries` | Carga países desde CSV |
| `V2021_08_11_11_33__Prefill_FacilityTypes` | Tipos de instalación según producto |
| `V2021_08_11_11_41__Prefill_MeasureUnitTypes` | Unidades de medida según producto |
| `V2023_03_09_14_33__Update_Value_Chains_Add_Coffee_Product_Type` | Tipo de producto para cadenas de valor |
| `V2025_11_19_02_00__Create_Default_Admin_Company` | Compañía administradora por defecto |
| `V2025_11_19_05_00__Prefill_SemiProducts` | Semi-productos de cacao |
| `V2025_11_20_01_00__Link_SemiProducts_To_ValueChains` | Vincula semi-productos a cadenas de valor |
| `V2025_11_20_02_00__Link_FacilityTypes_To_ValueChains` | Vincula tipos de instalación |
| `V2025_11_20_02_01__Link_MeasureUnitTypes_To_ValueChains` | Vincula unidades de medida |

## Crear una Nueva Migración

### Migración SQL

1. Crear archivo en `src/main/resources/db/migrations/`
2. Usar convención de nombre: `V{timestamp}__{descripcion}.sql`
3. Hacer cambios idempotentes (verificar existencia antes de crear)

Ejemplo:
```sql
-- Verificar si la columna existe antes de agregarla
SET @col_exists = (SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'MiTabla' 
    AND COLUMN_NAME = 'miColumna');

SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE MiTabla ADD COLUMN miColumna VARCHAR(255)', 
    'SELECT "Column already exists"');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
```

### Migración Java

1. Crear clase en `src/main/java/com/abelium/inatrace/db/migrations/`
2. Implementar interfaz `JpaMigration`
3. Usar convención de nombre: `V{timestamp}__{Descripcion}.java`

Ejemplo:
```java
public class V2025_12_16_10_00__Mi_Migracion implements JpaMigration {
    
    @Override
    public void migrate(EntityManager em, Environment environment) throws Exception {
        String productType = environment.getProperty("INATrace.product.type", "COFFEE");
        
        if ("COCOA".equalsIgnoreCase(productType)) {
            // Lógica específica para cacao
        }
    }
}
```

## Solución de Problemas

### Error: "Schema-validation: missing table"

**Causa**: Hibernate valida antes de que Flyway cree las tablas.

**Solución**: Verificar que `MigrationsConfiguration.flywayInitializer` ejecute las migraciones SQL (no debe ser vacío).

### Error: "Migration failed"

**Causa**: Una migración SQL o Java falló durante la ejecución.

**Solución**:
1. Revisar logs para identificar el error
2. Corregir la migración
3. Limpiar la entrada fallida en `schema_version`:
   ```sql
   DELETE FROM schema_version WHERE success = 0;
   ```

### Las migraciones Java no se ejecutan

**Verificar**:
1. La clase implementa `JpaMigration`
2. Tiene constructor sin argumentos
3. El nombre sigue la convención `V{version}__{descripcion}`
4. `spring.flyway.locations` incluye `com.abelium.inatrace.db.migrations`

## Notas Importantes

- **Nunca modificar** una migración ya ejecutada en producción
- Las migraciones deben ser **idempotentes** cuando sea posible
- Usar `hibernate.ddl-auto=validate` en producción
- Las migraciones Java se ejecutan **después** de las SQL
- El orden de ejecución se determina por la versión (timestamp)
