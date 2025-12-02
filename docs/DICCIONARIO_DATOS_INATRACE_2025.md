# Diccionario de Datos 2025 · INATrace Ecuador

Este documento registra los campos agregados o modificados durante las migraciones 2025. Sirve como referencia rápida para equipos funcionales, técnicos y de soporte.

## Convenciones

| Columna       | Significado                                                           |
|---------------|-----------------------------------------------------------------------|
| Tabla         | Nombre de la tabla en MySQL                                           |
| Campo         | Nombre del campo creado/modificado                                    |
| Tipo          | Tipo y longitud                                                       |
| Migración     | Archivo Flyway responsable del cambio                                 |
| Descripción   | Uso funcional del campo                                               |

---

## 1. Cacao · Producción y calidad

| Tabla         | Campo                         | Tipo           | Migración                                      | Descripción                                                                 |
|---------------|-------------------------------|----------------|------------------------------------------------|-----------------------------------------------------------------------------|
| `UserCustomer`| `farm_maxProductionQuantity`  | DECIMAL(19,2)  | V2025_09_24_23_35                              | Capacidad máxima estimada de producción (quintales).                        |
| `StockOrder`  | `weekNumber`                  | INT            | V2025_09_25_11_15                              | Semana (1–53) asociada a la entrega (trazabilidad estacional).             |
| `StockOrder`  | `parcelLot`                   | VARCHAR        | V2025_10_29_19_35                              | Identificador de parcela/lote de origen.                                   |
| `StockOrder`  | `variety`                     | VARCHAR        | V2025_10_29_19_35                              | Variedad de cacao declarada por el productor.                              |
| `StockOrder`  | `organicCertification`        | VARCHAR        | V2025_10_29_19_35                              | Certificación orgánica asociada a la entrega.                              |
| `StockOrder`  | `moisturePercentage`          | DECIMAL        | V2025_10_30_02_00                              | % de humedad aplicado al lote.                                             |
| `StockOrder`  | `moistureWeightDeduction`     | DECIMAL        | V2025_10_30_02_00                              | Peso deducido por humedad.                                                 |
| `StockOrder`  | `netQuantity`                 | DECIMAL        | V2025_10_31_01_00 / 01_01                      | Cantidad neta tras deducciones; recalculada para datos históricos.          |
| `CertificationType` | *(tabla completa)*      | Catálogo       | V2025_10_31_00_00                              | Tipos de certificación (orgánico, comercio justo, etc.).                   |

---

## 2. Camarón · Procesos industriales

| Tabla         | Campo                                   | Tipo            | Migración                                      | Descripción                                                                 |
|---------------|-----------------------------------------|-----------------|------------------------------------------------|-----------------------------------------------------------------------------|
| `Facility`    | `isCuttingProcess`                      | BIT(1)          | V2025_11_19_04_00                              | Indica si la instalación ejecuta procesos de corte.                        |
| `Facility`    | `isClassificationProcess`               | BIT(1)          | V2025_11_13_01_00                              | Marca facilities dedicadas a clasificación de camarón.                     |
| `StockOrder`  | `numberOfGavetas` / `numberOfBines`     | INT / VARCHAR   | V2025_11_11_01_00                              | Control de contenedores usados al recibir camarón.                         |
| `StockOrder`  | `numberOfPiscinas`                      | VARCHAR         | V2025_11_11_01_00                              | Piscinas o pools asociados a la cosecha.                                   |
| `StockOrder`  | `guiaRemisionNumber`                    | VARCHAR         | V2025_11_11_01_00                              | Número de guía de remisión.                                                |
| `StockOrder`  | `cuttingType`, `cuttingEntryDate`, `cuttingExitDate`, `cuttingTemperatureControl` | VARCHAR / DATE | V2025_11_19_01_00 | Datos de proceso de corte (tipos, fechas, temperatura).                    |
| `StockOrder`  | `treatmentType`, `treatmentEntryDate`, `treatmentExitDate`, `treatmentTemperatureControl`, `treatmentChemicalUsed` | VARCHAR / DATE | V2025_11_19_01_00 | Control de tratamientos (químicos, tiempos).                               |
| `StockOrder`  | `tunnelProductionDate`, `tunnelExpirationDate`, `tunnelNetWeight`, `tunnelSupplier`, `tunnelFreezingType`, `tunnelEntryDate`, `tunnelExitDate` | DATE / DECIMAL / VARCHAR | V2025_11_19_01_00 | Trazabilidad de túnel de congelación.                                      |
| `ProcessingClassificationBatch` | tabla nueva           | Ver SQL         | V2025_11_13_01_00                              | Encabezado de clasificaciones (orden, máquina, tiempos).                   |
| `ProcessingClassificationBatchDetail` | tabla nueva     | Ver SQL         | V2025_11_13_01_00                              | Detalle de tallas, cajas y pesos por clasificación.                        |

---

## 3. Laboratorio y Calidad

| Tabla             | Campo                                  | Tipo           | Migración                                      | Descripción                                                             |
|-------------------|----------------------------------------|----------------|------------------------------------------------|-------------------------------------------------------------------------|
| `LaboratoryAnalysis` | `sensorialRawOdor`, `sensorialRawTaste`, `sensorialRawColor`, etc. | VARCHAR | V2025_11_11_03_00 | Registro sensorial (olor, sabor, color en crudo/cocido).                  |
| `LaboratoryAnalysis` | `metabisulfiteLevelAcceptable`, `approvedForPurchase` | BIT/VARCHAR | V2025_11_16_01_00 | Control de nivel de metabisulfito y aprobación de compra.               |
| `LaboratoryAnalysis` | `destinationStockOrder_id`          | FK             | V2025_11_17_01_00                              | Relación análisis → orden de stock destino.                              |
| `StockOrder`        | `qualityDocument_id`                 | FK             | V2025_11_12_01_00                              | Documento de calidad asociado (certificados, reportes).                  |
| `StockOrder`        | `isLaboratory`                       | BIT(1)        | V2025_11_11_02_01                              | Distingue registros de laboratorio vs comerciales.                       |

---

## 4. Identidad y Comercial

| Tabla         | Campo                     | Tipo          | Migración                     | Descripción                                                      |
|---------------|---------------------------|---------------|-------------------------------|------------------------------------------------------------------|
| `UserCustomer`| `personType`, `documentType`, etc. | VARCHAR | V2025_11_15_01_00            | Clasificación de actores (persona natural/jurídica, documentos). |
| `StockOrder`  | `finalPriceDiscount`, `displayFinalPriceDiscount` | DECIMAL/BIT | V2025_11_06_00_00 / 11_19_03_00 | Control integrado de descuentos aplicados.                       |

---

## 5. Referencias

- Carpeta SQL: `src/main/resources/db/migrations/`
- Carpeta JPA: `src/main/java/com/abelium/inatrace/db/migrations/`
- Entidades afectadas: revisar `com.abelium.inatrace.db.entities.*`

> **Nota**: Todos los cambios siguen el patrón Flyway idempotente (verificación en `INFORMATION_SCHEMA` + `PREPARE/EXECUTE`).
