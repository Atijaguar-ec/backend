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
| `variety` | `String(255)` | Variedad genética (Nacional, CCN51) |
| `organicCertification` | `String(255)` | Certificación orgánica del lote |
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

### Tablas Nuevas: `CertificationType` + `CertificationTypeTranslation`
Catálogo de certificaciones orgánicas y sellos ambientales con soporte i18n.

---

## 5. Lógica de Negocio Crítica

### Cálculo de Peso Neto (`StockOrderService`)
```
Net = (Bruto − Tara − PesoDañado) × (Humedad / 100)
```
- Método: `calculateNetQuantity(grossQuantity, tare, damagedWeightDeduction, moisturePercentage)`
- Se calcula automáticamente al guardar un `StockOrder` tipo `PURCHASE_ORDER`.
- Si `finalPriceDiscount != null`, se resta del costo total: `cost = cost - finalPriceDiscount`

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

