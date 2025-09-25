# Campo maxProductionQuantity - Implementación Backend

## Resumen
Campo agregado para almacenar la cantidad máxima de producción en quintales (qq) en la información de la finca.

## Archivos Modificados

### 1. Entidad JPA
**Archivo:** `src/main/java/com/abelium/inatrace/db/entities/common/FarmInformation.java`
- ✅ Agregado campo `maxProductionQuantity` (BigDecimal)
- ✅ Agregados getters y setters

### 2. DTO API
**Archivo:** `src/main/java/com/abelium/inatrace/components/product/api/ApiFarmInformation.java`
- ✅ Agregado campo `maxProductionQuantity` (BigDecimal)
- ✅ Agregada anotación `@Schema(description = "Maximum production quantity (qq)")`
- ✅ Agregados getters y setters

### 3. Migración de Base de Datos
**Archivo:** `src/main/resources/db/migrations/V2025_09_24_23_35__Add_maxProductionQuantity_to_FarmInformation.sql`
- ✅ Agregada columna `max_production_quantity DECIMAL(19,2)` a tabla `user_customer`
- ✅ Agregado comentario descriptivo

### 4. Mappers/Converters
**Archivos modificados:**
- ✅ `CompanyApiTools.java` - Método `toApiFarmInformation()`
- ✅ `CompanyService.java` - Dos métodos de mapeo DTO → Entidad
- ✅ `ProductMapper.java` - Método de mapeo

## Próximos Pasos

### Backend
1. **Compilar y probar** el backend
2. **Ejecutar migración** de base de datos
3. **Regenerar documentación OpenAPI**

### Frontend
1. **Regenerar modelos TypeScript** desde OpenAPI actualizado
2. **Eliminar código temporal** del componente
3. **Verificar funcionamiento** del campo

## Comandos de Verificación

### Compilar Backend
```bash
cd /backend
./mvnw clean compile
```

### Ejecutar Migraciones
```bash
./mvnw flyway:migrate
```

### Regenerar OpenAPI (si aplica)
```bash
./mvnw spring-boot:run
# Acceder a http://localhost:8080/v3/api-docs
```

## Estructura de Base de Datos

```sql
-- Columna agregada a user_customer
max_production_quantity DECIMAL(19,2) NULL
-- Permite valores decimales hasta 99999999999999999.99
-- NULL permitido para compatibilidad con datos existentes
```

## Notas Técnicas

- **Tipo de Dato:** `BigDecimal` para precisión decimal
- **Unidad:** Quintales (qq) como se especifica en la descripción
- **Nullable:** Sí, para compatibilidad con registros existentes
- **Validación:** Sin validaciones específicas (se pueden agregar después)

## Testing

Después de la implementación, verificar:
1. ✅ Campo aparece en respuestas API
2. ✅ Campo se guarda correctamente en base de datos
3. ✅ Mapeo bidireccional funciona (DTO ↔ Entidad)
4. ✅ Frontend puede leer y escribir el campo
