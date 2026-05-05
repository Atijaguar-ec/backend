# Análisis Arquitectónico: ms-shrimp en Java (Spring Boot)

## 1. Contexto y Requerimiento
El cliente ha solicitado que el microservicio diseñado para la extensión camaronera (`ms-shrimp`) sea implementado en **Java** como un **proyecto independiente y nuevo contenedor**, alejándose de la propuesta inicial en NestJS / TypeScript.

## 2. Topología del Proyecto y Estructura: Nuevo Contenedor (Aprobado)
Se ha decidido que **ms-shrimp** será un proyecto totalmente nuevo e independiente.
* **Aislamiento Total:** El código fuente vivirá en su propio directorio/repositorio, sin tocar la estructura del Core.
* **Infraestructura:** Tendrá su propio `Dockerfile` y contenedor de despliegue (`inatrace-ms-shrimp`).
* **Comunicación:** Se conectará a la misma Base de Datos PostgreSQL, pero operará de manera autónoma gestionando únicamente el esquema `dufer.*`.

## 3. Evaluación de Versiones (Metodología de los 6 Sombreros)
Dado que el proyecto es un nuevo contenedor, surge la interrogante: *¿Debemos usar las versiones legacy del Core (Java 17, Spring Boot 3.3) o migrar a las más actuales (Java 21 LTS, Spring Boot 3.4+)?* 

Aplicamos la metodología de los 6 sombreros de Edward de Bono para decidir:

### ⚪ Sombrero Blanco (Hechos y Datos)
* **Core actual:** Java 17 LTS y Spring Boot 3.3.3.
* **Actualidad:** Java 21 es la versión LTS más reciente (con Java 25 en horizonte próximo). Spring Boot 3.4.x ya es estable.
* **Infraestructura:** Al ser un contenedor Docker independiente, la versión de la JDK en la máquina host es irrelevante; la imagen base dicta la versión.

### 🔴 Sombrero Rojo (Emociones e Intuición)
* A los desarrolladores les entusiasma y motiva trabajar con las últimas tecnologías y características del lenguaje.
* Existe una ligera preocupación o "pereza" operativa de tener que recordar sintaxis o comportamientos ligeramente distintos entre dos microservicios del mismo ecosistema.

### ⬛ Sombrero Negro (Riesgos y Precaución)
* **Divergencia de Conocimiento:** Mantener dos versiones distintas (Java 17 vs Java 21) puede causar fricción cuando un desarrollador cambie de un proyecto a otro.
* **Librerías Compartidas:** Si en el futuro se decide extraer utilidades comunes (ej. validación JWT o conexión a Keycloak) en un archivo `.jar` compartido, la incompatibilidad de versiones de bytecode podría romper la compilación.
* **Pipeline CI/CD:** Requerirá actualizar las herramientas del pipeline de Jenkins/GitHub Actions para soportar Java 21 de forma paralela.

### 🟡 Sombrero Amarillo (Beneficios y Optimismo)
* **Virtual Threads (Loom):** Java 21 trae *Virtual Threads*, mejorando masivamente el rendimiento concurrente sin requerir programación reactiva compleja (WebFlux).
* **Pattern Matching:** Código mucho más limpio para los DTOs y validaciones complejas.
* **Future-Proofing:** Este microservicio no requerirá una migración forzada en los próximos 5 años. Se convierte en la "punta de lanza" técnica que el Core podría usar como referencia para actualizarse.

### 🟢 Sombrero Verde (Creatividad y Alternativas)
* Al usar un contenedor aislado, podemos abstraer completamente el JDK de Java 21. 
* Podríamos crear un `spring-boot-starter-inatrace` en el futuro compilado en Java 17, el cual sería perfectamente consumible por el proyecto de Java 21 (backward compatibility).

### 🔵 Sombrero Azul (Decisión Final Modificada)
**Veredicto: Se aprueba el uso de Java 17 y Spring Boot 3.3.3 (Paridad con el Core).**
* **Justificación:** Aunque Java 21 ofrece ventajas técnicas a futuro, la necesidad operativa del equipo de **desplegar, debugear y correr ambos proyectos simultáneamente** en el mismo entorno local (IntelliJ IDEA) pesa más. Mantener la paridad exacta (Java 17) elimina configuraciones complejas de múltiples JDKs a nivel de módulos en el IDE y facilita una orquestación local fluida sin sobrecarga mental.

---

## 4. Stack Tecnológico Definitivo (`ms-shrimp`)
* **Lenguaje:** Java 17 LTS
* **Framework:** Spring Boot 3.3.3
* **Persistencia:** Spring Data JPA / Hibernate 6.x
* **Base de Datos:** PostgreSQL (apuntando al esquema `dufer`)
* **Autenticación:** Spring Security OAuth2 Resource Server (Tokens vía Keycloak 26)
* **Construcción:** Maven (Garantizando consistencia con `coffee-backend`)

## 5. Diseño de Entidades JPA (`dufer.*`)
El Core (`public.*`) no se toca. El microservicio Java gestionará las tablas:
1. `ReceptionExt` (`@Table(schema = "dufer", name = "reception_ext")`)
2. `Classification`
3. `ClassificationDetail`
4. `ProductiveDestination`

**Acoplamiento de Claves Foráneas (Soft References):**
`ms-shrimp` guardará los IDs del Core (ej. `Long stockOrderId`) sin declarar relaciones foráneas duras (`@ManyToOne`) a entidades físicas del Core en código. Las lecturas o validaciones cruzadas se ejecutarán mediante clientes HTTP (`RestClient`) hacia la API del Core.

## 6. Siguientes Pasos
1. Inicializar el repositorio/directorio `ms-shrimp` usando Spring Initializr con **Java 21**.
2. Escribir el `Dockerfile` específico para JDK 21.
3. Configurar conexión BD y los parámetros OIDC para validación Keycloak en el `application.properties`.
