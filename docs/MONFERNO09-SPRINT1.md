# Monferno · Reto 09 — Sprint 1 de SkyCampus v2 en Jira

Continúa el tablero del MVP ([`RETO09-JIRA.md`](RETO09-JIRA.md), que tiene HU-1 a HU-3). Las historias de la v2 siguen esa numeración, de HU-4 a HU-8, y salen de los requerimientos del reto 06 de Monferno ([`MONFERNO06-RF-RNF-V2.md`](MONFERNO06-RF-RNF-V2.md)).

## 1. Las 5 historias de usuario

| Historia | Texto | Requerimiento | MoSCoW |
|---|---|---|---|
| HU-4 | Como Operador de drones, quiero que el sistema asigne automáticamente el drone de mayor batería a cada solicitud, para no tener que comparar la flota a mano. | RF-07 (SC-07) | Must |
| HU-5 | Como Operador de drones, quiero que las misiones urgentes reciban el drone más rápido con batería suficiente, para que lo urgente salga primero sin arriesgar el vuelo. | RF-08 (corregido en el reto 06) | Should |
| HU-6 | Como Técnico de mantenimiento, quiero recibir una alerta cuando un drone entra en fallo, para atenderlo antes de que se pierda o quede parado sin que nadie lo sepa. | RF-09 | Must |
| HU-7 | Como Operador de drones, quiero que el sistema consulte el clima antes de lanzar una misión, para no enviar drones con viento fuerte o lluvia. | RF-10 | Should |
| HU-8 | Como Técnico de mantenimiento, quiero ver los drones en fallo y registrar su reparación, para devolverlos a la flota. | RF-11 | Could |

## 2. Estimación en Fibonacci (1, 2, 3, 5, 8)

**Referencia:** HU-6 vale 3 puntos y las demás se comparan con ella. No hay un historial de sprints previos con el que calibrar, así que la estimación es relativa.

| Historia | Puntos | Por qué |
|---|---|---|
| HU-4 | 5 | La parte central ya existe (`GestorFlota` y las tres estrategias, Monferno 03), pero falta lo que hace segura la asignación: batería mínima, capacidad por tipo, regla de CARGO, validación final y el estado de la solicitud. Es más que HU-6, pero con diseño ya resuelto. |
| HU-5 | 3 | Una estrategia compuesta (urgente con una regla, el resto con otra), batería mínima segura y respaldo. La decisión de diseño está tomada en el reto 06; falta implementarla y probarla. |
| HU-6 | 3 | Referencia. Los observadores y el evento de fallo ya existen; falta conectar el canal con el Sistema de Alertas y mostrar el aviso en el panel. |
| HU-7 | 8 | La más incierta: integración nueva con un sistema externo que todavía no existe en el código, con tiempo de espera de 3 segundos, falla segura, valores configurables y las pruebas con un doble de la API. |
| HU-8 | 5 | Pantalla nueva para el técnico y cambios en el modelo (hoy no existen los estados En carga y Mantenimiento). |

Total: 5 + 3 + 3 + 8 + 5 = **24 puntos**.

## 3. Qué cabe con una velocidad de 20 puntos

Se llena el sprint por prioridad MoSCoW, respetando las dependencias.

| Orden | Historia | Puntos | Acumulado | ¿Cabe? | Razón |
|---|---|---|---|---|---|
| 1 | HU-4 | 5 | 5 | Sí | Must; el resto depende de la asignación automática. |
| 2 | HU-6 | 3 | 8 | Sí | Must; es independiente y se puede hacer en paralelo con HU-4. |
| 3 | HU-5 | 3 | 11 | Sí | Should; se construye sobre HU-4 (es un caso particular de RF-07). |
| 4 | HU-7 | 8 | 19 | Sí | Should; alimenta el paso del clima de SC-07. |
| 5 | HU-8 | 5 | 24 | **No** | Could; con ella el sprint llegaría a 24 puntos y pasaría la velocidad de 20. |

**Sprint 1: HU-4, HU-5, HU-6 y HU-7, 19 puntos de 20.** HU-8 se queda en el backlog para el Sprint 2.

- **Meta del sprint:** el sistema asigna misiones solo, atiende primero lo urgente, avisa de los fallos y no lanza con mal clima.
- **Margen:** queda 1 punto libre (95 % de la velocidad). Es poco, pero el sprint sale de dos Must y dos Should; si HU-7 se atrasa por su incertidumbre, se traslada completa al Sprint 2 y no se sustituye por HU-8.
- **Por qué no se agrega HU-8 con un recorte:** una historia a medias no cuenta como terminada; es mejor dejarla completa para el siguiente sprint.

## 4. Criterios de aceptación en Gherkin (2 por historia)

### HU-4 — Asignación automática
```gherkin
Escenario 1: asigna el drone de mayor batería
  DADO QUE hay 3 drones disponibles con 95%, 60% y 40% de batería
    Y la solicitud es NORMAL, de 300 g, con destino Bloque C
  CUANDO el sistema ejecuta la asignación automática
  ENTONCES asigna el drone de 95%
    Y crea la misión en estado EN_VUELO
    Y notifica al PanelOperador y al SistemaLog

Escenario 2: no asigna si ningún drone tiene batería suficiente
  DADO QUE los drones disponibles tienen 18% y 25% de batería
  CUANDO el sistema ejecuta la asignación automática
  ENTONCES no crea ninguna misión
    Y publica el evento SIN_DRONE_DISPONIBLE
    Y la solicitud sigue PENDIENTE
```

### HU-5 — Prioridad para urgentes
```gherkin
Escenario 1: la urgente recibe el EXPRESS de mayor batería
  DADO QUE hay 3 drones disponibles con batería de 30% o más:
        un EXPRESS de 70%, un EXPRESS de 45% y un MINI de 95%
    Y la solicitud es URGENTE con un paquete de 300 g
  CUANDO el sistema ejecuta la asignación automática
  ENTONCES selecciona el drone de tipo EXPRESS con mayor batería (70%)
    Y cambia el estado del drone a EN_VUELO
    Y notifica al PanelOperador y al SistemaLog en menos de 500 ms

Escenario 2: el EXPRESS sin batería segura no se usa
  DADO QUE el único EXPRESS disponible tiene 5% de batería
    Y hay un MINI disponible con 95%
    Y la solicitud es URGENTE
  CUANDO el sistema ejecuta la asignación automática
  ENTONCES asigna el MINI, por la regla de mayor batería
    Y avisa al Operador de que la urgente no se atendió con el drone más rápido
```

### HU-6 — Alerta de fallo
```gherkin
Escenario 1: la alerta llega al técnico
  DADO QUE el drone D-03 está en vuelo
  CUANDO el drone reporta un fallo
  ENTONCES su estado pasa a FALLO
    Y se envía una alerta al Sistema de Alertas con el id del drone, la hora y su último estado
    Y el panel del operador muestra el aviso

Escenario 2: ningún fallo se pierde
  DADO QUE hay 3 observadores suscritos a la flota
  CUANDO ocurren 100 fallos seguidos
  ENTONCES cada observador recibe los 100 eventos en menos de 1 segundo
    Y se envía una alerta técnica por cada fallo
```

### HU-7 — Consulta meteorológica
```gherkin
Escenario 1: con clima apto la asignación continúa
  DADO QUE la API Meteorológica informa viento de 12 km/h y sin lluvia
  CUANDO el sistema va a lanzar una misión
  ENTONCES continúa con la asignación
    Y registra la lectura del clima junto a la misión

Esquema del escenario 2: con clima adverso o sin datos no se lanza
  DADO QUE <condicion>
  CUANDO el sistema va a lanzar una misión
  ENTONCES no se lanza ningún drone
    Y la solicitud sigue PENDIENTE
    Y el Operador ve el mensaje "<mensaje>"

  Ejemplos:
    | condicion                                       | mensaje              |
    | el viento es de 35 km/h (límite 30 km/h)        | clima no apto        |
    | hay lluvia                                      | clima no apto        |
    | la API no responde en 3 segundos                | clima no disponible  |
```

### HU-8 — Gestión por técnico (no entra en el Sprint 1)
```gherkin
Escenario 1: el técnico ve solo los drones en fallo
  DADO QUE D-04 está en FALLO y D-01 está DISPONIBLE
  CUANDO el técnico abre su lista
  ENTONCES ve D-04 con su id, tipo, batería y hora del fallo
    Y no ve D-01

Escenario 2: registrar la reparación devuelve el drone a la flota
  DADO QUE D-04 está en FALLO
  CUANDO el técnico registra su reparación
  ENTONCES D-04 pasa a DISPONIBLE
    Y sale de la lista del técnico
    Y vuelve a ser candidato para la asignación automática
```

## 5. Definition of Done del equipo SkyCampus v2

Una historia está terminada solo si cumple los seis puntos.

| # | Criterio | Cómo se comprueba en este proyecto |
|---|---|---|
| 1 | Código revisado en PR por al menos otro miembro | El PR queda aprobado por el revisor antes del merge |
| 2 | Pruebas unitarias con cobertura JaCoCo de 80 % o más | `mvn clean verify` falla si la cobertura de líneas baja del 80 % (configurado en el `pom.xml`) |
| 3 | SonarQube: 0 bugs y 0 vulnerabilidades | Se ejecuta el análisis a mano antes de cerrar el sprint. Todavía no es parte del build |
| 4 | Todos los criterios Gherkin de la historia pasan | Cada escenario tiene al menos una prueba JUnit con su nombre, y ninguna falla |
| 5 | Mergeado a `develop` con el flujo GitFlow correcto | Una rama `feature/MoralesZambrano-*` por historia, merge con `--no-ff` y solo después del APROBADO |
| 6 | Diagrama de contexto y plantilla DOSW actualizados si el RF cambió | Se revisan `MONFERNO05-CONTEXTO-V2.md` y la plantilla SC afectada; si el RF no cambió, se anota "sin cambios" en el PR |

## 6. Límites
- La velocidad de 20 puntos viene del enunciado; no se midió con sprints anteriores.
- El tablero de Jira lo crea quien tenga acceso: este documento da el contenido, las estimaciones y el plan de cada ticket.
- Los criterios de HU-7 y HU-5 usan valores propuestos en el reto 06 (30 km/h de viento y 30 % de batería mínima segura), pendientes de confirmar.
