# Tasks: frontend-mfe-migration

## Phase 1: Infrastructure Preparation
- [x] 1.1 Ensure Node.js and npm versions are perfectly aligned with Angular 19/Nx prerequisites.
- [x] 1.2 Execute `npx @nx/angular init` in the `inatrace-frontend` workspace root to migrate configuration.
- [x] 1.3 Clean up orphaned configurations (ensure `angular.json` is properly migrated into `project.json` and `nx.json`).
- [x] 1.4 Validate Build: Run `npx nx build inatrace-frontend` to ensure the Host application compiles under the new orchestration.

## Phase 2: Host Dynamic Federation Setup
- [ ] 2.1 Integrate `@nx/angular:mf` schematics or manually configure Webpack Module Federation for the Host.
- [ ] 2.2 Generate `webpack.config.js` and `webpack.prod.config.js` to share strict exact versions of Angular Core, Common, and RxJS.
- [ ] 2.3 Refactor `src/main.ts` into a top-level `import('./bootstrap')` to establish the required asynchronous boundary.
- [ ] 2.4 Validate Federation Shell: Spin up `npx nx serve inatrace-frontend` and ensure generic layout and navigation load without remote chunk errors.

## Phase 3: Auth Integration Validation
- [x] 3.1 Verify existing internal JWT Login Flow from the new Host shell.
- [x] 3.2 Ensure LocalStorage token states and HTTP Interceptors fire correctly.
- [x] 3.3 Execute existing testing suites (`npx nx test inatrace-frontend`) to confirm zero regressions in current routing guards.

## Phase 4: Sync Docs
- [x] 4.1 Sync SDD artifacts to `inatrace-backend/docs/frontend-mfe-migration/`.
