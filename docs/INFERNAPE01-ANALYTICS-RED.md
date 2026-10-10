# Infernape 01: analytics de eficiencia de la red en una sola pasada de Stream

## Reto

El superadmin necesita el dashboard de eficiencia de las 4 sedes (100 drones, miles de misiones). En una sola pasada de Stream hay que calcular, para cada sede:

1. Tasa de éxito (misiones entregadas sobre el total).
2. Tiempo promedio de entrega.
3. Drone más utilizado.
4. Porcentaje de misiones urgentes.

Las sedes sin actividad se representan con `Optional`. Las pruebas son paramétricas, con al menos 4 escenarios: sede vacía, sede con 1 misión, empate entre sedes y red completa.

## Decisiones donde el enunciado no define el comportamiento

| Tema | Decisión | Motivo |
|---|---|---|
| Qué cuenta como "total" | Todas las misiones de la sede, en cualquier estado | Es la lectura literal de "entregadas/total". |
| Qué es "entregada" | Estado `COMPLETADA` del modelo v2 | Se reutiliza `EstadoMision` y `Prioridad` de la v2. |
| Tiempo promedio | Solo sobre las misiones entregadas; vacío si no hubo ninguna | Una misión fallida o en vuelo no tiene tiempo de entrega. Usar 0 bajaría el promedio sin motivo. |
| Unidades | `tasaExito` entre 0.0 y 1.0; `porcentajeUrgentes` entre 0.0 y 100.0 | Siguen los nombres del enunciado (tasa y porcentaje). |
| Empate de "drone más utilizado" | Gana el id menor | Resultado estable, independiente del orden de las misiones. |
| Misión de una sede que no está en la red | `IllegalArgumentException` | No se descarta en silencio un dato que no encaja. |
| Sedes repetidas en la entrada | Aparecen una sola vez, en el orden recibido | La salida no depende de duplicados. |

## Diseño

Todo vive en un paquete nuevo, `skycampus.enterprise`, igual que la v2 se separó del MVP. Las 241 pruebas anteriores no se tocan.

| Clase | Rol |
|---|---|
| `model.Sede` | Sede de la red (record con nombre validado). |
| `model.MisionRed` | Misión con sede, drone, estado, prioridad y tiempo de entrega. |
| `analytics.EficienciaSede` | Resultado por sede: guarda los conteos y deriva `tasaExito()`, `tiempoPromedioEntregaMinutos()` y `porcentajeUrgentes()`. |
| `analytics.AcumuladorSede` | Acumulador mutable que calcula las cuatro métricas a la vez. |
| `analytics.AnalyticsRed` | `eficienciaPorSede(sedes, misiones)` y `ranking(sedes, misiones)`. |

### Una sola pasada

```java
Map<Sede, EficienciaSede> conActividad = misiones.stream()
        .collect(Collectors.groupingBy(MisionRed::sede, AcumuladorSede.recolector()));
```

`groupingBy` reparte cada misión a su sede, y el colector de cada sede (`Collector.of`) actualiza en la misma visita el total, las entregadas, los minutos, las urgentes y el conteo por drone. Las misiones se recorren una vez, sin listas intermedias por sede ni un segundo recorrido por métrica. El colector define un combinador, así que también es correcto con streams paralelos.

### Optional y orden

- Cada sede de la red aparece en el resultado: `Optional.of(eficiencia)` si tuvo misiones, `Optional.empty()` si no.
- El ranking usa un `Comparator` compuesto: tasa de éxito descendente, luego misiones entregadas descendente, luego nombre. Las sedes sin actividad quedan al final.

## Pruebas

`AnalyticsRedTest` ejecuta cada escenario con dos pruebas paramétricas (métricas y ranking):

| Escenario | Qué comprueba |
|---|---|
| Sede vacía | Sin misiones, el `Optional` queda vacío. |
| Sede con 1 misión | Todas las métricas salen de ese único dato (tasa 1.0, 100 % urgentes). |
| Empate entre sedes | Tres sedes con tasa 1.0: desempata por entregadas y luego por nombre. |
| Sede sin entregas | Tasa 0.0, sin tiempo promedio, empate de drones resuelto por id menor. |
| Red completa | 4 sedes con estados y prioridades mezclados, una de ellas sin actividad. |

Pruebas adicionales: sedes repetidas, misión de una sede ajena, argumentos nulos, tiempo promedio que ignora las fallidas, combinación de dos acumuladores, y resultado idéntico entre stream secuencial y paralelo. Las validaciones de los records tienen sus propias pruebas.

### Mutaciones manuales

Se introdujo cada defecto por separado y se comprobó que alguna prueba lo detecta:

| Defecto introducido | Pruebas que fallan |
|---|---|
| Condición de "entregada" invertida | 7 |
| Desempate de drone al revés (id mayor) | 2 |
| Tiempo de entrega ignorado en el promedio | 4 |
| Condición de "urgente" invertida | 3 |
| Sedes sin actividad al principio del ranking | 1 |
| Ranking sin desempate por entregadas | 1 |
| Ranking sin desempate por nombre | 1 |

## Resultado de las pruebas

JaCoCo 0.8.11 sobre las 264 pruebas, sin contar `app`: líneas 100 % (576 de 576) y ramas 100 % (198 de 198).

Salida de `mvn clean verify`: [PEGA: últimas líneas con el número de pruebas y `BUILD SUCCESS`]
