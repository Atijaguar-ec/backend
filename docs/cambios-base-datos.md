# Cambios en Base de Datos - Migraciones

Este documento detalla todas las migraciones de base de datos ejecutadas, incluyendo su descripción e impacto en el sistema.

## Tabla de Migraciones 2025-2026

| Archivo de Migración | Descripción | Impacto |
|---------------------|-------------|---------|
| `V2025_08_20_08_25__Add_order_to_FacilityType.sql` | Agrega campo `order` a `FacilityType` para ordenamiento dinámico de tipos de áreas | **Tabla**: `FacilityType`<br>**Cambios**: Nueva columna `order` (INT), índice `idx_facility_type_order`<br>**Datos**: Inicializa `order = id` para registros existentes |
| `V2025_08_20_09_47__Create_CompanyProcessingAction_table.sql` | Crea tabla `CompanyProcessingAction` para configuración de acciones de procesamiento específicas por compañía | **Tabla**: Nueva tabla `CompanyProcessingAction`<br>**Relaciones**: FK con `Company` y `ProcessingAction`<br>**Índices**: 2 índices de rendimiento<br>**Datos**: Inicializa combinaciones existentes de empresa-acción |
| `V2025_08_22_16_15__Fix_Payment_RecipientType_Enum.sql` | Cambia `Payment.recipientType` de ENUM a VARCHAR(32) para evitar deadlocks y mejorar flexibilidad | **Tabla**: `Payment`<br>**Cambios**: Modifica tipo de columna `recipientType`<br>**Performance**: Agrega índice `idx_payment_recipient_type` |
| `V2025_08_26_10_30__Drop_unique_index_on_Transaction_sourceStockOrder.sql` | Elimina índice único en `Transaction.sourceStockOrder` | **Tabla**: `Transaction`<br>**Índices**: Elimina restricción única |
| `V2025_08_26_10_35__Create_CompanyProcessingAction_if_missing.sql` | Crea tabla `CompanyProcessingAction` si no existe (migración de respaldo) | **Tabla**: `CompanyProcessingAction`<br>**Propósito**: Garantiza existencia de tabla en ambientes donde migración anterior falló |
| `V2025_08_26_10_40__Add_timestamps_to_CompanyProcessingAction.sql` | Agrega campos de auditoría timestamp a `CompanyProcessingAction` | **Tabla**: `CompanyProcessingAction`<br>**Cambios**: Columnas `creationTimestamp`, `updateTimestamp` |
| `V2025_08_26_10_45__Fix_unique_index_on_Transaction_sourceStockOrder.sql` | Corrige índice único en `Transaction.sourceStockOrder` | **Tabla**: `Transaction`<br>**Índices**: Recrea índice con configuración correcta |
| `V2025_08_26_10_50__Fix_unique_index_on_Transaction_inputMeasureUnitType.sql` | Corrige índice único en `Transaction.inputMeasureUnitType` | **Tabla**: `Transaction`<br>**Índices**: Ajusta índice para mejor rendimiento |
| `V2025_09_24_23_35__Add_maxProductionQuantity_to_FarmInformation.sql` | Agrega campo de producción máxima en quintales (qq) a información de fincas | **Tabla**: `UserCustomer`<br>**Cambios**: Nueva columna `farm_maxProductionQuantity` (DECIMAL 19,2)<br>**Funcionalidad**: Control de producción máxima para AgStack |
| `V2025_09_25_11_15__Add_weekNumber_to_StockOrder.sql` | Agrega número de semana (1-53) para trazabilidad de entregas de cacao | **Tabla**: `StockOrder`<br>**Cambios**: Columna `weekNumber` (INT), índice `idx_stock_order_week_number`<br>**Validación**: Constraint CHECK para rango 1-53 |
| `V2025_10_29_19_35__Add_parcel_variety_organicCert_to_StockOrder.sql` | Agrega campos de lote, variedad y certificación orgánica a órdenes | **Tabla**: `StockOrder`<br>**Cambios**: `parcelLot` (VARCHAR 8), `variety` (VARCHAR 32), `organicCertification` (VARCHAR 64) |
| `V2025_10_30_02_00__Add_moisture_percentage_fields.sql` | Agrega campos de porcentaje de humedad y deducción de peso | **Tablas**: `Facility`, `StockOrder`<br>**Cambios Facility**: `displayMoisturePercentage` (BIT)<br>**Cambios StockOrder**: `moisturePercentage` (DECIMAL 5,2), `moistureWeightDeduction` (DECIMAL 38,2) |
| `V2025_10_31_00_00__Create_CertificationType_table.sql` | Crea catálogo de tipos de certificación con soporte multiidioma | **Tablas**: Nuevas `CertificationType` y `CertificationTypeTranslation`<br>**Datos**: Seeds iniciales (FairTrade, Biosuisse, SPP) con traducciones ES |
| `V2025_10_31_01_00__Add_net_quantity_field.sql` | Agrega campo de cantidad neta después de todas las deducciones | **Tabla**: `StockOrder`<br>**Cambios**: `netQuantity` (DECIMAL 38,2) |
| `V2025_10_31_01_01__Recalculate_existing_net_quantities.sql` | Recalcula cantidades netas para registros existentes | **Tabla**: `StockOrder`<br>**Datos**: Actualiza `netQuantity` basado en fórmula de deducciones |
| `V2025_11_04_22_00__Add_level_to_Facility.sql` | Agrega nivel para ordenamiento personalizado de áreas | **Tabla**: `Facility`<br>**Cambios**: Columna `level` (INT)<br>**Datos**: Inicializa con `FacilityType.order` |
| `V2025_11_06_00_00__Add_final_price_discount.sql` | Agrega descuento en precio final | **Tablas**: `StockOrder`, `Facility`<br>**Cambios**: `finalPriceDiscount` (DECIMAL 38,2) y `displayFinalPriceDiscount` (BIT) |
| `V2025_11_11_01_00__Add_shrimp_specific_fields.sql` | **🦐 CAMARÓN**: Campos específicos para entregas normales de camarón | **Tabla**: `StockOrder`<br>**Cambios**: `numberOfGavetas`, `number_of_bines`, `numberOfPiscinas`, `guiaRemisionNumber` |
| `V2025_11_11_02_00__Add_laboratory_fields.sql` | **🦐 CAMARÓN**: Campos para entregas de laboratorio | **Tabla**: `StockOrder`<br>**Cambios**: `sampleNumber` (VARCHAR 100), `receptionTime` (TIME) |
| `V2025_11_11_02_01__Add_is_laboratory_field.sql` | **🦐 CAMARÓN**: Marca áreas de tipo laboratorio | **Tabla**: `Facility`<br>**Cambios**: `isLaboratory` (BIT) |
| `V2025_11_11_03_00__Add_sensorial_analysis_quality_fields.sql` | **🦐 CAMARÓN**: Crea tabla para análisis de laboratorio completo | **Tabla**: Nueva `LaboratoryAnalysis`<br>**Campos**: Análisis sensorial (crudo/cocido), microbiológico, químico, PCR<br>**Relaciones**: FK con `StockOrder` y `User` |
| `V2025_11_12_01_00__Add_quality_document_field.sql` | **🦐 CAMARÓN**: Referencia a documento PDF de calidad de laboratorio | **Tabla**: `StockOrder`<br>**Cambios**: `quality_document_id` (BIGINT)<br>**Relaciones**: FK con `Document` |
| `V2025_11_13_01_00__Add_classification_process.sql` | **🦐 CAMARÓN**: Soporte completo para proceso de clasificación | **Tablas**: Nuevas `ProcessingClassificationBatch` y `ProcessingClassificationBatchDetail`<br>**Cambios Facility**: `isClassificationProcess` (BIT)<br>**Funcionalidad**: Lotes de clasificación con detalles (tallas, cajas, pesos) |
| `V2025_11_13_02_00__Add_freezing_process.sql` | **🦐 CAMARÓN**: Flag para áreas con proceso de congelamiento | **Tabla**: `Facility`<br>**Cambios**: `isFreezingProcess` (BIT) |
| `V2025_11_15_01_00__Add_person_type_fields_to_UserCustomer.sql` | Agrega tipo de persona (natural/jurídica) a clientes | **Tabla**: `UserCustomer`<br>**Cambios**: `personType` (ENUM), `companyName` (VARCHAR 255), `legalRepresentative` (VARCHAR 255) |
| `V2025_11_16_01_00__Add_metabisulfite_and_approval_fields_to_LaboratoryAnalysis.sql` | **🦐 CAMARÓN**: Campos de metabisulfito y aprobación en análisis | **Tabla**: `LaboratoryAnalysis`<br>**Cambios**: `metabisulfiteLevelAcceptable` (TINYINT), `approvedForPurchase` (TINYINT) |
| `V2025_11_17_01_00__Add_destination_stock_order_to_LaboratoryAnalysis.sql` | **🦐 CAMARÓN**: Vincula análisis aprobado con orden de uso (evita reutilización) | **Tabla**: `LaboratoryAnalysis`<br>**Cambios**: `destinationStockOrder_id` (BIGINT)<br>**Relaciones**: FK con `StockOrder` |
| `V2025_11_18_01_00__Drop_legacy_snake_case_shrimp_fields.sql` | **🦐 CAMARÓN**: Limpieza de campos legacy en snake_case | **Tabla**: `StockOrder`<br>**Cambios**: Elimina/renombra campos duplicados (`number_of_*` → camelCase) |
| `V2025_11_19_01_00__Add_shrimp_processing_fields_to_StockOrder.sql` | **🦐 CAMARÓN**: Campos detallados de procesos de camarón (corte, tratamiento, túnel, lavado) | **Tabla**: `StockOrder`<br>**Cambios**: 14 campos nuevos para procesos de corte, tratamiento químico, túnel de congelación y lavado<br>**Incluye**: Fechas, temperaturas, tipos, pesos, proveedores |
| `V2025_11_19_03_00__Fix_Facility_displayFinalPriceDiscount.sql` | Corrige lógica invertida en migración anterior de `displayFinalPriceDiscount` | **Tabla**: `Facility`<br>**Cambios**: Asegura existencia de columna `displayFinalPriceDiscount` |
| `V2026_01_15_10_00__Cleanup_Shrimp_Tables_For_Non_Shrimp_Deployments.java` | **🧹 LIMPIEZA CONDICIONAL**: Elimina tablas y columnas de camarón en despliegues NO-SHRIMP | **Condición**: Solo ejecuta si `INATrace.product.type ≠ SHRIMP`<br>**Tablas eliminadas**: 23 tablas de camarón (catálogos, procesamiento, análisis)<br>**Columnas eliminadas**: 37+ columnas específicas de camarón en `StockOrder` y `Facility`<br>**Propósito**: Mantener esquema limpio por tipo de producto |

## Resumen de Impacto por Módulo

### 🦐 **Industria del Camarón** (11 migraciones)
Las migraciones más significativas se concentran en soporte para la cadena de valor del camarón, incluyendo:
- Gestión de entregas y laboratorio
- Análisis sensoriales, microbiológicos y químicos
- Procesos de clasificación y congelamiento
- Trazabilidad completa de procesos productivos
- **🧹 Limpieza condicional**: Eliminación automática de objetos de camarón en despliegues de otros productos (COCOA, COFFEE)

**Tablas nuevas**: `LaboratoryAnalysis`, `ProcessingClassificationBatch`, `ProcessingClassificationBatchDetail`
**⚠️ Nota importante**: Las tablas y columnas de camarón solo existen en despliegues con `INATrace.product.type=SHRIMP`

### 📊 **Trazabilidad y Certificación** (7 migraciones)
- Control de producción máxima
- Certificaciones orgánicas y sellos
- Número de semana para cacao
- Lotes, variedades y certificaciones

**Tablas nuevas**: `CertificationType`, `CertificationTypeTranslation`

### ⚙️ **Configuración y Performance** (8 migraciones)
- Ordenamiento dinámico de áreas
- Acciones de procesamiento por compañía
- Optimización de índices
- Corrección de tipos de datos (ENUM → VARCHAR)

**Tablas nuevas**: `CompanyProcessingAction`

### 💰 **Cálculos y Deducciones** (6 migraciones)
- Humedad y deducciones de peso
- Cantidad neta después de deducciones
- Descuentos en precio final

## Características Técnicas Comunes

✅ **Todas las migraciones**:
- Son **idempotentes** (verifican existencia antes de crear/modificar)
- Usan `INFORMATION_SCHEMA` para validaciones condicionales
- Emplean `PREPARE/EXECUTE` para SQL dinámico compatible con MySQL 8.0
- Incluyen comentarios descriptivos en columnas
- No destruyen datos existentes (excepto limpieza condicional)

## Migraciones Condicionales por Tipo de Producto

⚙️ **Sistema de migraciones basado en `INATrace.product.type`**:

Algunas migraciones Java verifican el tipo de producto antes de ejecutarse:

```java
String productType = environment.getProperty("INATrace.product.type", "COFFEE");

// Migraciones específicas de SHRIMP
if ("SHRIMP".equalsIgnoreCase(productType)) {
    // Crear tablas y datos de camarón
}

// Limpieza para NO-SHRIMP
if (!"SHRIMP".equalsIgnoreCase(productType)) {
    // Eliminar objetos de camarón
}
```

**Tipos de producto soportados**:
- `COFFEE` - Café
- `COCOA` / `CACAO` - Cacao
- `SHRIMP` / `CAMARON` - Camarón

**Despliegues por compañía**:
- **UNOCACE**: `COCOA` → Sin tablas de camarón
- **DUFER**: `SHRIMP` → Con todas las tablas de camarón
- **Otros**: Configuración en `application.properties` o variable `INATRACE_PRODUCT_TYPE`

## Notas de Compatibilidad

- **MySQL 8.0+**: Todas las migraciones son compatibles
- **Flyway**: Checksums validados en `schema_version`
- **Hibernate**: Configurado en modo `validate` para consistencia
- **Naming Strategy**: PhysicalNamingStrategyStandardImpl (camelCase en BD)
- **Migraciones Java**: Soporte para lógica condicional basada en configuración
