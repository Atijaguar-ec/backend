# Design: frontend-mfe-migration

## Technical Approach
Convert the existing Angular 19 project into an Nx Workspace with Module Federation. Set up `inatrace-frontend` as the Host (Shell) application to continue serving the generic Open Core and existing login flow. The migration will be done in-place to preserve git history and current build configurations.

## Architecture Decisions

### Decision: Nx Workspace Setup Method
**Choice**: Use the automated `npx @nx/angular init` command.
**Alternatives considered**: Create a new Nx workspace from scratch and manually copy source files.
**Rationale**: Since the application was recently migrated natively to Angular 19 via Angular CLI, the `nx init` schematic is heavily optimized to safely wrap `angular.json` into an Nx-compatible `project.json` while maintaining existing targets (like `generate-api`), minimizing the risk of broken builds.

### Decision: Module Federation Architecture
**Choice**: Dynamic Module Federation (`loadRemoteModule`).
**Alternatives considered**: Static Module Federation (hardcoding URLs in `webpack.config.js`).
**Rationale**: Dynamic federation allows the Host shell to fetch remote URLs from a runtime configuration file (`assets/mfe.manifest.json`) or API. This is crucial for INATrace's architecture so we can deploy new proprietary chains (e.g. Camarón) in the future without redeploying the Open Core Host.

### Decision: Shared Libraries Extraction Strategy
**Choice**: Defer extraction of `libs/` to a subsequent SDD phase.
**Alternatives considered**: Immediately extract `libs/ui`, `libs/auth` during this migration.
**Rationale**: To limit the blast radius and ensure the CI/CD pipeline remains stable, Phase 1 focuses *strictly* on standardizing the Nx ecosystem and securing the Host. Splitting cyclical dependencies into libraries is a complex task better tracked in its own change.

## Data Flow
The user navigates to the INATrace URL and hits the Host application:
    User ──→ App Shell (Host: inatrace-frontend)
                 │
                 ├── Auth: Internal JWT (Phase 1)
                 │
                 └── Router (Dynamic) ──→ loadRemoteModule() ──→ MFE (e.g. app-camaron)

## File Changes

| File | Action | Description |
|------|--------|-------------|
| `nx.json` | Create | Global Nx workspace configuration enabling caching. |
| `project.json` | Create | Project-level configuration replacing `angular.json` for task definitions. |
| `webpack.config.js` | Create | Module federation configuration exposing sheared dependencies (Angular Core, RxJS). |
| `webpack.prod.config.js` | Create | Production-specific federation build rules. |
| `angular.json` | Delete | Removed by the `nx init` underlying schematics. |
| `src/main.ts` | Modify | Wrapped in `import('./bootstrap')` to allow asynchronous chunk loading for MFEs. |

## Interfaces / Contracts
No API contracts change during this infrastructure phase.
The system will rely on Webpack 5 standard APIs for remote loading:
```typescript
loadRemoteModule({
  remoteEntry: 'http://localhost:4201/remoteEntry.js',
  remoteName: 'appCamaron',
  exposedModule: './Module'
}).then(m => m.CamaronModule)
```

## Testing Strategy

| Layer | What to Test | Approach |
|-------|-------------|----------|
| Unit | Existing Test Suites | Execute `npx nx test inatrace-frontend` ensuring Karma/Jasmine passes out-of-the-box. |
| Integration | Host Bootstrap | Validate that wrapping `main.ts` in an async boundary doesn't break app initialization. |
| Build | Output Artifacts | Verify `npx nx build` generates the correct bundles in the `dist/` envelope. |

## Migration / Rollout
No database or state migration is required.
Developers will replace `npm run start` with `npx nx serve inatrace-frontend` in their local environments.

## Open Questions

- [ ] ¿Cómo gestionar los entornos (dev, stg, prod) para las URLs dinámicas de los MFEs? (Sugerencia: un archivo `manifest.json` en `src/assets` que se reemplaza en el pipeline CI/CD).
