# Infernape · Reto 07 — Plantilla DOSW: SC-15 Planificar ruta multi-etapa inter-sede

| Campo | Valor |
|-------|-------|
| Código | SC-15 |
| Nombre | Planificar ruta multi-etapa inter-sede |
| Actor principal | Operador de drones: registra la misión que debe ir de una sede a otra |
| Actores secundarios | Aerocivil (sistema externo, flujos 17 y 18 del diagrama de contexto Enterprise) |
| Requerimientos que implementa | RF-14 (planificar la ruta entre sedes) y RF-16 (autorización de la Aerocivil), con RNF-08, RNF-09 y RNF-10 de la matriz del reto 06 |
| Casos de uso relacionados | Refina SC-10 (planificar una ruta entre sedes) y usa SC-12 (autorizar un vuelo entre sedes) |

Sobre el nombre: el enunciado llama "SC-15" a este requisito. En la matriz del reto 06 la planificación es RF-14 y la autorización es RF-16; SC-15 es el caso de uso completo que baja a detalle a los dos, y por eso quedó enlazado a ambos en la matriz.

Ejemplo guía del enunciado: una misión de la ECI a Uniandes que pasa por una estación de carga en la calle 116. La sección "Contradicción C-05" muestra que, con la regla de 5 km, esa única estación no alcanza.

## Precondiciones (existen ANTES de ejecutar)
Si alguna falta, SC-15 no puede iniciar.

- **P1.** El Superadmin registró las sedes de la red (ECI, UNAL, Uniandes, EAFIT) y, para cada punto de salida de un vuelo (sede o estación de carga), la restricción vigente de la Aerocivil (radio máximo y altura máxima). Sin restricción registrada, ese punto no permite despegar.
- **P2.** Existen estaciones de carga registradas con su ubicación en el mapa de la red. Que ninguna esté libre en este momento no impide iniciar: lo trata A4.
- **P3.** La flota de cada sede está registrada. Que no haya drones libres tampoco impide iniciar: lo trata A5.
- **P4.** El Operador pertenece a la sede de origen de la misión.

## Datos de entrada
Los datos anidados se desglosan en una fila por atributo.

| Nombre | Tipo de campo | Reglas | Oblig. |
|---|---|---|---|
| mision | — | — | Sí |
| mision.id | String | Código único de la misión | Sí |
| mision.paquete | — | — | Sí |
| mision.paquete.id | String | Código único del paquete | Sí |
| mision.paquete.pesoGramos | Integer | Entre 1 y 2000 g (RN-05) | Sí |
| mision.paquete.tipo | Enum(SOBRE, CARPETA, LIBRO, EQUIPO) | — | Sí |
| mision.paquete.prioridad | Enum(URGENTE, NORMAL, BAJO) | — | Sí |
| mision.origen | — | — | Sí |
| mision.origen.sede | String | Nombre de una sede de la red (RN-06) | Sí |
| mision.origen.punto | — | Punto del mapa de la red | Sí |
| mision.origen.punto.nombre | String | Texto no vacío | Sí |
| mision.origen.punto.xKm | Double | Kilómetros respecto al origen común de la red | Sí |
| mision.origen.punto.yKm | Double | Kilómetros respecto al origen común de la red | Sí |
| mision.destino | — | — | Sí |
| mision.destino.sede | String | Nombre de una sede de la red, distinta de la de origen (RN-06) | Sí |
| mision.destino.punto | — | Punto del mapa de la red | Sí |
| mision.destino.punto.nombre | String | Texto no vacío | Sí |
| mision.destino.punto.xKm | Double | Kilómetros respecto al origen común de la red | Sí |
| mision.destino.punto.yKm | Double | Kilómetros respecto al origen común de la red | Sí |
| mision.restricciones | List<RestriccionEspecial> | Puede estar vacía | No |
| mision.restricciones[] | Enum(EVITAR_EDIFICIOS) | Si está, el planificador usa los tramos que evitan edificios y mide cada tramo con su rodeo | No |

*Supuesto:* el enunciado habla de "restricciones especiales" sin enumerarlas. Definí un solo valor, EVITAR_EDIFICIOS, porque ya existe en el MVP (`RutaEvitandoEdificios`); si aparecen otras, se agregan como valores nuevos sin cambiar el flujo.

Datos que el sistema lee por su cuenta (nadie los digita):

| Nombre | Tipo de campo | De dónde sale |
|---|---|---|
| espacioAereo | — | Aerocivil, paso 2 |
| espacioAereo.vigente | Boolean | Aerocivil: verdadero si el corredor entre origen y destino está abierto |
| espacioAereo.zonaUrbana | Boolean | Aerocivil |
| espacioAereo.restriccion | — | Aerocivil |
| espacioAereo.restriccion.radioMaximoKm | Double | Aerocivil, mayor que 0 |
| espacioAereo.restriccion.alturaMaximaM | Integer | Aerocivil, mayor que 0; en zona urbana nunca más de 120 m (RN-04) |
| espacioAereo.consultadoEn | Instant | Hora de la consulta |
| estaciones | List<EstacionCarga> | Registro de estaciones (P2) |
| estaciones[].id | String | Código único de la estación |
| estaciones[].punto | — | Ubicación en el mapa de la red |
| estaciones[].punto.nombre | String | Texto no vacío |
| estaciones[].punto.xKm | Double | Kilómetros respecto al origen común de la red |
| estaciones[].punto.yKm | Double | Kilómetros respecto al origen común de la red |
| estaciones[].proximaFranjaLibre | Instant | Primera hora en que hay un puesto de carga libre |
| flota | List<Drone> | Estado vigente de la flota (P3) |
| flota[].id | String | Código único del drone |
| flota[].ubicacion | String | Nombre de la sede o id de la estación donde está el drone |
| flota[].bateria | Integer | Entre 0 y 100, estado vigente |
| flota[].disponible | Boolean | Estado vigente |

## Datos de salida

| Nombre | Tipo de campo | Reglas | Oblig. |
|---|---|---|---|
| ruta | — | Solo existe si la ruta se confirma (paso 6) | No (salida) |
| ruta.id | String | Código único de la ruta | No (salida) |
| ruta.etapas | List<EtapaRuta> | En orden de recorrido, empezando en 1 | No (salida) |
| ruta.etapas[].numero | Integer | Desde 1, sin saltos | No (salida) |
| ruta.etapas[].tipo | Enum(VUELO, CARGA) | Una etapa CARGA va siempre entre dos VUELO | No (salida) |
| ruta.etapas[].desde | String | Nombre del punto donde empieza | No (salida) |
| ruta.etapas[].hasta | String | Nombre del punto donde termina | No (salida) |
| ruta.etapas[].distanciaKm | Double | En VUELO, hasta 5 km (RN-01); en CARGA, 0 | No (salida) |
| ruta.etapas[].droneId | String | Solo en VUELO; el drone asignado a esa etapa (RN-03) | No (salida) |
| ruta.etapas[].estacionId | String | Solo en CARGA; la estación reservada | No (salida) |
| ruta.etapas[].minutosEnEstacion | Integer | Solo en CARGA; hasta 30 (RN-02) | No (salida) |
| ruta.distanciaTotalKm | Double | Suma de las distancias de las etapas VUELO | No (salida) |
| ruta.duracionTotalMin | Integer | Suma de vuelos y cargas | No (salida) |
| autorizacion.codigo | String | Código que devuelve la Aerocivil al autorizar | No (salida) |
| mision.estado | Enum(PENDIENTE, EN_VUELO, RECHAZADA) | EN_VUELO al iniciar; PENDIENTE en A1, A2, A4 y A5; RECHAZADA en A3 | No (salida) |
| motivo | String | Solo cuando la ruta no se confirma | No (salida) |
| evento | Enum(ETAPA_INICIADA, ETAPA_COMPLETADA, ETAPA_FALLIDA) | El que se publica a los observadores de la ruta, etapa por etapa | No (salida) |

## Flujo básico

1. El sistema recibe la misión del Operador y valida los datos de entrada (rangos de la tabla, RN-05 y RN-06).
2. El sistema verifica con la Aerocivil las condiciones actuales del espacio aéreo entre la sede de origen y la de destino (lee `espacioAereo`). *Si el espacio no está vigente o la Aerocivil lo rechaza: A1. Si la Aerocivil no responde en 3 segundos o devuelve un error: A2.*
3. El sistema calcula las etapas: parte el trayecto en tramos de vuelo de hasta 5 km (RN-01) e intercala una parada de carga entre cada par de tramos, usando el criterio de optimización elegido (menor distancia, menor duración o menos paradas) y, si la misión lo pide, los tramos que evitan edificios. Con el recorrido total conocido decide si la ruta es larga. *Si es larga y el paquete pesa más de 1000 g: A3.*
4. El sistema asigna una estación de carga a cada parada: una estación libre en la franja en que llegaría el paquete, de modo que el paquete no pase más de 30 minutos allí (RN-02) y se reserve esa franja (RN-07). *Si para alguna parada no hay estación que cumpla, o las estaciones disponibles no permiten tramos de hasta 5 km: A4.*
5. El sistema asigna un drone a cada etapa de vuelo: disponible, ubicado donde empieza la etapa, con al menos 30 % de batería y capacidad para el peso del paquete (RN-03). *Si alguna etapa se queda sin drone, o el drone de relevo no estaría listo antes de los 30 minutos de espera: A5.*
6. El sistema confirma la ruta: envía cada etapa de vuelo como un plan de vuelo a la Aerocivil y comprueba la altura y el radio efectivo del punto de salida (RN-04). Si todas las etapas se autorizan, reserva estaciones y drones de forma definitiva (RN-08) y guarda `autorizacion.codigo`. *Si la Aerocivil rechaza alguna etapa: A1. Si no responde: A2.*
7. El sistema inicia el vuelo: el drone de la etapa 1 despega, la misión pasa a EN_VUELO y se publica ETAPA_INICIADA a los observadores. Cada etapa siguiente publica ETAPA_COMPLETADA o ETAPA_FALLIDA al terminar.
8. El sistema entrega al Operador la ruta confirmada (`ruta.id`, `ruta.etapas`, `ruta.distanciaTotalKm`, `ruta.duracionTotalMin`) y el estado de la misión. SC-15 termina.

## Flujos alternos

- **A1. La Aerocivil rechaza (en el paso 2 o en el paso 6).**
  1. El sistema no inicia ningún vuelo y libera cualquier estación o drone que hubiera reservado (RN-08).
  2. Informa al Operador el motivo: espacio aéreo no vigente, altura o radio excedidos, o rechazo de la Aerocivil.
  3. La misión queda PENDIENTE. El sistema no reintenta solo: el Operador decide cuándo volver a planificar. SC-15 termina.
  - *Los límites propios no se saltan con la urgencia:* una misión URGENTE recibe el mismo rechazo, como se resolvió en C-03 de la matriz.
- **A2. La Aerocivil no responde o falla (en el paso 2 o en el paso 6, RNF-10).**
  1. Pasados 3 segundos sin respuesta, o ante cualquier error, el sistema trata la consulta como no autorizada (falla segura) y no inicia ningún vuelo.
  2. Libera lo reservado e informa al Operador: "Aerocivil no disponible".
  3. La misión queda PENDIENTE y se puede reintentar cuando el servicio responda. SC-15 termina.
- **A3. El paquete es demasiado pesado para una ruta larga (en el paso 3, RN-05).**
  1. El sistema rechaza la misión con el motivo: en rutas de más de 10 km el paquete admite hasta 1000 g.
  2. La misión pasa a RECHAZADA y no se reevalúa, porque el peso no va a cambiar. No se consulta ninguna estación ni se toca ningún drone.
  3. El Operador puede registrar una misión nueva con un paquete más liviano.
  - *Caso cercano:* un paquete de exactamente 1000 g sí puede hacer una ruta larga. Uno de 1500 g sale en una ruta corta (hasta 10 km) si hay un drone que lo soporte.
- **A4. No hay estación de carga disponible (en el paso 4).**
  1. El sistema prueba las demás estaciones y franjas que cumplan los 30 minutos de espera (RN-02).
  2. Si ninguna combinación permite tramos de hasta 5 km, no se confirma nada y se libera lo reservado.
  3. Informa al Operador el motivo: "sin estación de carga disponible" o "las estaciones registradas no cubren el recorrido en tramos de 5 km". La misión queda PENDIENTE para reintentar cuando se libere una estación o el Superadmin registre más. SC-15 termina.
- **A5. Una etapa se queda sin drone (en el paso 5).**
  1. El sistema intenta con otro drone que cumpla RN-03 y con otra estación si el relevo tardaría más de 30 minutos.
  2. Si no queda ninguna combinación, no se confirma nada y se libera lo reservado.
  3. Informa al Operador la etapa afectada y el motivo. La misión queda PENDIENTE. SC-15 termina.

## Reglas de negocio (aplican DURANTE la ejecución)

- **RN-01.** Con el paquete a bordo, un drone no puede volar más de 5 km sin recargar. Cada etapa de vuelo mide hasta 5 km inclusive. Se evalúa en el paso 3. *Supuesto:* leo "con carga" como "con el paquete", no como batería; el enunciado no fija un consumo por kilómetro, así que el límite de 5 km hace de proxy del alcance.
- **RN-02.** El paquete no puede quedar en una estación más de 30 minutos, contados desde que llega hasta que despega la etapa siguiente; 30 minutos exactos todavía cumple. La carga estándar dura 20 minutos (`MINUTOS_DE_CARGA` de Infernape 03), así que quedan 10 minutos de margen para el relevo. Se evalúa en los pasos 4 y 5.
- **RN-03.** El drone de cada etapa de vuelo debe estar disponible, estar donde empieza la etapa, tener al menos 30 % de batería al despegar (la RN-01 de la v2) y capacidad para el peso del paquete. Se evalúa en el paso 5. Como cada etapa tiene su propio drone, en cada estación el paquete se entrega al drone de relevo.
- **RN-04.** Cada etapa de vuelo se autoriza como un plan de vuelo propio: altura de hasta 120 m en zona urbana, distancia de hasta el radio efectivo del punto de salida (el menor entre lo configurado por el coordinador, el techo de la red y el límite de la Aerocivil, RF-12 y RF-13) y autorización de la Aerocivil. La urgencia no exime de nada de esto. Se evalúa en el paso 6.
- **RN-05.** El peso del paquete debe estar entre 1 y 2000 g (RN-03 de la v2). En una ruta larga, de más de 10 km, el paquete admite hasta 1000 g. *Supuesto:* el enunciado dice "demasiado pesado para ruta larga" sin cifras; propongo 10 km y 1000 g (la capacidad de los drones MINI y EXPRESS de la v2) como valores iniciales configurables, a confirmar con quien opere los drones. Se evalúa en los pasos 1 y 3.
- **RN-06.** La sede de origen y la de destino deben pertenecer a la red y ser distintas. Se evalúa en el paso 1.
- **RN-07.** Una estación no se asigna a dos paquetes a la vez en el mismo puesto de carga: se reserva la franja desde la llegada del paquete hasta su salida. La reserva se libera si la ruta no se confirma. Se evalúa en el paso 4.
- **RN-08.** La ruta se confirma completa o no se confirma: si falla cualquier etapa antes de iniciar el vuelo, no queda reservada ninguna estación ni ningún drone. Se evalúa en el paso 6.

## Cómo distinguí precondición de regla de negocio

| | Precondición | Regla de negocio |
|---|---|---|
| Cuándo se mira | Antes de empezar | Durante la ejecución, sobre un dato concreto |
| Si no se cumple | SC-15 no puede iniciar | SC-15 inicia y termina por un flujo alterno, con un motivo |
| Ejemplo aquí | P1: hay restricción de la Aerocivil registrada para el punto de salida | RN-01: ninguna etapa de vuelo pasa de 5 km |

Que no haya estación libre o drones libres **no** es una precondición: es una situación normal de la operación y por eso tiene sus flujos alternos (A4 y A5).

## Contradicción C-05: la estación de la calle 116 no alcanza con la regla de 5 km

El ejemplo del enunciado dice que la ruta de la ECI a Uniandes pasa por una estación de carga en la 116, y la regla RN-01 dice que con el paquete no se vuela más de 5 km sin recargar. Las dos cosas no caben juntas:

| Tramo | Distancia en línea recta (aprox.) |
|---|---|
| ECI a Uniandes | unos 20 km |
| ECI a la estación de la 116 | unos 9,5 km |
| Estación de la 116 a Uniandes | unos 11 km |

Las distancias salen de las coordenadas publicadas en Wikipedia para la ECI (4,7827° N, 74,0423° O) y para Uniandes (4,6016° N, 74,0652° O); la ubicación de la calle 116 es aproximada (4,697° N, 74,047° O), así que esas dos cifras pueden variar cerca de 1 km, pero no cambian la conclusión. Con una sola estación hay dos tramos, y a 5 km por tramo la ruta llega a 10 km como máximo, la mitad de lo que hace falta.

**Cómo se resuelve.** Se mantiene la regla (es de seguridad) y se interpreta el ejemplo como "la estación de la 116 es **una** de las estaciones de la ruta". Con unos 20 km, y en línea recta, hacen falta al menos 5 tramos de vuelo y 4 estaciones intermedias; si las estaciones registradas no permiten eso, el caso termina por A4 con el motivo "las estaciones registradas no cubren el recorrido". Con la velocidad de crucero que usa el código (30 km/h) y 20 minutos por carga, esa ruta ilustrativa son unos 41 minutos de vuelo y 80 de carga: algo más de 2 horas.

**A confirmar con quien opere los drones:** si 5 km es un límite firme o un valor de referencia, y dónde están realmente las estaciones; si lo fuera la 116 sola, habría que subir el límite a unos 11 km o agregar estaciones.

## Qué ya existe en el código y qué falta
| Parte de SC-15 | Estado |
|---|---|
| Rutas por etapas (tramos y paradas de carga, con anidación) y distancia, duración y paradas totales | Existe: `Ruta`, `TramoSimple`, `ParadaDeCarga`, `RutaCompuesta` (Infernape 03). Una ruta compuesta admite cualquier número de etapas |
| Criterio de optimización (menor distancia, menor duración, menos paradas) | Existe: `EstrategiaOptimizacionRuta` y sus tres estrategias |
| Paso 3: calcular las etapas | **Parcial:** `PlanificadorRutas` solo arma la ruta directa o con **una** parada de carga. No encadena varias estaciones, así que no puede producir la ruta de 5 tramos de la contradicción C-05 |
| RN-01 (tramos de hasta 5 km) | Parcial: `PlanificadorRutas` descarta las rutas con un tramo mayor que el alcance máximo, que es un parámetro (no está fijado en 5 km) |
| Duración de la carga (RN-02) | Parcial: la carga dura 20 min (`MINUTOS_DE_CARGA`), pero no se mide el tiempo de espera del paquete ni el relevo |
| Drone por etapa (paso 5) | Parcial: `FabricaDroneEtapa` crea un drone por etapa, pero no lo toma de la flota con su batería, ubicación y capacidad (RN-03) |
| Eventos por etapa (paso 7) | Existe: ETAPA_INICIADA, ETAPA_COMPLETADA y ETAPA_FALLIDA con `NotificadorRuta` |
| Paso 6: plan de vuelo con altura, radio y falla segura (RN-04, A1, A2) | Existe: `AutorizadorVuelo` y `PoliticaVuelo` (reto 06); cada etapa se pasaría como un `PlanVuelo`. El adaptador real de la Aerocivil y el límite de 3 s no existen |
| Paso 2: consultar las condiciones del espacio aéreo | No existe: el puerto `ServicioAerocivil` solo autoriza un plan, no entrega las condiciones |
| Estaciones de carga con disponibilidad y reservas (paso 4, RN-07, RN-08) | No existen |
| Peso en ruta larga (RN-05, A3), tipo de paquete y prioridad en la misión | No existen: `SolicitudEntrega` no tiene peso, tipo ni prioridad |
| Estado de la misión (PENDIENTE, EN_VUELO, RECHAZADA) | Existe en la v2 (`EstadoMision`), no en Enterprise |
| Restricción EVITAR_EDIFICIOS | Existe en el MVP (`RutaEvitandoEdificios`), sin conexión con la planificación Enterprise |

Esta plantilla describe el caso completo que se debe construir; lo que no existe es trabajo pendiente para los retos de implementación.

## Trazabilidad
SC-15 está en la matriz del reto 06 (`docs/trazabilidad`) como caso de uso de RF-14 y RF-16, junto a SC-10 y SC-12. `MatrizTrazabilidadTest` comprueba que no quede huérfano.

## Notas
- SC-15 consulta a la Aerocivil dos veces: en el paso 2 para saber si el espacio está abierto (condiciones) y en el paso 6 para autorizar el plan concreto de cada etapa. El enunciado nombra la verificación una sola vez al principio; las dos consultas responden a preguntas distintas, y la segunda no se puede hacer antes de conocer las etapas.
- La regla de urgentes de C-03 vale aquí: la prioridad URGENTE decide qué drone se asigna y en qué orden se atiende, nunca se salta la autorización ni los límites de altura y radio.
