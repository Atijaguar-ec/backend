# Análisis del Informe del Product Owner — Cadena de Camarón

## Documento revisado
`dufer-camaron/NFORME DE PRODUCT OWNER_.md` — Informe de PO usando Design Thinking para integrar la cadena de camarón en INATrace.

---

## Mapeo Épicas del PO → Fases de la Propuesta Técnica

| Épica del PO | User Stories | Nuestra Fase | Complejidad |
|---|---|---|---|
| **Épica 1**: Recepción y QC | US 1.1 (Lote Base), US 1.2 (Decisión Inicial) | Fase 1 — Config Core + vistas shrimpMfe | Baja |
| **Épica 2**: Clasificación + Rechazos | US 2.1 (Recuperación de Rechazo) | Fase 1 (clasificación básica) + Fase 3 (rechazo→microservicio) | Alta |
| **Épica 3**: Procesamiento + Sufijos | US 3.1 (Sufijos automáticos), US 3.2 (Empaque IQF/VA) | Fase 3 — Microservicio ShrimpTraceabilityService | Media |
| **Épica 4**: Masterizado + Liquidación | US 4.1 (Etiquetado QR), US 4.2 (Liquidación), US 4.3 (Transfer a Cámara) | Fase 3 — Microservicio + QR del Core | Alta |

---

## ✅ Lo que el PO tiene correcto y debemos respetar

1. **Sufijos son regla de negocio fija**: Bloque (sin sufijo), IQF (-2), VA (-3), Salmuera (-4). No son configurables.
2. **Lote base se genera en recepción y nunca se divide** administrativamente — solo se extiende con sufijos.
3. **El rechazo es conversión, no pérdida** — las piezas de entero que no pasan calidad se descabezan y reingresan como cola bajo el mismo lote. Esto es diferenciador vs café/cacao.
4. **Los sufijos se registran hoy manualmente** en el campo `Nombre del lote interno` (= `StockOrder.internalLotNumber`). Fase 1 puede funcionar así; Fase 3 lo automatiza.
5. **La etiqueta del Master incluye**: Marca, Talla, Lote, Peso, Presentación, Tipo de congelación.
6. **El QR del Master usa el módulo existente** de INATrace (`ProcessingAction.type = GENERATE_QR_CODE`).
7. **Liquidación = dos partes**: liquidación inicial en clasificación (entradas por talla) + liquidación final por área (output Masters vs input libras).

---

## ⚠️ Gaps: Lo que falta en el informe

### Gap 1: No hay US para doble unidad (cajetas + libras)
- **Dónde lo menciona**: Sección "Empatizar" — *"pesan en gavetas (libras) para algunos procesos, pero cuentan manualmente cajetas para otros"*
- **Problema**: No se traduce a una User Story implementable
- **US sugerida**:
  > **US 2.2** — Como Operario de Clasificación, quiero registrar tanto el conteo de cajetas como el peso en libras por cada talla clasificada, para que la liquidación use ambas unidades.

### Gap 2: No hay US para merma con motivo
- **Dónde lo menciona**: Sección "Definir" — *"registrar la diferencia de libras (por agua, basura o cabezas descartadas) como un ajuste o merma justificada"*
- **Problema**: US 4.2 habla de "documentar la merma justificable" pero no pide registrar el **motivo**
- **US sugerida**:
  > **US 4.4** — Como Supervisor de Área, quiero seleccionar el motivo de la merma (agua, basura, cabezas, ajuste de báscula) al liquidar, para que el cierre del lote explique el origen de cada diferencia.

### Gap 3: US 4.3 (FIFO en cámaras) es vaga
- **Qué dice**: *"mover los másters finales desde las áreas de empaque hacia la Cámara de Mantenimiento bajo la regla de inventario FIFO"*
- **Problema**: "FIFO" no tiene criterio de aceptación medible
- **Criterio sugerido**: El sistema muestra stock ordenado por fecha de ingreso y alerta si se despacha un master que no es el más antiguo disponible.

### Gap 4: No hay épica de reportes ni dashboards
- **Dónde lo alude**: Sección "Evaluar" — *"Transparencia de Lotes"*, *"Precisión de Inventario"*, *"Gestión de Exportaciones Visibles"*
- **Problema**: Habla de valor de negocio pero no pide funcionalidad de visualización
- **Épica sugerida**:
  > **Épica 5: Visibilidad y Reportes**
  > - US 5.1 — Diagrama Sankey del flujo de masa de un lote
  > - US 5.2 — Alerta de desviación de merma vs mediana histórica
  > - US 5.3 — Exportar historial completo de un lote en PDF

### Gap 5: No hay épica de autenticación/roles
- **Problema**: El PO asume roles (Recepcionista, Operario, Jefe Producción, Supervisor, Bodeguero) pero no define quién puede hacer qué en el sistema
- **Épica sugerida**:
  > **Épica 0: Seguridad y Roles (No Funcional)**
  > - US 0.1 — Como Admin, quiero asignar roles a usuarios para controlar qué acciones puede ejecutar cada uno en el sistema de camarón

### Gap 6: Flujo de re-clasificación del rechazo incompleto
- **Qué dice US 2.1**: *"reingresarlo al sistema como producto Cola"*
- **Qué no dice**: ¿La cola reingresada vuelve a pasar por las 4 máquinas clasificadoras? ¿Se genera un nuevo registro de clasificación vinculado al lote original? ¿La merma del descabezado (cabezas) se registra como WasteRecord o como deducción del StockOrder?

---

## Backlog Completo Propuesto (PO original + complementos)

### Épica 0: Seguridad y Roles *(nueva)*
| US | Actor | Historia | Prioridad |
|---|---|---|---|
| 0.1 | Admin | Asignar roles Keycloak para controlar acceso por función | Must |

### Épica 1: Gestión de Recepción y Control de Calidad
| US | Actor | Historia | Prioridad |
|---|---|---|---|
| 1.1 | Recepcionista | Registrar llegada de MP y generar lote base único | Must |
| 1.2 | Inspector QC | Seleccionar modo de entrada (Entero/Cola) para derivar a clasificación o descabezado | Must |

### Épica 2: Clasificación y Gestión de Rechazos
| US | Actor | Historia | Prioridad |
|---|---|---|---|
| 2.1 | Operario Clasificación | Registrar rechazo, enviar a descabezado, reingresar como Cola bajo mismo lote | Must |
| **2.2** *(nueva)* | Operario Clasificación | Registrar cajetas (conteo) Y libras (peso) por cada talla clasificada | Must |
| **2.3** *(nueva)* | Operario Clasificación | Ver resumen de lo clasificado por talla antes de cerrar la clasificación del lote | Should |

### Épica 3: Procesamiento y Sufijos de Destino
| US | Actor | Historia | Prioridad |
|---|---|---|---|
| 3.1 | Jefe Producción | Derivar producto clasificado asignándole sufijo automático según destino | Must |
| 3.2 | Operario Planta | Registrar procesamiento IQF/VA y empaque en fundas/cartones | Must |

### Épica 4: Masterizado, Identidad y Liquidación
| US | Actor | Historia | Prioridad |
|---|---|---|---|
| 4.1 | Supervisor Empaque | Validar Match (Marca, Talla, Lote, Congelación) y generar etiqueta QR para Cartón Máster | Must |
| 4.2 | Supervisor Área | Ingresar Masters producidos vs libras recibidas para liquidar lote por área | Must |
| 4.3 | Bodeguero | Transferir Masters a Cámara de Mantenimiento bajo regla FIFO | Must |
| **4.4** *(nueva)* | Supervisor Área | Seleccionar motivo de merma (agua/basura/cabezas/calibración) al liquidar | Should |
| **4.5** *(nueva)* | Bodeguero | Recibir alerta si despacha un Master fuera de orden FIFO | Could |

### Épica 5: Visibilidad y Reportes *(nueva)*
| US | Actor | Historia | Prioridad |
|---|---|---|---|
| **5.1** | Gerente | Ver diagrama Sankey del flujo de masa de un lote (base → destinos → masters) | Should |
| **5.2** | Jefe Producción | Recibir alerta si merma de descabezado supera mediana histórica en X% | Could |
| **5.3** | Auditor | Exportar historial completo de un lote en PDF para certificaciones | Should |

---

## Preguntas de Clarificación para el PO

> Estas preguntas deben resolverse **antes de iniciar Fase 3** (microservicio). Las Fases 1-2 pueden avanzar sin respuestas.

### Sobre el flujo de rechazo (Épica 2)
1. **¿La cola reingresada pasa por las mismas 4 máquinas clasificadoras?** ¿O va por una línea diferente? Esto define si generamos un nuevo `ClassificationRecord` o vinculamos al existente.
2. **¿La merma del descabezado (cabezas) se registra inmediatamente o al cierre del lote?** Ejemplo: si entran 360 lbs de rechazo entero y salen 306 lbs de cola → ¿las 54 lbs de merma se registran al momento del descabezado o en la liquidación del área?
3. **¿Puede haber más de un ciclo de rechazo por lote?** Es decir, ¿la cola reingresada puede generar su propio rechazo en la segunda clasificación?

### Sobre la clasificación (Épica 2)
4. **¿Cuántas tallas son exactamente?** El documento Dufer menciona 21/25, 26/30, 30/40, 40/50, 50/60. ¿Hay más? ¿Varían por temporada o por cliente?
5. **¿Las 4 máquinas clasificadoras procesan las mismas tallas o están especializadas?** Esto define si `machineId` es solo metadata o si afecta la lógica de clasificación.

### Sobre los sufijos (Épica 3)
6. **¿Un lote puede tener sufijos parciales?** Ejemplo: ¿es posible que un lote vaya solo a Bloque e IQF, sin generar -3 ni -4? ¿O siempre se generan los 4 destinos?
7. **¿El sufijo "-1" está reservado o no existe?** El esquema actual es: sin sufijo (Bloque), -2 (IQF), -3 (VA), -4 (Salmuera). ¿Nunca habrá un quinto destino?

### Sobre la liquidación (Épica 4)
8. **¿Qué tolerancia de merma es aceptable?** ¿Hay un % máximo de merma esperada por área antes de que se considere anomalía? Esto define los umbrales de alerta.
9. **¿La liquidación de clasificación se cierra al mismo tiempo que la liquidación de área, o son independientes?** El PO menciona "dos partes" pero no dice si una depende de la otra.

### Sobre la cámara de mantenimiento (Épica 4)
10. **¿Hay más de una cámara de mantenimiento?** Si hay varias, ¿el bodeguero elige destino o es automático?
11. **¿El FIFO es por lote o por talla?** ¿Se despacha primero el lote más antiguo sin importar talla, o se despacha primero el más antiguo DE LA TALLA solicitada?

### Sobre reportes (Épica 5)
12. **¿Quién consume los reportes?** ¿Solo la gerencia DUFER, o también compradores internacionales/auditores externos?
13. **¿Necesitan exportar a formatos específicos?** El Core actual soporta CSV, PDF y Excel. ¿Es suficiente?

---

## Conclusión

El informe del PO es sólido para las Épicas 1-4. Las User Stories son claras y accionables. Los gaps identificados (doble unidad, motivo de merma, FIFO, dashboards, roles) son **complementos necesarios** pero no bloquean el inicio del trabajo en Fase 1.

**Recomendación**: Enviar las preguntas de clarificación al PO y **comenzar Fase 1 en paralelo** — las respuestas solo son necesarias para Fase 3.
