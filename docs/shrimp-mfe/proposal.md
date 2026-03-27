# Proposal: Shrimp Micro-Frontend (Remote App)

## Intent
The "Shrimp" (Camarón) value chain possesses distinct business logic, specialized modules, and unique requirements compared to the Open Core of INATrace. To prevent monolithic entanglement and enable independent development lifecycles, the Shrimp module will be established as an independent Micro-Frontend (MFE) Remote application within the Nx workspace.

## Scope

### In Scope
- Scaffold a new Nx Angular Remote application named `shrimp-mfe`.
- Configure Webpack Module Federation to expose the `ShrimpModule`.
- Integrate the Remote application into the `inatrace-fe` Host shell's dynamic routing.
- Implement isolated state management and local routing within the `shrimp-mfe`.

### Out of Scope
- Full Keycloak authentication integration (deferred to the global Phase 2 Auth revamp).
- Heavy migration of all existing Shrimp-related legacy code (Initial phase focuses purely on structural scaffolding and connection validation).

## Approach
Utilize the `@nx/angular:setup-mf` schematic (or application generator) to generate a new Remote application within the existing Nx workspace. Configure the Host (`inatrace-fe`) to dynamically load `shrimp-mfe` using `loadRemoteModule` mapped to a specific internal route (e.g., `/shrimp`). The Remote will operate autonomously during isolated development but render seamlessly inside the Host's layout.

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `apps/shrimp-mfe` | New | New Nx project directory containing the remote app. |
| `apps/inatrace-fe/src/app/app-routing.module.ts` | Modified | Inject dynamic remote route binding. |
| `apps/inatrace-fe/module-federation.config.ts` | Modified | Register remote application endpoint definitions. |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| Dependency Version Mismatch | Low | Enforce strict shared dependency versions (Angular Core/Common, RxJS) in Webpack configuration. |
| Global CSS/Style Bleeding | Medium | Utilize strict Angular component view encapsulation (`Emulated`) within the remote application. |

## Rollback Plan
Remove the dynamic route mapping from the Host `app-routing.module.ts`, delete the `shrimp-mfe` project directory via the Nx CLI (`nx g @nx/workspace:remove shrimp-mfe`), and rollback configuration changes to the Host's `module-federation.config.ts`.

## Dependencies
- Webpack Module Federation plugin configuration.
- Operational `inatrace-fe` Host application infrastructure.

## Success Criteria
- [ ] `shrimp-mfe` application compiles and serves independently without errors.
- [ ] Host application dynamically loads and mounts the Shrimp remote context on the `/shrimp` route.
- [ ] Seamless navigation between Host native components and Remote components without full page reloads.
