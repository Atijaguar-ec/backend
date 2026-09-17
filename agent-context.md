# Backend Agent Context — INATrace Cacao (Fortaleza del Valle / UNOCACE)

> **Propósito:** Este archivo documenta las decisiones arquitectónicas, convenciones y reglas
> de este backend. Cualquier agente de IA o desarrollador que trabaje aquí **DEBE** leer este
> archivo antes de hacer cambios. Su objetivo es **evitar regresiones**.
>
> Este documento cubre lo **común** a ambas empresas (stack, convenciones de
> naming, reglas de migraciones). Para infraestructura y despliegue específicos
> de cada una, ver:
> - `backend/scripts/fortaleza/agent-context.md` (Jenkins, servidores ESPAM/CEDIA)
> - `backend/scripts/unocace/agent-context.md` (GitHub Actions, servidor 95.217.182.220)
> - `ina-docs/despliegue/matriz-fortaleza-unocace.md` (qué difiere entre ambas)

---

## 1. Identidad del Proyecto

| Atributo | Valor |
|---|---|
| **Producto** | INATrace — Plataforma de Trazabilidad Agrícola |
| **Especialización** | Cacao (única). NO es multi-producto. |
| **Organización** | Fortaleza del Valle / UNOCACE |
| **Rama principal de trabajo** | `agstack_dev` |
| **Repositorio** | `Atijaguar-ec/backend` (GitHub) |

---

## 2. Stack Tecnológico

| Componente | Tecnología | Versión |
|---|---|---|
| **Lenguaje** | Java | 17 |
| **Framework** | Spring Boot | 3.3.3 |
| **Persistencia** | Hibernate / JPA | 6.x (Jakarta EE) |
| **Base de datos** | PostgreSQL | 16 (verificado en ambos servidores: `postgres:16.10` Fortaleza, `postgres:16-alpine` UNOCACE) |
| **Dialecto** | `CustomPostgreSQLDialect` | Registra funciones `MONTH`, `YEAR`, `WEEK` |
| **Migraciones** | Flyway | `flyway-database-postgresql` |
| **Autenticación** | Keycloak (OAuth2 Resource Server) | — |
| **Build** | Maven | — |

### Configuración de Naming y Schema
```properties
# Hibernate preserva los nombres Java tal cual (Facility → Facility).
# PostgreSQL foldea identificadores sin comillas a minúsculas → facility, stockorder, etc.
spring.jpa.hibernate.naming.physical-strategy = org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl
spring.jpa.hibernate.naming.implicit-strategy = org.hibernate.boot.model.naming.ImplicitNamingStrategyComponentPathImpl

# Hibernate crea/actualiza el esquema al arrancar; Flyway corre DESPUÉS para DDL adicional + seeds.
spring.jpa.properties.hibernate.hbm2ddl.auto = update

# Flyway
spring.flyway.baseline-on-migrate = true
spring.flyway.out-of-order = true
spring.flyway.validate-on-migrate = false
spring.flyway.table = schema_version
spring.flyway.locations = com.abelium.inatrace.db.migrations,classpath:/db/migrations
```

> **REGLA:** `hbm2ddl.auto = update` crea las tablas base al arrancar.
> Flyway se usa para DDL que Hibernate no puede inferir (defaults, constraints, seeds).
> Las migraciones Flyway deben ser **idempotentes** (`IF NOT EXISTS` / `IF EXISTS`).

---

## 3. Convenciones de Naming

### Base de datos (PostgreSQL) — TODO MINÚSCULAS
PostgreSQL foldea identificadores sin comillas a minúsculas. Hibernate envía los nombres Java
sin comillas, por lo que la tabla real siempre es lowercase:

| Java (entidad) | PostgreSQL (tabla real) |
|---|---|
| `Facility` | `facility` |
| `FacilityType` | `facilitytype` |
| `StockOrder` | `stockorder` |
| `UserCustomer` | `usercustomer` |
| `CertificationType` | `certificationtype` |

- **Tablas:** lowercase sin separadores → `stockorder`, `facilitytype`, `usercustomer`
- **Columnas:** lowercase sin separadores → `weeknumber`, `parcellot`, `organiccertification`
- **Foreign keys:** `{entidad_referenciada}_id` lowercase → `company_id`, `facilitytype_id`
- **Embeddables:** prefijo del campo + `_` → `farm_maxproductionquantity`, `bank_accountnumber`
- **Constraints:** `uk_{descripcion}` → `uk_certification_type_code`

> **REGLA CRÍTICA:** NUNCA usar doble-comillas (`"Facility"`) en migraciones Flyway SQL
> ni en Java migrations, **excepto** para palabras reservadas de PostgreSQL
> (ej: `"order"` en FacilityType). Usar siempre identificadores sin comillas en minúsculas.

### Código Java
- **Entidades:** PascalCase, mismo nombre que la tabla → `StockOrder.java`
- **Campos:** camelCase → `private Integer weekNumber;`
- **DTOs (API models):** Prefijo `Api` → `ApiStockOrder.java`
- **Mappers:** Sufijo `Mapper` → `StockOrderMapper.java`
- **Services:** Sufijo `Service` → `StockOrderService.java`
- **Controllers:** Sufijo `Controller` → `StockOrderController.java`
- **Repositorios:** Sufijo `Repository` → No se usan Spring Data repos; se usa `EntityManager` + Torpedo Query.

### Migraciones Flyway
- **SQL:** `V{YYYY_MM_DD_HH_MM}__{Descripcion}.sql` en `src/main/resources/db/migrations/`
- **Java:** `V{YYYY_MM_DD_HH_MM}__{Descripcion}.java` en `com.abelium.inatrace.db.migrations` (extienden `BaseJavaMigration`)
- **Sintaxis:** PostgreSQL nativo. **NUNCA** MySQL (`SET @var`, `PREPARE/EXECUTE`, `INFORMATION_SCHEMA`-MySQL-style).
- **Baseline limpio:** Las migraciones legadas de 2023 fueron eliminadas (2026-04-13). Solo existen migraciones V2026+.
- **Idempotencia obligatoria:** Todo `ALTER TABLE` debe usar `IF NOT EXISTS` / `IF EXISTS`.
- **Tablas/columnas en SQL Flyway:** Siempre lowercase sin comillas. Ej: `ALTER TABLE stockorder ADD COLUMN IF NOT EXISTS weeknumber INT;`

---

## 4. Campos de Cacao Agregados (Inventario de Referencia)

Estos campos fueron portados desde `staging` (MySQL) y adaptados a PostgreSQL.
**NO deben eliminarse ni modificarse sin revisión explícita.**

### `StockOrder`
| Campo | Tipo | Propósito |
|---|---|---|
| `weekNumber` | `Integer` | Semana de entrega (1–53), trazabilidad estacional |
| `parcelLot` | `String(255)` | Lote de parcela de origen |
| `variety` | `String(255)` | Variedad genética: `NACIONAL`/`CCN51`, o `"1"`/`"2"` con `numericVarietyOptions` (UNOCACE, §18) |
| `organicCertification` | `String(255)` | Certificación del lote: el **nombre ES** del catálogo, como texto (§18) |
| `moisturePercentage` | `BigDecimal` | Porcentaje de humedad medido |
| `moistureWeightDeduction` | `BigDecimal` | Deducción de peso por humedad |
| `netQuantity` | `BigDecimal(38,2)` | Peso neto tras descuentos |
| `finalPriceDiscount` | `BigDecimal(38,2)` | Descuento al precio final |

### `Facility`
| Campo | Tipo | Propósito |
|---|---|---|
| `level` | `Integer` | Nivel jerárquico del área |
| `displayFinalPriceDiscount` | `Boolean` | Flag: mostrar campo descuento en UI |
| `displayMoisturePercentage` | `Boolean` | Flag: mostrar campo humedad en UI |

### `FacilityType`
| Campo | Tipo | Propósito |
|---|---|---|
| `order` | `Integer` | Orden de despliegue visual en UI |

### `FarmInformation` (embebida en `UserCustomer`)
| Campo | Tipo | Propósito |
|---|---|---|
| `maxProductionQuantity` | `BigDecimal` | Capacidad máxima producción (qq) |

### `UserCustomer`
| Campo | Tipo | Propósito |
|---|---|---|
| `personType` | `PersonType` enum | `NATURAL` / `LEGAL` |
| `companyName` | `String` | Razón social (persona jurídica) |
| `legalRepresentative` | `String` | Representante legal |
| `status` | `UserCustomerStatus` enum | `ACTIVE`/`SUSPENDED`/`RETIRED`. **Nullable a propósito** (ver §12); nulo == `ACTIVE` |
| `statusReason` | `String(255)` | Motivo del último *cambio* de estado |
| `statusUpdateTimestamp` | `Instant` | Cuándo cambió el estado |
| `statusUpdatedBy` | `User` (FK) | Quién lo cambió |

Reglas del estado (2026-08-14):
- Transiciones válidas: `ACTIVE`↔`SUSPENDED`, ambos → `RETIRED`, y `RETIRED` → `ACTIVE`
  (el agricultor puede reincorporarse). **No** `RETIRED` → `SUSPENDED`.
  Validado en `UserCustomerStatus.canTransitionTo()`.
- La auditoría registra **cambios**, no altas: crear un agricultor `ACTIVE` deja
  los tres campos de auditoría nulos. Volver a `ACTIVE` **limpia** `statusReason`.
- `status` nulo entrante en un update **no toca** el estado actual (clientes que
  no gestionan el campo pueden seguir editando el resto).

### `Plot`
| Campo | Tipo | Propósito |
|---|---|---|
| `productionEstimate` | `BigDecimal` → `numeric(38,2)` | Estimación de producción (2 decimales) |
| `certificationType` | **FK a `CertificationType`** | Tipo de certificación de la parcela |
| `cocoaVariety` | `CocoaVariety` enum | `ORGANICO` / `CCN51` |

> **No conviertas `certificationType` en un enum.** Se intentó el 2026-08-14 y hubo
> que revertirlo: apunta al **catálogo administrable** `CertificationType`
> (Ajustes → Tipos de certificación), que es **el mismo** que alimenta el campo
> "Tipo de certificación" del formulario de Recepción. Un enum crea un vocabulario
> paralelo que diverge del catálogo en cuanto alguien renombra un valor en Ajustes.
> Si faltan valores, se agregan **desde Ajustes**, no tocando código.
>
> Ojo con la asimetría existente: Recepción guarda su certificación en
> `StockOrder.organicCertification` como **el nombre en texto**, no como FK. Es
> anterior y no se refactorizó; la parcela sí usa FK porque nació sin datos.

### Tablas Nuevas: `CertificationType` + `CertificationTypeTranslation`
Catálogo de certificaciones orgánicas y sellos ambientales con soporte i18n.

### Tabla Nueva: `PlotDeforestationAnalysis`
Resultado de los análisis de deforestación de Whisp por parcela (§14). Historial, no
una fila por parcela: el expediente EUDR tiene que poder mostrar qué decía el análisis
en la fecha de la compra. **Todas las columnas son nullable** (§12) y el índice
`idx_plotdeforestationanalysis_plot` lo crea la migración
`V2026_08_27_10_00__Add_Plot_Deforestation_Analysis`.

---

## 5. Lógica de Negocio Crítica

### Cálculo de Peso Neto (`StockOrderService`)
```
Base              = Bruto − Tara − PesoDañado
DescuentoHumedad  = Base × Humedad / 100        (2 decimales, HALF_UP)
Net               = Base − DescuentoHumedad
cost              = (pricePerUnit − damagedPriceDeduction) × (Net ?? totalQuantity) − finalPriceDiscount
```
- Método: `calculateNetQuantity(ApiStockOrder api, StockOrder entity)`.
- Se calcula automáticamente al guardar un `StockOrder` tipo `PURCHASE_ORDER`.
- `cost` no se redondea en Java: lo redondea la columna `numeric(38,2)` al persistir.
- ⚠️ Hasta 2026-09-15 este archivo decía `Net = (Bruto − Tara − PesoDañado) × (Humedad / 100)`.
  Eso es el **descuento**, no el neto. No "corrijas" el código hacia esa fórmula.
  El orden completo del guardado y lo que exige a los clientes está en §16.

### Orgánico y certificación por defecto (`StockOrderService`)
CCN51 (`"CCN51"` o `"2"`) sin `organic` enviado queda como no orgánica; si una entrega
no orgánica llega sin certificación, se completa con la primera no orgánica del
catálogo. Lo enviado se respeta. Detalle y trampas en §18.

### Reportes Agrupados (`GroupStockOrderService`)
Las queries JPQL de agrupación incluyen: `weekNumber`, `parcelLot`, `variety`,
`organicCertification`, `facilityName`, `farmerName` en los `SELECT` y `GROUP BY`.
La exportación Excel genera columnas adicionales para estos campos.

---

## 6. Reglas de Prohibición (Anti-Patrones)

### ❌ NUNCA hacer

1. **NO agregar lógica de camarón (shrimp).** Este backend es exclusivo de Cacao.
   El micro-frontend `shrimpMfe` es un proyecto separado que NO se despliega en staging/producción de Cacao.

2. **NO usar sintaxis MySQL en migraciones Flyway.**
   - ❌ `SET @var := ...`; `PREPARE ... EXECUTE`; `INFORMATION_SCHEMA` con `TABLE_SCHEMA = DATABASE()`
   - ✅ `ALTER TABLE stockorder ADD COLUMN IF NOT EXISTS weeknumber INT;`

3. **NO usar doble-comillas en identificadores SQL** (excepto palabras reservadas como `"order"`).
   - ❌ `ALTER TABLE "StockOrder" ADD COLUMN "weekNumber" INT;`
   - ✅ `ALTER TABLE stockorder ADD COLUMN IF NOT EXISTS weeknumber INT;`
   PostgreSQL foldea todo a lowercase; poner comillas fuerza case-sensitivity y rompe todo.

4. **NO eliminar campos de Cacao** del inventario de la sección 4 sin revisión explícita del equipo.

5. **NO usar `globally_quoted_identifiers = true`.**
   Esto quoteaba también `columnDefinition` (`"TEXT"`) causando errores de tipo PostgreSQL.
   Se documentó como anti-patrón el 2026-04-13.

6. **NO usar `TokenAuthenticationFilter` ni JWT local.**
   La autenticación se maneja vía `KeycloakJwtAuthenticationConverter` + Spring Security OAuth2 Resource Server.

7. **NO crear nuevas entidades de camarón** (`LaboratoryAnalysis`, `FieldInspection`,
   `ProcessingClassificationBatch`, etc.). Fueron eliminadas intencionalmente.

8. **NO agregar flags de proceso de camarón a `Facility`:**
   `isFieldInspection`, `isLaboratory`, `isClassificationProcess`, `isFreezingProcess`,
   `isCuttingProcess`, `isDeheadingProcess`, `isRestArea`, `isWashingArea`, `isTreatmentProcess`, `isTunnelFreezing`.

9. **NO usar `PersonType` con más valores que `NATURAL` y `LEGAL`.**
   No existen tipos intermedios.

10. **NO reintroducir `CompanyProcessingAction`** en esta fase.
    Fue evaluada y excluida del alcance de la versión premium Cacao.
    El sistema usa directamente la entidad `ProcessingAction` original.

11. **NO usar `CACAO` como valor de variable de entorno.** El valor interno
    correcto es `COCOA` (`INATRACE_PRODUCT_TYPE`, `PRIMARY_PRODUCT_TYPE`).
    `CACAO`/"Cacao" es únicamente texto de interfaz para el usuario final.
    Verificado 2026-07-28: `INATRACE_PRODUCT_TYPE` no lo lee ningún código del
    backend (dead config), y en frontend `normalizeProductType()` solo baja a
    minúsculas sin traducir sinónimos — un valor `CACAO` no rompe el nombre
    mostrado (cae al `default:` del switch) pero sí rompe comparaciones exactas
    como `isProductType('COCOA')` y la resolución de íconos. Ver
    `ina-docs/despliegue/matriz-fortaleza-unocace.md` sección 3 para el detalle
    completo y los archivos que hoy tienen el valor incorrecto.

---

## 6bis. Patrón CRUD de Catálogos (Codebook) — Checklist Obligatorio

> Aplica a cualquier controller bajo `components/codebook/*` (`FacilityType`,
> `MeasureUnitType`, `ProductType`, `CertificationType`,
> `ProcessingEvidenceType`, etc.) y a cualquiera nuevo que se agregue con el
> mismo patrón. Extraído en vivo el 2026-07-29 tras encadenar 6 bugs distintos
> al portar la administración de `CertificationType` — cada uno pasaba
> inadvertido porque el anterior lo enmascaraba (guardaba pero no cerraba el
> modal, o el modal cerraba pero mostraba datos vacíos, etc.). Revisar esta
> lista completa **antes** de dar por terminado un catálogo nuevo o portado,
> no solo hasta que "deje de tirar error".

1. **`createOrUpdate` DEBE devolver `ApiResponse<ApiBaseEntity>`, nunca la
   entidad completa directa.** El frontend (`type-detail-modal.component.ts`)
   chequea `res.status === 'OK'` para decidir si cierra el modal y dispara el
   `saveCallback` (que refresca la lista). Si el endpoint devuelve el DTO
   pelado, `res.status` no existe (o, peor, coincide por casualidad con un
   campo de la propia entidad llamado `status`, como el `ACTIVE`/`INACTIVE`
   de `CertificationType`) y esa condición nunca es `true`: el guardado
   funciona en la base, pero la UI queda como si nada hubiera pasado.
   ```java
   // ❌
   public ApiCertificationType createOrUpdate(...) { return service...; }
   // ✅ (patrón real de FacilityTypeController/ProductTypeController)
   public ApiResponse<ApiBaseEntity> createOrUpdate(...) { return new ApiResponse<>(service...); }
   ```

2. **`delete` DEBE devolver `ApiDefaultResponse`, nunca `void`.** Mismo
   motivo: el frontend espera `res.status === 'OK'` antes de refrescar tras
   borrar.

3. **El método HTTP debe coincidir exactamente con el cliente ya generado en
   `fe/apps/inatrace-fe/src/api/api/<Tipo>ControllerService.ts`.** No asumas
   `@PostMapping` para "crear o actualizar" solo porque es lo más intuitivo:
   la convención real de este cliente (generado por swagger-codegen) es
   `PUT`. Si el backend mapea `@PostMapping` y el cliente manda `PUT`, da
   `405 Method Not Allowed`. Revisar el `.service.ts` real antes de decidir.

4. **El listado paginado (`GET .../list`) debe usar el mapper "completo" (con
   `translations`), no el mapper "Base".** El modal de edición
   (`type-list.component.ts`'s `edit(type)`) recibe la FILA de esta lista
   directamente como `typeElement`, sin volver a pedir el detalle por id. Si
   el mapper de listado omite `translations` (como hace la variante "Base",
   pensada para dropdowns de solo-lectura), el formulario de edición se ve
   con la Etiqueta vacía en todos los idiomas aunque el dato exista en la
   base. Ver `ProductTypeService.getProductTypes` → `toApiProductTypeDetailed`
   como referencia correcta.

5. **El mapper de traducción debe caer al valor base de la entidad si no hay
   traducción para el idioma pedido — nunca a un objeto de traducción vacío
   recién creado.**
   ```java
   // ❌ (bug real en ProductTypeMapper hasta 2026-07-29)
   translation = ....findFirst().orElse(new ProductTypeTranslation());
   apiDto.setName(translation.getName()); // null si no hay traducción
   // ✅ (patrón correcto, ya en CertificationTypeMapper)
   apiDto.setName(translation != null ? translation.getName() : entity.getName());
   ```
   En UNOCACE, la mayoría de los registros migrados desde MySQL solo tienen
   traducción en **español**, ninguna en inglés — este bug se dispara casi
   siempre, no es un caso límite raro.

6. **`entity.getTranslations().clear()` + repoblar en la misma transacción
   requiere un `em.flush()` explícito entre medio**, si la colección tiene un
   unique constraint compuesto (ej. `(entity_id, language)`) con
   `orphanRemoval = true`. Sin el flush, Hibernate puede ejecutar el INSERT
   de la traducción nueva antes del DELETE de la vieja en el mismo flush
   automático, chocando contra el constraint (`duplicate key value violates
   unique constraint`) al reescribir el mismo idioma que ya existía.

7. **Cuidado con `GROUP BY` en el conteo paginado (Torpedo + `orderBy`) bajo
   Postgres estricto** — mismo síntoma que el bug de `DashboardService`
   documentado en la sección de Lógica de Negocio: una query de `COUNT`
   que hereda un `ORDER BY` de una columna no agregada falla en Postgres
   aunque fuera tolerado en MySQL. `PaginationTools.createPaginatedResponse`
   ya tiene un `catch` con fallback de conteo estimado para este caso, pero
   si ves este error en logs para un catálogo nuevo, vale la pena investigar
   la causa real en vez de confiar en el fallback silencioso.

---

## 7. Decisiones Arquitectónicas Documentadas

| Decisión | Razón | Fecha |
|---|---|---|
| Excluir `CompanyProcessingAction` | Solo era un CRUD de personalización por empresa; no es requerido para Cacao | 2026-04-09 |
| Excluir flags de camarón en `Facility` | Son específicos de la operativa camaronera (inspección, laboratorio, clasificación, congelado) | 2026-04-09 |
| Consolidar migraciones en 1 script PostgreSQL | Las migraciones de staging eran incrementales MySQL con muchos fixes/rollbacks; en Postgres partimos limpio | 2026-04-09 |
| Naming lowercase en PostgreSQL | `PhysicalNamingStrategyStandardImpl` sin quoting → PostgreSQL auto-foldea a lowercase. Elimina toda complejidad de comillas dobles | 2026-04-13 |
| Eliminar migraciones V2023 legacy | Eran `DROP COLUMN` de campos que ya no existen en los modelos Java; inútiles en un arranque limpio | 2026-04-13 |
| `hbm2ddl.auto = update` + Flyway | Hibernate crea el esquema base; Flyway aporta DDL idempotente y seeds como safety net | 2026-04-13 |
| Idiomas reducidos a EN/ES | Solo se soportan inglés y español en producción | 2026-04-09 |
| Eliminar `quality_document_id` de StockOrder | Era para documentos de laboratorio de camarón; no aplica a Cacao | 2026-04-09 |
| Migración de Infraestructura Staging a PostgreSQL | Se clona el PostgreSQL limpio en Port 5432 paralelo a MySQL en el Remote Server de `test/fortaleza` para compatibilidad dual | 2026-04-10 |

---

## 8. Herramientas de Sanitización de Datos (MySQL -> PostgreSQL)

El directorio `scripts/` contiene las herramientas en Python necesarias para lidiar con el despliegue a Staging/Producción cuando se requiere la data viva del antiguo entorno MySQL:

- **`migrate_all_cacao_data.py`:**
  Script Python diseñado para extraer datos en bruto desde MySQL vía SSH Tunnel. Contiene regras de sanitización críticas (ej. rechazar `LARVA_GROWING` y variables de camarón, mapear booleanos `BIT(1)` a Strings para Postgres, rellenar defaults `entityversion=0`). Desactiva foreign key checks vía `session_replication_role = 'replica'` temporalmente.
- **Flujo de Restore Seguro:**
  La infraestructura remota asimila de mejor forma el volcado (`pg_dump`) de un contenedor *PostgreSQL Local* pre-poblado por el script, que ejecutar la ingesta remota múltiple de diccionarios.

---

## 9. Estructura del Proyecto

```
backend/
├── src/main/java/com/abelium/inatrace/
│   ├── components/           # Controllers, Services, Mappers, DTOs (por dominio)
│   │   ├── agstack/          # Integración AgStack
│   │   ├── codebook/         # Catálogos (FacilityType, SemiProduct, CertificationType...)
│   │   ├── company/          # Empresa, UserCustomer, CompanyUser
│   │   ├── facility/         # Áreas / Centros de acopio
│   │   ├── groupstockorder/  # Reportes agrupados + Excel
│   │   ├── processingaction/ # Acciones de procesamiento
│   │   ├── product/          # Productos
│   │   ├── stockorder/       # Lotes de entrega (core de trazabilidad)
│   │   └── ...
│   ├── configuration/        # Spring Security (Keycloak), CustomPostgreSQLDialect
│   ├── db/
│   │   ├── base/             # BaseEntity, TimestampEntity
│   │   ├── entities/         # Entidades JPA
│   │   ├── enums/            # Enumeraciones
│   │   └── migrations/       # Migraciones Flyway Java (JpaMigration)
│   └── tools/                # PaginationTools, TorpedoProjector
├── src/main/resources/
│   ├── application.properties
│   ├── application-{dev,staging,prod}.properties
│   ├── db/migrations/        # Migraciones Flyway SQL
│   └── i18n/                 # Mensajes internacionalizados
└── pom.xml
```

---

## 10. Checklist Pre-Commit

Antes de hacer commit de cualquier cambio en el backend, verificar:

- [ ] ¿El cambio es exclusivo de Cacao? (No toca nada de camarón)
- [ ] ¿Las migraciones SQL son PostgreSQL nativo? (No MySQL)
- [ ] ¿Los nombres de tablas/columnas siguen las convenciones de la sección 3?
- [ ] ¿Se actualizaron los DTOs (`Api*`) correspondientes?
- [ ] ¿Se actualizaron los Mappers correspondientes?
- [ ] ¿Se actualizó este archivo `agent-context.md` si se agregaron campos/tablas?
- [ ] ¿El proyecto compila con `mvn clean compile`?
- [ ] **¿Toda columna nueva sobre una tabla con datos es `nullable`?** (§12 — una
      columna `NOT NULL` falla en silencio y la app arranca sin ella)
- [ ] Si la columna es nullable pero el dominio tiene un valor por defecto,
      ¿se normaliza el nulo **tanto en el getter como en la query SQL**? (§12.3)
- [ ] Si se agregaron claves i18n, ¿los acentos van como `\uXXXX`? (§13 — los
      `.properties` son ISO-8859-1)
- [ ] **¿Toda búsqueda de texto nueva usa `TorpedoFunction.lower()` en los dos
      lados?** (§15.3 — en PostgreSQL un `.like()` pelado ignora lo que el
      usuario escribió en minúscula, sin dar error). Verificar con:
      `grep -rn "\.like()" src/main/java --include="*.java" | grep -v "TorpedoFunction.lower"`
- [ ] Si el cambio agrega una opción por empresa, ¿la clave **ausente** reproduce
      exacto el comportamiento anterior? (§15.1)
- [ ] Si tocaste el cálculo de la semana, ¿actualizaste también
      `fe/src/app/shared-services/week-number.util.ts`? (§15.2 — son espejos y el
      backend es el que persiste)
- [ ] Tras desplegar: ¿el log **no** tiene `GenerationTarget encountered exception`
      y la columna **existe** en `information_schema.columns`? (§12.2)
- [ ] Si tocaste `createOrUpdateStockOrder`, `updateUserCustomer` o sus permisos,
      ¿revisaste el contrato de §16? El ERP contable de Fortaleza depende de ese
      comportamiento exacto: una validación nueva hace fallar compras que hoy pasan.

---

## 11. Despliegue a Producción Fortaleza — Trampas Verificadas en Vivo

> Extraído el **2026-08-06/07** durante el primer despliegue real a producción
> (`gizpro`, `10.10.102.26` interno / `190.15.143.192` público) tras la
> migración a PostgreSQL 16 + Keycloak 26. Todo lo de abajo fue **verificado
> contra los servidores**, no inferido leyendo archivos. Varias afirmaciones
> aquí **contradicen** versiones anteriores de la documentación: cuando eso
> pasa, gana lo que está acá.

### 11.1 Los dos entornos de Fortaleza NO son intercambiables

| | Staging `190.15.143.254` | Producción `10.10.102.26` |
|---|---|---|
| Usuario SSH | `giz` | `administrador` |
| Base de datos app | `inatrace_fortaleza_stage` | `inatrace_prod_fortaleza` |
| Usuario de BD | `inatrace` | `inatrace_prod_fortaleza` |
| **Realm de Keycloak** | **`fortaleza`** | **`inatrace_fortaleza`** |
| `DB_VOLUME` | `/opt/inatrace/postgresql_data_test` | `/opt/inatrace/postgresql_data` |
| Almacenamiento de archivos | `/opt/inatrace/uploads` | `/opt/inatrace/file_storage` |
| Cómo despliega el Jenkinsfile | local al agente, solo `inatrace-backend` | remoto vía SSH, **todos** los servicios |

**Nunca copies configuración de un entorno al otro sin traducir estos valores.**
El realm en particular ha causado documentación equivocada dos veces: el
default `INATRACE_KEYCLOAK_ADMIN_REALM=${KEYCLOAK_ADMIN_REALM:-inatrace_fortaleza}`
del compose es **correcto para producción** y equivocado para staging.

### 11.2 El deploy a producción recrea la base de datos

El stage `🚀 Deploy Fortaleza` ejecuta, solo en la rama `main`:

```bash
docker compose up -d --remove-orphans --force-recreate
```

**Sin `--no-deps` y sin nombre de servicio** — o sea recrea `inatrace-postgres`,
`inatrace-keycloak` **y** `inatrace-backend`. Staging en cambio usa
`--no-deps ... inatrace-backend` y solo toca el backend.

Consecuencia crítica: si `DB_VOLUME` del `.env` no apunta **exactamente** al
directorio que el contenedor de Postgres usa hoy, el contenedor recreado
arranca sobre un data dir vacío y **la base de producción queda en blanco sin
ningún error visible**. Antes de cualquier deploy a `main`, correr:

```bash
python3 backend/scripts/fortaleza/preflight_prod.py \
    --ssh administrador@190.15.143.192 -i ~/.ssh/cedia_key --backup
```

Ese script (solo lectura salvo `--backup`) compara el `DB_VOLUME` del `.env`
contra el montaje real y aborta con código 1 si difieren. Vive en
`backend/scripts/`, que **está en `.gitignore` completo** — no aparece en el
repo; si no lo encontrás, hay que recrearlo.

### 11.3 `environment{}` de Jenkins pisa las asignaciones en runtime

Bug real que impidió el primer deploy a producción (corregido en `cf75f374`):
`DB_BACKUP_STATUS` estaba declarado en el bloque `environment{}` del
Jenkinsfile. **Jenkins reinyecta ese bloque en cada stage**, así que la
asignación `env.DB_BACKUP_STATUS = 'COMPLETADO'` hecha dentro del stage de
backup quedaba pisada al entrar al stage siguiente, y la guarda de aprobación
abortaba el despliegue aunque el respaldo se hubiera creado correctamente.

**Regla:** cualquier variable que un stage deba modificar en runtime va
inicializada en un bloque `script`, **nunca** en `environment{}`.

### 11.4 Heredocs hacia SSH: comillar el delimitador

El stage de backup usaba `cat <<EOSSH` (sin comillar) mientras el de deploy
usaba `cat <<'EOSSH'` (comillado). Sin comillar, el `$(du -h ...)` de la
verificación se ejecutaba **en el agente Jenkins** en vez del servidor remoto,
donde el archivo no existe — ensuciando el log con
`du: cannot access ... No such file or directory` y mostrando el tamaño vacío.

Ojo con la interacción de Groovy: dentro de `sh '''...'''`, Groovy procesa
`\$` y lo convierte en `$` **antes** de que el shell lo vea, así que escapar
con backslash no protege nada. **Siempre comillar el delimitador**
(`<<'EOSSH'`) y pasar las variables por el entorno del `ssh`:

```groovy
cat <<'EOSSH' | ssh ${SSH_OPTS} "${TARGET}" "VAR='${VAR}' bash -s"
```

### 11.5 Los archivos subidos NO se migran con la base de datos

Producción tenía **53 registros en `document`** pero **1 solo archivo** en
`/opt/inatrace/file_storage`. El endpoint `/api/public/image/{key}` devolvía
500 con `java.nio.file.NoSuchFileException`. La migración copió las filas de
la BD pero no el contenido del disco.

Al migrar o clonar un entorno, **el `pg_dump` no alcanza**: hay que copiar
también el árbol de almacenamiento, respetando que la ruta difiere por entorno
(ver 11.1). Estructura: `GENERAL/` (documentos) e `IMAGE/` con las variantes
`SMALL`, `MEDIUM`, `LARGE`, `XLARGE`, `XXLARGE` más el archivo base.

`GENERAL/` en producción pertenece a `root`, así que escribir ahí requiere
`sudo` (que pide contraseña en ese host); `IMAGE/` pertenece a `administrador`
y sí es escribible por SSH.

### 11.6 Exposición de puertos: no confiar en el compose ni en UFW

El compose versionado no declara `ports:` para Postgres, pero el contenedor que
corría en producción **sí publicaba `0.0.0.0:5432`** — se había creado con una
versión anterior del archivo. Docker inserta sus reglas de iptables **antes**
que UFW, así que el firewall puede parecer cerrado y el puerto estar abierto a
Internet igual.

**Verificá siempre con `docker ps --format '{{.Names}}\t{{.Ports}}'` en el
servidor, no leyendo el compose.** El deploy del 2026-08-07 cerró esa
exposición al recrear el contenedor.

### 11.7 🐛 ABIERTO: consultas con `GROUP BY` que funcionaban en MySQL

En producción se disparan continuamente, en cada carga de la pantalla de
Entregas:

```
SQLState: 42803
ERROR: column "so1_0.productiondate" must appear in the GROUP BY clause or be used in an aggregate function
ERROR: column "so1_0.updatetimestamp" must appear in the GROUP BY clause or be used in an aggregate function
```

MySQL tolera columnas no agregadas en `GROUP BY`; **PostgreSQL es estricto y
las rechaza**. Ya hubo un arreglo previo de esta misma familia (`f422aaf7`,
agregación del Dashboard).

Estado: **sin resolver**. Candidatos revisados:
`components/groupstockorder/GroupStockOrderService.java` (su `SELECT` y su
`GROUP BY` sí coinciden, así que la sospecha principal es el `ORDER BY`
dinámico construido con `request.sortBy`, que puede referenciar una columna
ausente del `GROUP BY`) y `components/dashboard/DashboardService.java`.

Al investigarlo: **no asumas que el `SELECT` es el culpable**; revisá el
`ORDER BY` armado dinámicamente y confirmá contra el SQL real que emite
Hibernate, no contra el JPQL del código.

### 11.8 Infraestructura del agente Jenkins

Jenkins corre en el servidor de **staging** (`190.15.143.254`), no en uno
dedicado. Dos jobs: `Deploy-Backend` y `Deploy-Frontend`, ambos manuales con
parámetro `BRANCH` (`staging` o `main`) — no hay trigger automático por push.

Presiones observadas el 2026-08-07: `/var` al 86% (≈10 GB solo de build cache
de Docker) y swap al 76%. Un reinicio del servicio Jenkins durante un build de
Angular mató el `docker build` con `context canceled`; el pipeline lo reporta
como fallo del despliegue, pero **no es un problema del código**. Antes de
culpar a un cambio, revisá si hubo `Resuming build ... after Jenkins restart`
en el log.


---

## 12. Migraciones de esquema: `hbm2ddl` gana la carrera a Flyway

> Extraído en vivo el 2026-08-14 tras un **500 en producción-staging de
> UNOCACE**. Documentado porque el modo de fallo es silencioso: la aplicación
> arranca sana y el error recién aparece cuando un usuario entra a la pantalla.

### 12.1 La trampa

`application.properties` tiene **dos mecanismos de esquema activos a la vez**:

```properties
spring.jpa.properties.hibernate.hbm2ddl.auto = update   # línea 60
spring.flyway.locations = com.abelium.inatrace.db.migrations, ...  # línea 71
```

El Flyway "custom" de este proyecto (`JpaMigrationStrategy`) recibe un
`EntityManagerFactory` **ya construido**, y construirlo dispara primero el
`hbm2ddl.auto=update` de Hibernate. O sea: **Hibernate intenta reconciliar el
esquema desde el mapeo de la entidad ANTES de que cualquier clase
`JpaMigration` llegue a ejecutarse.**

Consecuencia concreta, verificada en vivo: se agregó

```java
@Column(length = Lengths.ENUM, nullable = false)   // ❌
private UserCustomerStatus status = UserCustomerStatus.ACTIVE;
```

Hibernate generó `alter table usercustomer add column status varchar(40) not null`,
Postgres lo **rechazó** porque la tabla ya tenía 979 filas (no se puede agregar
`NOT NULL` sin default a una tabla poblada), Hibernate **solo lo registró como
`WARN` y siguió arrancando**, y la columna nunca se creó. La app quedó healthy;
el `500` recién apareció al abrir la pantalla de Agricultores:

```
ERROR: column uc1_0.status does not exist
```

### 12.2 Reglas

1. **Toda columna nueva sobre una tabla con datos va `nullable`.** Sin
   excepciones. Si conceptualmente no admite nulos, normalizá el nulo en el
   getter de la entidad y en las queries (ver 12.3), no en el DDL.
2. **Un `WARN` de `GenerationTarget encountered exception` en el arranque es un
   fallo de esquema**, no ruido. Verificalo así tras cualquier despliegue que
   agregue columnas:
   ```bash
   docker logs <contenedor-be> 2>&1 | grep -i "GenerationTarget encountered exception"
   ```
3. **No confíes en que tu `JpaMigration` corrió.** Verificá contra la BD real:
   ```sql
   SELECT column_name FROM information_schema.columns WHERE table_name = 'x';
   ```

### 12.3 El nulo tiene que normalizarse en DOS lugares, no en uno

Si una columna queda nullable pero el dominio dice "nulo == valor por defecto",
hay **dos caminos independientes** que leen ese dato y ambos necesitan la regla:

- **En memoria** — el getter de la entidad:
  ```java
  public UserCustomerStatus getStatus() {
      return status != null ? status : UserCustomerStatus.ACTIVE;
  }
  ```
- **En SQL** — Torpedo consulta la **columna cruda**; el default del getter no
  llega ahí. Ver `CompanyService.userCustomerStatusCondition()`:
  ```java
  equalsWanted.or(Torpedo.condition(userCustomer.getStatus()).isNull())
  ```

Olvidar el segundo hace que **todos los registros previos a la feature
desaparezcan en silencio** de cualquier pantalla que filtre por ese campo.

### 12.4 El resolver custom de Flyway puede no estar corriendo nunca

En UNOCACE staging, `schema_version` tiene **solo la fila `<< Flyway Baseline >>`**:
ninguna clase `JpaMigration` se ejecutó jamás ahí, ni siquiera las de julio. Las
columnas existen únicamente porque son nullable y las creó `hbm2ddl`.

**Implicación práctica:** escribí las migraciones `JpaMigration` de todos modos
(son el registro de intención y sirven en instalaciones limpias), pero **el
diseño no puede depender de que corran**. Por eso la regla 12.2.1 y la
normalización de 12.3 no son redundancia: son el mecanismo real.

---

## 13. Encoding de los bundles i18n (`.properties`)

> Extraído el 2026-08-14. Casi se despliega texto con mojibake.

`messages_{en,es,de,rw}.properties` son **ISO-8859-1**, y
`ResourceBundleMessageSource` (ver `CustomLanguageConfiguration`) los lee como
tal — no define `setDefaultEncoding`.

**Escribir acentos como UTF-8 directo produce mojibake** (`Estimación` →
`EstimaciÃ³n`). Los acentos van como escapes `\uXXXX`, que es el estilo de las
claves preexistentes:

```properties
export.plots.column.productionEstimate.label=Estimación de producción
```

Verificación (emula el lector de `java.util.Properties`):

```bash
python3 -c "
props={}
for line in open('src/main/resources/i18n/messages_es.properties', encoding='iso-8859-1'):
    if '=' in line and not line.startswith('#'):
        k,v=line.rstrip('\n').split('=',1)
        props[k]=v.encode('latin-1','backslashreplace').decode('unicode_escape')
print(props['export.plots.column.productionEstimate.label'])"
```

Cuidado adicional: un enum cuyo nombre supere **40 caracteres** no entra en
`Lengths.ENUM`. `PlotCertificationType.ORGANICO_UE_NOP_BIOSUISSE_NATURLAND_FAIRTRADE_SPP`
tiene 49 y usa `Lengths.DEFAULT` — si se declara con `Lengths.ENUM` se trunca.

---

## 14. Integraciones geoespaciales: AgStack + Whisp

> Escrito el **2026-08-27** al conectar Whisp; revisado el **2026-08-31**. Lo marcado
> como *verificado* se probó contra la API pública real, no contra la documentación.
> Lo marcado como *supuesto* NO se comprobó: tratarlo como pendiente, no como hecho.

| | |
|---|---|
| 14.1 | Cómo encajan AgStack y Whisp, y las dos vías hacia Whisp |
| 14.2 | Hechos verificados de la API de Whisp |
| 14.3 | Hechos verificados de AgStack — **empezar acá si algo falla** |
| 14.4 | La cadena completa, verificada en vivo el 2026-08-31 |
| 14.5 | Reglas de diseño |
| 14.6 | Configuración por entorno |
| 14.7 | Endpoints |

**Si el Geo-ID no se genera**, la escalera de diagnóstico está en §14.3 punto 3 y el modo
de fallo silencioso en §14.5. Guía operativa para humanos:
`ina-docs/operacion/analisis-deforestacion-whisp.md`.

### 14.1 Cómo encajan las dos piezas

```
Parcela (coordenadas)
   └─ AgStack  /register-field-boundary  →  geoId  (columna plot.geoid)
        └─ Whisp  /submit/geo-ids  (o /submit/wkt)  →  token de job
              └─ Whisp  /status/{token}  →  indicadores EUDR  →  plotdeforestationanalysis
```

AgStack **no** hace análisis de deforestación: solo registra el polígono y devuelve un
identificador global (`geoId`). El análisis lo hace Whisp (Open Foris / Forest Data
Partnership). Antes de 2026-08-27 el backend solo tenía la primera mitad.

No busques una implementación de referencia en el proyecto original: los tres repos
oficiales de INATrace —hoy en la organización `agstack` de GitHub, no en `INATrace`—
tampoco tienen integración por API con Whisp, pese a que el material de difusión del
proyecto la anuncia. Verificado el 2026-08-28 sobre `agstack/inatrace-backend`,
`agstack/inatrace-mobile` y `agstack/inatrace-frontend`.

> **Ojo: ya existe una segunda vía hacia Whisp, en el frontend.** El popup de cada
> parcela en el mapa (`shared/map/map.component.ts`) tiene un botón *Open in Whisp* que
> abre un modal con un `<iframe>` a
> `https://whisp.earthmap.org/?aoi=WHISP&boundary&geoId=...&layers={JRCForestMask,CocoaETH,OilPalmFDAP}&scripts={WhispSummary}&statisticsOpen=true`.
> Es **verificación visual**: no guarda nada, no deja fecha ni evidencia, y sólo aparece
> si la parcela tiene `geoId`. Ese botón es también el consumidor del endpoint
> `POST /company/userCustomers/{id}/plots/{plotId}/updateGeoID` (botón *Refresh* cuando
> falta el `geoId`). La integración por API de §14 no lo reemplaza: lo convierte en
> resultado almacenado y auditable. Verificado el 2026-08-28 leyendo el frontend.

### 14.2 Hechos verificados de la API de Whisp

1. **La cabecera es `x-api-key`**, y la key la emite la app web de Whisp desde la página
   de cuenta. Los tokens de Keycloak **no** sirven. Verificado: una key inválida
   responde `401 {"code":"auth_invalid_api_key","message":"Invalid or expired API key."}`.
2. **`/result-fields/lookup-datasets` devuelve CSV, no JSON**, a diferencia de todos los
   demás endpoints (que responden el sobre `{code, message, cause, data}`). Verificado:
   `HTTP 200`, 246 líneas, sin comillas. `WhispClientService.parseCsv()` lo convierte a
   JSON para que el resto del backend solo vea JSON.
3. **Un token es un job, no una parcela.** Un envío en lote devuelve un solo token con
   todas las features dentro. Por eso hoy se envía **una parcela por job**: repartir las
   features de un lote entre parcelas sin un `externalIdColumn` bien atado es una
   fuente silenciosa de resultados cruzados.
4. **Los resultados de Whisp son efímeros** (los snapshots de progreso expiran a los 10
   minutos). Persistir el resultado no es una optimización: es la única copia.
5. **Enviar por geo id exige además la cabecera `x-geoid-token`.** Que ese token sea el
   de AgStack es un **supuesto, no un hecho verificado**. En contra juega un dato duro:
   `GET /api/config` (público) declara `"geoidBaseUrl": "https://data.apps.fao.org/geoid"`,
   o sea que **Whisp resuelve los geo id contra un servicio de la FAO, no contra
   `api-ar.agstack.org`**. No está garantizado que un identificador registrado en AgStack
   sea resoluble ahí. Por eso `WhispAnalysisService` degrada a polígono cuando Whisp
   rechaza el geo id: la geometría es la misma y la parcela no se queda sin analizar.
6. El análisis es **asíncrono** (`analysisOptions.async = true`): submit encola y
   devuelve token, `/status/{token}` responde `202` con `percent` mientras corre y `200`
   con un `FeatureCollection` cuando termina.
7. **`GET /api/config` es público y no pide key.** Es el chequeo de alcanzabilidad más
   barato. Verificado el 2026-08-31, devuelve:
   `geometryLimitSync 250`, `geometryLimitAsync 5000`, `analysisTimeoutSyncSeconds 60`,
   `analysisTimeoutAsyncSeconds 600`, `maxRequestBodySizeKb 10240`,
   `openforisWhispVersion 3.0.0a17`, `geoidBaseUrl https://data.apps.fao.org/geoid`.
   Los límites de geometrías por job son el techo real de cualquier envío en lote futuro.
8. **La API distingue dos fallos de credencial**, y conviene no confundirlos al depurar:
   sin cabecera devuelve `401 auth_missing_api_key` ("API key is required for this
   request"), con una key que no sirve devuelve `401 auth_invalid_api_key`.

### 14.3 Hechos verificados de AgStack

1. **Las credenciales que emite la web de AgStack NO son las que consume este código.**
   `user-registry.agstack.org` → "Generate API Key and Client Secret" entrega un par
   (API Key hexadecimal + Client Secret) que se valida en
   `GET /verify-api-secret-keys` con las cabeceras `API-KEY` y `CLIENT-SECRET`. Pero
   `AgStackClientTokenManager` hace `POST {loginBaseURL}/login` con `{email, password}`
   y espera `access_token`. **Poner la API Key en el `.env` no habilita nada.** Esta
   confusión costó una sesión entera el 2026-08-30/31.
2. **El flujo correcto lo dio el soporte de AgStack por correo** (hilo del 1-9 de octubre
   de 2025, `docs/2026/Gmail-API Keys.pdf`). Son **dos pasos**, y el API Key / Client
   Secret **no intervienen en ninguno**:

   1. `POST` con `{email, password}` -> devuelve `access_token` y `refresh_token`.
   2. `POST https://api-ar.agstack.org/register-field-boundary` con
      `Authorization: Bearer <access_token>` y `{s2_index, wkt}`.

   Se intento exactamente lo contrario --mandar `API-KEY` y `CLIENT-SECRET` como cabeceras
   a `register-field-boundary`-- y devolvio **401**. Esa es la trampa: la web entrega esas
   credenciales pero la API de registro no las acepta.

   **El `access_token` del login si caduca** (el ejemplo del soporte trae `exp` a 4 horas)
   y su `sub` es un UUID de usuario, no el dominio. Por eso queda descartada la idea de
   usar el Client Secret como Bearer: aunque es un JWT sin caducidad, su `sub` es el
   dominio y no el UUID que el registro espera. El refresco cada 3 h de
   `AgStackClientTokenManager` es correcto.

3. **Cual host para el login: `user-registry.agstack.org`, no `api.terrapipe.io`.**
   El ejemplo del soporte usa `POST https://api.terrapipe.io/` --- la **raiz**, sin path.
   Pero este codigo hace `POST {loginBaseURL}/login`, y verificado el 2026-08-31:

   | Llamada | Respuesta |
   |---|---|
   | `POST https://user-registry.agstack.org/login` | `400 {"message":"Invalid request or missing credentials"}` |
   | `POST https://api.terrapipe.io/` | `400 {"message":"Invalid request or missing credentials"}` |
   | `POST https://api.terrapipe.io/login` | **404 Not Found** |

   Los dos primeros son la misma aplicacion con el mismo contrato; en `user-registry` la
   vista esta montada en `/` **y** en `/login` (ver su `app.py`), en terrapipe solo en `/`.
   Con `loginBaseURL = https://api.terrapipe.io` este cliente pegaria contra un 404.

   ```properties
   INATrace.agstack.baseURL      = https://api-ar.agstack.org
   INATrace.agstack.loginBaseURL = https://user-registry.agstack.org
   ```

   **Validado en vivo el 2026-08-31** desde el servidor de staging de UNOCACE: los tres
   hosts (`user-registry.agstack.org/login`, `api.terrapipe.io/` y `api-ar.agstack.org/login`)
   llegan al mismo servicio de autenticacion y responden identico. La configuracion de
   arriba es la correcta para este cliente.

   **Escalera de diagnostico del login**, util porque los codigos distinguen la causa:

   | Respuesta | Significa |
   |---|---|
   | `400 {"message":"Invalid request or missing credentials"}` | El cuerpo no llego o esta mal formado |
   | `401 {"message":"Incorrect Password!"}` | El correo existe, la clave no es la buena |
   | `404` HTML | Host/ruta equivocados (p.ej. `api.terrapipe.io/login`) |
   | `200` con `access_token` | Correcto |

   Que diga "Incorrect Password!" y no "usuario no encontrado" es informacion: confirma
   que el correo esta registrado y que el problema es solo la clave.

4. **El asset registry rechaza campos de mas de 1000 acres (~405 ha)**, confirmado por el
   soporte: fue la causa real del primer fallo reportado, con un WKT de prueba de
   **30.415 acres**. Devuelve **HTTP 200** (no un error) con
   `{"Field area (acres)": ..., "message": "Cannot register a field with Area greater than 1000 acres"}`
   y **sin `Geo Id`**. `generatePlotGeoID` lo registra en el log y devuelve `null`.
5. **Las tres respuestas de `register-field-boundary`**, confirmadas por el soporte, son
   justo las que mapea `ApiRegisterFieldBoundaryResponse`: `200` con `"Geo Id"` al
   registrar; `400` con `"matched geo ids"` si el poligono ya estaba registrado; y `200`
   con solo `message` si excede el area. El DTO existente es correcto.
4. **Las dos URLs son hosts distintos, y ni el upstream ni la documentación lo dicen.**
   El template de `agstack/inatrace-backend` trae las cuatro propiedades vacías, su
   `ci/.env` versionado no tiene ninguna clave `AGSTACK_*`, y ni el README ni la
   documentación técnica mencionan un solo dominio. Resuelto el 2026-08-31 probando ambos
   `/login` con credenciales basura:

   | Host | Respuesta a `POST /login` |
   |---|---|
   | `https://user-registry.agstack.org` | `400 {"message":"Invalid request or missing credentials"}` |
   | `https://api-ar.agstack.org` | sin respuesta (HTTP 000) |

   El primero devuelve JSON con el campo `message`, que es exactamente lo que mapea
   `ApiLoginErrorResponse` y consume el `.onStatus(BAD_REQUEST, ...)` del cliente. Por lo
   tanto:

   ```properties
   INATrace.agstack.baseURL      = https://api-ar.agstack.org        # register-field-boundary
   INATrace.agstack.loginBaseURL = https://user-registry.agstack.org # login
   ```

   El host del asset registry está vivo (`/swagger/` responde 200; `/health` no existe).
   **Pendiente:** que unas credenciales reales sean aceptadas. Lo verificado es la forma
   de la respuesta, no el login completo.
5. **El cliente AgStack del upstream es byte-idéntico al de este fork** antes de las
   correcciones del 2026-08-27 (`diff` sobre `AgStackClientService` y
   `AgStackClientTokenManager`, rama `main`, push del 2026-08-30). Los seis defectos
   corregidos en §14.5 **siguen presentes upstream**: no eran una degradación local.

### 14.4 Cadena completa verificada en vivo

**2026-08-31, UNOCACE staging, parcela 616** (agricultor 523, 1,51 ha, 47 vertices):
AgStack registro el poligono y devolvio
`b3eb95f47f201e73c8a88cbe46078ae13dd6510cd0767f0bd33c8c310244d0d3`, y el visor de Whisp
lo resolvio y dibujo la parcela correcta con las capas de palma, JRC y cacao ETH.

Dos cosas que esto zanja:

- **El `geoId` de AgStack SI es resoluble por Whisp**, pese a que `/api/config` declare
  `geoidBaseUrl` apuntando a la FAO. Ojo al alcance: lo probado es el **visor**
  (`whisp.earthmap.org`), no el endpoint `/submit/geo-ids` de la API. Siguen siendo dos
  caminos distintos y el segundo continua sin verificar.
- **La configuracion de §14.3 punto 3 es la correcta.** Con `baseURL` en
  `api-ar.agstack.org` y `loginBaseURL` en `user-registry.agstack.org`, el login pasa y
  el registro devuelve `Geo Id`.

**Lo que hizo falta para llegar aca**, en orden: las cuatro variables en el `.env` del
servidor (no en el workflow de develop), la contrasena correcta de la cuenta AgStack --la
primera dio `401 Incorrect Password!`--, recrear el contenedor, y **recargar la pagina**
por el bug del globo descrito abajo.

**Bug del frontend corregido el 2026-08-31** (`fe`, `map.component.ts`): el HTML del globo
se arma una sola vez al crear el marcador. Si la parcela no tenia `geoId` en ese momento,
el boton *Open in Whisp* no se generaba ni se le registraba el listener, y `refreshGeoId`
solo reemplazaba el boton por el texto del identificador. Resultado: el usuario veia el
Geo-ID recien creado sin nada que pulsar hasta recargar. Ahora `showGeoIdInPopup()` deja
el globo igual que si la parcela hubiera llegado con `geoId`.

### 14.5 Reglas

- **Nunca llamar a Whisp dentro de una transacción.** Un análisis tarda minutos. Por eso
  `WhispAnalysisService` (orquestación, sin transacción) y `WhispPersistenceService`
  (`@Transactional`) son **beans distintos**: el proxy de Spring no aplica
  `@Transactional` a una llamada interna del mismo bean, y así la llamada HTTP queda
  entre dos transacciones cortas en vez de mantener una conexión abierta.
- **Los nombres de columna de Whisp cambian entre versiones** (`Ind_1_treecover` pasó a
  `Ind_01_treecover`). `WhispAnalysisMapper` busca sin distinguir mayúsculas y cae a una
  regla por substring; lo que no matchea **no se pierde**: el conjunto completo de
  propiedades queda en `resultjson` y se devuelve al cliente en `results`.
- **Una caída de Whisp no es un análisis fallido.** Al refrescar un job, si Whisp no
  responde se deja el estado guardado como está y el motivo viaja solo en esa respuesta.
  Marcarlo `FAILED` perdería un job que probablemente termine bien.
- **Toda integración opcional se apaga sola si no está configurada.** `isEnabled()` en
  ambos clientes; sin credenciales `generatePlotGeoID` devuelve `null` sin intentar el
  login (antes hacía un login fallido por cada parcela guardada).
- **Cuidado: esa misma guarda produce un silencio total en la UI.** Sin AgStack
  configurado, el botón *Refresh* del mapa llama al backend, recibe `200` con `geoId`
  nulo, y el frontend hace `if (data.geoId) {...}` **sin rama `else`**: ni error, ni
  mensaje, ni cambio visual. Y en el log tampoco hay rastro, porque la guarda evita la
  llamada. Si alguien reporta "el botón no hace nada", ese es el primer sospechoso —
  verificar con `docker exec <be> env | grep AGSTACK` antes de buscar en el código.
- **Todo `.block()` lleva timeout.** Sin él, el hilo de la petición espera para siempre
  cuando el servicio externo deja de responder y el pool de Hikari se drena.

### 14.6 Configuración

```properties
INATrace.whisp.baseURL = https://whisp.openforis.org/api   # instancia pública
INATrace.whisp.apiKey  =                                    # vacío = integración apagada
```

La aplicación lee `INATRACE_WHISP_APIKEY` / `INATRACE_WHISP_BASEURL` (relaxed binding de
Spring). Pero **ese no es el nombre que se escribe a mano en ningún lado**: los compose lo
mapean desde un nombre más corto. La cadena completa es:

| Dónde | Nombre | Quién lo pone |
|---|---|---|
| `.env` del servidor | `WHISP_API_KEY`, `AGSTACK_EMAIL`, `AGSTACK_PASSWORD`, ... | a mano |
| compose | `INATRACE_WHISP_APIKEY=${WHISP_API_KEY:-}` | ya versionado |
| aplicación | `INATrace.whisp.apiKey` | Spring lo resuelve |

Entornos reales, verificado el 2026-08-31:

- **UNOCACE staging** (`ci/unocace/docker-compose.yml`) — es el que **auto-despliega con
  push a `staging`**. El pipeline sí sincroniza el compose al servidor, pero **no
  reescribe el `.env`**: sólo actualiza `IMAGE_NAME` y `TAG`. Los valores se ponen a mano
  en `/opt/inatrace-test/.env` y se recrea el contenedor.
- **Fortaleza** (`ci/fortaleza/docker-compose.yml`) — producción, vía Jenkins.
- **`.github/workflows/deploy-backend-develop.yml` NO cuenta.** Lleva la línea del secret
  `DEV_WHISP_API_KEY`, pero sólo se dispara con push a `develop`, y GitHub Actions ejecuta
  la copia del workflow **de la rama pusheada** — donde esa línea no existe. Además
  `develop` está congelada desde 2025-12-19 y sigue en MySQL. Esa línea nunca se ejecuta.

Diagnóstico sin tocar la base de datos: `GET /whisp/status` dice si hay key, si el
servicio responde y si se puede enviar por geo id o solo por polígono.

### 14.7 Endpoints

| Método | Ruta | Qué hace |
|---|---|---|
| `POST` | `/whisp/plots/{plotId}/analysis` | Encola el análisis de la parcela |
| `GET` | `/whisp/plots/{plotId}/analysis` | Último análisis; refresca desde Whisp si el job sigue corriendo |
| `GET` | `/whisp/schemas` | Catálogo de datasets y campos (los "esquemas de análisis") |
| `GET` | `/whisp/status` | Configuración y alcanzabilidad de la conexión |

---

## 15. Configuración por empresa, semanas y búsquedas

> Escrito el **2026-09-04**, al implementar los pedidos de Fortaleza. Lo marcado
> como *verificado* se comprobó con tests o con datos reales ese día.

### 15.1 `company.configuration` es el lugar de las opciones por cliente

Columna **jsonb libre** (`Map<String,Object>` en `Company`, línea ~168). Agregar
una opción por empresa **no requiere migración ni tocar el modelo de la API**: se
lee con `company.getConfiguration()` y se administra desde el frontend. El
espejo, con la lista de claves vigentes, está en `fe/agent-context.md` §14.1.

La regla al agregar la próxima: **el valor ausente tiene que reproducir exacto el
comportamiento anterior**. Si no, cambiaste el sistema para todas las empresas
sin querer.

Ojo, conviven dos mecanismos de configuración por cliente y no son lo mismo:

- **`company.configuration`** (este): datos que un administrador cambia desde la
  aplicación, sin desplegar.
- **`application-<cliente>.properties`** (perfiles Spring, agregados por otro
  frente en 2026-09): configuración de despliegue.

Antes de agregar algo, decidí en cuál de los dos vive. No dupliques.

### 15.2 Numeración de semanas: `WeekNumberTools`

Fortaleza no usa ISO-8601: su semana 1 empieza el **primer lunes de enero** y
corre de lunes a viernes. El esquema sale de `configuration.weekNumberingScheme`;
sin la clave se usa ISO, que es lo que ve el resto.

- **Este cálculo es el que manda.** `createOrUpdateStockOrder` recalcula la
  semana y **descarta lo que manda el frontend** cuando hay fecha de producción.
  Antes de 2026-09-04 imponía ISO siempre, y por eso un arreglo solo en el
  cliente no se guardaba nunca. `fe/src/app/shared-services/week-number.util.ts`
  repite el cálculo para mostrarlo: **si cambiás la regla acá, cambiala allá**.
- **Sábado y domingo devuelven `null`** bajo `FIRST_MONDAY` (no se trabaja). En
  ese caso se respeta el número que el usuario haya escrito a mano. No lo
  "arregles" devolviendo un número.
- Expresar la regla como "primer lunes del año" y no como una fecha ancla
  guardada es deliberado: se recalcula sola cada enero.
- `WeekNumberToolsTest` cubre el caso reportado, el límite de fin de año (el 1 de
  enero de 2027 es la semana 52 de 2026), los fines de semana, el ciclo de
  colores y el retorno a ISO sin configuración. **Verificado**, y además las dos
  implementaciones se compararon sobre las 1.827 fechas de 2024 a 2028: cero
  diferencias.

### 15.3 PostgreSQL distingue mayúsculas en `LIKE` — MySQL no

**La trampa más cara de la migración, y la más fácil de repetir.** MySQL comparaba
texto ignorando mayúsculas por defecto; PostgreSQL **no**. Todas las búsquedas
escritas para el motor anterior quedaron rotas en silencio: la tabla no
encontraba nada escrito en minúscula.

Se corrigieron las **35 búsquedas de los 8 servicios** el 2026-09-04. La forma
correcta, que ya existía en `CompanyService.getUserCustomersForCompanyAndType`:

```java
Torpedo.condition(TorpedoFunction.lower(proxy.getName())).like().any(query.toLowerCase())
```

**Toda búsqueda de texto nueva tiene que ir así.** Un `.like()` sin `lower()`
compila, corre y devuelve resultados — solo que ignora lo que el usuario escribió
en minúscula. No hay error que lo delate. Para verificar que no se coló ninguna:

```bash
grep -rn "\.like()" src/main/java --include="*.java" | grep -v "TorpedoFunction.lower"
```

**Lo que esto NO cubre: las tildes.** Pasar a minúscula no convierte "efraín" en
"efrain". Haría falta la extensión `unaccent` de PostgreSQL, que **no está
instalada** y necesita superusuario y migración. Sigue pendiente de decisión.

### 15.4 Export a Excel: la columna de color va al final

`GroupStockOrderService` agrega "Color de Semana" **al final** cuando la empresa
tiene `weekColorCodes`, y solo entonces. Dos razones que conviene no deshacer:
agregarla al final no corre las columnas que ya consume quien usa el archivo, y
la condición mantiene el export del resto byte a byte igual.

Los estilos de celda se crean **una sola vez fuera del bucle**: POI crea un
objeto por llamada y Excel tiene un tope de estilos por libro, así que crear uno
por celda infla el archivo hasta romperlo.

### 15.5 Dos tests fallan de antes y no son tuyos

`INATraceBackendApplicationTests.contextLoads` y `GroupStockOrderServiceTest` son
`@SpringBootTest`: levantan el contexto completo y **necesitan base de datos
real**, así que fallan en cualquier máquina sin una. Verificado el 2026-09-04
corriéndolos sobre el código sin cambios. Los otros 31 tests pasan. Antes de
culpar a tu cambio por un `mvn test` en rojo, mirá si son solo esos dos.

---

## 16. Contrato de compras y socios para integraciones externas

> Escrito el **2026-09-15**, al revisar el código para la integración con el sistema
> contable de Fortaleza del Valle. La especificación completa está en el workspace
> `giz`, en `docs/fv/especificacion_interoperabilidad_contable_inatrace_fortaleza.md`.
> Todo lo de esta sección se **verificó** en el código ese día, y los datos en la base
> de staging de Fortaleza (solo lectura).

Un sistema externo (el ERP) va a llamar a estos endpoints con un usuario técnico.
Lo que sigue es comportamiento del que **depende**. Cambiarlo no es un refactor: es
un cambio de contrato, y hay que avisar y actualizar la especificación.

### 16.1 Orden real de `createOrUpdateStockOrder` (`PURCHASE_ORDER`)

1. `calculateNetQuantity`: `netQuantity` y `moistureWeightDeduction` (§5).
2. `calculateQuantities`: **exige `api.totalQuantity` y `api.fulfilledQuantity`
   no nulos** ("Total quantity cannot be null!", "Fulfilled quantity cannot be
   null!") **antes** de que nada los calcule. Con ese valor fija `fulfilledQuantity`
   y, en compras nuevas, `availableQuantity`.
3. `switch PURCHASE_ORDER`: **recalcula** `totalQuantity = bruto − tara − merma`,
   pisando lo recibido, y luego `cost`, `balance` y `paid`.

Consecuencias:

- **Los clientes están obligados a enviar `totalQuantity` y `fulfilledQuantity`**,
  aunque el servidor recalcule el total. El ERP envía ambos con `bruto − tara − merma`.
- `availableQuantity` sale del `totalQuantity` **del cliente**. La web envía
  `bruto − tara`, sin merma, así que en sus compras con merma
  `availableQuantity > totalQuantity` (1 caso en staging). Para saber si una compra
  ya se procesó, compara `availableQuantity` con `fulfilledQuantity`, no con `totalQuantity`.
- Si reordenas los pasos para calcular el total antes de validar, el cambio es
  bienvenido, pero no quites la aceptación de los dos campos: el ERP los seguirá enviando.
- `measureUnitType` enviado se **ignora**: la unidad sale del semiproducto.
- La semana se recalcula con `WeekNumberTools` (§15.2), salvo sábado y domingo
  con `FIRST_MONDAY`, donde se guarda el `weekNumber` recibido.
- Toda compra con precio queda con `balance = cost` y `paid = 0` si no hay pagos
  registrados: las 113 compras de Fortaleza en staging aparecen como pendientes.

### 16.2 Lo que la API **no** valida (y los clientes compensan)

| Falta de validación | Dónde | Riesgo |
| :--- | :--- | :--- |
| Estado del productor (`SUSPENDED`/`RETIRED`) | `createOrUpdateStockOrder` | Compras a socios bloqueados. Solo la web filtra. |
| Que `producerUserCustomer` pertenezca a la empresa de la instalación | `fetchEntity(..., UserCustomer.class)` sin chequeo | **Multiempresa CEDIA**: un id equivocado asigna la compra a un socio de otra organización. |
| Que el semiproducto esté habilitado en la instalación | — | La compra se guarda con un producto que la instalación no maneja. |
| Unicidad de `identifier` o idempotencia | — | Un reintento duplica compras. El ERP busca su `ERP:{id}` en `comments` antes de reintentar. |
| Unicidad de `farmerCompanyInternalId` por empresa | `addUserCustomer` | Socios duplicados. |

Agregar cualquiera de estas validaciones es **correcto y deseable** (brechas
B-03, B-05, B-12 y B-16 de la especificación). Pero cambia el contrato: devuelve el
mensaje en un `ApiException` claro, avisa al equipo del ERP y actualiza la
especificación.

### 16.3 `PUT /api/company/userCustomers/edit` — semántica de nulos

- **Lista u objeto nulo** (`plots`, `certifications`, `associations`,
  `cooperatives`, `bank`, `farm`, `location.address.country`) → `NullPointerException`
  y **error 500**, sin guardar.
- **Lista vacía** → **borra** lo que haya: parcelas con su georreferenciación y
  certificaciones. Un cliente que reutilice el cuerpo del alta (`"plots": []`)
  borra las parcelas del socio.
- **`status` nulo → no cambia el estado.** El ERP omite `status` a propósito, para
  no revertir una suspensión hecha entre su lectura y su escritura. **No cambies la
  semántica de `status` nulo.**

Si conviertes los nulos en "sin cambios" (brecha B-17), es una mejora compatible.
Convertir la lista vacía en "sin cambios" **no** lo es: la web la usa para borrar.

### 16.4 Permisos y autenticación

- `DELETE /api/chain/stock-order/{id}` exige **`COMPANY_ADMIN`** o administrador del
  sistema (`checkUserIfCompanyEnrolledAndAdminOrSystemAdmin`). Tampoco impide
  borrar compras ya usadas en un proceso.
- El JWT se resuelve por el claim `email`, con `preferred_username` como respaldo, y
  el usuario se busca con **`fetchUserByEmail`**. Una cuenta de servicio de Keycloak
  (`service-account-<cliente>`, sin email) recibe 401, salvo que el cliente tenga un
  *mapper* que emita el email del usuario técnico.

### 16.5 Columnas que usa el ERP

Verificado con `information_schema` en staging: `cost`, `netquantity` y
`totalquantity` son `numeric(38,2)`; `comments`, `identifier` y `parcellot` son
`varchar(255)`.

- `comments` lleva un texto estructurado del ERP:
  `LC:{estab}-{punto}-{secuencial}|CA:{clave de acceso}|ERP:{id}`. Es la única llave
  entre la liquidación SRI y la compra. **No lo reutilices** ni lo trunques.
- `parcelLot` es texto libre y hoy mezcla nombre de parcela, posición y número
  escrito a mano (ver `fe/agent-context.md` §15.4). La solución de fondo es una FK
  `plot_id` (brecha B-01). No asumas que `parcelLot` identifica una parcela.
- `usercustomer.status` está en **NULL** para los socios anteriores a la migración de
  estados (los 873 de Fortaleza). El getter lo normaliza a `ACTIVE`; en SQL usa
  `coalesce(status, 'ACTIVE')` (misma regla de §12.3).

---

## 17. Sacos de salida de un procesamiento (`repackedOutputs`)

> Escrito el **2026-09-15**, tras encontrar en producción de Fortaleza un lote que
> subía de peso cada vez que se editaba. La causa está en el código base de INATrace
> (viene del original de 2021), así que **afecta a todas las empresas**, no solo a
> Fortaleza: cualquier acción de procesamiento cuyo semiproducto de salida tenga
> `repackedOutputs` con `maxOutputWeight` parte el lote en varios `StockOrder`
> ("sacos"), y todas caían en lo mismo.

### 17.1 Qué es un saco y por qué hay muchos `StockOrder` por lote

Cuando el semiproducto de salida tiene `repackedOutputs`, la salida de un
`ProcessingOrder` **no es un `StockOrder`, son N**: uno por saco, cada uno con
`sacNumber` y con el mismo `repackedOriginStockOrderId` (un UUID que genera el
frontend para agruparlos). El peso del lote es la **suma** de los sacos; el campo
"cantidad de salida" del formulario es solo una ayuda de pantalla y no se guarda.

### 17.2 La trampa: quitar un saco no lo borraba

`ProcessingOrder.targetStockOrders` es `@OneToMany(mappedBy = "processingOrder")`
**sin `orphanRemoval`** ([ProcessingOrder.java](src/main/java/com/abelium/inatrace/db/entities/processingorder/ProcessingOrder.java)).
Hasta el 2026-09-15, `createOrUpdateProcessingOrder` calculaba los sacos que ya no
venían en la petición y solo hacía `entity.getTargetStockOrders().removeAll(...)`:
eso **no genera ningún DELETE**. El saco seguía en la base, con su
`processingorder_id` y su cantidad disponible, y la API respondía **200**.

Consecuencia real (Fortaleza, lote `2026-34-02`): el usuario dejó unas pocas filas
y agregó 14 sacos nuevos; se crearon los 14 y no se borró ninguno de los 42
anteriores → **42 + 14 = 56 sacos, 3.864 kg** cuando el lote debía pesar ~1.000 kg.
Editando otra vez solo se actualizaban las filas que quedaban, así que el peso casi
no bajaba y los `sacNumber` quedaban repetidos.

### 17.3 Reglas al tocar este código

1. **Los sacos quitados se borran con `em.remove`**, no basta con sacarlos de la
   colección. Si alguien reescribe este bloque con `removeAll` a secas, vuelve el
   fallo.
2. **En `TRANSFER` nunca se borra el destino.** Ahí el `StockOrder` destino **es el
   mismo de origen** (la validación exige el mismo ID, ver el `case TRANSFER` de
   `createOrUpdateProcessingOrder`). Borrarlo eliminaría la entrega original del
   socio. Al 2026-09-15 había 31 transferencias con varios destinos en Fortaleza,
   así que no es un caso raro. El borrado solo aplica a `PROCESSING`,
   `FINAL_PROCESSING` y `GENERATE_QR_CODE`.
3. **Antes de borrar, verificar que el saco no se usó:** cantidad consumida
   (`totalQuantity > availableQuantity`), pagos asociados, o transacciones que lo
   tengan como `sourceStockOrder`. Si se usó, `VALIDATION_ERROR`; nunca un OK falso.
   La validación vieja comparaba al revés (`total - available < 0`) y no bloqueaba
   nunca: no la "restaures" por parecer equivalente.
4. **El orden de los sacos importa.** `targetStockOrders` es un `Set` (`HashSet`):
   el orden cambia entre lecturas. `ProcessingOrderMapper` los ordena por
   `sacNumber` y luego por `id`. Sin ese orden, el usuario edita filas distintas
   cada vez que abre la pantalla y termina pisando sacos al azar.
   Lo cubre `ProcessingOrderMapperTest`.

### 17.4 Cómo detectar lotes ya dañados (cualquier empresa)

Los números de saco repetidos son la huella del fallo:

```sql
select so.company_id, so.processingorder_id, so.internallotnumber,
       count(*) sacos, count(distinct so.sacnumber) numeros_distintos,
       sum(so.totalquantity) peso
from stockorder so
where so.processingorder_id is not null and so.sacnumber is not null
group by 1,2,3
having count(*) <> count(distinct so.sacnumber)
order by 1,2;
```

Al 2026-09-15 en producción de Fortaleza salían `2026-34-01` (33 sacos, 26 números)
y `2026-34-02` (56 sacos, 35 números). Corregirlos es trabajo de datos aparte: el
arreglo del código no deshace lo ya guardado.

---

## 18. Variedad 1/2 y certificación no orgánica "Convencional" (UNOCACE)

> Escrito el **2026-09-16** (commit `1bd38c1e`, junto con el frontend `bc1fee1e`).
> *Verificado* ese día con `StockOrderServiceTest` y en `inatrace_unocace_stage`.
> Espejo del frontend: `fe/agent-context.md` §17. Plan, decisiones y conteos:
> `docs/cambios/2026-09-16-unocace-variedad-y-certificaciones-plan.md`.

### 18.1 Dónde vive cada dato

| Dato | Tabla/columna | Forma |
|---|---|---|
| Variedad de la parcela | `plot.cocoavariety` | enum `ORGANICO`/`CCN51`. La pantalla muestra 1/2; **no** migres a números |
| Certificación de la parcela | `plot.certificationtype_id` | FK: renombrar el catálogo no rompe nada |
| Variedad de la entrega | `stockorder.variety` | texto: `"1"`/`"2"` con `numericVarietyOptions`; si no, `NACIONAL`/`CCN51` |
| Certificación de la entrega | `stockorder.organiccertification` | **texto con el nombre ES**: renombrar el catálogo **no** actualiza las entregas |
| Nombre que se muestra | `certificationtypetranslation.name` | **no** `certificationtype.name`: `CertificationTypeMapper` elige la traducción. Al renombrar, actualizá las dos tablas |

### 18.2 Reglas de `StockOrderService` (bloque tras `setVariety`)

- `isCcn51Variety()` acepta `"CCN51"` **y** `"2"`. Antes solo comparaba `"CCN51"`,
  así que las entregas de UNOCACE (que guardan `"2"`) nunca entraban a la regla.
- **Ya no se fuerza `organic = true` para CCN51.** Ese forzado (commit `f422aaf7`)
  contradecía al frontend: guardaba "Orgánico: Sí" con una certificación de
  transición. Ahora CCN51 con `organic == null` pasa a `false`, y un valor enviado
  se respeta.
- `defaultNonOrganicCertificationName()` busca en `CertificationType.findAllActive`
  (ordenado por `code`) la primera cuyo **nombre ES** cumpla
  `isNonOrganicCertificationName()`. Si no hay ninguna, devuelve `null`. **No
  vuelvas a poner un nombre literal**: el `"Transición / Fairtrade / SPP"` que había
  dejó de existir con el catálogo nuevo.
- `isNonOrganicCertificationName()` reconoce
  `convencional | conventional | transicion | transition` (NFD, sin marcas, en
  minúscula). **Tiene que coincidir con `isNonOrganicCertification()` del frontend.**
- Solo se completa la certificación si viene vacía. Este código corre para
  **cualquier** `StockOrder` que pase por aquí; en Fortaleza no aplica, porque su
  frontend siempre manda `organic = true` y una certificación.

### 18.3 Tests

`StockOrderServiceTest` (unitario, sin Spring): 5/5 el 2026-09-16.

```bash
mvn -q -o test -Dtest=StockOrderServiceTest -Dsurefire.failIfNoSpecifiedTests=false
```

`-q` no imprime nada si todo pasa: mirá
`target/surefire-reports/com.abelium.inatrace.components.stockorder.StockOrderServiceTest.txt`.

### 18.4 Script de datos (solo UNOCACE)

`scripts/unocace/sql/2026-09-16-catalogo-certificaciones-y-variedad.sql`. **No está en
git**: el `.gitignore` excluye `scripts/*`, porque ahí vive el dump de producción de
UNOCACE. La copia está en la máquina de Álvaro y el procedimiento, en el plan de `docs/`.

- Renombra por **código** (no por id ni por nombre):
  - `NATURLAND_FT_SPP` → `ORGANICO_UE_NOP_BIOSUISSE_NATURLAND_FT_SPP`
  - `BIOSUISSE_FT_SPP` → `ORGANICO_UE_NOP_BIOSUISSE_FT_SPP`
  - `FAIRTRADE_SPP` → `ORGANICO_UE_NOP_FT_SPP`
  - `TRANSICION_FT_SPP` → `CONVENCIONAL_FT`
  
  También pasa las parcelas de `FAIRTRADE` ("Comercio Justo") a `CONVENCIONAL_FT` y
  deja `FAIRTRADE` en `INACTIVE`.
- Actualiza las traducciones ES y EN, renombra el texto de las entregas, pone
  `organic = false` en las "Convencional Fairtrade", activa `numericVarietyOptions` en
  **todas** las empresas y pasa la variedad de las entregas de `NACIONAL`/`CCN51` a
  `1`/`2`.
- Es idempotente y aborta si no existe la empresa `UNOCACE`. **Nunca en Fortaleza.**
- Hay que correrlo **después** de desplegar este código: con el catálogo nuevo y el
  código viejo, el combo de entregas no orgánicas queda vacío.
- Ejecutado en pruebas el 2026-09-16. Respaldo en el servidor:
  `/root/respaldos/antes-catalogo-certificaciones-20260916_133707.dump`.

### 18.5 Antes de tocar esto, revisá

```bash
grep -rn "organicCertification\|isNonOrganic\|isCcn51" src/main/java ../fe/apps/inatrace-fe/src/app
```

Además del código, dependen del texto guardado:
- las vistas de BI `bi.fact_cacao_purchase` y `bi.rpt_cacao_compras_certificadas`,
  que agrupan por el texto;
- `GroupStockOrderService` (export agrupado);
- `batch-history` y el PDF del frontend, que lo muestran crudo.


---

## 19. Importación masiva de polígonos (GeoJSON)

> Escrito el **2026-09-16**. Espejo del frontend: `fe/agent-context.md` §19. Plan,
> datos y decisiones: `docs/cambios/2026-09-16-unocace-reemplazo-poligonos.md`.
> *Verificado* ese día con los tests unitarios y con el archivo real de UNOCACE
> (104 polígonos de 2 de Mayo) contra una **copia** de `inatrace_unocace_stage`:
> vista previa, rechazos y aplicación. La copia se borró al terminar.

UNOCACE pidió borrar todos los polígonos cargados y dejar solo los validados en campo
(pilotaje con la asociación 2 de Mayo). No se hace por SQL: hay un endpoint con vista
previa, y la pantalla *Agricultores → Importar polígonos (GeoJSON)* lo usa.

### 19.1 Piezas

| Pieza | Qué hace |
|---|---|
| `POST /api/company/{id}/plots/import-geojson` | Solo `SYSTEM_ADMIN`. Multipart `file` + `scope`, `apply`, `skipInvalidFeatures`, `expectedPlotsToDelete`, `expectedPlotsToCreate` |
| `plotimport/PlotGeoJsonReader` | Lee el `FeatureCollection`; errores de archivo → `ApiException`, errores de un elemento → quedan en el elemento |
| `plotimport/PlotGeoJsonImportPlanner` | Puro, sin BD: empareja agricultores, decide lotes, incidencias y qué empresas se reemplazan |
| `plotimport/PlotGeoJsonImportService` | Carga el alcance, aplica el plan en **una** transacción y genera los Geo-ID después del commit |
| `agstack/PlotGeoIdGenerator` | La generación de Geo-ID que antes era privada de `CompanyService`; la usan todos los caminos que crean lotes |

### 19.2 Reglas del archivo (formato de las capas GIS de UNOCACE)

- `ID_INTERNO` = `UserCustomer.farmerCompanyInternalId`, sin distinguir mayúsculas ni
  espacios alrededor. **No** se quita el prefijo `MY`: `0906065859` y `MY0906065859`
  son agricultores distintos en la BD (hay un duplicado de Manuel Avilés así).
- `COD_LOTE` → nombre del lote (obligatorio y único por agricultor).
- `HECTAREA` → tamaño; si falta o es ≤ 0, se usa el área geodésica del polígono.
- `CULTIVOPRI` → cultivo por **nombre** de `ProductType` ("CACAO CCN51", "CACAO
  NACIONAL", como hizo la migración) y variedad: CCN51 → `CCN51`, Nacional →
  `ORGANICO` (= variedad 1, ver §18). Si no coincide, el tipo de producto del
  agricultor con menor id.
- Se aceptan `Polygon`, `MultiPolygon` **de una sola parte** (QGIS exporta así) y
  `Point`. Se rechazan multipartes, huecos, coordenadas fuera de rango y un `crs` que
  no sea WGS 84 (un UTM se guardaría como basura).
- El anillo se guarda **cerrado** (primer vértice repetido), como los 727 de 728 lotes
  que ya había; `PlotGeometryTools` acepta ambos.

### 19.3 Alcance y seguridad

- Los agricultores se buscan en la empresa seleccionada **y sus empresas conectadas**
  (las que comparten un producto con ella: en UNOCACE, las 20 asociaciones). No sirve
  `getAssociations`: las asociaciones de UNOCACE son `PRODUCER`, no `ASSOCIATION`.
- `scope = COMPANY_AND_CONNECTED` borra los lotes de todas esas empresas;
  `MATCHED_COMPANIES` solo los de las empresas que reciben lotes del archivo. Usá el
  segundo para cargar la próxima asociación validada **sin borrar 2 de Mayo**.
- Sin `apply=true` no se guarda nada. Para aplicar hay que mandar los
  `plotsToDelete`/`plotsToCreate` de la vista previa; si cambiaron, se rechaza.
- Con incidencias no se aplica, salvo `skipInvalidFeatures=true` explícito.
- Borrado con JPQL masivo en orden `PlotDeforestationAnalysis` → `PlotCoordinate` →
  `Plot` (un `DELETE` JPQL **no** aplica cascadas) y luego `em.clear()`. No cambies a
  `em.remove` sobre lotes que siguen en `UserCustomer.plots` (EAGER, `orphanRemoval`):
  Hibernate falla con "deleted object would be re-saved by cascade".
- Ninguna otra tabla referencia `plot` por FK. `stockorder.parcellot` es texto sin
  FK (nombre del lote, o la posición "1", "2"… en los datos viejos; ver
  `fe/agent-context.md` §12): las entregas existentes no cambian. No las "arregles".
- **Sin lote no se vende** (`fe/agent-context.md` §12.1). Tras un reemplazo con
  `COMPANY_AND_CONNECTED`, los agricultores de las empresas que no recibieron lotes
  no pueden registrar entregas hasta que se carguen los suyos (o se active
  `parcelLotFreeText` en esa empresa). En UNOCACE, el 2026-09-16, ninguna empresa lo
  tenía activo.
- Se pierden los datos que solo vivían en el lote: certificación, variedad, estimado
  de producción, plantas, Geo-ID y análisis Whisp. UNOCACE decidió recargarlos.

### 19.4 Geo-ID después del commit

Con AgStack configurado (UNOCACE lo está), `apply` registra un
`afterCommit` que genera los Geo-ID en **un** hilo propio (`plot-geoid-backfill`),
uno por lote, cada escritura en su propia transacción corta. La respuesta dice cuántos
quedaron pendientes (`geoIdsPending`) y el log cierra con
`Plot GeoJSON import: generated X of Y AgStack geo ids`. No lo muevas dentro de la
transacción (§14.5): 100 llamadas HTTP tardan minutos. Si el contenedor se reinicia en
medio, los que falten se generan desde el mapa (*Actualizar*).

### 19.5 Arreglos en la subida por agricultor (`uploadGeoData`)

- Ignoraba en silencio todo `MultiPolygon`: con el archivo de UNOCACE respondía OK y
  no creaba nada. Ahora usa el mismo lector y rechaza el archivo si algún elemento no
  se puede guardar.
- Calculaba el área como `m² / 1000`: los tamaños salían **10 veces** más grandes.
  Ahora `m² / 10 000`. Los lotes subidos antes por esa vía conservan el tamaño malo.
- Si el agricultor no tenía tipo de producto, fallaba con `IndexOutOfBounds`.

### 19.6 Pruebas

```bash
mvn -q -o test -Dtest='PlotGeoJson*Test' -Dsurefire.failIfNoSpecifiedTests=false
```

14 tests (lector 8, planificador 6). Los JPQL solo se prueban contra una base real:
el procedimiento con una copia de la BD y un túnel SSH está en el plan de `docs/`.
