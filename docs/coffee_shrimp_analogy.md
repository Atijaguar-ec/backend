# ☕🦐 Analogía: INATrace Core (Café) vs. shrimpMfe (Camarón)

## Tabla de Equivalencia de Conceptos

| Concepto del Core (Café) | Módulo / Componente Core | Equivalente Camarón (Dufer) | Módulo shrimpMfe | Estado |
|---|---|---|---|---|
| **Empresa** / Company | `company-detail/` | **Dufer Cia. Ltda.** | Hardcoded en layout | ✅ Seedeado |
| **Producto** / Product | `m-product/product-list/` | **Camarón Blanco** (Litopenaeus vannamei) | Seedeado en DB | ✅ Seedeado |
| **Cadena de Valor** / Value Chain | Product → Value Chain config | **Camarón Dufer** (ValueChain) | Seedeado en DB | ✅ Seedeado |
| **Tipo de Producto** / Product Type | Settings → Types | **SHRIMP** (ProductType) | Seedeado en DB | ✅ Seedeado |

---

## Tabla de Equivalencia del Flujo Operativo

| Paso | Café (Core INATrace) | Componente Core | Camarón (Dufer) | Componente shrimpMfe | Estado |
|---|---|---|---|---|---|
| **1. Recepción** | Agricultor entrega cereza de café al centro de acopio. Se pesa y se genera `Purchase Order`. | `stock-deliveries/` → Delivery tab | Proveedor entrega camarón en gavetas/bines. Se pesa (lbs) y se genera **Lote Base** (ej. `250121`). | `recepcion/` | ✅ |
| **2. Tipo de entrada** | Cereza húmeda vs. Pergamino seco | Delivery → Semi-product selection | **Entero** 🦐 vs. **Cola** 🔪 | `recepcion/` → type selector | ✅ |
| **3. Procesamiento** | Despulpado, Lavado, Secado, Trillado | `stock-processing/` → Processing Order (type=PROCESSING) | **Clasificación por Tallas** (máquinas 1-4) | `clasificacion/` | ✅ |
| **4. Semi-productos** | Pergamino, Trillado, Verde, Tostado | `SemiProduct` entities | **Talla 21/25, 26/30, 31/35, 36/40** (por talla) + Entero/Cola (por tipo) | `SemiProduct` seedeados | ✅ |
| **5. Lote interno** | LOT prefix + secuencial (ej. `LOT-001`) | `StockOrder.internalLotNumber` | **Lote Base + Sufijo**: `250121` (Bloque), `250121-2` (IQF), `-3` (VA), `-4` (Salmuera) | `LotNumberUtil.generateSuffix()` | ✅ |
| **6. Transferencia** | Envío de bodega A → bodega B | `stock-processing/` → Processing Order (type=TRANSFER) | **Destinos**: Mover lote clasificado a Túnel IQF, Cámara, etc. | `destinos/` | ✅ |
| **7. Rechazo / Reproceso** | Café defectuoso → separado y descartado | N/A (café no se reprocesa) | **Rechazo cíclico**: Entero → descabezado → reingreso como Cola bajo mismo lote | `rechazo/` | ⚠️ Parcial |
| **8. Empaque / Repack** | Empaque en sacos de 60-69 kg con `sacNumber` | `output.repackedOutputs[]` → Repacked SO with `sacNumber` | **Masterizado**: Cajetas → Cajas Master (cartones de 6 o 10 cajetas) | `masterizado/` | ⚠️ Parcial |
| **9. Etiqueta / QR** | Generar QR Code con datos de trazabilidad del producto final | `GENERATE_QR_CODE` ProcessingAction → `generate-qr-code-modal/` | **Etiqueta de Cartón Master**: Marca, Talla, Lote, Peso, Tipo Congelación + QR | — | ❌ Falta |
| **10. Liquidación** | Balance: peso entrada cereza vs. peso salida pergamino + merma | `estimatedOutputQuantityPerUnit` + range validation (80%-120%) | **Liquidación Matemática**: lbs recibidas = Σ(clasificado) + merma | `liquidacion/` | ⚠️ Parcial |
| **11. Inventario final** | `stock-all-stock/` → All Stock tab (filtrable por warehouse) | `stock-all-stock/` | **Cámaras de Mantenimiento FIFO**: Inventario de masters congelados | — | ❌ Falta |
| **12. Pagos** | Payment to farmer per purchase order | `stock-payments/` | N/A para Fase 1 (pago a proveedores es externo) | — | N/A |
| **13. Órdenes / Exportación** | Beyco integration, customer quote orders | `beyco-offer/`, `company-orders/` | N/A para Fase 1 | — | N/A |

---

## Tabla de Equivalencia de Instalaciones (Facilities)

| Tipo Facility Core (Café) | Uso en Café | Tipo Facility Camarón | Uso en Camarón | Seedeado |
|---|---|---|---|---|
| Collection center | Centro de acopio donde el agricultor entrega la cereza | **Recepción** | Área donde el proveedor descarga gavetas/bines | ✅ |
| Wet/Dry mill | Planta de despulpado, lavado, secado | **Clasificadora** | 4 máquinas clasificadoras por talla | ✅ |
| Warehouse/storage | Bodega de almacenamiento | **Cámara Frigorífica** | Cámara de mantenimiento de producto congelado | ✅ |
| — | — | **Túnel IQF** | Túneles estáticos y máquinas de congelación individual rápida | ✅ |
| — (no existe equivalente) | — | Área de Descabezado | Transformación de Entero a Cola | ❌ Falta |
| — | — | Área de Valor Agregado | Procesamiento PUD, P&D, Hidratado | ❌ Falta |
| — | — | Área de Salmuera | Preparación en solución salina | ❌ Falta |

---

## Tabla de Equivalencia de Processing Actions

| Processing Action Core | Tipo | Café | Equivalente Camarón | Tipo | Seedeado |
|---|---|---|---|---|---|
| Despulpado | `PROCESSING` | Cereza → Pergamino húmedo | **Clasificación por Tallas** | `PROCESSING` | ✅ |
| Secado | `PROCESSING` | Pergamino húmedo → Pergamino seco | **Procesamiento IQF** | `PROCESSING` | ❌ |
| Trillado | `PROCESSING` | Pergamino → Verde (café oro) | **Procesamiento VA (PUD/P&D)** | `PROCESSING` | ❌ |
| Transfer | `TRANSFER` | Bodega A → Bodega B | **Asignación IQF (-2)** | `TRANSFER` | ✅ |
| Transfer | `TRANSFER` | — | **Ingreso a Cámara** | `TRANSFER` | ❌ |
| Descabezado | — | No existe | **Rechazo a Descascarado** (Entero→Cola) | `PROCESSING` | ✅ |
| Empaque | `FINAL_PROCESSING` | Empaque en sacos de exportación | **Masterizado** (Cajetas→Cartón Master) | `FINAL_PROCESSING` | ❌ |
| Generate QR | `GENERATE_QR_CODE` | QR para producto final etiquetado | **Etiqueta Master + QR** | `GENERATE_QR_CODE` | ❌ |

---

## Tabla de Equivalencia de Unidades de Medida

| Unidad Core (Café) | Uso | Unidad Camarón | Uso |
|---|---|---|---|
| **kg** | Peso estándar de cereza, pergamino, verde | **Libras (lbs)** | Peso bruto en recepción y clasificación |
| **Sacos** (60/69 kg) | Conteo de sacos de exportación | **Cajetas** | Conteo manual de bloques (~2kg/~4.41 lbs cada uno) |
| `sacNumber` | Número de saco individual | **Cartón Master** | Caja que agrupa 6 o 10 cajetas |
| — | — | **Gavetas/Bines** | Conteo de contenedores de recepción |

---

## Tabla de Equivalencia del Modelo de Datos

| Entidad Core | Campo Clave | Entidad Camarón (shrimpMfe) | Campo Clave |
|---|---|---|---|
| `ApiStockOrder` | `.internalLotNumber`, `.totalQuantity` | `ReceptionLot` | `.base_lot_number`, `.gross_weight_lbs` |
| `ApiStockOrder.orderType` | `PURCHASE_ORDER` | `ReceptionLot.product_type` | `ENTERO` ó `COLA` |
| `ApiTransaction` | `.sourceStockOrder`, `.inputQuantity` | `ClassificationOutput` | `.reception_lot_id`, `.weight_lbs` |
| `ApiProcessingOrder` | `.inputTransactions[]`, `.targetStockOrders[]` | — (mock en service) | `mockState.classification_outputs[]` |
| `ApiFinalProduct` | `.name`, `.measurementUnitType` | `MasterBox` (por crear) | `.lot_suffix`, `.cajetas_count` |
| `ApiSemiProduct` | `.name` | Tallas: `21/25`, `26/30`, etc. | `.size_grade` |
| `ApiFacility` | `.name`, `.company` | `ShrimpFacility` | `.id`, `.name` (consume API real) |
| `StockOrder.comments` | Texto libre | `LotNumberUtil.packDualUnitComment()` | JSON serializado `{dualUnit, lbs, cajetas, v}` |

---

## Flujo Comparativo Visual

```
☕ CAFÉ                              🦐 CAMARÓN
──────────────                       ──────────────
Agricultor                           Proveedor camaronero
    ↓                                    ↓
Centro de Acopio                     ━━ RECEPCIÓN ━━
(Purchase Order,                     (Lote Base, Peso Bruto,
 peso cereza en kg)                   lbs, gavetas, Entero/Cola)
    ↓                                    ↓
Despulpado/Lavado                    ━━ CLASIFICACIÓN ━━
(Processing Action:                  (4 máquinas, tallas,
 cereza → pergamino)                  destino + sufijo automático)
    ↓                                    ↓
     ──── N/A ────                   ━━ RECHAZO ━━ (cíclico)
                                     (Entero → Cola bajo mismo lote)
    ↓                                    ↓
Secado/Trillado                      ━━ PROCESAMIENTO ━━
(Processing: pergamino               (IQF / VA(PUD,P&D) / Salmuera)
 → verde/oro)                            ↓
    ↓                                ━━ DESTINOS ━━
Transfer (bodega→bodega)             (Transfer a Túnel/Cámara/Área)
    ↓                                    ↓
Empaque en sacos                     ━━ MASTERIZADO ━━
(sacNumber, repacked                 (Cajetas → Cartón Máster
 outputs con maxWeight)               + Etiqueta + QR)
    ↓                                    ↓
QR Code generation                   ━━ ETIQUETA/QR ━━
(GENERATE_QR_CODE action)           (Marca, Talla, Lote, Peso)
    ↓                                    ↓
All Stock (inventario)               ━━ CÁMARAS FIFO ━━
                                     (Inventario congelado, FIFO)
    ↓                                    ↓
━━ LIQUIDACIÓN ━━                    ━━ LIQUIDACIÓN ━━
(input × ratio = output              (lbs entrada = Σ salidas
 ± range 80-120%)                     + merma justificada)
```
