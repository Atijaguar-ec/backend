# Design: Shrimp Chain Backend (ms-shrimp)

## Technical Approach

El diseño backend ha evolucionado de un modelo de parche en el Core a una arquitectura de **Microservicios (Strangler Fig Pattern)** y **Aislamiento Total (Polyrepo)**. 
El microservicio independiente `ms-shrimp` (repositorio aislado) gestiona toda la lógica transaccional de la empacadora (recepción, clasificación, sufijos, mermas), utilizando el esquema PostgreSQL `shrimp.*`.
La autenticación está centralizada en Keycloak (OIDC/OAuth2), donde el Core y el nuevo microservicio operan como "Resource Servers".

## Key Business Rules (Domain Logic)

Según el análisis del Product Owner, la arquitectura modela 4 reglas de negocio críticas:

1. **Lote Base Único:** La trazabilidad no mezcla productos. El número generado en la recepción (ej. `250215`) es el ancla jerárquica para todo el flujo.
2. **Transformación Cíclica (Rechazo de Entero a Cola):** El producto "Entero" rechazado estéticamente se descabeza manualmente y reingresa a la clasificadora como "Cola". El sistema debe documentar este bucle y la merma justificada (shrinkage) de las cabezas.
3. **Gestión de Derivaciones (Sufijos):** El lote base se extiende alfanuméricamente según su destino final para preservar la trazabilidad ininterrumpida:
   - Bloque: Sin sufijo (Lote base puro)
   - IQF: Sufijo `-2`
   - Valor Agregado: Sufijo `-3`
   - Salmuera: Sufijo `-4`
4. **Liquidación Escalonada Multimodal:** El motor de conciliación debe soportar conversiones entre mediciones continuas (Libras en Gavetas) y mediciones discretas (Cajetas, Cartones Máster).

## Architecture Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| **Estrategia de Repositorio** | Polyrepo (`ms-shrimp` fuera del Core) | Aislamiento total. El CI/CD del Core y sus ramas no son contaminados por el microservicio. Escalabilidad independiente de equipos. |
| **Separación de Datos (Database-per-Service)** | Esquema aislado (`shrimp.*`) | Evita cuellos de botella en la DB heredada (MySQL) del Core. Permite el uso de PostgreSQL con restricciones JSONB y FK puras de la industria camaronera. |
| **Integración de Auth** | Spring Boot OAuth2 Resource Server | Delega la verificación JWT directamente a Keycloak mediante JWKS. |
| **Modelo Sincronización** | Referencias "Suaves" y llamadas REST | Las entidades Core (Facilities, StockOrders) se guardan solo por ID (`stock_order_id`). |

## Entity Data Model (JPA Entities)

Mapeo directo con las tablas especificadas en `shrimp_schema_spec.md`:

| Entity | Relación | Key Fields |
|--------|----------|------------|
| `ReceptionExt` | Root Entity (Asociada a `StockOrder` del Core) | `stockOrderId`, `internalLotBase` (UK), `shrimpType` (ENTERO/COLA), `totalWeightLbs` |
| `Classification` | N:1 con `ReceptionExt` | `roundNumber` (Control de Ciclos de Rechazo) |
| `ClassificationDetail` | N:1 con `Classification` | `shrimpSize`, `weightLbs` |
| `ProductiveDestination`| N:1 con `ClassificationDetail` | `destinationType` (IQF, BLOQUE, VALOR_AGREGADO), `suffix`, `allocatedLbs` |
| `ProcessingLot` | N:1 con `ProductiveDestination` | `outputMasters`, `shrinkageLbs`, `shrinkageReason` (Liquidación por Área) |
| `ColdStorageSlot` | Entidad Maestro | `chamberName`, `slotCode`, `capacityMasters` |
| `ColdStorageMovement` | Historial | `processingLotId`, `slotId`, `quantityMasters`, `movementType` |

## Data Flow

```ascii
[Client: shrimpMfe]
        │
        ▼ (Bearer JWT from Keycloak)
   [API Gateway / Nginx Proxy]
        │
   ┌────┴─────────────────────────────┐
   ▼ /api/core/**                     ▼ /api/shrimp/**
[Core INATrace]                   [ms-shrimp]
   │    │                             │    │
   │    └─ GET /facilities (Sync) ────┘    │
   │                                       │
   ▼                                       ▼
[MySQL (Core)]                        [PostgreSQL]
(db: inatrace)                        (schema: shrimp)
```

## Migration / Rollout

1.  **Fase 1 (Auth Centralizada):** Configuración de Keycloak (Realm `giz-tenant`, usuario admin).
2.  **Fase 2 (Modelado):** Creación de Repositories y Entities en `ms-shrimp` bajo BDD/TDD.
3.  **Fase 3 (Casos de Uso):** Desarrollo del motor de asignación de sufijos y conciliación multimodal.
4.  **Fase 4 (Presentación):** Actualización de `shrimpMfe` en Angular para consumir `/api/shrimp/`.

## Open Questions (Resolved)
- ~~¿El microservicio `ShrimpTraceabilityService` residirá en el mismo monorepo Maven que el core (`inatrace-backend`) o en un repositorio git completamente separado?~~
  **Resolución:** Se decidió el Enfoque Polyrepo (Opción A). El microservicio fue extraído al repositorio aislado `/ms-shrimp`.
