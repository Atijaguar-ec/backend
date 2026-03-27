# Proposal: Shrimp Chain Frontend — Mi Recomendación Técnica Real

> **Contexto**: El frontend ya está en Angular 19 + Nx + Module Federation + shrimpMfe scaffoldeado. Estas son decisiones que ya ejecutamos. Esta propuesta define QUÉ construir y EN QUÉ ORDEN, basándome en lo que conozco del código.

## La verdad sobre dónde estamos

| Lo que ya existe | Estado |
|---|---|
| Angular 19 + TypeScript 5.8 | ✅ Migrado (commit `795cd328`) |
| Nx workspace + Module Federation | ✅ Migrado (commit `f558b4a4`) |
| `shrimpMfe/` Remote App | ✅ Scaffolded con entrada vacía |
| `apps/inatrace-fe/` Host | ✅ Funcional, Module Federation configurado |
| API client auto-generada | ✅ `generate-api.js` genera desde OpenAPI spec |
| Test suite | ✅ Karma + Jest funcionando |

## Approach — Alineado con las 4 Fases del Backend

### Fase 1: shrimpMfe consume APIs existentes del Core (Semana 1-2)

**No esperamos al microservicio.** El Core de INATrace ya tiene APIs de StockOrder, ProcessingAction, y ProcessingOrder que cubren ~70% del flujo. El shrimpMfe empieza a funcionar contra el Core:

```
shrimpMfe/
├── src/app/
│   ├── remote-entry/           ← ya existe (entry point)
│   ├── recepcion/              ← Crear StockOrder (PURCHASE_ORDER)
│   │   └── recepcion.component.ts
│   ├── clasificacion/          ← Ejecutar ProcessingAction (PROCESSING)
│   │   └── clasificacion.component.ts
│   ├── destino/                ← Ejecutar ProcessingAction por destino
│   │   └── destino.component.ts
│   ├── lote-detail/            ← Ver StockOrders de un lote
│   │   └── lote-detail.component.ts
│   └── shared/
│       ├── shrimp-api.service.ts    ← Wrapper sobre API Core existente
│       ├── lot-number.util.ts       ← Lógica sufijos en frontend temporalmente
│       └── talla-presets.ts         ← Constantes de tallas de camarón
```

**Lo que se hace en frontend (temporal) hasta que exista el microservicio:**
- Sufijos de lote: `lot-number.util.ts` agrega `-2`, `-3`, `-4` al `internalLotNumber` del StockOrder
- Tallas: Un dropdown con presets (21/25, 26/30, etc.) que selecciona el SemiProduct correcto
- Doble unidad: Dos inputs (cajetas + lbs) que se combinan en `comments` del StockOrder hasta que el microservicio tenga campos propios

**Por qué esto funciona:** El operador ya puede registrar recepciones y clasificaciones reales. Descubrimos qué falta en el modelo ANTES de escribir el microservicio.

### Fase 2: Keycloak en el Host (Semana 3-4)

```
libs/shared-auth/
├── keycloak.service.ts          ← Init Keycloak, expone token
├── auth.guard.ts                ← Protege rutas por rol
├── role.guard.ts                ← Guard granular por rol de Keycloak
├── token.interceptor.ts         ← Bearer JWT en cada HTTP request
└── auth.config.ts               ← Config: realm, clientId, URL
```

**El Host (`inatrace-fe`):**
- `APP_INITIALIZER` invoca `keycloak-angular` (Authorization Code + PKCE)
- ELIMINA el login propio de `/api/user/login`
- Token se comparte con `shrimpMfe` vía Module Federation shared scope

**shrimpMfe:**
- Importa `shared-auth` de la lib Nx
- NO reinicializa Keycloak
- Sus guards usan roles: `operador_recepcion`, `jefe_produccion`, etc.

**Resultado:** Un usuario hace login UNA vez y navega entre Core y Camarón sin re-auth.

### Fase 3: shrimpMfe consume microservicio dedicado (Semana 5-8)

Ahora sí, el shrimpMfe cambia de apuntar al Core a apuntar al ShrimpTraceabilityService:

```diff
- // Fase 1: consumía API Core
- this.http.post('/api/processing-orders', ...)
  
+ // Fase 3: consume API Shrimp
+ this.http.post('/api/shrimp/lots', ...)
+ this.http.post('/api/shrimp/lots/250121/classify', ...)
+ this.http.post('/api/shrimp/lots/250121/batches', ...)
+ this.http.post('/api/shrimp/lots/250121/settle', ...)
```

**Nuevas vistas que necesitan el microservicio:**

| Vista | Por qué no se podía con el Core |
|---|---|
| **Liquidación** | Core no tiene concepto de cierre de lote por área |
| **Merma** | Core tiene `damagedWeightDeduction` pero sin motivo |
| **Inventario Cámara** | Core tiene `isAvailable` pero no vista consolidada por cámara |

**Eliminamos el código temporal de Fase 1:**
- `lot-number.util.ts` → el backend genera sufijos
- Hack de `comments` → el backend tiene campos propios para doble unidad

### Fase 4: Dashboards analíticos (Semana 9+)

| Dashboard | Implementación | Complejidad |
|---|---|---|
| **Sankey (flujo de masa)** | D3.js custom en componente Angular dentro del shrimpMfe | Media — librería d3-sankey |
| **Shrinkage Deviation** | Metabase embed (iframe con JWT de Keycloak) | Baja — Metabase 0.48+ sabe embeberse |
| **Rendimiento por Talla** | Metabase embed | Baja |
| **Ocupación Cámaras** | Componente Angular con polling cada 30s al API inventory | Baja |

## UI para Planta — Lo que realmente importa

No es cosmético. Los operadores trabajan en una **planta de procesamiento fría** con **guantes**, en **turnos largos**. La UI debe:

| Decisión | Razón real |
|---|---|
| Botones ≥48px | Guantes de latex/nitrilo reducen precisión táctil |
| Teclado numérico propio (no el del OS) | El teclado nativo ocupa media pantalla en tablet portrait |
| Presets de tallas como botones grandes | Un operador clasifica 200+ gavetas/hora, no puede estar tipeando |
| Font ≥16px en inputs | iOS hace auto-zoom con <16px y rompe el layout |
| Modo oscuro por defecto | Planta fría con iluminación industrial — pantalla brillante fatiga |
| Feedback con sonido + vibración | En planta ruidosa el toast visual no se ve |
| Offline-first para registros críticos | Si se cae el WiFi en planta, no se pierde el conteo |

## Affected Areas

| Area | Fase | Impact |
|------|------|--------|
| `shrimpMfe/src/app/` | 1,3 | Vistas operativas, primero vs Core, luego vs microservicio |
| `libs/shared-auth/` | 2 | New — Keycloak OIDC |
| `libs/shared-ui/` | 1 | New — NumericKeypad, SizeSelector, WeightInput |
| `apps/inatrace-fe/` | 2 | Modified — APP_INITIALIZER Keycloak, sidebar "Camarón" |
| `shrimpMfe/src/app/dashboards/` | 4 | New — Sankey D3.js + Metabase embeds |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| APIs Core no cubren suficiente del flujo en Fase 1 | Med | Descubrimos rápido qué falta; ajustamos scope del microservicio |
| Token Keycloak no se comparte correctamente con Remote | Med | `@angular-architects/module-federation-tools` resuelve singletons |
| Operadores rechazan la UI | High | Testing en planta REAL en Fase 1; iterar antes de invertir en microservicio |
| WiFi inestable en planta | Med | Service Worker + IndexedDB para offline-first de registros |

## Rollback Plan

- **Fase 1**: Desregistrar shrimpMfe del Host — el Core sigue funcionando
- **Fase 2**: Revertir a JWT interno — un cambio en config Spring Security + quitar keycloak-angular
- **Fase 3**: Apuntar shrimpMfe de vuelta a APIs Core — solo cambiar URLs de servicio
- **Fase 4**: Quitar iframes de Metabase — son independientes

## Success Criteria

- [ ] Fase 1: Un operador registra una recepción + clasificación de un lote REAL en tablet
- [ ] Fase 2: Login con Keycloak funciona para Host + shrimpMfe sin re-auth
- [ ] Fase 3: Lote completo trazado end-to-end con sufijos, liquidación y merma
- [ ] Fase 4: Sankey diagram de un lote real visible en el dashboard
- [ ] UX: Operador completa un ciclo de clasificación en <30 segundos por gaveta
