# Tasks: Shrimp Chain Adaptation

Este desglose cubre las **Fases 1 y 2** de la propuesta, priorizando entregar valor inmediato al negocio ("ShrimpMfe consumiendo API del Core INATrace") antes de construir nueva infra de microservicios (Fase 3).

## Phase 1.1: Core Database Configuration (Zero Code Backend)

- [x] 1.1.1 Crear script Python `scripts/seed_shrimp_chain.py` (usando `psycopg2` o llamadas API) para inicializar los datos base.
- [x] 1.1.2 El script debe limpiar/crear la `ValueChain` (Camarón Dufer).
- [x] 1.1.3 El script debe configurar `FacilityType`s (Recepción, Clasificación, Túnel IQF, Cámara).
- [x] 1.1.4 El script debe configurar `SemiProduct`s (Entero/Cola + arreglo de Tallas: 21/25, 26/30...).
- [x] 1.1.5 El script debe configurar `ProcessingAction`s (Clasificación, Destino IQF, Etiquetado QR).
- [x] 1.1.6 Ejecutar `python3 scripts/seed_shrimp_chain.py` y validar que toda la cadena aparezca correctamente configurada en el Admin UI de INATrace.

## Phase 1.2: Frontend Shared Libs

- [x] 1.2.1 Nx: Generar librería `shared-ui` (`npx nx g @nx/angular:lib shared-ui`)
- [x] 1.2.2 `shared-ui`: Implementar Dumb Component `NumericKeypadComponent` (48x48px min targets)
- [x] 1.2.3 `shared-ui`: Implementar `SizeSelectorComponent` (Presets tallas)
- [x] 1.2.4 Nx: Generar librería `shared-models` para alojar DTOs Temporales (StockOrder, ProcessingAction wrappers)

## Phase 1.3: ShrimpMfe - Vistas Básicas (consumen API Core)

- [x] 1.3.1 `shrimpMfe`: Implementar Layout principal (Sidebar nav)
- [x] 1.3.2 `shrimpMfe`: Crear vista `/recepcion` (Llama a `POST /api/stock-orders` interno temporal)
- [x] 1.3.3 Frontend util: Crear generador de sufijos temporal `lot-number.util.ts` (`-2`, `-3`)
- [x] 1.3.4 `shrimpMfe`: Crear vista `/clasificacion` usando `NumericKeypad` y `SizeSelector`
- [x] 1.3.5 `shrimpMfe`: En `clasificacion`, combinar inputs cajetas+lbs temporalmente en campo `comments` del StockOrder

## Phase 2: Keycloak Integration (Auth)

- [x] 2.1.1 Docker: Añadir contenedor `keycloak` a `docker-compose.yml` (v24.0.0+) exponiendo 8080 en 8082
- [x] 2.1.2 Keycloak Admin: Crear realm `inatrace` y client `inatrace-frontend` (Public, PKCE)
- [x] 2.1.3 Backend `inatrace-backend`: Reemplazar JWT custom por `oauth2-resource-server` en `SecurityConfig.java`
- [x] 2.1.4 Frontend Nx: Generar librería `shared-auth`
- [x] 2.1.5 `shared-auth`: Importar `keycloak-angular` y configurar AuthGuard, Interceptor, y KeycloakService
- [x] 2.1.6 Host App `inatrace-fe`: Iniciar Keycloak en `APP_INITIALIZER` llamando a `shared-auth`
- [x] 2.1.7 MFE Config: Modificar `module-federation.config.ts` para compartir `keycloak-angular` y `@angular/common/http` como Singletons estables.

---
*(La Fase 3 — Creación de ShrimpTraceabilityService y Nginx Proxy — se abordará en un desglose subsiguiente tras validar la UX de las Fases 1 & 2 en planta).*
