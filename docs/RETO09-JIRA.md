# Reto 09 — Agilismo y Jira: organizar el MVP de SkyCampus

## 1. Jerarquía

| Nivel | Clave en Jira | Ticket | Texto |
|-------|---------------|--------|-------|
| Épica | JM-5 | Épica del MVP | Digitalizar el reparto interno de la ECI mediante una flota de drones supervisada |
| Feature | JM-13 | FEAT-01 Gestión de flota | Gestión de la flota de drones del campus |
| Historia de usuario | JM-14 | HU-1 | Ver la flota de drones disponibles |
| Historia de usuario | JM-15 | HU-2 | Asignar un drone a una solicitud de reparto |
| Historia de usuario | JM-19 | HU-3 | Cancelar una solicitud de reparto pendiente |
| Subtarea | JM-16, JM-17 y JM-18 | 3 subtareas | Cuelgan de HU-2 (sección 3) |

### Cómo resolví el nivel "Feature" en Jira

El Jira estándar tiene una jerarquía fija: Épica, luego Historia o Tarea, luego Subtarea. Jira no permite crear un nivel entre la épica y la historia: solo deja agregar niveles por encima de la épica, y eso requiere el plan Premium. Por eso una Feature no puede contener historias.

Lo que hay en mi tablero:
- **La Feature (JM-13) es un ticket de un tipo de trabajo distinto al de las historias y al de la épica.** En el Backlog cada tipo tiene su propio icono: la Feature, el marcador verde de las historias y el rayo morado de la épica. Así la Feature se distingue por su tipo y no solo por el título "FEAT-01".
- **Cada historia queda atada a su Feature de dos formas:** el enlace "relates to" hacia JM-13, que se ve en la ficha de la historia, y la etiqueta `gestion-flota`, que se ve en el Backlog y la llevan los cuatro tickets (la Feature y las tres historias).
- **Los cuatro tickets cuelgan de la épica (JM-5)**, y las subtareas cuelgan directamente de HU-2.

## 2. Historias de usuario

Todas siguen el formato "Como [rol], quiero [qué], para [beneficio]".

- **HU-1 — Ver la flota.** Como Operador de drones, quiero ver qué drones están disponibles con su batería y su ubicación, para elegir el drone más adecuado antes de asignar una misión.
  - *Se apoya en lo ya hecho:* las consultas de `ConsultasFlota` (reto 01) y el mock del panel de monitoreo con la identidad definida (reto 08).
- **HU-2 — Asignar drone a misión.** Como Operador de drones, quiero asignar un drone disponible a una solicitud de reparto pendiente, para que el documento salga hacia su destino.
- **HU-3 — Cancelar solicitud pendiente.** Como Operador de drones, quiero cancelar una solicitud de reparto que todavía está pendiente, para que mi lista de pendientes solo tenga pedidos que se van a entregar y no perder tiempo con los que ya no se necesitan ni asignarles un drone por error.

### Decisión abierta cerrada: qué se cancela en HU-3

**Se cancela la solicitud, no la misión.** En el modelo, una misión nace con su drone y ya sale en vuelo (RF-03 y SC-01); una misión PENDIENTE sin drone no existe. Lo que sí está pendiente es la solicitud, que espera a que el Operador le asigne un drone. Por eso:
- La solicitud pasa al estado CANCELADA y deja de aparecer en la lista de solicitudes pendientes.
- **Por qué el Operador.** En el diagrama de contexto (C4), el Solicitante solo registra solicitudes (flecha 3) y recibe su estado (flecha 4). La lista de solicitudes pendientes la ve y la trabaja el Operador (flechas 1 y 2), y es él quien decide a cuál asignarle un drone. Por eso es él quien mantiene esa lista. Si más adelante el Solicitante debe cancelar sus propias solicitudes, habrá que agregar esa flecha al C4.
- **El beneficio real.** Cancelar una solicitud pendiente no libera ningún drone, porque el drone solo se reserva en el paso 5 de SC-01. Lo que evita es que la lista de pendientes se llene de pedidos obsoletos y que alguien les asigne un drone por error.
- Cancelar una misión que ya está EN_VUELO queda fuera del MVP.
- Esta historia introduce un requerimiento que no está en el reto 06 (cancelar una solicitud pendiente). Queda anotado como candidato a RF-04; el enum `estadoSolicitud` de SC-01 se actualiza con el valor CANCELADA.

## 3. HU-2 — Asignar drone a misión: subtareas y criterios de aceptación

### Subtareas

Cada una es una acción técnica que hace una sola persona y apunta a un criterio de aceptación.

| Clave | Subtarea | Responsable | Estado en Jira | Criterio | Evidencia |
|-------|----------|-------------|----------------|----------|-----------|
| JM-16 | Implementar `ValidadorBateria`, que rechaza un drone con batería menor al 30% (RN-01) | Julian | Aprobado | 2 | Reto 03: clase `ValidadorBateria` |
| JM-17 | Implementar el paso 5 de SC-01: pasar la solicitud a ATENDIDA y guardar en ella el `codigoMision` al crear la misión | Julian | En Diseño | 1 | Reto 07: diseño del paso 5 y del enlace solicitud-misión |
| JM-18 | Escribir las pruebas unitarias de `AsignadorMision` para el caso de éxito (misión EN_VUELO y drone no disponible) y para el rechazo de un drone con 18% de batería | Julian | Por hacer | 1 y 2 | Reto 12 (TDD) |

### Criterios de aceptación

1. **Asignación exitosa.** Dado un drone disponible con 85% de batería y una solicitud pendiente con un destino válido, cuando el Operador confirma el registro, entonces el sistema crea la misión en estado EN_VUELO, el drone deja de aparecer como disponible, la solicitud pasa a ATENDIDA y el Operador ve el código de la misión.
2. **Batería insuficiente (RN-01).** Dado un drone disponible con 18% de batería, cuando el Operador intenta asignarlo a una solicitud pendiente, entonces el sistema rechaza el registro, muestra "El drone D-04 tiene batería insuficiente (18%). Mínimo requerido: 30%." y no crea ninguna misión; la solicitud sigue pendiente.

El rechazo por destino inválido (RN-02) está descrito en SC-01 (reto 07) y queda fuera de los dos criterios de esta historia.

## 4. Estados del tablero

Mi Jira usa estos nombres de estado: **Por hacer** equivale a To Do, **En Diseño** a In Progress y **Aprobado** a Done.

- **Aprobado (Done):** la subtarea JM-16, porque `ValidadorBateria` ya está implementado y publicado.
- **En Diseño (In Progress):** la subtarea JM-17, cuyo diseño ya está en SC-01, y la historia HU-2 (JM-15), que ya tiene una subtarea terminada y otra en curso.
- **Por hacer (To Do):** HU-1 (JM-14), HU-3 (JM-19), la Feature (JM-13) y la subtarea JM-18.
