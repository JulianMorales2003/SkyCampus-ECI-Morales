# Reto 03 — Patrones de diseño (SkyCampus MVP)

| Problema | (a) Patrón | (b) Por qué este y no otro | (c) Clases |
|----------|-----------|----------------------------|------------|
| 1. Misión con campos obligatorios y opcionales | Builder | Se construye paso a paso solo con los campos necesarios, en vez de un constructor por cada combinación de opcionales. | `MisionBuilder`, `Mision` |
| 2. Validaciones en orden donde cada una pasa o rechaza | Chain of Responsibility | Cada validador decide si rechaza o entrega la misión al siguiente, y se pueden agregar o reordenar sin tocar a los demás. | `ValidadorEnCadena`, `ValidadorBateria`, `ValidadorDestino`, `ValidadorCarga`, `ResultadoValidacion` |
| 3. Algoritmo de asignación intercambiable | Strategy | Cada criterio (mayor batería, más cercano, más rápido) vive en su propia clase detrás de una interfaz, y se cambia sin modificar el asignador. | `EstrategiaAsignacion`, `EstrategiaMayorBateria`, `EstrategiaDroneEnOrigen`, `AsignadorDrones` |

## Supuestos que tomé
- `tipoCarga` es obligatorio en el Builder (el enunciado del problema 1 lista drone, origen y destino) porque `Mision` lo define y `ValidadorCarga` lo necesita.
- `Drone` no tiene capacidad en el MVP. `TipoCarga` guarda el peso en gramos (SOBRE 50, CARPETA 300, LIBRO 800) y `ValidadorCarga` recibe la capacidad por constructor (500 g en la demo).
- Sin hora máxima de entrega se usa `Mision.SIN_HORA_LIMITE` (`LocalTime.MAX`), para no manejar `null`.
- `Mision` y `ResultadoValidacion` validan sus invariantes en el constructor del record; el Builder avisa con un mensaje distinto por cada campo obligatorio que falte.
- `EstrategiaDroneEnOrigen` no calcula distancias (el MVP no tiene coordenadas): elige entre los drones disponibles ubicados exactamente en el origen al de mayor batería.
