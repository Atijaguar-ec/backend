# Shrimp Chain Backend — Specification

## Purpose

Especificación del dominio de trazabilidad de camarón para el microservicio `ShrimpTraceabilityService` y la configuración del Core INATrace. Cubre las Épicas 0-5 del backlog con criterios de aceptación testables.

---

## Domain: Lot Management (Épica 1)

### Requirement: Lote Base Único

El sistema MUST generar un identificador de lote base único e inmutable al registrar la recepción de materia prima.

#### Scenario: Creación exitosa de lote base
- GIVEN un recepcionista autenticado con rol `operador_recepcion`
- WHEN registra una recepción con peso bruto (lbs), cantidad de gavetas/bines, y tipo (ENTERO o COLA)
- THEN el sistema genera un lote base con formato `DDMMYY` (ej: `250121`)
- AND el lote base queda asociado a la facility de recepción y la compañía del usuario
- AND el `lotNumber` es inmutable — no puede modificarse después de la creación

#### Scenario: Lote base duplicado en mismo día
- GIVEN que ya existe un lote `250121`
- WHEN se registra otra recepción el mismo día
- THEN el sistema genera un lote con sufijo secuencial: `250121-A`, `250121-B`

#### Scenario: Decisión inicial Entero vs Cola
- GIVEN un lote base recién creado
- WHEN el inspector de calidad selecciona el modo de entrada
- THEN si es "Entero" → el lote se deriva directamente a clasificación
- AND si es "Cola" → el lote pasa por pesado y descabezado previo antes de clasificación

---

## Domain: Classification (Épica 2)

### Requirement: Clasificación por Talla con Doble Unidad

El sistema MUST registrar la clasificación del camarón por talla, capturando TANTO el conteo de cajetas (piezas) como el peso en libras (gavetas) por cada talla.

#### Scenario: Registro de clasificación exitoso
- GIVEN un lote en estado `EN_CLASIFICACION` y un operario con rol `operador_clasificacion`
- WHEN el operario registra la salida para talla `26/30`
- THEN MUST ingresar cajetas (conteo entero ≥ 0) Y libras (decimal ≥ 0)
- AND MUST seleccionar la máquina clasificadora (1-4) donde se procesó
- AND el registro se asocia al lote base y a la ronda de clasificación (1=original, 2=reingreso cola)

#### Scenario: Resumen antes de cerrar clasificación
- GIVEN que se han registrado múltiples salidas por talla
- WHEN el operario solicita cerrar la clasificación del lote
- THEN el sistema MUST mostrar un resumen: total cajetas y total libras por talla
- AND SHOULD alertar si la suma de libras clasificadas difiere >5% del peso bruto de entrada

### Requirement: Ciclo de Rechazo — Conversión Entero → Cola

El sistema MUST soportar el reingreso de camarón rechazado como cola re-clasificable bajo el mismo lote base.

#### Scenario: Rechazo y reingreso exitoso
- GIVEN un lote de camarón entero en clasificación
- WHEN el operario registra 360 lbs de rechazo de entero
- THEN el sistema crea un `RejectionCycle` vinculado al lote original
- AND el rechazo se envía a descabezado
- AND al completar descabezado, se registran: lbs de cola recuperada (ej: 306) y lbs de merma-cabeza (ej: 54)
- AND la cola recuperada reingresa a las mismas 4 máquinas clasificadoras como ronda 2

#### Scenario: Re-clasificación de cola reingresada
- GIVEN cola reingresada de un ciclo de rechazo
- WHEN la cola pasa por las máquinas clasificadoras
- THEN se genera un nuevo `ClassificationRecord` con `round=2` y `sourceType=COLA_REINGRESO`
- AND las tallas clasificadas se asocian al lote base original (inmutable)

#### Scenario: Merma de descabezado registrada
- GIVEN un ciclo de rechazo con 360 lbs de entero rechazado
- WHEN se completa el descabezado con 306 lbs de cola y 54 lbs de cabezas
- THEN se crea un `WasteRecord` con motivo `CABEZAS` y 54 lbs
- AND el balance del lote refleja: 306 lbs reingresadas + 54 lbs merma = 360 lbs originales

---

## Domain: Processing & Lot Suffixes (Épica 3)

### Requirement: Derivación por Destino con Sufijo Automático

El sistema MUST asignar sufijos automáticos al lote base cuando el jefe de producción derive producto a un destino productivo.

#### Scenario: Asignación de sufijos según destino
- GIVEN un lote clasificado con producto disponible
- WHEN el jefe de producción asigna porciones a destinos
- THEN cada destino genera un `ShrimpBatch` con sufijo fijo:
  - Bloque → sin sufijo (lote base)
  - IQF → `-2`
  - Valor Agregado → `-3`
  - Salmuera → `-4`
- AND el `ShrimpBatch.lotIdentifier` = `lotNumber` + sufijo (ej: `250121-2`)

#### Scenario: Sufijos parciales — no todos los destinos
- GIVEN un lote clasificado
- WHEN el jefe de producción solo asigna a Bloque e IQF
- THEN se crean solo 2 `ShrimpBatch`: `250121` (bloque) y `250121-2` (IQF)
- AND NO se crean batches para VA ni Salmuera

#### Scenario: Peso asignado no puede exceder el disponible
- GIVEN un lote con 2000 lbs disponibles después de clasificar
- WHEN se intenta asignar 1200 lbs a Bloque y 1000 lbs a IQF (total 2200 > 2000)
- THEN el sistema MUST rechazar la operación con error de validación
- AND SHOULD mostrar el peso disponible restante

### Requirement: Procesamiento IQF y Valor Agregado

El sistema MUST registrar el procesamiento específico por destino.

#### Scenario: Empaque IQF
- GIVEN un `ShrimpBatch` con sufijo `-2` (IQF)
- WHEN el operario registra el procesamiento
- THEN MUST capturar: tipo de congelación (Brine CBFI / Cyclone1000), peso por funda, cantidad de fundas
- AND el estado del batch cambia a `CONGELADO`

#### Scenario: Procesamiento Valor Agregado
- GIVEN un `ShrimpBatch` con sufijo `-3` (VA)
- WHEN el operario registra el procesamiento
- THEN MUST capturar: sub-tipo (PPV / PUD / P&D / EZ-PEEL / Estuche), peso procesado
- AND se vincula al proceso de hidratación o marinado si aplica

---

## Domain: Mastering & Settlement (Épica 4)

### Requirement: Masterizado con Etiquetado y QR

El sistema MUST validar la coincidencia del producto con la orden de producción antes de generar la etiqueta del cartón máster.

#### Scenario: Match exitoso y generación de etiqueta
- GIVEN un `ShrimpBatch` congelado listo para masterizar
- WHEN el supervisor valida: Marca, Talla, Lote (con sufijo), Tipo de congelación, Presentación
- THEN el sistema genera una etiqueta con todos los campos
- AND genera un código QR vinculado al lote+sufijo usando `ProcessingAction.type=GENERATE_QR_CODE` del Core
- AND crea un `MasterCarton` con peso bruto, presentación y referencia al batch

#### Scenario: Match fallido
- GIVEN un producto cuya talla no coincide con la orden de producción
- WHEN el supervisor intenta validar
- THEN el sistema MUST rechazar el masterizado
- AND SHOULD mostrar las discrepancias encontradas

### Requirement: Liquidación Escalonada por Área

El sistema MUST soportar liquidación en dos partes: clasificación (entrada) y área destino (salida).

#### Scenario: Liquidación de clasificación
- GIVEN un lote con clasificación completada (todas las rondas)
- WHEN el supervisor cierra la liquidación de clasificación
- THEN el sistema calcula: total lbs entrada vs suma (lbs clasificadas por talla + lbs merma registradas)
- AND la diferencia MUST ser ≤ tolerancia configurable (default 1%)
- AND si supera tolerancia → SHOULD alertar pero permitir cierre con justificación

#### Scenario: Liquidación de área destino
- GIVEN un área (ej: IQF) con batches procesados y masters producidos
- WHEN el supervisor cierra la liquidación del área
- THEN el sistema calcula: lbs recibidas (input) vs (masters * peso por master + merma registrada)
- AND el supervisor MUST registrar motivo de merma si existe diferencia
- AND motivos válidos: AGUA, BASURA, CABEZAS, CALIBRACION, OTRO

#### Scenario: Alerta de merma anómala
- GIVEN histórico de merma por área con ≥10 liquidaciones previas
- WHEN la merma del lote actual supera la mediana histórica + 2 desviaciones estándar
- THEN el sistema SHOULD generar una alerta de desviación de merma
- AND registrar el evento para análisis posterior

### Requirement: Transferencia a Cámara con FIFO

El sistema MUST registrar la transferencia de masters a la cámara de mantenimiento y SHOULD alertar sobre despachos fuera de orden FIFO.

#### Scenario: Transferencia exitosa a cámara
- GIVEN masters empacados y liquidados en un área
- WHEN el bodeguero ejecuta la transferencia a cámara
- THEN se crea un registro en `ChamberInventory` con: lote, sufijo, marca, talla, presentación, cantidad masters, cámara destino, fecha ingreso

#### Scenario: Alerta FIFO al despachar
- GIVEN stock de talla 26/30 con masters del lote 250120 (más antiguo) y 250121 (más reciente)
- WHEN el bodeguero intenta despachar masters del lote 250121
- THEN el sistema SHOULD alertar que existen masters más antiguos de la misma talla
- AND el bodeguero MAY continuar con el despacho justificando la excepción

---

## Domain: Auth & Roles (Épica 0)

### Requirement: Control de Acceso por Rol vía Keycloak

El sistema MUST validar los roles del JWT de Keycloak para controlar acceso a funciones.

#### Scenario: Acceso autorizado por rol
- GIVEN un usuario con rol `jefe_produccion` en el JWT de Keycloak
- WHEN intenta acceder a la función de derivación por destinos (Épica 3)
- THEN el sistema MUST permitir el acceso

#### Scenario: Acceso denegado por rol insuficiente
- GIVEN un usuario con rol `operador_recepcion` (sin rol `jefe_produccion`)
- WHEN intenta acceder a la función de derivación por destinos
- THEN el sistema MUST denegar con HTTP 403
- AND SHOULD mostrar mensaje claro indicando el rol requerido

| Rol | Acciones permitidas |
|---|---|
| `operador_recepcion` | Crear lotes, registrar recepciones, ver inventario |
| `operador_clasificacion` | Registrar clasificaciones, registrar rechazos |
| `jefe_produccion` | Asignar destinos con sufijos, ver todo |
| `supervisor_area` | Cerrar liquidaciones, registrar merma, masterizar |
| `auditor_calidad` | Ver todo (read-only), exportar reportes |
| `bodeguero` | Transferir a cámaras, despachar, ver inventario |

---

## Domain: Visibility & Reporting (Épica 5)

### Requirement: Diagrama Sankey de Flujo de Masa

El sistema SHOULD proveer una visualización tipo Sankey del flujo de masa de un lote.

#### Scenario: Sankey de lote completo
- GIVEN un lote con clasificación, derivación a destinos y liquidación completadas
- WHEN el gerente selecciona el lote en el dashboard
- THEN se muestra un diagrama Sankey: Recepción (total lbs) → Tallas → Destinos (Bloque/IQF/VA/Salmuera) → Masters + Merma
- AND cada nodo muestra las libras correspondientes
- AND la suma de todas las salidas + merma MUST igualar la entrada

### Requirement: Alerta de Desviación de Merma

El sistema SHOULD alertar cuando la merma de un proceso supere la mediana histórica.

#### Scenario: Alerta por merma atípica en descabezado
- GIVEN que la mediana de merma en descabezado de los últimos 30 lotes es 15%
- WHEN un nuevo lote registra 22% de merma en descabezado
- THEN el sistema calcula que 22% > mediana(15%) + 2σ
- AND genera una alerta visible para `jefe_produccion` y `auditor_calidad`

### Requirement: Exportación de Historial de Lote

El sistema SHOULD permitir exportar el historial completo de un lote en PDF.

#### Scenario: Exportación exitosa
- GIVEN un lote con trazabilidad completa (recepción → masterizado → cámara)
- WHEN el auditor solicita exportar
- THEN se genera un PDF con: datos recepción, clasificaciones por talla, destinos con sufijos, liquidaciones por área, masters producidos, mermas con motivo, movimientos de cámara
