# ¿Por qué existe `ProcessingClassificationBatch`?

Esta tabla es parte del módulo de **clasificación de camarón** 🦐. Su propósito es almacenar la información de cada **lote de clasificación** que se genera cuando una orden de procesamiento produce un stock order de salida en una instalación (Facility) de tipo clasificación.

## Contexto de negocio

Cuando el camarón llega a una planta de clasificación, se realiza un proceso donde:

1. **Se recibe producto** de un proveedor (libras recibidas, libras de basura, libras netas)
2. **Se clasifica por tallas** (16/20, 21/25, etc.), marcas, cajas y pesos
3. **Se genera un resultado** que puede ser:
   - **PROCESSED** (producto principal → va a congelación)
   - **REJECTED** (producto rechazado → va a descabezado para reproceso)
4. **Se calcula una liquidación de compra** con precios por libra, totales por línea, rendimiento, etc.

## ¿Por qué no se guarda directamente en `StockOrder`?

Porque la información de clasificación es **compleja y tiene estructura maestro-detalle**:

- **`ProcessingClassificationBatch`** = cabecera del lote (hora inicio/fin, orden de producción, tipo de congelación, máquina, marca, datos de liquidación, libras recibidas/procesadas/rechazadas, rendimiento)
- **`ProcessingClassificationBatchDetail`** = líneas de detalle por talla (marca, talla, cajas, peso por caja, tipo de proceso HEAD_ON/SHELL_ON, grado de calidad, precio por libra, total de línea)

Un solo `StockOrder` de salida tiene **un batch** con **múltiples líneas de detalle**. Meter todo esto como columnas planas en `StockOrder` sería inmanejable.

## Relación con el resto del sistema

```
ProcessingOrder (orden de procesamiento)
  └── StockOrder (salida/target)
        └── ProcessingClassificationBatch (1:1, cabecera de clasificación)
              └── ProcessingClassificationBatchDetail (1:N, detalle por talla)
```

El código en `@/Users/alvarosanchez/proyectos/giz/backend/src/main/java/com/abelium/inatrace/components/stockorder/StockOrderService.java:172-178` consulta estos batches cuando se pide la orden de procesamiento de un stock order, y los mapea a los campos de la API (`classificationStartTime`, `classificationDetails`, etc.).

## ¿Por qué se perdió?

Probablemente la tabla fue eliminada durante algún cambio de esquema o reset de base de datos, pero Flyway ya tenía registrada la migración original como ejecutada en `schema_version`. Por eso la migración nueva que creamos es **idempotente**: solo crea las tablas si no existen, evitando este problema en el futuro.

## Campos principales de `ProcessingClassificationBatch`

- `id`: Identificador único
- `targetStockOrderId`: FK al StockOrder de salida
- `startTime`/`endTime`: Horas de clasificación
- `productionOrder`: Orden de producción
- `freezingType`: Tipo de congelación (IQF, Block)
- `machine`: Máquina usada
- `brandHeader`: Marca general
- `providerName`: Nombre del proveedor
- `settlementNumber`: Número de liquidación
- `processType`: Tipo de proceso (HEAD_ON/SHELL_ON)
- `poundsReceived`/`poundsWaste`/`poundsNetReceived`: Libras del proveedor
- `poundsProcessed`/`yieldPercentage`: Resultado del proceso
- `totalAmount`/`averagePrice`: Liquidación financiera
- `outputType`: PROCESSED o REJECTED
- `poundsRejected`: Libras rechazadas
- `rejectedStockOrderId`: FK al StockOrder de producto rechazado

## Campos principales de `ProcessingClassificationBatchDetail`

- `id`: Identificador único
- `batchId`: FK al batch padre
- `brandDetail`: Marca específica de esta línea
- `size`: Talla (16/20, 21/25, etc.)
- `boxes`: Número de cajas
- `weightPerBox`: Peso por caja
- `weightFormat`: LB o KG
- `processType`: HEAD_ON o SHELL_ON
- `qualityGrade`: A, B, o C
- `presentationType`: SHELL_ON_A, BROKEN_VS, etc.
- `pricePerPound`: Precio por libra (USD)
- `lineTotal`: Total de esta línea (calculado)

Esta estructura permite generar reportes de liquidación de compra separados por tipo de proceso (entero vs cola) y calcular rendimientos precisos del proceso de clasificación.
