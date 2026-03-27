# Exploración: Adaptación de la Cadena de Camarón (Dufer) al Modelo INATrace

## Current State

INATrace posee un modelo de dominio **genérico y configurable** mediante `ValueChain` + `ProductType`. Soporta:
- **ValueChain**: template con FacilityTypes, SemiProducts, MeasureUnitTypes, ProcessingEvidenceFields
- **ProcessingAction**: transformaciones con input/output SemiProducts, tipos (PROCESSING, TRANSFER, QUOTE, FINAL_PROCESSING)
- **StockOrder**: inventario con `internalLotNumber`, `lotPrefix`, `totalQuantity`, `totalGrossQuantity`, `tare`, `damagedWeightDeduction`

El sistema fue diseñado para café pero su abstracción permite otras cadenas.

## Flujo Dufer (Camarón)

1. **Recepción**: Lote base único (ej: 250121)
2. **Decisión Inicial**: Entero → Clasificación | Cola → Pesado + Descabezado → Clasificación
3. **Clasificación**: Por tallas con 4 máquinas. Rechazo → descabezado → re-clasificación como cola
4. **Destino Productivo**: Bloque | IQF | Valor Agregado (PPV, PUD, P&D, EZ-PEEL) | Salmuera
5. **Sufijos de Lote**: sin sufijo (Bloque), -2 (IQF), -3 (VA), -4 (Salmuera)
6. **Congelación**: Brine IQF CBFI, IQF Cyclone1000, Túneles Estáticos
7. **Masterizado**: Match → Etiquetado → Cartón máster
8. **Liquidación Escalonada**: Clasificación (cajetas + peso por talla) → cada área destino (libras vs masters)
9. **Almacenamiento**: Cámara de mantenimiento con trazabilidad total

## Mapeo Conceptual Dufer → INATrace

| Concepto Dufer | Mapeo INATrace | Gap |
|---|---|---|
| Lote base | `StockOrder.internalLotNumber` | ✅ Directo |
| Sufijo destino | `StockOrder.lotPrefix` + convención | ⚠️ Requiere lógica automática |
| Talla (21/25, etc.) | `SemiProduct` (uno por talla) | ✅ Soportado |
| Presentación Entero/Cola | `SemiProduct` | ✅ Dos SemiProducts |
| Destino productivo | `ProcessingAction.type = PROCESSING` | ✅ Soportado |
| Rechazo → re-clasificación | `ProcessingAction` con ciclo | ⚠️ INATrace solo DAGs |
| Liquidación escalonada | No existe | ❌ Concepto nuevo |
| Merma/Ajuste | `damagedWeightDeduction` + `tare` | ⚠️ Sin motivo de merma |
| Doble unidad (piezas + peso) | `totalQuantity` + `measurementUnitType` | ⚠️ Una sola unidad |
| Inventario cámara | `Facility` + `isAvailable` | ✅ Soportado |
| Masterizado | `FINAL_PROCESSING` + repack | ✅ Soportado |

## Approaches

### 1. Configuración Pura (Zero-Code Backend)
Configurar con herramientas existentes: ValueChain "Camarón", SemiProducts por talla, ProcessingActions por paso.

- Pros: Cero cambios backend, rápido, bajo riesgo
- Cons: No soporta ciclos, sin sufijos automáticos, sin liquidación nativa, sin doble unidad
- Effort: **Bajo**

### 2. Extensión Ligera del Backend + MFE Frontend (Recomendado)
Extender `StockOrder` con campos opcionales (`lotSuffix`, `shrimpSize`). Crear `ShrimpProcessingService` para sufijos, re-entrada de rechazo y liquidación. El MFE `shrimpMfe` consume APIs genéricas + endpoints específicos.

- Pros: Modelo Open Core intacto, extensiones opcionales, flujo rechazo modelable, liquidación nativa
- Cons: Cambios en backend, riesgo moderado de acoplamiento
- Effort: **Medio**

### 3. Microservicio Separado
`shrimp-traceability-service` independiente comunicándose con Core via REST.

- Pros: Aislamiento total, escala independiente
- Cons: 2 servicios + 2 BDs, consistencia eventual, overhead operativo
- Effort: **Alto**

## Recommendation

**Approach 2: Extensión Ligera + MFE**, porque:
1. El modelo INATrace cubre ~70% del flujo
2. Los gaps son concretos (sufijos, ciclo rechazo, liquidación, doble unidad) → 3-5 campos + 1 servicio
3. `shrimpMfe/` ya está scaffoldeado
4. Evita complejidad de microservicios para equipo pequeño
5. Permite migrar a microservicio después si el negocio lo justifica

### Configuración Sugerida
- **ValueChain**: "Camarón Dufer"
- **SemiProducts**: Camarón Entero, Camarón Cola, uno por talla
- **FacilityTypes**: Recepción, Laboratorio, Clasificación, Descabezado, Valor Agregado, Túnel IQF/Brine/Estático, Masterizado, Cámara
- **ProcessingActions**: Recepción→Clasificación, Clasificación→Destino, Rechazo→Descabezado, Congelación, Masterizado
- **FinalProducts**: Bloque 5lb, IQF Funda, Salmuera Cartón, VA (PPV/PUD/P&D/EZ-PEEL)

## Risks

- **Ciclo de rechazo**: Modelar como nuevo StockOrder vinculado, no como ciclo real
- **Liquidación escalonada**: Requiere lógica nueva o reportes calculados en frontend
- **Doble unidad**: Dos StockOrders relacionados o campo extra
- **Campos café hardcodeados**: `requiredWomensCoffee`, `womenShare` — NO repetir este anti-patrón

## Ready for Proposal
**Sí** — Proceder con `sdd-propose shrimp-chain-adaptation`
