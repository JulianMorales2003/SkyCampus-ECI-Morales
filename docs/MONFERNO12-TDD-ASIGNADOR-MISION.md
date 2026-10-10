# Monferno · Reto 12 — TDD de `AsignadorMision` con Mockito

El asignador se escribió con el ciclo rojo, verde, refactor: primero las pruebas, que fallan; después el mínimo código que las hace pasar; al final se ordena el código con las pruebas en verde. La API del clima es un sistema externo (reto 05), así que se simula con Mockito.

## 1. Qué se construyó

| Pieza | Paquete | Qué hace |
|---|---|---|
| `AsignadorMision` | `skycampus.v2.service` | Valida el peso, consulta el clima, filtra los drones aptos, elige uno y avisa del resultado con un evento |
| `ApiMeteorologica` | `skycampus.v2.clima` | Interfaz del sistema externo (`boolean esApto()`); la implementación real vendrá en otro reto |
| `EstrategiaPorPrioridad` | `skycampus.v2.asignacion` | Urgentes al EXPRESS con más batería (si no hay, al de mayor batería); el resto, al de mayor batería |
| `TipoDrone.admitePeso(int)` | `skycampus.v2.model` | Peso mínimo y capacidad por tipo (RN-02 y RN-04) |
| 3 tipos nuevos en `TipoEvento` | `skycampus.v2.eventos` | `CLIMA_ADVERSO`, `PAQUETE_EXCEDE_CAPACIDAD` y `URGENTE_SIN_DRONE_RAPIDO` |

Reglas del caso SC-07 que implementa ([`MONFERNO07-SC07-ASIGNACION-AUTOMATICA.md`](MONFERNO07-SC07-ASIGNACION-AUTOMATICA.md)):
- RN-01: el drone tiene al menos 30 % de batería.
- RN-02: un CARGO no lleva paquetes de menos de 100 g.
- RN-03: el paquete pesa hasta 2000 g.
- RN-04: el drone tiene capacidad para el peso (MINI 1000 g, CARGO 2000 g, EXPRESS 1000 g; valores supuestos en SC-07, a confirmar).
- RN-05 (pasa/no pasa): el clima permite volar. La API no responde o lanza un error se trata como clima no apto (falla segura, RNF-06).
- Precedencia RF-08 sobre RF-07 para las urgentes, con el texto corregido del reto 06.

### Adaptaciones al enunciado
| Enunciado | En SkyCampus v2 | Por qué |
|---|---|---|
| `crearMisionTest(...)` y `Mision` como entrada | `SolicitudMision` | En v2 una `Mision` ya trae su drone; lo que entra al asignador es la solicitud |
| `asignador.asignar(flota, m)` | `asignar(solicitud, flota, ahora)` | Mismo orden de parámetros que `GestorFlota.asignar`; el instante entra como argumento para que la prueba controle el tiempo |
| `ObservadorDrone.onEstadoCambiado(a, b)` | `ObservadorFlota.alOcurrir(EventoFlota)` | Es el observador que ya existe en v2 desde el reto 03 |
| Devuelve `Optional<Drone>` | Igual | El motivo de un resultado vacío (clima, peso o sin drones) viaja en el evento, no en el valor de retorno |

## 2. El ciclo, paso por paso

| Paso | Qué se hizo | Resultado de las pruebas |
|---|---|---|
| 1. Rojo | Las 5 pruebas pedidas y un esqueleto de `AsignadorMision` que lanza `UnsupportedOperationException` | Las 5 nuevas fallan; las 208 anteriores pasan |
| 2. Verde | Lo mínimo para que pasen: peso, clima, filtro por batería y por EXPRESS si es urgente, mayor batería | 213 pasan |
| 3. Rojo (casos límite) | 10 pruebas más (dos parametrizadas, 12 casos en total) que el código mínimo no cubría | 8 fallan: peso de 2000 g, 1500 g sin drone que lo soporte, CARGO con menos de 100 g, API que lanza error, urgente sin EXPRESS, empate de batería y dos de argumentos nulos |
| 4. Verde | Capacidad por tipo, mínimo del CARGO, falla segura, respaldo de las urgentes con su aviso y validación de argumentos | 225 pasan |
| 5. Refactor | Se extrae la regla de prioridad a `EstrategiaPorPrioridad`, el peso por tipo a `TipoDrone.admitePeso` y el método largo en métodos con nombre; con sus pruebas | 240 pasan |

Dos detalles del ciclo:
- En el paso 3 pasó algo útil: las pruebas de batería de 30 % y 29 % ya pasaban con el código mínimo, porque `>= 30` ya estaba escrito. Una prueba verde desde el inicio no es un error, pero no aporta información nueva sobre ese código.
- Por eso el esqueleto del paso 1 lanza una excepción en vez de devolver `Optional.empty()`: con un vacío, tres de las cinco pruebas habrían pasado sin que existiera ninguna lógica.

## 3. Las cinco pruebas pedidas

| Prueba | Qué comprueba |
|---|---|
| `misionNormal_asignaDroneMayorBateria` | Con clima apto, una misión NORMAL recibe el drone de mayor batería (D-02, 91 %) y se publica `MISION_ASIGNADA` |
| `climaAdverso_retornaVacio` | Con clima no apto no se asigna nada y se publica `CLIMA_ADVERSO` |
| `sinDronesAptos_retornaVacio` | Un drone con 29 % y otro no disponible: no se asigna y se publica `SIN_DRONE_DISPONIBLE` |
| `paqueteMuyPesado_retornaVacioSinConsultarElClima` | 2001 g: se rechaza, se publica `PAQUETE_EXCEDE_CAPACIDAD` y **no se consulta el clima** |
| `misionUrgente_asignaDroneExpressAunqueOtroTengaMasBateria` | Una misión URGENTE recibe el EXPRESS (45 %) aunque haya un MINI con 95 % |

## 4. Casos límite
- Batería de 30 % se asigna y de 29 % no.
- Peso de 2000 g se asigna a un CARGO; 2001 g se rechaza.
- 1500 g con solo MINI y EXPRESS libres: no se rechaza el paquete, se trata como "sin drone disponible" (flujo A3 de SC-07).
- CARGO: 99 g no, 100 g sí.
- La API del clima lanza una excepción: se trata como clima adverso.
- Urgente sin EXPRESS apto (el EXPRESS tiene 20 %): se asigna el de mayor batería y se publican `URGENTE_SIN_DRONE_RAPIDO` y después `MISION_ASIGNADA`.
- Empate de batería: gana el id menor (igual que `EstrategiaMayorBateria`).
- Un solo evento por asignación; dependencias y argumentos nulos lanzan `IllegalArgumentException`.

## 5. Cómo se usa Mockito

| Elemento | Uso |
|---|---|
| `@ExtendWith(MockitoExtension.class)` | Crea los mocks antes de cada prueba y activa los *stubs* estrictos: una simulación configurada y no usada hace fallar la prueba |
| `@Mock ApiMeteorologica` | Simula el clima con `when(clima.esApto()).thenReturn(...)` y, en la falla, con `thenThrow(...)` |
| `@Mock ObservadorFlota` | Registra los avisos para comprobarlos |
| `@InjectMocks AsignadorMision` | Construye el asignador pasando los dos mocks al constructor |
| `@Captor ArgumentCaptor<EventoFlota>` | Captura los eventos para comprobar su tipo y su orden |
| `verify(clima, never()).esApto()` | Demuestra que con un paquete muy pesado no se llama al clima |
| `verify(notificador, times(n))` | Comprueba cuántos avisos se publicaron |

**Qué no se simula:** `Drone`, `SolicitudMision` y `Prioridad` son valores (records y enumeraciones), así que se usan reales con `DatosV2`. Simular un valor haría la prueba más frágil sin ganar nada.

## 6. Qué cambió en el refactor

| Antes (verde) | Después |
|---|---|
| Un método de unas 50 líneas con ifs anidados | `asignar` cuenta el caso en pasos; `climaPermiteVolar`, `dronesAptos` y `publicar` hacen una cosa cada uno |
| `2000`, `1000`, `100` y `30` escritos en el código | `PESO_MAXIMO_GRAMOS` y `BATERIA_MINIMA` en el asignador y el peso por tipo en `TipoDrone` |
| La regla de urgentes dentro del asignador | `EstrategiaPorPrioridad`, una clase pequeña con sus propias pruebas |
| `EstrategiaTipoSegunPaquete` con su propio 1000 | Usa `TipoDrone.MINI.capacidadGramos()` |

Las pruebas del asignador no cambiaron en el refactor y siguieron en verde en cada cambio.

## 7. Verificación de las pruebas
Se probó el código con 15 mutaciones manuales (por ejemplo `>` por `>=` en el peso y en la batería, quitar el aviso de asignación, invertir la condición de urgentes, cambiar el mínimo del CARGO, devolver `true` en la falla del clima). Las 15 hicieron fallar al menos una prueba.

## 8. Decisiones y pendientes
- **El asignador solo elige.** No crea la `Mision`, no marca el drone como ocupado ni cambia el estado de la solicitud (pasos 6, 7 y 9 de SC-07). `GestorFlota` ya crea la misión; falta decidir si el `AsignadorMision` se integra en él o lo reemplaza.
- **La estrategia es fija.** `AsignadorMision` usa `EstrategiaPorPrioridad` por dentro, con un constructor de dos parámetros: así `@InjectMocks` puede construirlo con solo los dos mocks. Si se quisiera cambiar la estrategia en caliente, habría que aceptarla por constructor o por un método, como hace `GestorFlota`.
- **Capacidades por tipo:** son el supuesto de RN-04 de SC-07 (CARGO 2000 g, EXPRESS 1000 g) y están pendientes de confirmar.
- **API del clima:** solo existe la interfaz. Falta la implementación con el tiempo máximo de 3 s y el límite de viento de 30 km/h (RN-05).
- **Eventos nuevos:** `PanelOperador` y `SistemaLog` los muestran sin cambios; `AlertaTecnico` los ignora porque solo reacciona a `FALLO_DRONE`.
- **Mockito en JDK 21:** el modo *inline* de Mockito 5 se engancha al arrancar y la JVM puede mostrar un aviso de agente dinámico; no afecta el resultado.
