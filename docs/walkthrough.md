# Walkthrough — Sesión de Trabajo: Shrimp Traceability

**Fecha**: 2026-03-27
**Alcance**: Resolución de visibilidad en Core, refactoring Recepción MFE, especificación esquema `dufer`

---

## 1. Problemas Resueltos en el Core (vía datos, NO código)

### 1.1 Países faltantes — Migraciones Java no ejecutadas

**Problema**: La tabla `country` estaba vacía (0 filas). Los países se cargan vía una migración Java de Flyway (`V2020_03_27_15_00__Prefill_Countries.java`) que lee `import/countries.csv`.

**Causa raíz**: El baseline de Flyway está en versión `1`, y las migraciones Java tienen versiones `2020.*`–`2023.05.*`. Con `ignore-missing-migrations = true`, Flyway las salta silenciosamente.

**Fix**: Se integró la carga del CSV de 249 países directamente en el script de seeding (`seed_shrimp_chain.py`), replicando la lógica de la migración Java.

```diff
+ # ── 0. Countries (from Java migration V2020_03_27_15_00 that Flyway skipped)
+ csv_path = os.path.join(os.path.dirname(__file__), "..", "import", "countries.csv")
+ with open(csv_path, encoding="utf-8") as f:
+     reader = csv.DictReader(f)
+     for row in reader:
+         cur.execute("INSERT INTO country (code, name) VALUES (%s, %s);", (row["Code"], row["Name"]))
```

> [!NOTE]
> Las siguientes migraciones Java TAMPOCO se ejecutaron y el seed las compensa:
> - `V2021_08_11_11_33__Prefill_FacilityTypes` → El seed crea 4 facility types de camarón
> - `V2021_08_11_11_41__Prefill_MeasureUnitTypes` → El seed crea LBS, CAJETA, KG

### 1.2 NullPointerException en FacilityMapper

**Problema**: `FacilityMapper.toApiFacilityBase()` línea 56 crasheaba con NPE al acceder `getFacilityLocation().getLatitude()` porque las facilities seeded no tenían `FacilityLocation`.

**Decisión**: **NO se modificó el código del Core**. En su lugar, se corrigió el seed para crear registros `FacilityLocation` con coordenadas de Guayaquil:

```python
cur.execute("""
    INSERT INTO facilitylocation
        (entityversion, latitude, longitude, pinname,
         address_address, address_city, address_state, address_country_id,
         ispubliclyvisible, numberoffarmers)
    VALUES (0, %s, %s, %s, %s, %s, %s, %s, false, 0) RETURNING id;
""", (-2.1894, -79.8891, fac_name,
      "Km 10.5 Vía Daule", "Guayaquil", "Guayas", ecuador_id))
```

### 1.3 Traducciones EN faltantes

**Problema**: Backend usa `language` header con default `EN`. Las queries JPQL hacen `INNER JOIN ... WHERE t.language = :language`. Sin traducción EN → 0 resultados.

**Fix**: `upsert_translation()` ahora inserta **ambos idiomas** (ES + EN) para todas las entidades.

---

## 2. Reglas Establecidas

### 🔒 Regla Estricta: No Modificar el Core

> **NO se debe modificar el INATrace Core** (backend Java ni frontend Angular del Core) bajo ninguna circunstancia, a menos que el usuario lo autorice explícitamente.

**Aplica a:**
- `backend/src/` — todo el código Java
- `fe/apps/inatrace-fe/` — todo el frontend Angular del Core

**SÍ se puede modificar:**
- `fe/shrimpMfe/` — microfrontend de camarón
- `backend/scripts/` — scripts de utilidad/seeding
- Datos vía SQL/seed/configuración

---

## 3. Flujo del Proceso Dufer (Memorizado)

![Flujo de Proceso Dufer](dufer_process_flow.png)

| Etapa | Módulo MFE | Estado |
|---|---|---|
| 1. Recepción de Materia Prima | `recepcion/` | ✅ Implementado |
| 2. Decisión Inicial (Entero/Cola) | `recepcion/` | ✅ Selector de tipo |
| 3. Clasificación por Tallas | `clasificacion/` | 🔲 Scaffold |
| 4. Decisión de Destino Productivo | `destinos/` | 🔲 Scaffold |
| 5. Asignación de Lote por Destino | `utils/lot-number.util.ts` | ✅ Sufijos -2/-3/-4 |
| 6. Procesamiento (Bloque/IQF/VA/Salmuera) | `masterizado/` | 🔲 Scaffold |
| 7. Almacenamiento Final (Cámaras FIFO) | `liquidacion/` | 🔲 Scaffold |
| – Rechazo cíclico a Cola | `rechazo/` | 🔲 Scaffold |

---

## 4. Recepción MFE — Cambios Realizados

### Archivos modificados (solo en `shrimpMfe/`):

#### [shrimp-data.service.ts](file:///Users/alvarosanchez/proyectos/giz/fe/shrimpMfe/src/app/services/shrimp-data.service.ts)

- **Nuevo interface** `ShrimpSupplier` con `displayName`, `type` (FARMER/COLLECTOR)
- **Nuevo método** `getSuppliers(companyId)` → consume `/api/company/userCustomers/{id}/FARMER`
- **`ReceptionLot`** extendido con `supplier_id` y `supplier_name`
- Compatibilidad con mock data existente (cast seguro)

#### [recepcion.component.ts](file:///Users/alvarosanchez/proyectos/giz/fe/shrimpMfe/src/app/recepcion/recepcion.component.ts)

Formulario refactorizado con 2 campos nuevos:

| Antes | Después |
|---|---|
| Campo "Lote Base / Proveedor" (mixto) | **Dropdown de Proveedor** (datos de Core API) |
| — | **Campo Número de Lote** (con botón Auto `DDMMYY-001`) |

Mejoras UX:
- Al guardar, mantiene proveedor seleccionado para entradas en lote
- Preview de sub-lotes derivados bajo el campo de lote
- Supplier info muestra ubicación y teléfono

---

## 5. Mapeo API — Recepción → Core StockOrder

Documento completo en: [recepcion_api_mapping.md](file:///Users/alvarosanchez/.gemini/antigravity/brain/2622dd90-e6cb-4a67-a030-1027320422a4/recepcion_api_mapping.md)

### Resumen:

| Categoría | Cantidad | Detalle |
|---|---|---|
| ✅ Campos mapeados directamente | 14 | proveedor, lote, peso, facility, moneda, etc. |
| ⚠️ Workaround necesario | 4 | gavetas en `comments`, temp en evidence fields |
| ❌ Sin equivalente en Core | 3 | distribución tallas, cámaras FIFO, estado sanitario |

**Endpoint**: `PUT /api/chain/stock-order` con `orderType: PURCHASE_ORDER`

---

## 6. Esquema de Extensión `inatrace.dufer`

Documento completo en: [dufer_schema_spec.md](file:///Users/alvarosanchez/.gemini/antigravity/brain/2622dd90-e6cb-4a67-a030-1027320422a4/dufer_schema_spec.md)

### 8 tablas propuestas:

| Tabla | Dominio | FK Principal |
|---|---|---|
| `reception_ext` | Recepción | `public.stockorder` |
| `classification` | Clasificación | `public.stockorder` |
| `classification_detail` | Clasificación | `dufer.classification` |
| `productive_destination` | Destinos | `public.stockorder` |
| `processing_lot` | Procesamiento | `dufer.productive_destination` |
| `cold_storage_slot` | Almacenamiento | `public.facility` |
| `cold_storage_movement` | Almacenamiento | `dufer.cold_storage_slot` |
| `sanitary_hold` | Calidad | `public.stockorder` |

**Microservicio futuro**: `ms-shrimp` (NestJS + TypeORM, esquema `dufer`)

---

## 7. Estado del Seed Script

**Archivo**: [seed_shrimp_chain.py](file:///Users/alvarosanchez/proyectos/giz/backend/scripts/seed_shrimp_chain.py)

**Datos cargados**:
- ✅ 249 países (desde `import/countries.csv`)
- ✅ 1 empresa (Dufer Cia. Ltda.)
- ✅ 4 usuarios (admin + 3 operadores)
- ✅ 1 value chain (Camarón Dufer)
- ✅ 4 facility types + 4 facilities con FacilityLocation
- ✅ 3 unidades de medida (LBS, CAJETA, KG)
- ✅ 6 semi-productos (Entero, Cola, U10, 16/20, 21/25, 26/30)
- ✅ 3 processing actions
- ✅ 1 producto (Camarón Blanco Dufer)
- ✅ 1 moneda (USD)
- ✅ 5 proveedores (4 camaroneros + 1 acopiador)
- ✅ Traducciones ES + EN en todas las entidades

**Uso**:
```bash
python3 backend/scripts/seed_shrimp_chain.py          # Idempotent
python3 backend/scripts/seed_shrimp_chain.py --clean   # Truncate + reseed
```

---

## 8. Próximos Pasos

1. **Conectar Recepción al API del Core** — `PUT /api/chain/stock-order`
2. **Implementar Clasificación** — módulo `clasificacion/`
3. **Crear esquema DDL** — Ejecutar las sentencias SQL de `dufer_schema_spec.md` en PostgreSQL
4. **Scaffoldar `ms-shrimp`** — Microservicio NestJS para las tablas `dufer.*`
5. **Implementar Destinos** — Fork de lotes con sufijos
6. **Cámaras FIFO** — Módulo de almacenamiento
