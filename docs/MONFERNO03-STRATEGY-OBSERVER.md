# Monferno · Reto 03 — Strategy y Observer sobre SkyCampus v2

Todo el código nuevo vive en `v2` (`skycampus.v2.asignacion`, `skycampus.v2.eventos`, `skycampus.v2.service`), igual que en el Reto 01. El MVP queda intacto.

## 1. Strategy: cómo se elige el drone

`EstrategiaAsignacion` define un solo método: `seleccionar(SolicitudMision, List<Drone>)` devuelve `Optional<Drone>`. `GestorFlota` solo conoce esa interfaz, no las clases concretas, y la estrategia se puede cambiar en tiempo de ejecución con `cambiarEstrategia`.

Todas parten de los mismos candidatos (`EstrategiaAsignacion.candidatos`): drones con `disponible == true` **y** `estado == DISPONIBLE`. Se piden las dos condiciones para que un drone con datos inconsistentes (disponible pero en `FALLO`) nunca se asigne.

| Estrategia | Regla | Desempate |
|---|---|---|
| `EstrategiaMayorBateria` | El candidato con más batería | Id menor |
| `EstrategiaTipoSegunPaquete` | `URGENTE` → EXPRESS; peso > 1000 g → CARGO; si no → MINI. Dentro del tipo, el de más batería | Id menor |
| `EstrategiaBateriaJusta` | El de **menor** batería que aún llega al mínimo (30 por defecto, configurable) | Id menor |

`EstrategiaTipoSegunPaquete` no cae a otro tipo si no hay uno adecuado: devuelve vacío. Es una decisión de negocio (no mandar un MINI con un paquete pesado); si se quisiera un plan B se resolvería componiendo estrategias.

El desempate por id hace que el resultado no dependa del orden de la lista.

## 2. Observer: quién se entera de lo que pasa

`GestorFlota` es el sujeto. Publica un `EventoFlota` (tipo, detalle, momento) a todo `ObservadorFlota` suscrito.

| Evento | Se publica cuando |
|---|---|
| `MISION_ASIGNADA` | `asignar` encontró drone |
| `SIN_DRONE_DISPONIBLE` | `asignar` no encontró drone |
| `MISION_COMPLETADA` | `completar` |
| `FALLO_DRONE` | `reportarFallo` |

| Observador | Reacción |
|---|---|
| `PanelOperador` | Muestra todos los eventos en la pantalla que le inyectan (`Consumer<String>`) |
| `SistemaLog` | Guarda una línea por evento; expone la lista solo de lectura |
| `AlertaTecnico` | Solo reacciona a `FALLO_DRONE`; envía la alerta y la cuenta |

Los observadores reciben su salida por constructor en vez de escribir con `System.out`, lo que evita el smell `java:S106` y permite probarlos sin capturar la consola.

## 3. Abierto/cerrado: agregar un cuarto observador

Para sumar un observador (por ejemplo, uno que mande un correo) se escribe una clase que implemente `ObservadorFlota` y se suscribe. `GestorFlota` no cambia: solo recorre la lista. La prueba `agregarCuartoObservador_sinTocarElGestor_recibeLosEventos` lo demuestra con un cuarto observador definido dentro de la propia prueba.

Lo mismo con una cuarta estrategia: se implementa la interfaz y se pasa al gestor.

## 4. Detalle de diseño: iterar sobre una copia

`notificar` recorre `List.copyOf(observadores)`. Si un observador se cancela a sí mismo mientras recibe un evento, iterar la lista original lanzaría `ConcurrentModificationException`. La prueba `notificar_observadorQueSeCancelaDuranteLaNotificacion_noRompeLaIteracion` lo cubre.

## 5. Pruebas

34 pruebas nuevas:
- `EstrategiasAsignacionTest` (17): las tres estrategias, empates, límites (peso 1000 vs 1001, batería igual al mínimo), flota sin candidatos, estados inconsistentes, nulos.
- `ObservadoresTest` (7): panel, log (orden e inmutabilidad), alerta técnica (solo fallos), nulos.
- `GestorFlotaTest` (10): asignar, cambio de estrategia en ejecución, notificación a varios, sin drone, completar, fallo, cuarto observador, cancelar, cancelación durante la notificación, nulos.

Se comprobó con 8 mutaciones hechas a mano (quitar el desempate por id, `>` por `>=` en el peso, alerta técnica para todos los eventos, iterar sin copia, mínimo estricto en vez de inclusivo, ignorar el estado del drone, evento equivocado cuando no hay drone, ignorar la prioridad urgente). Las 8 hicieron fallar al menos una prueba.

## 6. Demo

`app.Monferno03App` asigna la misma solicitud con las tres estrategias y muestra cómo cambia el drone elegido, y luego completa una misión y reporta un fallo. Se ve lo que muestran el panel, el log y la alerta técnica.
