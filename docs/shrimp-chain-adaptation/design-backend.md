# Design: Shrimp Chain Backend

## Technical Approach

El diseño backend sigue una estrategia de implementación en 4 fases incrementales (como definido en proposal v3). 
El objetivo es aislar la complejidad del procesamiento de camarón en un microservicio Spring Boot 3 independiente (`ShrimpTraceabilityService`), pero validar el flujo inicial en la Fase 1 usando el modelo de datos genérico existente del Core INATrace (`ValueChain`, `ProcessingAction`, `StockOrder`). Keycloak se introduce en Fase 2 para centralizar la autenticación, convirtiendo al Core y al nuevo microservicio en Resource Servers de OAuth2. El enrutamiento se manejará vía proxy inverso Nginx.

## Architecture Decisions

| Decision | Choice | Alternatives Considered | Rationale |
|----------|--------|--------------------------|-----------|
| **Capa de Enrutamiento** | Nginx Reverse Proxy | Spring Cloud Gateway | Nginx ya está configurado en el stack Docker de UI. Para solo 2 servicios backend, SCG añade complejidad innecesaria. |
| **Integración de Auth** | Spring Boot OAuth2 Resource Server | Filtros JWT Custom | Spring Security soporta validación JWT contra JWKS de Keycloak nativamente sin código custom. |
| **Separación de Datos** | Database-per-Service (shrimp_trace) | Tablas en BD Core INATrace | Permite evolución independiente del esquema de camarón y evita cuellos de botella en la DB heredada. |
| **Modelo Sincronización** | REST API Calls al Core | Bus de Eventos (Kafka) | Las entidades Core (Companies, Facilities) cambian poco. Consultas REST con caché local son suficientes y mucho más simples de operar. |

## Data Flow

```ascii
[Client: shrimpMfe]
        │
        ▼ (Bearer JWT)
   [Nginx Proxy]
        │
   ┌────┴─────────────────────────────┐
   ▼ /api/core/**                     ▼ /api/shrimp/**
[Core INATrace]               [ShrimpTraceabilityService]
   │    │                             │    │
   │    └─ GET /facilities (Sync) ────┘    │
   │                                       │
   ▼                                       ▼
[PostgreSQL]                          [PostgreSQL]
(db: inatrace)                        (db: shrimp_trace)
```

## Entity Data Model (ShrimpTraceabilityService)

| Entity | Primary Key | Foreign Keys (Core) | Key Fields |
|--------|-------------|---------------------|------------|
| `ShrimpLot` | `id` (UUID) | `companyId`, `facilityId` | `lotNumber` (String, UK), `receptionDate`, `totalWeightLbs`, `shrimpType` (ENTERO/COLA) |
| `ShrimpBatch` | `id` (UUID) | - | `lotId` (FK), `suffix` (-2/-3/-4), `destination` (Enum), `status` (Enum) |
| `ClassificationRecord` | `id` (UUID) | - | `lotId` (FK), `round` (Int), `sourceType` (Enum), `shrimpSize`, `pieceCount`, `weightLbs`, `machineId` |
| `RejectionCycle` | `id` (UUID) | - | `originalLotId` (FK), `rejectedLbs`, `headLbs`, `recoveredColaLbs` |
| `SettlementReport` | `id` (UUID) | - | `lotId` (FK), `area` (Enum), `inputLbs`, `outputMasters`, `shrinkageLbs`, `shrinkageReason` (Enum) |
| `ChamberInventory` | `id` (UUID) | `chamberFacilityId` | `lotId` (FK), `suffix`, `marca`, `talla`, `presentacion`, `masterCount` |

## File Changes (Fase 1 - Core Config)

| File | Action | Description |
|------|--------|-------------|
| `db/migration/VXX__insert_shrimp_value_chain.sql` | Create | Inserta configuración base en BD Core (ValueChains, SemiProducts, Actions) para validar flujo en Fase 1. |

## File Changes (Fase 3 - Microservicio)

El nuevo microservicio `shrimp-traceability-service` seguirá arquitectura hexagonal / capas estándar de Spring Boot:

| Path | Description |
|------|-------------|
| `src/main/java/.../domain/model/` | Entidades JPA (`ShrimpLot`, `ShrimpBatch`, etc.) |
| `src/main/java/.../domain/repository/` | Spring Data JPA Repositories |
| `src/main/java/.../application/service/` | Casos de uso (`LotClassificationService`, `BatchDestinationService`) |
| `src/main/java/.../infrastructure/web/` | RestControllers (`LotController`, `SettlementController`) |
| `src/main/java/.../infrastructure/client/` | FeignClients o RestTemplate para llamar al Core INATrace |
| `src/main/resources/application.yml` | Config DB, OAuth2 Resource Server (Keycloak url) |
| `pom.xml` | Deps: `spring-boot-starter-web`, `data-jpa`, `oauth2-resource-server`, `postgresql` |

## API Contracts (Shrimp Service)

```yaml
POST /api/shrimp/lots
body:
  totalWeightLbs: numeric
  receptionDate: date-time
  shrimpType: ENTERO | COLA
  facilityId: uuid
response:
  lotNumber: string (e.g., "250121")

POST /api/shrimp/lots/{lotNumber}/classify
body:
  round: integer
  sourceType: ENTERO | COLA_REINGRESO
  machineId: string
  records:
    - shrimpSize: string
      pieceCount: integer
      weightLbs: numeric
```

## Migration / Rollout

La migración será **incremental por fases**:
1.  **Fase 1 (Zero Code Backend):** Setup de datos en el Core mediante script SQL. Permite empezar el FE.
2.  **Fase 2 (Auth):** Despliegue de Keycloak. Migración de usuarios. Rollback: restaurar config JWT interna en el Core.
3.  **Fase 3 (Microservicio):** Despliegue de `ShrimpTraceabilityService` en paralelo al Core. Proxy Nginx enruta tráfico.

## Testing Strategy

| Layer | What to Test | Approach |
|-------|-------------|----------|
| Unit | Lógica de negocio de servicios (asignación de sufijos, cálculo de merma) | JUnit 5 + Mockito. Componentes aislados. |
| Integration | Repositorios JPA y validación JWT | `@DataJpaTest` y `@SpringBootTest` con WebTestClient y Keycloak Testcontainers. |
| E2E | Flujo completo de lote (Recepción -> Clasificación -> Batches -> Liquidación) | Test automatizado llamando a los endpoints REST en secuencia en un entorno docker-compose de prueba. |

## Open Questions
- [ ] ¿El microservicio `ShrimpTraceabilityService` residirá en el mismo monorepo Maven que el core (`inatrace-backend`) o en un repositorio git completamente separado? (Recomendado: mismo repositorio, módulo Maven independiente para simplificar CI/CD inicial).
