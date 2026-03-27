# Design: Shrimp Chain Frontend

## Technical Approach

El diseño frontend materializa la propuesta v3 (Microfrontend `shrimpMfe`), la cual se construye sobre un framework Nx + Angular 19 + Module Federation.
La arquitectura aísla el comportamiento específico del flujo de camarón en un Remote independiente.
El Host inicializa Keycloak, establece el JWT, y protege la ruta remota, compartiendo el contexto de autenticación al Remote vía inyección de dependencias `shared-auth`.
Esto permite un "incremental delivery" (Fase 1: consumo APIs del Core -> Fase 3: consumo desde microservicio).

## Architecture Decisions

| Decision | Choice | Alternatives Considered | Rationale |
|----------|--------|--------------------------|-----------|
| **Carga de Microfrontend** | Module Federation (Nx Webpack) | Angular Elements, iframes | MFE Nativo proporciona integración "seamless" (misma SPA), compartiendo bibliotecas core (Angular, Keycloak) evitando overhead de carga. |
| **Integración Keycloak en Host** | `keycloak-angular` library | Implementación manual oidc-client-ts | Biblioteca robusta probada en Angular, abstrae la lógica PKCE y exposición del token HttpInterceptor. |
| **Comunicación Host \<\-\> Remote** | Tokens compartidos (Module Federation) | PostMessage, LocalStorage | Compartir la biblioteca `keycloak-angular` y sus tokens en Webpack evita reinicializaciones o leaks; el token interceptor del Host puede inyectarse globalmente. |
| **Arquitectura UI de Planta** | Componentes reusables (`shared-ui`) | Componentes locales acoplados | Los botones `SizeSelector` y el `NumericKeypad` formarán parte de la librería Nx `shared-ui` para que el Core pueda usarlos a futuro si es necesario. |

## Data Flow

```ascii
[Client Browser]
        │
   [Keycloak Login] (Redirección si no Auth)
        │
   (OIDC + PKCE Flow) -> access_token
        ▼
[Host App (inatrace-fe)]
   ├─ Initializes `keycloak-angular` via APP_INITIALIZER
   ├─ Registers HttpInterceptor that injects `Bearer JWT`
   │
   └─ Route: `/camaron`
        ▼ (Module Federation Lazy Load)
[Remote App (shrimpMfe)]
   ├─ /recepcion
   ├─ /clasificacion
   └─ /destino
        │
        ▼ (API REST calls)
   [Nginx Proxy]
```

## File Changes (shrimpMfe & common libs)

| File | Action | Description |
|------|--------|-------------|
| `libs/shared-auth/src/lib/` | Create | Proveedor Keycloak, `AuthGuard` y `TokenInterceptor`. |
| `libs/shared-ui/src/lib/keypad/` | Create | Componente estúpido (`Dumb Component`) del teclado numérico grande optimizado para touch. |
| `apps/shrimp-mfe/src/app/remote-entry/` | Modify | Entry module, definir rutas hijas operativas `/recepcion`, `/clasificacion`, etc. |
| `apps/inatrace-fe/src/app/` | Modify | `app.module.ts`: Añadir APP_INITIALIZER Keycloak, interceptores. Sidebar: agregar sección Camarón. |
| `apps/inatrace-fe/module-federation.config.ts` | Modify | Compartir `keycloak-angular`, `@angular/core` como Singletons estables. |

## Interfaces / Contracts (Frontend Models)

Se creará una biblioteca `libs/shared-models` para alojar los DTO compartidos generados, o bien interfaces temporales para la Fase 1:

```typescript
export interface ClassificationEntry {
  size: SizeCategory;     // e.g., '21/25'
  cajetas: number;        // Conteo manual
  weightLbs: number;      // Peso de la gaveta
  machineId: string;
}

export interface SettlementSummary {
  area: ProductionArea;
  inputWeightLbs: number;
  outputMasterCount: number;
  shrinkageLbs: number;
  reason: WasteReason;
}
```

## Testing Strategy

| Layer | What to Test | Approach |
|-------|-------------|----------|
| Unit (Components) | Renderizado del Keypad Numérico y su emisión de Eventos (EventEmitters). Validadores de formularios (Validators.required). | Jest (`@nx/jest`). Se usan stubs para servicios HTTP y AuthService. |
| Unit (Guards/Injectors) | `AuthGuard` navega a Unauthorized o Keycloak si no tiene rol. `TokenInterceptor` adjunta headers `Bearer` correctamente. | Jest. Mockeo de `KeycloakService`. |
| E2E | Flujo de login completo desde Host, carga del ShrimpMFE y navegación entre módulos. | Cypress (`@nx/cypress`) mockeando respuestas del API Gateway y Keycloak JWT. |

## Migration / Rollout

La adopción es **incremental** y sin regresiones para operaciones existentes:
1. Se desarrolla la UI `shrimpMfe` apuntando (mediante servicios Wrapper RxJS en FE) a los endpoints actuales de INATrace Core (ProcessingAction, StockOrder). Fase 1.
2. El Keycloak se integra protegiendo la nueva ruta `/camaron` en el Host. Resto del Host sigue usando JWT legacy o migra iterativamente. Fase 2.
3. Se refactorizan los "Wrapper Services" RxJS en el `shrimpMfe` para apuntar a los endpoint `/api/shrimp/*` del nuevo microservicio backend cuando esté disponible. Fase 3.

## Open Questions

- [ ] Nx Module Federation Singletons: Al integrar Keycloak-Angular, nx genera problemas a veces por la diferente instanciación entre Host y Remote del `HttpClient` en Angular 19. ¿Usamos `provideHttpClient()` a nivel del Shell Host e impedimos que el Remote importe su propio `HttpClientModule`? (Decisión por realizar al instante de implementar la Lib Auth compartida).
