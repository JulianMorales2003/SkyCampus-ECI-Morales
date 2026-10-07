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
