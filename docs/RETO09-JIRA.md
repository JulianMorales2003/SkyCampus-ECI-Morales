# Reto 09 — Agilismo y Jira: organizar el MVP de SkyCampus

## 1. Jerarquía

| Nivel | Ticket | Texto |
|-------|--------|-------|
| Épica | Épica del MVP | Digitalizar el reparto interno de la ECI mediante una flota de drones supervisada |
| Feature | Gestión de flota | Gestión de la flota de drones del campus |
| Historia de usuario | HU-1 | Ver la flota de drones disponibles |
| Historia de usuario | HU-2 | Asignar un drone a una solicitud de reparto |
| Historia de usuario | HU-3 | Cancelar una solicitud de reparto pendiente |
| Subtarea | 3 subtareas | Cuelgan de HU-2 (sección 3) |

### Limitación de Jira con el nivel "Feature"

El Jira estándar tiene una jerarquía fija: Épica, luego Historia o Tarea, luego Subtarea. Un nivel "Feature" entre la épica y la historia solo se puede crear con un plan Premium, y aun ahí los niveles nuevos se agregan por encima de la épica. En una cuenta gratuita, la Feature queda como un tipo de ticket propio (del mismo nivel que las historias) que cuelga de la épica y se enlaza con las historias, en vez de contenerlas.

Cómo quedó en el tablero: la Épica es el padre de la Feature y de las 3 historias, y cada historia está enlazada a la Feature "Gestión de flota" y lleva la etiqueta `gestion-flota`. Las subtareas sí cuelgan directamente de HU-2.

## 2. Historias de usuario

Todas siguen el formato "Como [rol], quiero [qué], para [beneficio]".

- **HU-1 — Ver la flota.** Como Operador de drones, quiero ver qué drones están disponibles con su batería y su ubicación, para elegir el drone más adecuado antes de asignar una misión.
- **HU-2 — Asignar drone a misión.** Como Operador de drones, quiero asignar un drone disponible a una solicitud de reparto pendiente, para que el documento salga hacia su destino.
- **HU-3 — Cancelar solicitud pendiente.** Como Operador de drones, quiero cancelar una solicitud de reparto que todavía está pendiente, para no gastar un drone en un reparto que ya no se necesita y conservar el control del panel.

### Decisión abierta cerrada: qué se cancela en HU-3

**Se cancela la solicitud, no la misión.** En el modelo, una misión nace con su drone y ya sale en vuelo (RF-03 y SC-01); una misión PENDIENTE sin drone no existe. Lo que sí está pendiente es la solicitud, que espera a que el Operador le asigne un drone. Por eso:
- La solicitud pasa al estado CANCELADA y deja de aparecer en la lista de solicitudes pendientes.
- Quien la cancela es el Operador, porque la heurística 3 de Nielsen (control del usuario) se refiere al operador del panel.
- Cancelar una misión que ya está EN_VUELO queda fuera del MVP.
- Esta historia introduce un requerimiento que no está en el reto 06 (cancelar una solicitud pendiente). Queda anotado como candidato a RF-04; el enum `estadoSolicitud` de SC-01 se actualiza con el valor CANCELADA.
