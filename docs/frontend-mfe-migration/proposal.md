# Proposal: frontend-mfe-migration

## Intent
Migrar la arquitectura del monolito Angular actual (`inatrace-frontend`) hacia un modelo **Open Core con Micro-Frontends (MFEs)**. Esto aislará el desarrollo de cadenas productivas específicas (como Camarón) en aplicaciones remotas que se inyectan dinámicamente, manteniendo intacto el core open-source para facilitar actualizaciones de la comunidad y unificando la seguridad con Keycloak.

## Scope

### In Scope
- Inicializar un workspace de Nx.
- Transferir la aplicación Angular existente (`inatrace-frontend`) para fungir como el **Host / App Shell**.
- Configurar Webpack Module Federation en el Host.
- **Mantener el mecanismo de autenticación actual (JWT interno)** durante esta primera fase migratoria para reducir variables de error.
- Crear una arquitectura de esqueleto para el primer Micro-Frontend Remoto (Template de Remote App).

### Out of Scope
- Integración de **Keycloak (SSO)** (Se pospone para una Fase 2, una vez estabilizado el entorno Nx).
- Extraer módulos del backend a microservicios reales (esto será otra propuesta separada).
- Migrar lógicas existentes y fuertemente acopladas como Beyco a un MFE propio en esta primera iteración.

## Approach
Implementar el **Patrón Open Core (App Shell)** apoyado por **Nx (Nrwl)**. 
1. `inatrace-frontend` será el contenedor huésped (Host).
2. La identidad visual y la sesión de usuario (Keycloak) residen en el Host.
3. El frontend de Camarón vivirá en un MFE remoto cargado bajo demanda.
*Nota: Este diseño se fundamenta en la previa validación arquitectónica de "Multi-Chain Microservices Scaling".*

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `inatrace-frontend/angular.json` | Replaced | Migración hacia la ejecución basada en `project.json` de Nx. |
| `inatrace-frontend/package.json` | Modified | Inclusión de Nx tools y dependencias de Module Federation. |
| `inatrace-frontend/src/app/*` | Modified | Ajustes en las rutas principales y guards para soportar Keycloak y MFEs remotos. |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| Conflictos de dependencias entre el Host y Nx | Medium | Utilizar la migración automatizada de Nx (`nx init`) y ajustar versiones de Node/Angular estrictamente. |
| Pérdida del Lazy Loading actual en Angular | Low | Nx y Module Federation exponen los módulos mediante `loadRemoteModule`, replicando el Lazy Loading clásico. |

## Rollback Plan
- Descartar la rama `feature/frontend-mfe-migration`.
- Borrar cualquier configuración residual de `nx.json` si se subió algún archivo accidental a master, o restaurar un punto de control de Git desde el último release de `inatrace-frontend`.

## Dependencies
- Ninguna externa en Fase 1 (Keycloak fue diferido a Fase 2).

## Success Criteria
- [ ] El workspace de Nx se compila correctamente sin errores (`npx nx build inatrace-frontend`).
- [ ] La aplicación original sirve el login genérico actual y los paneles principales de forma idéntica, pero soportada en el servidor de Nx.
