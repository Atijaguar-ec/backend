# Registro de Cambios (Changelog)

Todas las modificaciones notables de este proyecto se documentarán en este archivo.

## [2025-07-10] - Correcciones de Estabilidad y Documentación

### Corregido (Fixed)

-   **Errores de Migración de Flyway**: Se han modificado múltiples scripts de migración de base de datos para garantizar la idempotencia, especialmente en operaciones destructivas (`DROP TABLE`, `DROP COLUMN`, `DROP FOREIGN KEY`). Se ha adoptado el uso de la sintaxis `IF EXISTS` para prevenir fallos cuando se intentan ejecutar las migraciones sobre una base de datos que ya ha sido parcialmente migrada. Esto resuelve los fallos críticos que impedían el arranque de la aplicación.
-   **Estabilidad del Arranque**: Se ha resuelto el problema de arranque del backend que era causado por un estado inconsistente en la tabla `flyway_schema_history` y scripts de migración no idempotentes.

### Añadido (Added)

-   **Guía de Solución de Problemas**: Se ha añadido una sección completa de "Solución de Problemas Comunes (Troubleshooting)" al archivo `README-ES.md`.

### Documentación (Docs)

-   Se ha documentado el procedimiento para resolver los siguientes problemas:
    -   Errores de migración de Flyway.
    -   Conflictos de puertos (`Port already in use`).
    -   Errores `401 Unauthorized` debidos a la falta de claves de API para servicios externos.
-   Se ha añadido la URL y las instrucciones para acceder a la interfaz de Swagger UI (`/swagger-ui.html`) y poder probar los endpoints de la API del backend.
