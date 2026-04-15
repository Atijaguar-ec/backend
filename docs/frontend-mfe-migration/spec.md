# Delta for Infrastructure

## ADDED Requirements

### Requirement: Nx Workspace Integration
The system MUST run inside an Nx Workspace to support Monorepo structure for future Micro-Frontends.

#### Scenario: Building the Host Application
- GIVEN the repository is configured with Nx
- WHEN the command `npx nx build inatrace-frontend` is executed
- THEN the application MUST build successfully without dependency errors.

### Requirement: Module Federation Host
The `inatrace-frontend` application MUST be configured as a Module Federation Host (App Shell) to accept future remote modules dynamically.

#### Scenario: Loading the Shell
- GIVEN the host application is running via `npx nx serve inatrace-frontend`
- WHEN a user accesses the root URL `/`
- THEN the core layout AND existing routes MUST render normally without throwing loading errors.

# Delta for Auth

## MODIFIED Requirements

### Requirement: Existing Login Compatibility (Phase 1)
The system MUST continue utilizing the existing internal JWT authentication mechanism during Phase 1, deferring Keycloak SSO to Phase 2.
(Previously: The system proposed migrating to Keycloak directly)

#### Scenario: Authenticating in the Nx Shell
- GIVEN the application is running in the Nx Host Mode
- WHEN a user submits valid email and password in the login screen
- THEN the system MUST authenticate using the existing REST endpoint `/api/auth/login`
- AND save the returned JWT token to local storage exactly as it previously did.
