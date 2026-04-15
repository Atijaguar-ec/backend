# INATrace Frontend — Micro-Frontend Architecture

## Overview

The INATrace frontend has been migrated from a monolithic Angular application to an **Nx Workspace** with **Webpack Module Federation**, enabling a Micro-Frontend (MFE) architecture.

### Architecture Diagram

```
┌──────────────────────────────────────────────────────────┐
│                    Nx Workspace                          │
│  inatrace-frontend/                                      │
│                                                          │
│  ┌─────────────────────┐    ┌──────────────────────┐     │
│  │  apps/inatrace-fe   │    │    shrimpMfe          │     │
│  │  (Host / Consumer)  │◄───│  (Remote / Producer)  │     │
│  │  :4200              │    │  :4201                │     │
│  └─────────────────────┘    └──────────────────────┘     │
│           │                          │                    │
│           ▼                          ▼                    │
│  ┌─────────────────────────────────────────────────┐     │
│  │         Shared Dependencies (Angular, RxJS)     │     │
│  └─────────────────────────────────────────────────┘     │
└──────────────────────────────────────────────────────────┘
```

## Project Structure

```
inatrace-frontend/
├── apps/
│   └── inatrace-fe/              # Host (App Shell)
│       ├── module-federation.config.ts
│       ├── webpack.config.ts
│       ├── webpack.prod.config.ts
│       ├── project.json
│       └── src/
│           ├── main.ts           # import('./bootstrap')
│           ├── bootstrap.ts      # AppModule bootstrap
│           └── app/
│               ├── app.module.ts
│               └── app-routing.module.ts
├── shrimpMfe/                    # Remote (Shrimp Module)
│   ├── module-federation.config.ts
│   ├── webpack.config.ts
│   ├── webpack.prod.config.ts
│   ├── project.json
│   └── src/
│       ├── main.ts
│       ├── bootstrap.ts
│       └── app/
│           └── remote-entry/
│               ├── entry.module.ts     # RemoteEntryModule
│               ├── entry.component.ts  # Main entry
│               └── entry.routes.ts     # Remote routes
├── scss/                         # Shared SCSS (root level)
├── nx.json
├── tsconfig.base.json
└── package.json
```

## Prerequisites

| Requirement | Version |
|-------------|---------|
| Node.js     | >= 20.x |
| npm         | >= 10.x |
| Angular     | 19.x    |
| Nx          | 22.x    |

### Set Node.js Default

```bash
nvm alias default 20
# Close and reopen terminal, then verify:
node -v
```

## Commands

### Development

```bash
# Start Host + all Remotes together (recommended)
NODE_OPTIONS="--max_old_space_size=8192" npx nx serve inatrace-fe

# Start only the Shrimp remote (independent development)
npx nx serve shrimpMfe
```

### Build

```bash
# Build Host
NODE_OPTIONS="--max_old_space_size=8192" npx nx build inatrace-fe

# Build Shrimp Remote
NODE_OPTIONS="--max_old_space_size=8192" npx nx build shrimpMfe

# Build all projects
NODE_OPTIONS="--max_old_space_size=8192" npx nx run-many --target=build --all
```

### Tests

```bash
# Host tests
NODE_OPTIONS="--max_old_space_size=8192" npx nx test inatrace-fe --watch=false --browsers=ChromeHeadless

# Shrimp remote tests
npx nx test shrimpMfe --watch=false
```

### Useful Nx Commands

```bash
# View project dependency graph
npx nx graph

# Show project details
npx nx show project inatrace-fe
npx nx show project shrimpMfe

# Reset Nx cache (if builds behave unexpectedly)
npx nx reset
```

## URLs (Development)

| Application | URL | Description |
|-------------|-----|-------------|
| Host (INATrace) | `http://localhost:4200` | Main application |
| Shrimp via Host | `http://localhost:4200/shrimpMfe` | Remote loaded inside Host |
| Shrimp standalone | `http://localhost:4201` | Remote running independently |

## Module Federation Configuration

### Host (`apps/inatrace-fe/module-federation.config.ts`)

```typescript
const config: ModuleFederationConfig = {
  name: 'inatrace-fe',
  remotes: ['shrimpMfe'],
};
```

### Remote (`shrimpMfe/module-federation.config.ts`)

```typescript
const config: ModuleFederationConfig = {
  name: 'shrimpMfe',
  exposes: {
    './Module': 'shrimpMfe/src/app/remote-entry/entry.module.ts',
  },
};
```

## Adding a New Remote MFE

To create another remote application (e.g., for a different value chain):

```bash
# Remote names must be valid JS identifiers (no hyphens)
npx nx g @nx/angular:remote myNewMfe \
  --host=inatrace-fe \
  --port=4202 \
  --style=scss \
  --standalone=false \
  --e2eTestRunner=none \
  --linter=none \
  --interactive=false
```

> **Important**: Module Federation remote names **cannot contain hyphens**. Use camelCase (e.g., `shrimpMfe`, `coffeeMfe`).

After generation:
1. Clean up `nx-welcome.component.ts` (delete it)
2. Update `entry.component.ts` with your business logic
3. Update `entry.module.ts` to remove `NxWelcomeComponent`
4. Build and validate: `npx nx build myNewMfe`

## Known Issues & Gotchas

### Memory Limits
The Host build requires increased Node.js heap size due to Webpack's memory consumption with the large INATrace bundle:
```bash
NODE_OPTIONS="--max_old_space_size=8192"
```

### SCSS Include Paths
Component SCSS files that import `src/_variables` work because `project.json` includes:
```json
"stylePreprocessorOptions": {
  "includePaths": [".", "apps/inatrace-fe"]
}
```
New remotes may need similar configuration if they share the Host's SCSS variables.

### Font Paths
FontAwesome and PathwayGothic fonts are served from `apps/inatrace-fe/src/assets/webfonts/`. The SCSS variable `$fa-font-path` in `apps/inatrace-fe/src/_variables.scss` points to:
```scss
$fa-font-path: '../apps/inatrace-fe/src/assets/webfonts';
```

### `.npmrc`
The workspace uses `legacy-peer-deps=true` to bypass Angular peer dependency conflicts during Nx installation.

## Migration History

| Date | Phase | Description |
|------|-------|-------------|
| 2026-03-23 | Phase 1 | Nx workspace initialization (`npx nx init`) |
| 2026-03-23 | Phase 2 | Module Federation Host setup (`@nx/angular:setup-mf`) |
| 2026-03-23 | Phase 3 | Auth validation (43/43 tests passing) |
| 2026-03-23 | Phase 4 | Shrimp MFE Remote scaffolded and connected |

## Phased Roadmap

- [x] **Phase 1**: Nx Workspace + Module Federation Host
- [x] **Phase 2**: First Remote (Shrimp MFE)
- [ ] **Phase 3**: Keycloak Authentication Integration
- [ ] **Phase 4**: Shared Libraries Extraction (`libs/`)
- [ ] **Phase 5**: Additional Remotes (per value chain)
- [ ] **Phase 6**: Environment-specific remote URLs (`mfe.manifest.json`)
