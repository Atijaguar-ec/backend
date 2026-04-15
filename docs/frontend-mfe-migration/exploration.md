## Exploration: Escalamiento Multi-Cadena con Microservicios (Camarón, Cacao, Café)

### Current State
Actualmente, el sistema (INATrace) está construido como un monolito tanto en el backend (Spring Boot) como en el frontend (Angular). 
La base de datos y el modelo de dominio principal han sido diseñados de forma abstracta mediante la entidad `ValueChain` y `ProductType`, permitiendo configurar dinámicamente diferentes cadenas a nivel de datos y relacionarlas con instalaciones (`Facilities`), pasos de procesamiento y evidencias.
Sin embargo, **existe acoplamiento fuerte en módulos específicos**:
- **Backend**: El servicio `BeycoOrderService` (y sus endpoints/DTOs) está fuertemente acoplado a la cadena de Café, incluyendo lógica harcodeada para países (Honduras, Rwanda), tamaños de grano (Screen Size), puntajes de cata (Cupping Score) y grados de calidad.
- **Frontend**: Existen componentes UI específicos para integraciones de café (`beyco-offer-list`) y el código cliente para esta API está autogenerado dentro del mismo bundle del frontend.

### Affected Areas
- **Backend**: 
  - `src/main/java/com/abelium/inatrace/db/entities/value_chain/ValueChain.java` — Módulo dinámico base.
  - `src/main/java/com/abelium/inatrace/components/beycoorder/*` — Lógica acoplada a café que debería ser extraíble.
  - `src/main/java/com/abelium/inatrace/components/stockorder/StockOrderService.java` — Dependencias cruzadas.
- **Frontend**: 
  - `src/app/company/company-stock/beyco-offer/*` — Lógica de UI específica para café integrada en el monolito.
  - Generación de APIs (`src/api/*`).

### Approaches (Backend)
1. **Core Service + Chain-Specific Microservices (Recomendado)** — Extraer la lógica de trazabilidad genérica (Usuarios, Compañías, StockOrders genéricos, ValueChains) en un **Core Traceability Microservice**. Para requerimientos específicos de cadenas (ej. integración Beyco para Café, validaciones especiales para Camarón), crear microservicios separados (ej. `Coffee Integration Service`, `Shrimp Quality Service`) que se comuniquen mediante eventos a través de **Redis (Pub/Sub o Streams)** o API REST con el Core.
   - Pros: Aísla la complejidad específica de cada cadena, permite escalar los servicios de Camarón independientemente de los de Café. Mantiene 1 sola fuente de verdad para usuarios. Usar Redis aligera dramáticamente la infraestructura inicial frente a Kafka.
   - Cons: Requiere refactorizar las llamadas síncronas actuales y separar la base de datos de auditoría.
   - Effort: Alto

2. **Microservicios Aislados por Cadena** — Desplegar una instancia completa de backend para cada cadena (`trace-coffee`, `trace-shrimp`). 
   - Pros: Aislamiento absoluto. Cero esfuerzo de refactorización inicial (simplemente se despliegan instancias separadas apuntando a BDs separadas configuradas cada una con su `ValueChain`).
   - Cons: Un usuario que participe en múltiples cadenas (ej. una cooperativa que produce cacao y café) debe tener cuentas separadas o se debe duplicar la data. No es arquitectura de microservicios reales sino despliegues multi-tenant físicos.
   - Effort: Bajo

### Approaches (Frontend)
1. **Dynamic Configurable SPA (Monolito Dinámico)** — Mantener Angular como un solo proyecto, pero que la interfaz (rutas, menús, pestañas) se renderice 100% basada en la configuración JSON del `ValueChain` que devuelve el backend. Los componentes de "Camarón" solo se cargan (Lazy Loading) si el usuario pertenece a la cadena Camarón.
   - Pros: Más fácil de mantener para equipos pequeños. El Lazy Loading de Angular mitiga el tamaño del bundle inicial.
   - Cons: El repositorio sigue creciendo; riesgo de acoplar variables globales.
   - Effort: Medio

2. **Micro-Frontends (Module Federation)** — Implementar un **App Shell** genérico que maneje autenticación, layout y perfiles de usuario. Las funcionalidades específicas de cada cadena (Módulo de Café, Módulo de Camarón) son aplicaciones separadas compiladas y desplegadas independientemente que se inyectan en el Shell en tiempo de ejecución.
   - Pros: Equipos independientes pueden trabajar en la cadena de Camarón sin tocar el código de Café ni de Cacao. Despliegues independientes.
   - Cons: Configuración compleja en Angular (Webpack Module Federation), manejo de estado compartido más difícil. *(Nota: El uso de herramientas de monorepo como **Nx (Nrwl)** reduce drásticamente esta complejidad).*
   - Effort: Alto

### Recommendation
**Para Backend**: Adoptar **Core Service + Chain-Specific Services**. La base dinámica `ValueChain` ya adelanta gran parte del trabajo genérico. El esfuerzo debe centrarse en desacoplar integraciones hardcodeadas (como Beyco) hacia servicios externos manejados por eventos. Se recomienda encarecidamente integrar un Proveedor de Identidad (IdP) como **Keycloak** para gestionar la autenticación (SSO) de forma centralizada entre el monolito actual y los nuevos microservicios.

**Para Frontend**: Configurar el repositorio mediante **Nx Workspace** adoptando el patrón **Open Core con Micro-Frontends**. 
- **App Shell (Host)**: Mantener el proyecto actual open-source (`inatrace-frontend`) intacto como el contenedor principal. Esto asegurará que puedas absorber actualizaciones de la comunidad (ej. migraciones a Angular 19) sin conflictos.
- **Remote Apps (MFEs)**: Desarrollar interfaces específicas de cadenas complejas (ej. `app-camaron`) como aplicaciones remotas completamente separadas e independientes. El Shell cargará estas vistas dinámicamente.
- **SSO**: Integrar **Keycloak** en el App Shell asegura que el token de sesión se comparta sin fricción con los MFEs, unificando la experiencia del usuario final sin tener que re-autenticar al pasar de un módulo genérico a uno de camarón.

### Risks
- **Data Consistency**: Extraer lógica específica requerirá manejar transacciones distribuidas o eventual consistencia patronal (Saga) en lugar de depender de `@Transactional` de Hibernate.
- **Complejidad de Infraestructura**: Múltiples microservicios y micro-frontends requieren CI/CD avanzado y orquestación (Kubernetes).
- **Riesgo de Regresión**: Al separar el `BeycoOrderService`, se pueden romper flujos existentes de exportación de café.

### Ready for Proposal
Yes — La evaluación arquitectónica está completa y documentada de forma aislada.
