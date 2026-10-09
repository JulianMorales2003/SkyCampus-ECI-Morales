# Monferno · Reto 01 — Streams sobre el modelo de SkyCampus v2
Estudiante: Julian Felipe Morales Zambrano

## Contexto
La v2 trae 20 drones de 3 tipos (`MINI`, `CARGO`, `EXPRESS`), misiones con peso en gramos y prioridad (`URGENTE`, `NORMAL`, `BAJO`). El dashboard del operador necesita estadísticas agrupadas. Todas las consultas están en `v2.service.EstadisticasFlota` y usan solo Streams.

## Decisión de diseño: el modelo v2 vive en el paquete `v2`
El modelo del MVP (`model.Drone`, `model.Mision`) ya tiene 121 pruebas que dependen de sus constructores. Cambiarlos para agregar tipo, peso y prioridad como enum habría roto esas pruebas. Por eso el modelo v2 es un paquete nuevo (`v2.model`) con la forma que pide el enunciado, y el MVP queda intacto. Los retos siguientes de Monferno (Strategy, Observer) se construyen sobre `v2`.

## Las cuatro consultas
| # | Pregunta | Método | Operaciones de Stream |
|---|----------|--------|-----------------------|
| 1 | Tipo de drone → misiones completadas | `completadasPorTipo` | `filter` + `groupingBy` (con `EnumMap`) + `counting` |
| 2 | Drone con más misiones completadas | `droneConMasCompletadas` | `filter` + `groupingBy` por id + `max` con comparador |
| 3 | % de misiones fallidas | `porcentajeFallidas` | `filter` + `count` |
| 4 | ¿Urgente pendiente hace más de 10 min? | `hayUrgentePendienteMasDeDiezMinutos` | `filter` + `filter` + `anyMatch` |

## Supuestos
- **"Del día":** la lista que recibe cada método ya es la del día; los métodos no filtran por fecha.
- **Campo nuevo `creadaEn` (`Instant`) en `Mision`:** el enunciado no lo trae, pero la consulta 4 necesita saber cuánto lleva pendiente una misión. El "ahora" se recibe como parámetro para que la consulta sea determinista y se pueda probar sin depender del reloj.
- **"Más de 10 minutos":** estrictamente mayor. Una misión pendiente hace exactamente 10 minutos no cuenta.
- **Mapa de la consulta 1:** los tipos sin misiones completadas no aparecen (cuentan 0).
- **Empate en la consulta 2:** gana el drone de id menor, para que el resultado no dependa del orden de la lista.
- **Lista vacía en la consulta 3:** devuelve 0.0 (no `NaN`).
- **Se agrupa por id de drone, no por el record `Drone`:** dos fotos del mismo drone con distinta batería serían claves distintas.

## Pruebas
23 pruebas nuevas (6 de modelo y 17 de consultas), nombradas `metodo_condicion_resultado`. Resultado local con JUnit 5.10: 23 exitosas, 0 fallidas.

## Mutaciones (hechas a mano sobre `EstadisticasFlota`)
| # | Mutación | Pruebas que fallan |
|---|----------|--------------------|
| 1 | `> 0` → `>= 0` en la comparación de los 10 minutos | 1 |
| 2 | Quitar el filtro `COMPLETADA` en `completadasPorTipo` | 2 |
| 3 | Desempate con `naturalOrder` en vez de `reverseOrder` | 1 |
| 4 | Porcentaje sin multiplicar por 100 | 1 |
| 5 | Cambiar `URGENTE` por `NORMAL` en la consulta 4 | 2 |

Las cinco mutaciones fueron detectadas.

## Demo
`app.Monferno01App` arma 3 drones y 8 misiones de ejemplo y muestra:
```
1. Completadas por tipo: {MINI=2, CARGO=1, EXPRESS=1}
2. Drone con más completadas: D-01
3. Misiones fallidas: 12.5%
4. ¿Urgente pendiente hace más de 10 min?: true
```
