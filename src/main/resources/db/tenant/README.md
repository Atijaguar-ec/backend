# Migraciones de datos por tenant

UNOCACE y Fortaleza del Valle corren en **servidores y bases independientes**, cada uno con
su propia lógica y sus propios datos. El esquema es común a los dos; la configuración y los
datos no lo son.

| Carpeta | Se aplica a | Rango de versiones | Se activa con |
| :--- | :--- | :--- | :--- |
| `classpath:db/migration` | Las dos bases | `V1`–`V10xx` | Siempre |
| `classpath:db/tenant/fv` | Solo Fortaleza del Valle | `V1101`, `V1102`, ... | Perfil `fortaleza` |
| `classpath:db/tenant/unocace` | Solo UNOCACE | `V1201`, `V1202`, ... | Perfil `unocace` |

## Qué va en cada sitio

- **`db/migration`**: cambios de esquema (DDL) y datos verdaderamente comunes. Nada que
  dependa de qué organización es la dueña de la base.
- **`db/tenant/<tenant>`**: todo lo que escriba `company.configuration`, `facility` o
  `stockorder` para un cliente concreto. Al vivir fuera del `locations` del otro servidor,
  **no puede** ejecutarse allí ni por error.

Los rangos de versión no se solapan, así que las dos carpetas comparten la tabla
`schema_version` sin colisionar y `out-of-order = false` sigue siendo válido.

## Prohibido

Delimitar el alcance de una migración por el nombre de la empresa:

```sql
-- NUNCA
WHERE NOT (upper(name) LIKE '%FORTALEZA%' OR upper(abbreviation) LIKE '%FDV%')
```

Fue el origen de la regresión que corrige `V11__align_fv_tenant_configuration.sql`: las
organizaciones socias de un tenant no repiten el nombre de su matriz, así que el `LIKE`
las clasifica en el tenant equivocado. Ver `agent-context.md`, sección 28.

## Requisito de despliegue

El perfil del tenant debe estar activo para que su carpeta entre en el `locations`:

```
SPRING_PROFILES_ACTIVE=prod,fortaleza    # servidor de Fortaleza del Valle
SPRING_PROFILES_ACTIVE=prod,unocace      # servidor de UNOCACE
```

Sin el perfil, las migraciones de esa carpeta **no se ejecutan** (no fallan, se omiten).
Después de cada despliegue, verificar en el log la línea de Flyway con las migraciones
aplicadas.
