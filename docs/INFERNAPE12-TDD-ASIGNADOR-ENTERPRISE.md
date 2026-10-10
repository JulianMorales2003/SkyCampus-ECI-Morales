# Infernape · Reto 12 — Tres capas de pruebas para la creación de misiones de Enterprise

## 1. Qué se pide
Tres capas de pruebas alrededor del `AsignadorMision` de Enterprise: unitarias, integración del endpoint REST y un extremo a extremo simulado; cobertura de JaCoCo ≥ 85 % con ramas; y los **5 flujos alternos** (clima adverso, sin drones, paquete pesado, Aerocivil rechaza, sede inactiva), cada uno con su prueba.

## 2. Qué se construyó
El proyecto no tenía endpoint ni conocía el peso o las sedes inactivas, así que había que crear lo que las pruebas ejercitan, **sin romper la arquitectura por capas** (RNF-11, `ArquitecturaCapasTest`):

| Pieza | Capa | Qué hace |
|---|---|---|
| `CatalogoSedes` (puerto) | dominio | Qué sedes operan y a cuántos km queda una de otra |
| `CrearMision`, `ComandoMision`, `ResultadoMision`, `MotivoMision`, `PrioridadMision` | aplicación | Caso de uso completo: sedes activas → peso ≤ 2000 g → ruta definida → `AsignadorMision` → `AutorizadorVuelo`. La urgencia viaja en el plan, no salta ninguna regla (C-03) |
| `MisionesApi` | api (entrada REST) | `POST /api/v3/misiones`, con el servidor HTTP del JDK |
| `CatalogoSedesEnMemoria`, `RepositorioFlotaEnMemoria` | infraestructura | Adaptadores en memoria (reemplazan la BD de H2) |
| `app/MisionesApiApp` | demo | Levanta el endpoint en el puerto 8080 con datos de ejemplo (fuera de la cobertura, como las demás apps) |

Contrato del endpoint (decisión mía: el enunciado solo da el 201):

| Caso | Estado HTTP | Código |
|---|---|---|
| Misión creada | 201 | `{"id","droneAsignado":{id,sede,bateria},"estado":"EN_VUELO"}` |
| Datos inválidos | 400 | `DATOS_INVALIDOS` |
| Clima adverso / sin drones / sede inactiva | 409 | `CLIMA_ADVERSO` / `SIN_DRONE_DISPONIBLE` / `SEDE_INACTIVA` |
| Paquete pesado / ruta sin definir | 422 | `PAQUETE_EXCEDE_PESO` / `RUTA_NO_DEFINIDA` |
| Aerocivil rechaza, radio o altura excedidos, sin restricción | 403 | `AEROCIVIL_RECHAZA` / `RADIO_EXCEDIDO` / `ALTURA_EXCEDIDA` / `SIN_RESTRICCION_VIGENTE` |
| Aerocivil no responde (falla segura) | 503 | `AEROCIVIL_NO_RESPONDE` |

Cuerpo de la petición: `{"origen":"ECI","destino":"UNAL","pesoPaquete":300,"prioridad":"NORMAL"}`. Añadí `origen` porque el ejemplo del enunciado no lo trae y el asignador necesita la sede de la que sale el drone. La prioridad es opcional (NORMAL).

## 3. La pirámide de SkyCampus Enterprise
| Capa | Qué prueba | Herramienta | Archivo |
|---|---|---|---|
| 1 · Unitarias (base) | `AsignadorMision` y `CrearMision` aislados | JUnit 5 + Mockito (y lambdas) | `AsignadorMisionTest` (ya existía), `AsignadorMisionBordesTest`, `CrearMisionTest` |
| 2 · Integración (medio) | Aplicación + dominio + adaptadores en memoria juntos, con clima y Aerocivil simulados | JUnit 5 + Mockito | `MisionIntegrationTest` |
| 3 · E2E simulado (punta) | Una petición HTTP real al endpoint, de la API al dominio | `java.net.http.HttpClient` + servidor HTTP del JDK | `MisionesApiTest` |
| Arquitectura | La api no depende de la infraestructura ni de frameworks | JUnit 5 | `ApiCapaTest` |

### Adaptación al enunciado (importante)
El enunciado usa `@SpringBootTest`, `MockMvc` y H2. **El proyecto no usa Spring** (el `pom.xml` solo tiene JUnit y Mockito) y su regla de arquitectura prohíbe frameworks en el dominio y la aplicación. En lugar de meter Spring solo para esta prueba, se usó el servidor HTTP que ya trae el JDK y un cliente HTTP real, de modo que la capa 3 es aún más "de verdad" que `MockMvc` (hay red local, serialización y códigos HTTP reales). Los adaptadores en memoria hacen el papel de H2. Si la cátedra exige Spring, los tests de capa 3 se portan a `MockMvc` casi línea por línea (la misma petición y las mismas aserciones) añadiendo `spring-boot-starter-web` y un controlador que llame a `CrearMision`.

## 4. Los 5 flujos alternos y dónde se prueba cada uno
| Flujo | Capa 1 | Capa 2 | Capa 3 |
|---|---|---|---|
| Clima adverso | `CrearMisionTest#climaAdverso`, `#climaLanzaExcepcion`; `AsignadorMisionBordesTest#climaAdverso_detalleNombraLasSedes` | `crearMision_climaAdverso` | `climaAdverso_409` |
| Sin drones | `CrearMisionTest#sinDrones`; `AsignadorMisionBordesTest#todosBajoElMinimo_sinDrone` | `crearMision_sinDrones` | `sinDrones_409` |
| Paquete pesado | `CrearMisionTest#peso_limite` (1, 2000, 2001, 5000 g) | `crearMision_paquetePesado` | `paquetePesado_422` |
| Aerocivil rechaza | `CrearMisionTest#aerocivilRechaza`, `#urgente_noSeSaltaLaAutorizacion` | `crearMision_aerocivilRechaza` | `aerocivilRechaza_403` |
| Sede inactiva | `CrearMisionTest#sedeInactiva_origen`, `#sedeInactiva_destino` | `crearMision_sedeInactiva` | `sedeInactiva_409` |

Otros casos de borde cubiertos: batería de 29/30/31 % (ya existía), empate de batería, Aerocivil sin respuesta, radio y altura excedidos, sede sin restricción vigente, ruta sin definir, prioridad omitida, cuerpos inválidos (9 variantes), método no permitido (405) y ruta inexistente (404).

## 5. Cobertura y cómo verificar
Las pruebas nuevas suman **53 métodos de prueba** (unos 56 casos con los parametrizados). El `pom.xml` ya trae el *quality gate* de JaCoCo: `mvn verify` **falla** si la cobertura de líneas o de ramas baja del 85 %, y no hace falta tocarlo.

```
mvn clean verify
```
El informe queda en `target/site/jacoco/index.html`.

**Lo que se verificó aquí y lo que no.** En el entorno donde se escribió el código no se puede descargar de Maven Central (política de red), así que **no se ejecutó `mvn verify` ni se midió JaCoCo**. Lo que sí se comprobó: todo el proyecto compila con `javac`, y un programa de humo con los mismos escenarios contra el endpoint real confirmó los 201, 400, 403, 404, 405, 409, 422 y 503 esperados. Las pruebas JUnit no se pudieron ejecutar: antes de integrar, corre `mvn clean verify` y revisa que pase y que JaCoCo marque ≥ 85 % (si un test falla por un detalle de sintaxis o de estricticidad de Mockito, es lo primero a corregir).

## 6. Pendiente y límites
- RF-15 con urgencia (C-04): `CrearMision` usa la misma estrategia (mayor batería) para urgentes y normales; la estrategia para urgentes sigue pendiente, como en la matriz.
- La altura de crucero (100 m) y la zona urbana son constantes del caso de uso; cuando exista el plan de ruta real, vendrán de ahí.
- El adaptador HTTP real de la Aerocivil y el de clima siguen sin existir: se simulan.
- El endpoint no tiene autenticación ni límites de tamaño: es un adaptador de demostración.
