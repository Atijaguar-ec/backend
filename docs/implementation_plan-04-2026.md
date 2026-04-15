# Inventario: Cambios Cacao de `staging` a migrar a `agstack_dev`

La rama `agstack_dev` es el INATrace original modernizado (PostgreSQL + Keycloak).
La rama `staging` contiene todo el desarrollo de Cacao construido sobre el INATrace original (MySQL).
**Objetivo:** Migrar limpiamente la lógica de Cacao desde `staging` hacia `agstack_dev` para tener una versión premium con Postgres + Keycloak + Cacao.

---

## 1. Entidad `StockOrder` (Lotes/Entregas de Acopio)

**7 campos nuevos** que existen en `staging` pero NO en `agstack_dev`:

| Campo | Tipo | Propósito Cacao |
|---|---|---|
| `weekNumber` | `Integer` | Número de semana (1-53) de la entrega para trazabilidad estacional |
| `parcelLot` | `String(255)` | Lote de la parcela de origen del cacao |
| `variety` | `String(255)` | Variedad genética (Nacional, CCN51, etc.) |
| `organicCertification` | `String(255)` | Detalle de certificación orgánica del lote |
| `moisturePercentage` | `BigDecimal` | Porcentaje de humedad medido en bodega |
| `moistureWeightDeduction` | `BigDecimal` | Deducción de peso por humedad |
| `netQuantity` | `BigDecimal(38,2)` | Peso neto real tras descuentos (tara, daño, humedad) |
| `finalPriceDiscount` | `BigDecimal(38,2)` | Descuento al precio final del productor |

**Archivos afectados:**
- [StockOrder.java](file:///Users/alvarogeovani/proyectos/giz/backend/src/main/java/com/abelium/inatrace/db/entities/stockorder/StockOrder.java) — Entidad JPA
- [ApiStockOrder.java](file:///Users/alvarogeovani/proyectos/giz/backend/src/main/java/com/abelium/inatrace/components/stockorder/api/ApiStockOrder.java) — DTO API
- [StockOrderMapper.java](file:///Users/alvarogeovani/proyectos/giz/backend/src/main/java/com/abelium/inatrace/components/stockorder/mappers/StockOrderMapper.java) — Mapeo entity↔API (3 métodos)
- [StockOrderService.java](file:///Users/alvarogeovani/proyectos/giz/backend/src/main/java/com/abelium/inatrace/components/stockorder/StockOrderService.java) — Lógica de negocio (setters + `calculateNetQuantity()`)

---

## 2. Entidad `ApiGroupStockOrder` (Reportes Agrupados / Excel)

**6 campos nuevos** para el agrupamiento de lotes en reportes:

| Campo | Tipo | Propósito Cacao |
|---|---|---|
| `weekNumber` | `Integer` | Semana en agrupación |
| `parcelLot` | `String` | Parcela en agrupación |
| `variety` | `String` | Variedad en agrupación |
| `organicCertification` | `String` | Certificación en agrupación |
| `facilityName` | `String` | Nombre del Centro de Acopio |
| `farmerName` | `String` | Nombre del Productor |

**Archivos afectados:**
- [ApiGroupStockOrder.java](file:///Users/alvarogeovani/proyectos/giz/backend/src/main/java/com/abelium/inatrace/components/groupstockorder/api/ApiGroupStockOrder.java) — DTO
- [GroupStockOrderService.java](file:///Users/alvarogeovani/proyectos/giz/backend/src/main/java/com/abelium/inatrace/components/groupstockorder/GroupStockOrderService.java) — Queries JPQL + lógica de Excel export

---

## 3. Entidad `Facility` (Áreas/Centros de Acopio)

**3 campos nuevos**:

| Campo | Tipo | Propósito Cacao |
|---|---|---|
| `level` | `Integer` | Nivel jerárquico del área dentro de la cadena |
| `displayFinalPriceDiscount` | `Boolean` | Flag: ¿Mostrar campo de descuento en UI? |
| `displayMoisturePercentage` | `Boolean` | Flag: ¿Mostrar campo de humedad en UI? |

**Archivos afectados:**
- [Facility.java](file:///Users/alvarogeovani/proyectos/giz/backend/src/main/java/com/abelium/inatrace/db/entities/facility/Facility.java) — Entidad JPA
- [ApiFacility.java](file:///Users/alvarogeovani/proyectos/giz/backend/src/main/java/com/abelium/inatrace/components/facility/api/ApiFacility.java) — DTO API
- [FacilityMapper.java](file:///Users/alvarogeovani/proyectos/giz/backend/src/main/java/com/abelium/inatrace/components/facility/FacilityMapper.java) — Mapeo

---

## 4. Entidad `FacilityType` (Tipos de Área)

**1 campo nuevo**:

| Campo | Tipo | Propósito Cacao |
|---|---|---|
| `order` | `Integer` | Orden de despliegue visual en el UI |

**Archivos afectados:**
- [FacilityType.java](file:///Users/alvarogeovani/proyectos/giz/backend/src/main/java/com/abelium/inatrace/db/entities/codebook/FacilityType.java) — Entidad JPA

---

## 5. Entidad `FarmInformation` (Información de Finca)

**1 campo nuevo**:

| Campo | Tipo | Propósito Cacao |
|---|---|---|
| `maxProductionQuantity` | `BigDecimal` | Capacidad máxima de producción declarada por la finca |

**Archivos afectados:**
- [FarmInformation.java](file:///Users/alvarogeovani/proyectos/giz/backend/src/main/java/com/abelium/inatrace/db/entities/common/FarmInformation.java) — Entidad JPA

---

## 6. Entidad `UserCustomer` (Productores/Clientes)

**3 campos nuevos** para personas jurídicas:

| Campo | Tipo | Propósito Cacao |
|---|---|---|
| `personType` | `PersonType` (enum) | NATURAL o LEGAL (persona natural vs. jurídica) |
| `companyName` | `String` | Razón social (para persona jurídica) |
| `legalRepresentative` | `String` | Representante legal |

**Archivos afectados:**
- [UserCustomer.java](file:///Users/alvarogeovani/proyectos/giz/backend/src/main/java/com/abelium/inatrace/db/entities/common/UserCustomer.java) — Entidad JPA
- [ApiUserCustomer.java](file:///Users/alvarogeovani/proyectos/giz/backend/src/main/java/com/abelium/inatrace/components/company/api/ApiUserCustomer.java) — DTO API

**Nuevo enum requerido:**
- `PersonType.java` — `NATURAL`, `LEGAL`

---

## 7. Nuevas Entidades Completas (No existen en `agstack_dev`)

### 7.1 `CertificationType` + `CertificationTypeTranslation`
Catálogo de tipos de certificaciones orgánicas y sellos ambientales.

| Archivos | Tipo |
|---|---|
| `CertificationType.java` | Entidad JPA |
| `CertificationTypeTranslation.java` | Entidad JPA (i18n) |
| `CertificationTypeController.java` | REST Controller |
| `CertificationTypeService.java` | Service |
| `CertificationTypeMapper.java` | Mapper |
| `ApiCertificationType.java` | DTO |
| `ApiCertificationTypeTranslation.java` | DTO |

**Nuevos enums requeridos:**
- `CertificationCategory.java` — `CERTIFICATE`, `SEAL`
- `CertificationStatus.java` — `ACTIVE`, `INACTIVE`
- `CodebookStatus.java` — Estado general de codebooks

### 7.2 `CompanyProcessingAction`
Configuración específica por empresa para acciones de procesamiento (habilitar/deshabilitar, renombrar, reordenar).

| Archivos | Tipo |
|---|---|
| `CompanyProcessingAction.java` | Entidad JPA |
| `CompanyProcessingActionController.java` | REST Controller |
| `CompanyProcessingActionService.java` | Service |
| `CompanyProcessingActionMapper.java` | Mapper |
| `ApiCompanyProcessingAction.java` | DTO |

---

## 8. Lógica de Negocio Adicional en `StockOrderService`

El servicio en `staging` incluye lógica de cálculo sumamente importante que no existe en `agstack_dev`:

- **`calculateNetQuantity()`**: Función que calcula el peso neto aplicando fórmula: `Net = (Bruto - Tara - PesoDañado) × (Humedad / 100)`
- **Descuento de precio final**: `cost = cost.subtract(entity.getFinalPriceDiscount())`
- **Exportación Excel**: `GroupStockOrderService` incluye columnas adicionales de semana, variedad, parcela, certificación orgánica, y nombre del agricultor en la generación del reporte.

---

## 9. Migraciones Flyway de Cacao (Referencia)

Estos son los scripts SQL que crearon los campos anteriores en el esquema MySQL de `staging`. **No los copiaremos directamente** sino que escribiremos **1 nueva migración consolidada para PostgreSQL**:

| Migración SQL (staging - MySQL) | Qué hacía |
|---|---|
| `V2025_08_20_08_25__Add_order_to_FacilityType.sql` | Agrega `order` a FacilityType |
| `V2025_08_20_09_47__Create_CompanyProcessingAction_table.sql` | Crea tabla CompanyProcessingAction |
| `V2025_09_24_23_35__Add_maxProductionQuantity_to_FarmInformation.sql` | Agrega capacidad máxima finca |
| `V2025_09_25_11_15__Add_weekNumber_to_StockOrder.sql` | Agrega número de semana |
| `V2025_10_29_19_35__Add_parcel_variety_organicCert_to_StockOrder.sql` | Agrega parcela, variedad, certificación |
| `V2025_10_30_02_00__Add_moisture_percentage_fields.sql` | Agrega campos de humedad |
| `V2025_10_31_00_00__Create_CertificationType_table.sql` | Crea catálogo certificaciones |
| `V2025_10_31_01_00__Add_net_quantity_field.sql` | Agrega peso neto |
| `V2025_11_04_22_00__Add_level_to_Facility.sql` | Agrega nivel al área |
| `V2025_11_06_00_00__Add_final_price_discount.sql` | Agrega descuento precio final |
| `V2025_11_15_01_00__Add_person_type_fields_to_UserCustomer.sql` | Agrega persona jurídica a UserCustomer |

---

## Resumen Numérico

| Categoría | Cantidad |
|---|---|
| **Campos nuevos a agregar en entidades existentes** | 15 |
| **Entidades completamente nuevas** | 3 (CertificationType, CertificationTypeTranslation, CompanyProcessingAction) |
| **Enums nuevos** | 4 (PersonType, CertificationCategory, CertificationStatus, CodebookStatus) |
| **Controllers nuevos** | 2 (CertificationType, CompanyProcessingAction) |
| **Services nuevos** | 2 |
| **Mappers nuevos** | 2 |
| **DTOs nuevos** | 4 |
| **Lógica de negocio nueva** | calculateNetQuantity(), descuento, Excel export |
| **Migración Flyway PostgreSQL a crear** | 1 consolidada |
