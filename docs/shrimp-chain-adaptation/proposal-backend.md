# Proposal: Shrimp Chain Backend — Mi Recomendación Técnica Real

> **Contexto**: Conozco este codebase a fondo — hice la migración Angular 10→19, PostgreSQL, Nx/MFE, y leí cada entidad del dominio. Esta propuesta refleja lo que **realmente recomiendo** dado el estado actual del código, no un escenario aspiracional.

## La verdad sobre el estado actual

Lo que ya tenemos funcionando:

| Componente | Estado | Versión |
|---|---|---|
| Backend | Spring Boot 3.3.3, Java 17, PostgreSQL, Hibernate+Envers, Flyway | ✅ Producción |
| Frontend | Angular 19, Nx workspace, Module Federation, shrimpMfe scaffolded | ✅ Listo |
| Auth actual | JWT interno con cookie `inatrace-accessToken`, emitido por `/api/user/login` | ✅ Funcional |
| Modelo dominio | ValueChain + ProcessingAction + StockOrder (genérico, configurable) | ✅ ~70% fit para camarón |

## Mi opinión honesta sobre las opciones

### ❌ Spring Cloud Gateway + Strangler Fig + Kafka + Debezium + Metabase + Grafana + Istio

Es la arquitectura **correcta para una empresa con 50 ingenieros y un equipo de platforma**. Para el estado actual de este proyecto, es **sobreingeniería que va a paralizar el delivery**. Razones:

- Estamos pasando de un monolito funcional a **8 piezas de infraestructura nuevas** antes de escribir una sola línea de lógica de negocio de camarón
- Kafka + Debezium + ClickHouse requieren operación dedicada
- Todavía no hay un solo usuario final usando el sistema para camarón
- No sabemos si el modelo de dominio es correcto hasta que un operador real lo use

### ❌ NestJS puro (mi propuesta v1)

Era pragmática pero tenía un problema real: **dos stacks** (Java + Node) para un equipo que ya tiene Spring Boot funcionando. Share de DTOs vía Nx es elegante en teoría, pero añade complejidad de build y debugging. Un schema OpenAPI del backend genera DTOs TypeScript automáticamente — ya se hace en el proyecto actual con `generate-api.js`.

### ✅ Lo que realmente recomiendo: Spring Boot Sidecar + Keycloak + Fases incrementales

Un **microservicio Spring Boot 3 ligero** (ShrimpTraceabilityService) desplegado junto al Core con un **reverse proxy simple** (Nginx o Traefik — ya tenemos Nginx en el Docker del frontend). Keycloak se introduce pero como **primera fase aislada** antes del microservicio. Y lo más importante: **empezar con las vistas del MFE contra el Core existente** usando ProcessingActions configuradas, y **extraer al microservicio solo la lógica que el Core NO puede cubrir**.

## Approach — 4 Fases Incrementales

### Fase 1: Configuración del Dominio en Core (Semana 1-2)
**Zero código nuevo en backend.** Usar las herramientas existentes de INATrace:

```
ValueChain: "Camarón Dufer"
├── SemiProducts:
│   ├── Camarón Entero (por talla: 21/25, 26/30, 30/40, 40/50, 50/60)
│   └── Camarón Cola (por talla: 16/20, 21/25, 26/30)
├── FacilityTypes:
│   ├── Recepción, Laboratorio, Clasificación
│   ├── Descabezado, Valor Agregado
│   ├── Túnel IQF, Túnel Brine, Túnel Estático
│   └── Masterizado, Cámara Mantenimiento
├── ProcessingActions:
│   ├── Recepción → Clasificación (type: TRANSFER)
│   ├── Clasificación → Bloque (type: PROCESSING)
│   ├── Clasificación → IQF (type: PROCESSING)
│   ├── Clasificación → VA (type: PROCESSING)
│   ├── Clasificación → Salmuera (type: PROCESSING)
│   ├── Rechazo → Descabezado (type: PROCESSING)
│   └── Congelación → Masterizado (type: FINAL_PROCESSING + repack)
└── FinalProducts:
    ├── Bloque 5lb, IQF Funda, Salmuera Cartón
    └── VA: PPV, PUD, P&D, EZ-PEEL, Estuche
```

**Esto se puede hacer HOY desde la UI de admin de INATrace.** El shrimpMfe puede empezar a consumir las APIs existentes de ProcessingAction y StockOrder para el flujo básico. Los operadores ya podrían registrar recepciones y clasificaciones.

### Fase 2: Keycloak (Semana 3-4)
**Primera pieza de infraestructura nueva.** Antes de crear microservicios, centralizamos auth:

1. Desplegar Keycloak 24 (Quarkus-based, ligero, ~150MB)
2. Crear realm `inatrace` con roles: `admin`, `operador_recepcion`, `operador_clasificacion`, `jefe_produccion`, `supervisor_area`, `auditor_calidad`
3. Migrar el Core de JWT interno → Resource Server (`spring-boot-starter-oauth2-resource-server`)
4. El frontend (Host) usa `keycloak-angular` con OIDC + PKCE
5. **No se rompe nada**: el Core sigue siendo el único backend, solo cambia quién emite el JWT

```
Antes:  Angular → /api/user/login → JWT cookie → Angular → /api/**
Ahora:  Angular → Keycloak (PKCE) → JWT Bearer → /api/** → Core valida via JWKS
```

### Fase 3: ShrimpTraceabilityService (Semana 5-8)
**Solo ahora** creamos el microservicio, y **solo para lo que el Core NO puede hacer**:

| Gap del Core | Solución en microservicio |
|---|---|
| Sufijos de lote automáticos (-2, -3, -4) | `ShrimpLotService.createBatch()` genera sufijo según destino |
| Ciclo de rechazo (entero → cola → re-clasificación) | `RejectionService.processRejection()` crea nuevo StockOrder vinculado |
| Liquidación escalonada (input lbs vs output masters por área) | `SettlementService.closeArea()` calcula balance y merma |
| Merma con motivo específico | `WasteRecord` entity con enum de motivos |
| Doble unidad (cajetas count + peso lbs) | `ClassificationRecord` con ambos campos |

**Infraestructura mínima:**
```
                    ┌─── Nginx (reverse proxy) ───┐
                    │                              │
                    │  /api/**     → Core (8080)   │
                    │  /api/shrimp/** → Shrimp (8081)│
                    │                              │
                    └──────────────────────────────┘
                              ▲
                              │ JWT (validado via Keycloak JWKS)
                              │
                    ┌─────────┴──────────┐
                    │     Keycloak       │
                    └────────────────────┘
```

No Spring Cloud Gateway. No Kafka. No Service Mesh. **Nginx** ya está en el Docker setup del frontend. Un bloque `location /api/shrimp/` es todo lo que necesitamos para enrutar.

**El microservicio es Spring Boot 3 con:**
- `spring-boot-starter-web`
- `spring-boot-starter-data-jpa` + PostgreSQL (BD separada `shrimp_trace`)
- `spring-boot-starter-oauth2-resource-server` (valida JWT de Keycloak)
- `spring-boot-starter-actuator` (health + métricas básicas)

**Modelo de dominio:**

| Entidad | Campos clave |
|---------|-------------|
| `ShrimpLot` | lotNumber, receptionDate, totalWeightLbs, shrimpType(ENTERO/COLA), coreCompanyId, coreFacilityId |
| `ShrimpBatch` | lot_id, suffix, destination(BLOQUE/IQF/VA/SALMUERA), weightLbs |
| `ClassificationRecord` | lot_id, shrimpSize, pieceCount, weightLbs, machineId |
| `RejectionCycle` | originalLot_id, rejectedLbs, headLbs, recoveredColaLbs |
| `SettlementReport` | lot_id, area, inputLbs, outputMasters, shrinkageLbs, reason |
| `WasteRecord` | lot_id, lbs, reason(AGUA/BASURA/DESCARTE/CALIBRACION) |
| `ChamberInventory` | lot_id, suffix, marca, talla, presentacion, masters, chamber |

**Comunicación con Core:** REST simple. El shrimp service llama al Core para obtener compañías/facilities. No necesita evento bus — son consultas de referencia que cambian raramente.

### Fase 4: Analytics y Observabilidad (Semana 9+)
**Solo después de que el sistema funcione en planta:**

1. **Metabase** conectado directo a `shrimp_trace` (read-only) — sin CDC ni Kafka necesarios inicialmente
2. **Dashboards**: Sankey de flujo de masa, Shrinkage Deviation, Rendimiento por talla
3. **Prometheus + Grafana** si el volumen de transacciones lo justifica
4. **CDC + Kafka + Data Warehouse** — solo si las consultas analíticas impactan el rendimiento transaccional

## Affected Areas

| Area | Impact | Fase |
|------|--------|------|
| INATrace admin UI (config) | Modified — crear ValueChain Camarón | 1 |
| `inatrace-backend/` security | Modified — oauth2-resource-server | 2 |
| Keycloak (infra) | New | 2 |
| `shrimp-traceability-service/` | New — Spring Boot 3 | 3 |
| Nginx config | Modified — añadir `/api/shrimp/` proxy | 3 |
| PostgreSQL `shrimp_trace` | New | 3 |
| Metabase (infra) | New | 4 |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| El modelo ProcessingAction del Core no cubre todos los flujos | Med | Fase 1 lo descubre rápido antes de invertir en microservicio |
| Keycloak migration rompe login existente | Med | Fase 2 tiene rollback claro a JWT interno |
| El microservicio añade latencia | Low | Mismo host Docker, REST local (<2ms) |
| El equipo no tiene experiencia operando Keycloak | Med | Keycloak 24 es Quarkus (auto-contenido), Docker image oficial |

## Rollback Plan

- **Fase 1**: Borrar ValueChain/SemiProducts de la admin UI — reversible en minutos
- **Fase 2**: Revertir auth a JWT interno (`/api/user/login`) — un cambio en Spring Security config + frontend
- **Fase 3**: Apagar contenedor shrimp, quitar bloque Nginx — Core no depende del microservicio
- **Fase 4**: Apagar Metabase — completamente desacoplado

## Dependencies

- PostgreSQL 16+ (ya tenemos, migrado recientemente)
- Keycloak 24+ (Docker image: `quay.io/keycloak/keycloak:24.0`)
- Nginx (ya en el setup Docker del frontend)
- Spring Boot 3.3.x (mismo que el Core)

## ¿Por qué esta propuesta y no la del PDF?

| Tu PDF propone | Yo recomiendo | Por qué |
|---|---|---|
| Todo desde el día 1 | 4 fases incrementales | Descubrimos errores en el modelo ANTES de invertir en infra |
| Spring Cloud Gateway | Nginx reverse proxy | Ya lo tenemos; Gateway es overhead para 2 servicios |
| Kafka + Debezium + CDC | Metabase directo a BD | Kafka tiene sentido con 10+ servicios, no con 2 |
| Istio + Envoy Sidecar | Nada (2 servicios) | Service mesh es para >10 servicios en producción |
| ClickHouse + Data Warehouse | Metabase → PostgreSQL read replica | Suficiente hasta millones de registros |
| OpenTelemetry + Prometheus + Grafana día 1 | Spring Actuator + logs estructurados | Escalamos a full observability cuando tengamos volumen |

**El PDF es excelente como VISIÓN A LARGO PLAZO. Esta propuesta es el CAMINO para llegar allí sin morir en el intento.**

## Success Criteria

- [ ] Fase 1: Operadores registran recepciones y clasificaciones usando APIs existentes del Core
- [ ] Fase 2: Login con Keycloak funciona para Core + shrimpMfe sin re-auth
- [ ] Fase 3: ShrimpTraceabilityService maneja sufijos, rechazo, liquidación y merma
- [ ] Fase 3: Un lote completo (recepción → clasificación → destino → congelación → masterizado) se traza end-to-end
- [ ] Fase 4: Dashboard Sankey muestra flujo de masa de un lote real
- [ ] Fase 4: Alerta de desviación de merma funciona sobre datos reales
