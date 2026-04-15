# Shrimp MFE Remote Application — Implementation Plan

## Goal

Scaffold and integrate the `shrimp-mfe` Nx Angular Remote application into the existing `inatrace-fe` Module Federation Host.

## Proposed Changes

### Remote Application Scaffold

#### [NEW] `apps/shrimp-mfe/`

Generate a new Angular Remote application via:
```bash
npx nx g @nx/angular:remote shrimp-mfe --host=inatrace-fe --port=4201 --routing=true --style=scss --standalone=false --interactive=false
```

This creates:
- `apps/shrimp-mfe/project.json` — Build/serve/test targets
- `apps/shrimp-mfe/module-federation.config.ts` — Remote federation config exposing `ShrimpEntryModule`
- `apps/shrimp-mfe/webpack.config.ts` / `webpack.prod.config.ts`
- `apps/shrimp-mfe/src/` — App shell with `RemoteEntryModule`

---

### Host Configuration Updates

#### [MODIFY] [module-federation.config.ts](file:///Users/alvarogeovani/proyectos/inatrace/inatrace-frontend/apps/inatrace-fe/module-federation.config.ts)

Register `shrimp-mfe` in the `remotes` array. The Nx generator should do this automatically.

#### [MODIFY] [app-routing.module.ts](file:///Users/alvarogeovani/proyectos/inatrace/inatrace-frontend/apps/inatrace-fe/src/app/app-routing.module.ts)

Add a lazy-loaded route for `/shrimp` that uses `loadRemoteModule` to dynamically load the remote entry. The generator should inject this automatically; we'll verify and adjust if needed.

---

### Post-Scaffold Cleanup

- Ensure `shrimp-mfe`'s `project.json` uses proper `stylePreprocessorOptions` if it imports shared SCSS.
- Increase Node heap size for builds if needed (`NODE_OPTIONS=--max_old_space_size=8192`).

## Verification Plan

### Automated Tests

1. **Remote Build**: `NODE_OPTIONS="--max_old_space_size=8192" npx nx build shrimp-mfe`
2. **Host Build**: `NODE_OPTIONS="--max_old_space_size=8192" npx nx build inatrace-fe`
3. **Host Tests**: `NODE_OPTIONS="--max_old_space_size=8192" npx nx test inatrace-fe --watch=false --browsers=ChromeHeadless`

### Manual Verification

1. Run `npx nx serve inatrace-fe` and `npx nx serve shrimp-mfe` simultaneously.
2. Navigate to `http://localhost:4200/shrimp` in a browser.
3. Confirm the remote module renders its default component inside the Host shell without a full page reload.
