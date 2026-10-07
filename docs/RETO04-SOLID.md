# Reto 04 — Principios SOLID sobre `GestorDrone`

## 1. ¿Qué principios viola y por qué?

| Principio | ¿Violado? | Por qué (una oración) |
|-----------|-----------|-----------------------|
| S (responsabilidad única) | Sí | `GestorDrone` tiene 5 razones para cambiar (asignar, guardar, alertar, reportar y calcular ruta), así que cambiar el SMTP obliga a tocar la misma clase que calcula rutas. |
| O (abierto/cerrado) | Sí | `calcularRuta` decide con un `if/else` por tipo, así que cada tipo de ruta nuevo obliga a modificar una clase que ya funcionaba. |
| L (sustitución de Liskov) | No | El fragmento no tiene herencia, así que no hay una subclase que pueda romper el contrato del padre. |
| I (segregación de interfaces) | En potencia | Quien solo necesita asignar misiones queda atado a `guardarEnBD`, `enviarAlertaEmail` y `generarReportePDF`, aunque no los use. |
| D (inversión de dependencias) | Sí | `guardarEnBD` crea la conexión con `DriverManager` dentro de la clase, así que la lógica de negocio depende de MySQL (un detalle concreto) y no de una abstracción. |

Observación extra: el usuario y la contraseña (`root` / `1234`) están escritos en el código, lo cual es un riesgo de seguridad además de un problema de diseño.

## 2. Rediseño

| Clase / interfaz | Única razón para cambiar | Principio que resuelve |
|------------------|--------------------------|------------------------|
| `AsignadorMision` | Las reglas para asignar una misión a un drone | S |
| `RepositorioMision` (interfaz) + `RepositorioMisionEnMemoria` | El almacenamiento de misiones (otra implementación, como MySQL, no toca a quien la usa) | D, I |
| `AlertaOperador` (interfaz) + `AlertaOperadorConsola` | El canal de aviso al operador (email, SMS, consola) | D, I |
| `GeneradorReporte` | El contenido y formato del reporte de misiones | S |
| `EstrategiaRuta` (interfaz) + `RutaDirecta`, `RutaEvitandoEdificios` | Un tipo de ruta concreto; agregar uno es una clase nueva, sin editar las existentes | O |

## Supuestos
- No hay MySQL, SMTP ni iText en el proyecto: `RepositorioMisionEnMemoria` y `AlertaOperadorConsola` son implementaciones de ejemplo detrás de las interfaces.
- `GeneradorReporte` devuelve texto en vez de un PDF para no agregar una librería; el formato PDF sería un cambio futuro de esa clase.
- `EstadoMision` no tiene un estado "ASIGNADA", así que asignar deja la misión en `EN_VUELO`.
- `RutaEvitandoEdificios` pasa por un punto de desvío fijo ("Corredor aéreo libre") porque el MVP no tiene mapa ni coordenadas.
- `AsignadorMision` ata una misión a un drone ya elegido; `AsignadorDrones` (reto 03) es el que elige el drone.
