# Relación `UserCustomer` ↔ `Plot` ↔ `PlotCoordinate`

## Alcance
Este documento describe la relación entre las entidades JPA:

- `UserCustomer`
- `Plot`
- `PlotCoordinate`

y cómo se materializan en el esquema MySQL (Flyway), con base en:

- `src/main/java/com/abelium/inatrace/db/entities/common/UserCustomer.java`
- `src/main/java/com/abelium/inatrace/db/entities/common/Plot.java`
- `src/main/java/com/abelium/inatrace/db/entities/common/PlotCoordinate.java`
- `src/main/resources/db/migrations/V1__Initial_schema.sql`

## Modelo (JPA)

### `UserCustomer` → `Plot`
En `UserCustomer`:

- Relación: `@OneToMany(mappedBy = "farmer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)`
- Campo: `private Set<Plot> plots;`

En `Plot`:

- Relación inversa: `@ManyToOne`
- Campo: `private UserCustomer farmer;`

**Cardinalidad:**
- Un `UserCustomer` (el *farmer*) puede tener **muchos** `Plot`.
- Cada `Plot` pertenece a **un** `UserCustomer` (farmer).

**Ownership (lado dueño de la FK):**
- El dueño es `Plot.farmer` (lado `@ManyToOne`).
- `UserCustomer.plots` es el lado inverso (`mappedBy = "farmer"`).

**Cascada y orphan removal:**
- Desde `UserCustomer` hacia `Plot` hay `CascadeType.ALL`.
- `orphanRemoval = true` implica que si se quita un `Plot` del `Set` y se persiste el `UserCustomer`, el `Plot` queda marcado para borrado.

**Fetch:**
- `UserCustomer.plots` está declarado como `FetchType.EAGER`.

### `Plot` → `PlotCoordinate`
En `Plot`:

- Relación: `@OneToMany(mappedBy = "plot", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)`
- Campo: `private List<PlotCoordinate> coordinates;`

En `PlotCoordinate`:

- Relación inversa: `@ManyToOne`
- Campo: `private Plot plot;`

**Cardinalidad:**
- Un `Plot` puede tener **muchas** `PlotCoordinate`.
- Cada `PlotCoordinate` pertenece a **un** `Plot`.

**Ownership (lado dueño de la FK):**
- El dueño es `PlotCoordinate.plot`.
- `Plot.coordinates` es el lado inverso (`mappedBy = "plot"`).

**Cascada y orphan removal:**
- Desde `Plot` hacia `PlotCoordinate` hay `CascadeType.ALL` y `orphanRemoval = true`.

**Fetch:**
- `Plot.coordinates` está declarado como `FetchType.LAZY`.

## Modelo (Base de datos / Flyway)
Según `V1__Initial_schema.sql`:

### Tabla `Plot`
- Columna FK: `farmer_id` → `UserCustomer(id)`
- Columna FK: `crop_id` → `ProductType(id)`

Fragmento relevante:

- `CONSTRAINT FKmckxho0qyajs99kvv1hstk7de FOREIGN KEY (farmer_id) REFERENCES UserCustomer (id)`

### Tabla `PlotCoordinate`
- Columna FK: `plot_id` → `Plot(id)`

Fragmento relevante:

- `CONSTRAINT FKaajjvpqh3l69s73fo95qs99sk FOREIGN KEY (plot_id) REFERENCES Plot (id)`

## Observaciones / Riesgos

### 1) `UserCustomer.plots` es `EAGER`
Esto puede causar:

- Cargas de entidades grandes cuando se consulta `UserCustomer` (potencial N+1 o payload grande).
- Impacto en performance especialmente si un farmer tiene muchos plots.

**Nota:** No es un bug necesariamente, pero es una decisión de performance importante.

### 2) Consistencia bidireccional en memoria
La relación es bidireccional (`UserCustomer.plots` ↔ `Plot.farmer` y `Plot.coordinates` ↔ `PlotCoordinate.plot`).

Para evitar estados inconsistentes al persistir, es recomendable que cuando se agregue:

- Un `Plot` a un `UserCustomer`, también se setee `plot.setFarmer(userCustomer)`.
- Un `PlotCoordinate` a un `Plot`, también se setee `plotCoordinate.setPlot(plot)`.

Si esto no se cumple, se pueden observar:

- FKs nulas (`farmer_id` o `plot_id`) en inserts.
- Relaciones “perdidas” al serializar/mapping si se confía en un lado solamente.

### 3) `Plot.coordinates` LAZY pero el mapper las usa
`PlotMapper.toApiPlot()` hace:

- `plot.getCoordinates().stream()...`

Si `Plot` se usa fuera de una sesión abierta (transaction) y `coordinates` no fue inicializada, esto puede provocar `LazyInitializationException`.

Mitigaciones típicas:

- Fetch join en el query cuando se necesita serializar.
- Inicializar explícitamente en service.
- Cambiar estrategia de fetch (con cuidado).

## Resumen de cardinalidades

- `UserCustomer (farmer) 1 ── * Plot`
- `Plot 1 ── * PlotCoordinate`

## Importación de polígonos desde GeoJSON

### Archivo fuente
- **Ubicación:** `import/poligonos_16_10_2025.geojson`
- **Total features:** 1260 polígonos
- **Formato:** GeoJSON FeatureCollection con geometrías tipo `MultiPolygon`

### Estructura del GeoJSON

Cada `Feature` contiene:

**Properties:**
- `ID_INTERNO`: Código único del agricultor (mapea a `UserCustomer.farmerCompanyInternalId`)
- `ID_DE_UN`: Identificador de unidad/lote (ej: "ASH5493 LOTE 1")
- `COD_LOTE`: Código del lote
- `HECTAREA`: Superficie en hectáreas
- `CULTIVOGEN`: Cultivo genérico (ej: "CACAO")
- `CULTIVOPRI`: Cultivo principal (ej: "CACAO NACIONAL")
- `ASOCIACION`: Asociación del agricultor (ej: "APROCASH")
- `FLOID`: ID de sistema externo
- `ORDEN`: Orden/secuencia

**Geometry:**
- `type`: "MultiPolygon"
- `coordinates`: Array de polígonos con coordenadas `[longitude, latitude]`

### Proceso de importación

#### 1. Mapeo `ID_INTERNO` → `UserCustomer` (Matching Inteligente)

El campo `ID_INTERNO` del GeoJSON debe mapearse con `UserCustomer.farmerCompanyInternalId` usando **dos estrategias**:

##### Estrategia 1: Match exacto
Intentar primero coincidencia exacta:

```sql
SELECT id, name, surname, farmerCompanyInternalId 
FROM UserCustomer 
WHERE farmerCompanyInternalId = '<ID_INTERNO_del_GeoJSON>'
  AND type = 'FARMER';
```

##### Estrategia 2: Extracción de número de cédula
Si no hay match exacto, **extraer el número de cédula** del `ID_INTERNO` y buscar por ese número:

**Patrones identificados:**

| Formato en GeoJSON | Prefijo | Número de cédula | Sufijo | Buscar en BD |
|-------------------|---------|------------------|--------|--------------|
| `VI2400185795` | VI | `2400185795` | - | `2400185795` |
| `Z0101721926` | Z | `0101721926` | - | `0101721926` |
| `MY1201209648-L1` | MY | `1201209648` | -L1 | `1201209648` |
| `M1201870100` | M | `1201870100` | - | `1201870100` |
| `ASH5493` | ASH | `5493` | - | Match exacto solamente |
| `ASH 6690` | ASH | `6690` | - | Match exacto con espacios |

**Regla de extracción:**
1. Si el `ID_INTERNO` tiene prefijo de 1-3 letras + 10-12 dígitos → extraer el número
2. Ignorar sufijos como `-L1`, `-L2`, `-LOTE1`, etc.
3. El número extraído corresponde al **número de cédula del agricultor**

```sql
-- Buscar por número de cédula extraído
SELECT id, name, surname, farmerCompanyInternalId 
FROM UserCustomer 
WHERE farmerCompanyInternalId LIKE '%<numero_cedula>%'
  AND type = 'FARMER';

-- O con REGEXP (MySQL 8+)
SELECT id, name, surname, farmerCompanyInternalId 
FROM UserCustomer 
WHERE farmerCompanyInternalId REGEXP '[A-Z]{1,3}?<numero_cedula>(-.*)?$'
  AND type = 'FARMER';
```

**Algoritmo de matching completo:**

```java
public UserCustomer findFarmerByIdInterno(String idInterno) {
    // 1. Intento: Match exacto
    UserCustomer farmer = repository.findByFarmerCompanyInternalIdAndType(
        idInterno, UserCustomerType.FARMER);
    
    if (farmer != null) {
        return farmer;
    }
    
    // 2. Intento: Extraer número de cédula
    String cedula = extractCedulaNumber(idInterno);
    
    if (cedula != null) {
        // Buscar por número de cédula (puede estar con prefijo/sufijo en BD)
        List<UserCustomer> candidates = repository
            .findByFarmerCompanyInternalIdContainingAndType(
                cedula, UserCustomerType.FARMER);
        
        if (candidates.size() == 1) {
            return candidates.get(0);
        } else if (candidates.size() > 1) {
            // Múltiples matches: refinar con prefijo
            String prefix = extractPrefix(idInterno);
            return candidates.stream()
                .filter(c -> c.getFarmerCompanyInternalId().startsWith(prefix))
                .findFirst()
                .orElse(null);
        }
    }
    
    return null; // No encontrado
}

private String extractCedulaNumber(String idInterno) {
    // Patrón: 1-3 letras + 10-12 dígitos (+ opcional sufijo)
    Pattern pattern = Pattern.compile("^[A-Z]{1,3}([0-9]{10,12})(-.*)?$");
    Matcher matcher = pattern.matcher(idInterno);
    
    if (matcher.matches()) {
        return matcher.group(1); // Número de cédula
    }
    
    return null;
}

private String extractPrefix(String idInterno) {
    Pattern pattern = Pattern.compile("^([A-Z]{1,3})[0-9]");
    Matcher matcher = pattern.matcher(idInterno);
    return matcher.find() ? matcher.group(1) : "";
}
```

**Importante:**
- Algunos `ID_INTERNO` tienen **espacios** (ej: "ASH 6690", "ASH 0545"). Estos solo hacen match exacto.
- Los códigos con **10-12 dígitos** (ej: VI2400185795, MY1201209648) representan cédulas de identidad.
- Si el agricultor no existe en BD, se debe crear primero o registrar como error de importación.

#### 2. Conversión Feature → Plot → PlotCoordinate

Para cada `Feature` en el GeoJSON:

1. **Crear entidad `Plot`:**
   - `farmer_id` = ID del `UserCustomer` encontrado
   - `plotName` = valor de `ID_DE_UN` (ej: "ASH5493 LOTE 1")
   - `size` = valor de `HECTAREA`
   - `unit` = "ha" (hectáreas)
   - `crop_id` = buscar/crear `ProductType` según `CULTIVOPRI`
   - `geoId` = puede usar `ID_INTERNO` + `COD_LOTE` para identificación única
   - `lastUpdated` = fecha de importación

2. **Crear entidades `PlotCoordinate`:**
   - Para cada punto del polígono en `geometry.coordinates`:
     - Extraer cada par `[longitude, latitude]` del primer ring del primer polígono
     - Crear un `PlotCoordinate` con:
       - `plot_id` = ID del `Plot` creado
       - `longitude` = primer valor del par
       - `latitude` = segundo valor del par

**Ejemplo de extracción de coordenadas:**

```javascript
// Geometría del GeoJSON (MultiPolygon)
"coordinates": [ [ [ 
  [ -76.730581666686433, -0.122241975276335 ],
  [ -76.730636666686635, -0.123448641942977 ],
  [ -76.731846666686664, -0.12359197527611 ],
  [ -76.731686666710715, -0.122240687479383 ],
  [ -76.730581666686433, -0.122241975276335 ]
] ] ]

// Se convierte en 5 PlotCoordinate (uno por vértice):
// PlotCoordinate 1: longitude=-76.730581666686433, latitude=-0.122241975276335
// PlotCoordinate 2: longitude=-76.730636666686635, latitude=-0.123448641942977
// PlotCoordinate 3: longitude=-76.731846666686664, latitude=-0.12359197527611
// PlotCoordinate 4: longitude=-76.731686666710715, latitude=-0.122240687479383
// PlotCoordinate 5: longitude=-76.730581666686433, latitude=-0.122241975276335 (cierre)
```

#### 3. Casos especiales: múltiples lotes por agricultor

**Hallazgo:** Existen **268 `ID_INTERNO` duplicados**, lo que significa que un mismo agricultor tiene múltiples lotes.

Ejemplos:
- `ASH3589`: aparece 2 veces (LOTE 1 y LOTE 2)
- `ASH8911`: aparece 2 veces (LOTE 1 y LOTE 2)
- `ASH5751`: aparece 2 veces (LOTE 1 y LOTE 2)
- `APSH`: aparece 2 veces

**Solución:**
- Un `UserCustomer` (farmer) puede tener múltiples `Plot` (relación 1:N ✓).
- Cada feature debe crear un `Plot` **independiente**, aunque el `ID_INTERNO` se repita.
- Diferenciar usando `plotName` (que incluye el código de lote).

### Validaciones recomendadas

1. **Pre-importación:**
   - ✅ Verificar que todos los `ID_INTERNO` existan en `UserCustomer.farmerCompanyInternalId`
   - ✅ Identificar `ID_INTERNO` faltantes y crear los agricultores primero
   - ✅ Validar que no existan `Plot` duplicados para el mismo farmer+lote

2. **Durante importación:**
   - ✅ Validar geometrías (al menos 3 puntos para formar un polígono)
   - ✅ Verificar coordenadas en rango válido (latitud: -90 a 90, longitud: -180 a 180)
   - ✅ Manejar `MultiPolygon` correctamente (algunos features pueden tener múltiples polígonos)

3. **Post-importación:**
   - ✅ Verificar que cada `Plot` tenga al menos 3 `PlotCoordinate`
   - ✅ Validar que la relación `UserCustomer.plots` esté correctamente poblada

### Consideraciones técnicas

#### Transaccionalidad
- Importar por lotes (batch) para mejor performance
- Usar transacciones para garantizar atomicidad (Plot + PlotCoordinate juntos)
- Si falla la creación de coordenadas, hacer rollback del Plot

#### Cascade y orphan removal
Como `UserCustomer.plots` tiene `cascade = ALL` y `orphanRemoval = true`:
- Si se borra un `UserCustomer`, se borran automáticamente sus `Plot`
- Si se borra un `Plot`, se borran automáticamente sus `PlotCoordinate`

#### Performance
- **EAGER fetch** en `UserCustomer.plots` puede ser problemático con muchos plots
- Considerar usar fetch join cuando se necesite cargar plots+coordinates
- Índices en `Plot.farmer_id` y `PlotCoordinate.plot_id` ya existen (FKs automáticos)

### Estadísticas del archivo GeoJSON

- **Total de features/polígonos:** 1,260
- **IDs únicos de agricultores:** ~992 (268 tienen múltiples lotes)
- **Máximo de lotes por agricultor:** varía (algunos tienen 2-3 lotes)
- **Asociación principal:** APROCASH
- **Cultivo principal:** CACAO NACIONAL
- **Rango de hectáreas:** 0 - 52 ha

### Script de importación (pseudocódigo)

```java
// 1. Cargar GeoJSON
FeatureCollection features = parseGeoJSON("import/poligonos_16_10_2025.geojson");

int importedCount = 0;
int notFoundCount = 0;
List<String> notFoundIds = new ArrayList<>();

// 2. Para cada feature
for (Feature feature : features.getFeatures()) {
    String idInterno = feature.getProperty("ID_INTERNO");
    
    // 3. Buscar UserCustomer usando matching inteligente
    UserCustomer farmer = findFarmerByIdInterno(idInterno);
    
    if (farmer == null) {
        log.warn("Farmer not found for ID_INTERNO: {}", idInterno);
        notFoundIds.add(idInterno);
        notFoundCount++;
        continue; // Saltar esta feature o crear el farmer
    }
    
    // 4. Crear Plot
    Plot plot = new Plot();
    plot.setFarmer(farmer);
    plot.setPlotName(feature.getProperty("ID_DE_UN"));
    plot.setSize(feature.getProperty("HECTAREA"));
    plot.setUnit("ha");
    plot.setGeoId(idInterno + "_" + feature.getProperty("COD_LOTE"));
    plot.setLastUpdated(new Date());
    
    // Opcional: asociar crop/ProductType
    String cultivoPri = feature.getProperty("CULTIVOPRI");
    ProductType crop = findOrCreateProductType(cultivoPri);
    plot.setCrop(crop);
    
    // 5. Extraer coordenadas del MultiPolygon
    MultiPolygon geometry = (MultiPolygon) feature.getGeometry();
    
    // Manejar MultiPolygon: tomar el primer polígono del primer array
    if (geometry.getCoordinates().length > 0 
        && geometry.getCoordinates()[0].length > 0) {
        
        Polygon firstPolygon = geometry.getCoordinates()[0][0];
        List<PlotCoordinate> coordinates = new ArrayList<>();
        
        for (Position position : firstPolygon) {
            PlotCoordinate coord = new PlotCoordinate();
            coord.setPlot(plot);
            coord.setLongitude(position.getLongitude());
            coord.setLatitude(position.getLatitude());
            coordinates.add(coord);
        }
        
        // Validar: al menos 3 coordenadas para formar un polígono
        if (coordinates.size() >= 3) {
            plot.setCoordinates(coordinates);
            
            // 6. Persistir (cascade guardará las coordenadas)
            plotRepository.save(plot);
            importedCount++;
            
            log.debug("Plot importado: {} para farmer: {}", 
                     plot.getPlotName(), farmer.getFarmerCompanyInternalId());
        } else {
            log.error("Polígono inválido (< 3 puntos) para: {}", idInterno);
        }
    } else {
        log.error("Geometría vacía para: {}", idInterno);
    }
}

// 7. Resumen de importación
log.info("=== Importación completada ===");
log.info("Total features procesadas: {}", features.getFeatures().size());
log.info("Plots importados: {}", importedCount);
log.info("Farmers no encontrados: {}", notFoundCount);

if (!notFoundIds.isEmpty()) {
    log.warn("IDs no encontrados: {}", String.join(", ", notFoundIds));
}

// Método auxiliar: matching inteligente (definido arriba en sección 1)
private UserCustomer findFarmerByIdInterno(String idInterno) {
    // Ver implementación completa en sección "Matching Inteligente"
    // 1. Match exacto
    // 2. Extracción de cédula y búsqueda parcial
    ...
}
```

## Archivos relevantes

- `src/main/java/com/abelium/inatrace/db/entities/common/UserCustomer.java`
- `src/main/java/com/abelium/inatrace/db/entities/common/Plot.java`
- `src/main/java/com/abelium/inatrace/db/entities/common/PlotCoordinate.java`
- `src/main/resources/db/migrations/V1__Initial_schema.sql`
- `import/poligonos_16_10_2025.geojson`
