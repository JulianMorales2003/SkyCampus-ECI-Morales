# Infernape · Reto 14 — Análisis de calidad con SonarQube en Enterprise
Estudiante: Julian Felipe Morales Zambrano

## Qué se pide

Ejecutar SonarQube en el sistema Enterprise y demostrar que el código de producción es mantenible: sin duplicaciones entre zonas, sin métodos que superen la complejidad ciclomática de 10 y con cobertura de 85 % o más. Hay que documentar la deuda técnica por módulo (PlanificadorRuta, GestorZonas y MonitorFlota), corregir todos los issues críticos y mostrar el Quality Gate final en verde.

**Resultado:** gate `SkyCampus Infernape` en **OK**, 0 issues abiertas, 0 minutos de deuda, 0,0 % de duplicación, 98,5 % de cobertura y ningún método con complejidad ciclomática mayor a 10 (el máximo es 9, de 343 métodos medidos).

## 1. Cómo se analizó

Se reutiliza el Quality Gate del reto 13 (ver [`INFERNAPE13-QUALITY-GATE-ENTERPRISE.md`](INFERNAPE13-QUALITY-GATE-ENTERPRISE.md)) y se agregan dos controles para la complejidad:

1. **El build la vigila.** El `pom.xml` tiene una regla de JaCoCo sobre cada método (`COMPLEXITY`, valor `TOTALCOUNT`, máximo 10), así que `mvn verify` falla si algún método supera 10, junto con la regla anterior de cobertura del 85 % en líneas y ramas.
2. **SonarQube la muestra.** `scripts/configurar-quality-gate.ps1 -Nivel Infernape -PerfilComplejidad` crea el perfil Java `SkyCampus Infernape (Java)` con la regla `java:S1541` (métodos demasiado complejos) en umbral 10 y se lo asigna al proyecto. La regla no viene activa en el perfil por defecto (`Sonar way`).

La complejidad se mide por dos vías que no son idénticas: SonarQube suma la complejidad por archivo y paquete, y JaCoCo da la complejidad de cada método a partir del código compilado. Por eso las tablas de abajo etiquetan cuál es cuál.

## 2. Módulos del reto y paquetes reales

El enunciado nombra tres módulos, pero el proyecto no tiene clases llamadas `GestorZonas` ni `MonitorFlota`, y `PlanificadorRuta` se llama `PlanificadorRutas`. No se crearon clases para que encajaran; se asignó a cada módulo del reto el conjunto de paquetes que cumple esa responsabilidad:

| Módulo del reto | Paquetes reales | Por qué |
|---|---|---|
| **PlanificadorRuta** | `skycampus.enterprise.ruta` (con `drone`, `evento` y `optimizacion`) | Contiene `PlanificadorRutas`, `GestorMisiones`, `RutaCompuesta` y las estrategias de optimización |
| **GestorZonas** | `skycampus.enterprise.dominio`, `.model` y `.aplicacion` | Las zonas son las sedes y su espacio aéreo: catálogo de sedes, radio de cada sede, política de vuelo, restricciones aéreas, autorización y asignación desde la sede de origen |
| **MonitorFlota** | `skycampus.enterprise.analytics` | Observa la flota y mide la eficiencia por sede y por red |
| Otros de Enterprise | `.api`, `.diseno`, `.infraestructura` | No pertenecen a ninguno de los tres módulos |

## 3. Deuda técnica por módulo

### Deuda que había y deuda que queda

El análisis inicial de Enterprise (reto 13) encontró 18 issues y 146 minutos de deuda. Repartidas por módulo:

| Módulo | Issues | Deuda inicial | Deuda final | Qué había |
|---|---|---|---|---|
| PlanificadorRuta | 2 | 25 min | 0 min | `RutaCompuesta` como `record` (20 min) y una lambda con dos llamadas en `PlanificadorYGestorTest` (5 min) |
| GestorZonas | 2 | 10 min | 0 min | Lambdas con dos llamadas en `CrearMisionTest` |
| MonitorFlota | 0 | 0 min | 0 min | Sin issues |
| Otros de Enterprise | 11 | 89 min | 0 min | `.diseno`: 9 issues y 79 min, casi todo expresiones regulares y bloques de texto; `.api`: 1 issue y 5 min; pruebas de requisitos: 1 issue y 5 min |
| Demos (`src/app`) | 3 | 22 min | 0 min | `MisionesApiApp` imprimía con `System.out` en vez de `util.Consola` |
| **Total** | **18** | **146 min** | **0 min** | |

Dos lecturas de esa tabla. Más de la mitad de la deuda estaba en las pruebas (79 de 146 minutos) y no en el código de producción (67). Y el módulo con más deuda fue el de diseño (`.diseno`, 79 minutos), no ninguno de los tres módulos del reto.

El detalle de cada issue, su causa raíz y su commit está en la tabla del reto 13. Los minutos por módulo salen de la deuda de las issues corregidas que informa SonarQube; los 22 minutos de las demos son la diferencia con el total inicial de 146 minutos.

### Métricas finales por módulo

| Módulo | Líneas (ncloc) | Complejidad (Sonar) | Complejidad cognitiva | Cobertura | Duplicación | CC máxima de un método (JaCoCo) | Métodos |
|---|---|---|---|---|---|---|---|
| PlanificadorRuta | 495 | 88 | 26 | 100 % | 0,0 % | 5 | 79 |
| GestorZonas | 472 | 92 | 50 | 100 % | 0,0 % | 9 | 52 |
| MonitorFlota | 133 | 27 | 11 | 100 % | 0,0 % | 7 | 17 |
| Otros de Enterprise | 331 | 64 | 36 | 100 % | 0,0 % | 7 | 39 |
| **Total Enterprise** | **1431** | **271** | **123** | **100 %** | **0,0 %** | **9** | **187** |

Todo el código de producción de Enterprise tiene 100 % de cobertura. El 98,5 % del proyecto completo se debe a clases de niveles anteriores (por ejemplo el paquete `model` del MVP, con 31 %) y a que las demos (`src/app`) quedan fuera de la medición.

## 4. Complejidad ciclomática

JaCoCo midió **343 métodos** del proyecto (los 187 de Enterprise y los de niveles anteriores). **Ninguno supera 10.** Los ocho más complejos:

| Complejidad | Clase | Método |
|---|---|---|
| 9 | `CrearMision` | `crear` |
| 7 | `TemaSede` | constructor |
| 7 | `EficienciaSede` | constructor |
| 7 | `PlanVuelo` | constructor |
| 6 | `MisionesApi` | `leerComando` |
| 6 | `AsignadorMision` (v2) | `asignar` |
| 5 | `AlertaOperadorConsola` | `enviar` |
| 5 | `RutaCompuesta` | constructor |

`CrearMision.crear` es el que está más cerca del límite, con 9. Está ahí porque encadena validaciones, autorización de la Aerocivil y asignación en un solo caso de uso. Con la regla de JaCoCo en el build, añadirle una rama más haría fallar `mvn verify` y obligaría a extraer un método antes de integrar.

## 5. Duplicación entre zonas

La duplicación es 0,0 % en todos los paquetes de Enterprise, incluidos los tres que forman GestorZonas (`.dominio`, `.model` y `.aplicacion`). SonarQube solo marca bloques de unas 10 líneas o más, y por debajo de ese tamaño hay una repetición pequeña: el método auxiliar `exigirTexto` (3 líneas) existe en `SolicitudEntrega`, `Drone` y `ComandoMision`. No se centralizó porque está bajo el umbral, no cuenta como duplicación para el gate y el dominio se mantiene sin depender de `util`; es una mejora posible, no un incumplimiento.

## 6. Issues críticas

Las 18 issues del análisis inicial se clasificaban así: 14 MAJOR, 3 MINOR y 1 INFO. **Ninguna era CRITICAL ni BLOCKER.** Todas se corrigieron en el reto 13, sin supresiones, y hoy hay **0 issues abiertas** (comprobado con la API de SonarQube, con el perfil de complejidad aplicado). En este reto no hizo falta corregir código; el trabajo fue medir por módulo y dejar la complejidad vigilada.

## 7. Quality Gate final

Estado del gate `SkyCampus Infernape`: **OK**.

| Indicador | Meta | Resultado |
|---|---|---|
| Line coverage | ≥ 85 % | 97,9 % |
| Branch coverage | ≥ 75 % | 100 % |
| Bugs | 0 | 0 |
| Vulnerabilidades | 0 | 0 |
| Deuda técnica | < 15 min | 0 min |
| Duplicación | < 3 % | 0,0 % |

Los porcentajes de líneas y ramas son los del análisis del reto 13: en este reto solo se cambiaron el `pom.xml` y el script, no el código de `src/`.

El estado del gate se comprobó con la API de SonarQube justo antes de subir el trabajo (no se adjunta captura en este reto):

```
Gate asignado:    SkyCampus Infernape
Estado del gate:  OK
Issues abiertas:  0
```

La complejidad por carpeta que muestra SonarQube (Measures → Complexity) suma 271 para Enterprise: `ruta` 88, `dominio` 48, `aplicacion` 39, `diseno` 30, `analytics` 27, `api` 20, `infraestructura` 14 y `model` 5. Coincide con la columna "Complejidad (Sonar)" de la sección 3.

## 8. Cómo reproducirlo

```powershell
$env:SONAR_TOKEN = "<tu token>"
mvn clean verify          # falla si la cobertura baja del 85 % o un método supera la complejidad 10
.\scripts\configurar-quality-gate.ps1 -Nivel Infernape -PerfilComplejidad
mvn org.sonarsource.scanner.maven:sonar-maven-plugin:sonar "-Dsonar.host.url=http://localhost:9000" "-Dsonar.token=$env:SONAR_TOKEN" "-Dsonar.ws.timeout=600"
```

La complejidad por método sale del informe de JaCoCo (`target/site/jacoco/jacoco.xml`): cada `<method>` trae un contador `COMPLEXITY` con sus ramas cubiertas y no cubiertas, y su suma es la complejidad del método.

## 9. Límites

- **El mapeo de módulos es una decisión de este documento:** `GestorZonas` y `MonitorFlota` no existen como clases. Si el revisor espera otro reparto de paquetes, los datos por paquete de la sección 3 permiten recalcularlo.
- **La deuda inicial por módulo se reconstruye con los datos de SonarQube** (issues corregidas y su esfuerzo). SonarQube también conserva las issues de niveles anteriores, por eso solo se atribuyeron las 18 del análisis del reto 13.
- **Las métricas de Sonar y de JaCoCo no son comparables entre sí:** la complejidad de Sonar suma por paquete y la de JaCoCo se da por método y cuenta también constructores y expresiones lambda.
- **La regla `java:S1541` depende de la versión de SonarQube.** Si no existe en la versión instalada, el script lo avisa y el control de complejidad lo da solo la regla de JaCoCo del build.
- **Sigue la supresión `@SuppressWarnings("java:S106")` de `util/Consola.java`**, anterior al reto 13 y explicada allí.
- **No se midió el comportamiento con 60 drones**: el enunciado lo usa como contexto, y las pruebas de rendimiento existentes son las del RNF-08 (planificar con 20 estaciones).
