# Shrimp Chain Frontend — Specification

## Purpose

Especificación del micro-frontend `shrimpMfe` y las libs compartidas Nx para la cadena de camarón. Define el comportamiento de vistas, navegación, autenticación y UX para operadores de planta.

---

## Domain: Navigation & MFE Integration

### Requirement: Carga Remota del shrimpMfe

El Host MUST cargar el `shrimpMfe` como Remote Application vía Module Federation bajo la ruta `/camaron`.

#### Scenario: Navegación exitosa al módulo camarón
- GIVEN un usuario autenticado en el Host
- WHEN navega a `/camaron` desde el sidebar
- THEN el Host carga el chunk del shrimpMfe de forma lazy
- AND muestra las sub-rutas del módulo de camarón sin recarga de página

#### Scenario: Fallback si el Remote no está disponible
- GIVEN que el shrimpMfe no está desplegado o no responde
- WHEN el usuario navega a `/camaron`
- THEN el Host MUST mostrar un mensaje de error amigable
- AND MUST NOT romper la navegación del resto de la aplicación

---

## Domain: Reception View

### Requirement: Formulario de Recepción de Materia Prima

La vista de Recepción MUST permitir crear un lote base con los datos mínimos de la materia prima.

#### Scenario: Registro de recepción exitoso
- GIVEN un operario con rol `operador_recepcion` en la vista `/camaron/recepcion`
- WHEN completa: peso bruto (lbs), cantidad gavetas/bines, tipo (Entero/Cola) y presiona "Registrar"
- THEN el sistema crea el lote base vía API
- AND muestra confirmación con el número de lote generado (ej: `250121`)
- AND el lote aparece en la lista de lotes activos

#### Scenario: Validación de campos obligatorios
- GIVEN el formulario de recepción
- WHEN el operario intenta registrar sin ingresar peso bruto
- THEN el sistema MUST bloquear el envío y resaltar el campo faltante

---

## Domain: Classification View

### Requirement: Registro de Clasificación con Doble Unidad

La vista de Clasificación MUST permitir registrar salidas por talla con cajetas (conteo) y libras (peso).

#### Scenario: Registro por talla con presets
- GIVEN un lote en clasificación y un operario con rol `operador_clasificacion`
- WHEN el operario selecciona talla `26/30` desde los **botones preset** (no dropdown)
- THEN se abre un formulario con dos inputs: cajetas (numérico entero) y libras (decimal)
- AND MUST seleccionar máquina clasificadora (1-4)
- AND al confirmar, el registro se agrega a la tabla resumen

#### Scenario: Teclado numérico propio
- GIVEN un input de peso o conteo activo
- WHEN el operario toca el campo
- THEN el sistema MUST mostrar un teclado numérico custom (no el del OS)
- AND los botones del teclado MUST tener un tamaño mínimo de 48x48px

#### Scenario: Registro de rechazo
- GIVEN un lote en clasificación
- WHEN el operario presiona "Registrar Rechazo"
- THEN MUST ingresar: libras de rechazo entero
- AND el sistema envía el rechazo al flujo de descabezado
- AND muestra estado "Pendiente de reingreso como cola"

---

## Domain: Destination View

### Requirement: Asignación de Destinos con Sufijos

La vista de Destino MUST permitir al jefe de producción asignar porciones del lote a destinos productivos.

#### Scenario: Asignación parcial de destinos
- GIVEN un lote clasificado con 2000 lbs disponibles y un usuario con rol `jefe_produccion`
- WHEN selecciona destinos mediante checkboxes: ☑ Bloque, ☑ IQF, ☐ VA, ☐ Salmuera
- AND asigna 1200 lbs a Bloque y 800 lbs a IQF
- THEN el sistema crea dos `ShrimpBatch`: `250121` (Bloque) y `250121-2` (IQF)
- AND muestra los sufijos generados automáticamente
- AND el total asignado (2000) MUST igualar el disponible

#### Scenario: Validación de peso excedido
- GIVEN 2000 lbs disponibles
- WHEN el usuario asigna 1500 + 800 = 2300 lbs (excede disponible)
- THEN el formulario MUST mostrar error en tiempo real antes de enviar
- AND MUST resaltar el campo que causa el exceso

---

## Domain: Settlement View

### Requirement: Liquidación Visual por Área

La vista de Liquidación MUST mostrar el cierre escalonado con input vs output por área.

#### Scenario: Liquidación con selección de motivo de merma
- GIVEN un área IQF con 800 lbs recibidas y 780 lbs en masters producidos
- WHEN el supervisor cierra la liquidación
- THEN el sistema calcula 20 lbs de merma (2.5%)
- AND MUST solicitar motivo de la merma via selector: AGUA / BASURA / CABEZAS / CALIBRACION / OTRO
- AND muestra resumen visual: barra de progreso input → output + merma

#### Scenario: Alerta de merma anómala
- GIVEN que la mediana histórica de merma en IQF es 2%
- WHEN la merma actual es 5% (supera mediana + 2σ)
- THEN la vista SHOULD mostrar un banner de alerta en color rojo/naranja
- AND el banner muestra: "Merma actual 5% — Mediana histórica 2% — ⚠️ Desviación significativa"

---

## Domain: Inventory View

### Requirement: Inventario de Cámaras con Búsqueda

La vista de Inventario MUST mostrar el stock congelado en cámaras con filtros.

#### Scenario: Búsqueda por lote base
- GIVEN stock en múltiples cámaras
- WHEN el bodeguero ingresa "250121" en el campo de búsqueda
- THEN la vista filtra y muestra todos los registros con lote base `250121` (incluyendo sufijos: 250121, 250121-2, 250121-3)

#### Scenario: Alerta FIFO al despachar
- GIVEN masters de talla 26/30 del lote 250120 (antiguo) y 250121 (reciente)
- WHEN el bodeguero selecciona masters del lote 250121 para despacho
- THEN el sistema SHOULD mostrar alerta: "Existen masters más antiguos de esta talla (Lote 250120)"
- AND el bodeguero MAY continuar el despacho ingresando justificación

---

## Domain: Keycloak Auth in MFE

### Requirement: SSO entre Host y Remote sin Re-auth

El shrimpMfe MUST consumir el token de Keycloak inicializado por el Host sin re-autenticación.

#### Scenario: Navegación sin re-login
- GIVEN un usuario autenticado en el Host con Keycloak
- WHEN navega de una vista Core a `/camaron/recepcion`
- THEN el shrimpMfe MUST tener acceso al token JWT sin solicitar login
- AND las llamadas HTTP del shrimpMfe MUST incluir el Bearer token

#### Scenario: Token expirado durante uso del MFE
- GIVEN un usuario trabajando en el shrimpMfe con token próximo a expirar
- WHEN el token expira
- THEN `keycloak-angular` MUST renovar el token silenciosamente
- AND MUST NOT interrumpir el trabajo del operario con redirect a login
- AND si la renovación falla → redirigir a login de Keycloak

---

## Domain: Plant-Optimized UX

### Requirement: UI Adaptada para Operadores de Planta Fría

La UI MUST estar optimizada para uso con guantes en ambiente industrial frío.

#### Scenario: Tap targets mínimos
- GIVEN cualquier elemento interactivo (botón, dropdown, checkbox, input)
- WHEN se renderiza en pantalla
- THEN MUST tener un área táctil mínima de 48x48px (estándar WCAG 2.5.5)

#### Scenario: Modo oscuro por defecto
- GIVEN la primera carga del shrimpMfe
- WHEN el sistema no tiene preferencia guardada del usuario
- THEN MUST renderizar en modo oscuro por defecto
- AND el usuario MAY cambiar a modo claro desde settings

#### Scenario: Feedback multisensorial
- GIVEN que el operario registra una clasificación exitosa
- WHEN el servidor confirma la operación
- THEN la UI MUST mostrar toast visual de confirmación
- AND SHOULD emitir feedback háptico (vibración 100ms) en dispositivos que lo soporten
- AND MAY emitir sonido corto de confirmación

#### Scenario: Inputs con font ≥16px
- GIVEN cualquier campo de texto o input numérico
- WHEN se renderiza en un dispositivo iOS
- THEN el font-size MUST ser ≥16px para evitar auto-zoom del navegador
