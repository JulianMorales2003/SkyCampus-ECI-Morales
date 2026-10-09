# Monferno · Reto 07 — Plantilla DOSW: SC-07 Asignar automáticamente drone a misión

| Campo | Valor |
|-------|-------|
| Código | SC-07 |
| Nombre | Asignar automáticamente drone a misión |
| Actor principal | Solicitante: al registrar su solicitud (RF-02) dispara el caso |
| Actores secundarios | Operador de drones (recibe el aviso en su panel) y API Meteorológica (sistema externo, flecha 9 del diagrama v2) |
| Requerimiento que implementa | RF-07 (reto 06 de Monferno), con la regla de RF-08 para urgentes y la consulta de clima de RF-10 |

Decisión sobre el actor: en el MVP la asignación la hacía el Operador (SC-01). En la v2 la hace el sistema, así que el caso lo dispara la llegada de una solicitud; el Operador pasa a ser quien se entera, no quien decide.

## Precondiciones (existen ANTES de ejecutar)
Si alguna falta, SC-07 no puede iniciar.

- **P1.** El Admin registró la flota de drones (puede que ninguno esté disponible en este momento; eso lo trata A1).
- **P2.** El Admin configuró la lista de destinos válidos (BLOQUE_A, BLOQUE_B, BLOQUE_C, BLOQUE_D y BIBLIOTECA).
- **P3.** El sistema tiene una estrategia de asignación activa (mayor batería, tipo según paquete o batería justa).

## Datos de entrada
Los datos anidados se desglosan en una fila por atributo.

| Nombre | Tipo de campo | Reglas | Oblig. |
|---|---|---|---|
| paquete | — | — | Sí |
| paquete.peso | Integer | Entre 1 y 2000 gramos | Sí |
| paquete.tipo | Enum(SOBRE, CARPETA, LIBRO, EQUIPO) | — | Sí |
| paquete.prioridad | Enum(URGENTE, NORMAL, BAJO) | — | Sí |
| destino | Enum(BLOQUE_A, BLOQUE_B, BLOQUE_C, BLOQUE_D, BIBLIOTECA) | Uno de los destinos válidos configurados (P2) | Sí |

Datos que el sistema lee por su cuenta (nadie los digita):

| Nombre | Tipo de campo | De dónde sale |
|---|---|---|
| flota | List<Drone> | Estado vigente de la flota |
| flota[].id | String | Formato D-XX |
| flota[].tipo | Enum(MINI, CARGO, EXPRESS) | Estado vigente |
| flota[].bateria | Integer | Entre 0 y 100, estado vigente |
| flota[].disponible | Boolean | Estado vigente |
| clima.vientoKmh | Number | API Meteorológica |
| clima.lluvia | Boolean | API Meteorológica |

## Datos de salida

| Nombre | Tipo de campo | Reglas | Oblig. |
|---|---|---|---|
| mision | — | Solo existe si se asigna un drone | No (salida) |
| mision.id | String | Código único de la misión | No (salida) |
| mision.estado | Enum(PENDIENTE, EN_VUELO, COMPLETADA, FALLIDA) | EN_VUELO al asignar | No (salida) |
| droneAsignado | — | Calculado por el sistema según la estrategia activa | No (salida) |
| droneAsignado.id | String | Formato D-XX | No (salida) |
| droneAsignado.bateria | Integer | Mínimo 30 % (RN-01) | No (salida) |
| droneAsignado.tipo | Enum(MINI, CARGO, EXPRESS) | — | No (salida) |
| estadoSolicitud | Enum(PENDIENTE, ATENDIDA, RECHAZADA) | ATENDIDA al asignar; PENDIENTE en A1, A2 y A4; RECHAZADA en A3 | No (salida) |
| motivo | String | Solo cuando no se asigna: sin drones, clima, o paquete demasiado pesado | No (salida) |
| evento | Enum(MISION_ASIGNADA, SIN_DRONE_DISPONIBLE) | El que se publica a los observadores | No (salida) |

## Flujo básico

1. El sistema recibe la solicitud con el paquete (peso, tipo, prioridad) y el destino.
2. El sistema valida los datos de entrada (rangos de la tabla, RN-03 y destino válido). *Si el paquete pesa más de 2000 g: A3.*
3. El sistema consulta el clima a la API Meteorológica y comprueba RN-05. *Si es adverso o la API no responde: A2.*
4. El sistema filtra los drones aptos: disponibles, con batería suficiente (RN-01), con capacidad para el peso (RN-04) y sin incumplir RN-02. *Si no queda ninguno: A1.*
5. El sistema aplica la estrategia activa sobre los drones aptos y obtiene un drone. Para las misiones URGENTES rige la regla corregida de RF-08 (drone rápido con batería mínima segura, con respaldo en la mayor batería).
6. El sistema valida el drone elegido contra su estado vigente (batería, capacidad, disponibilidad), porque el estado pudo cambiar desde el paso 4. *Si ya no cumple: A4.*
7. El sistema asigna: crea la misión en estado EN_VUELO y marca el drone como no disponible.
8. El sistema notifica a los observadores suscritos el evento MISION_ASIGNADA (panel del operador, registro).
9. El sistema pasa la solicitud a ATENDIDA y entrega la misión y el drone asignado (`mision.id`, `droneAsignado.id`).

## Flujos alternos

- **A1. Sin drones aptos (en el paso 4).**
  1. El sistema no crea ninguna misión.
  2. Publica el evento SIN_DRONE_DISPONIBLE a los observadores; el Operador lo ve en su panel con el motivo (ningún drone disponible, con batería suficiente o con capacidad para el peso).
  3. La solicitud queda PENDIENTE para que se reintente cuando haya un drone apto. SC-07 termina.
- **A2. Condiciones climáticas adversas o clima no disponible (en el paso 3, RN-05).**
  1. El sistema no asigna ningún drone y no consume ninguno.
  2. Informa al Operador el motivo: "clima no apto" con el viento y la lluvia leídos, o "clima no disponible" si la API no respondió en 3 segundos (RNF-06, falla segura).
  3. La solicitud queda PENDIENTE y se reintenta cuando el clima mejore o cuando el Operador lo pida. SC-07 termina.
- **A3. El paquete supera la capacidad máxima (en el paso 2, RN-03).**
  1. El sistema rechaza la solicitud con el motivo: el peso supera los 2000 g que admite el sistema.
  2. La solicitud pasa a RECHAZADA con ese motivo y no se vuelve a evaluar. No se consulta el clima ni se toca ningún drone.
  3. El Solicitante verá el estado RECHAZADA con su motivo y podrá registrar una solicitud nueva con un paquete más liviano.
  - *Caso cercano:* si el peso está dentro de los 2000 g pero ningún drone disponible lo soporta (por ejemplo, 1500 g y solo hay MINI libres), no se rechaza el paquete: se trata como A1, con el motivo "ningún drone disponible soporta ese peso".
- **A4. El drone elegido ya no es válido en la validación final (en el paso 6).**
  1. El sistema descarta ese drone y no crea ninguna misión con él.
  2. Vuelve al paso 5 con los demás drones aptos. Si no queda ninguno, sigue A1.

## Reglas de negocio (aplican DURANTE la ejecución)

- **RN-01.** El drone asignado debe tener al menos 30 % de batería. Se evalúa en el paso 4 y se vuelve a comprobar en el paso 6.
- **RN-02.** Un drone de tipo CARGO no puede usarse para paquetes de menos de 100 g. Un paquete de exactamente 100 g sí puede ir en CARGO. Se evalúa en los pasos 4 y 6.
- **RN-03.** El peso del paquete debe estar entre 1 y 2000 g. Se evalúa en el paso 2.
- **RN-04.** El drone debe tener capacidad para el peso del paquete. Capacidades por tipo: MINI 1000 g (valor que ya usa `EstrategiaTipoSegunPaquete`), CARGO 2000 g y EXPRESS 1000 g. *Supuesto:* el enunciado solo fija el máximo del sistema (2000 g); los valores de CARGO y EXPRESS hay que confirmarlos.
- **RN-05.** El clima debe permitir volar: viento de hasta 30 km/h y sin lluvia (valor inicial de RF-10, configurable, a confirmar). Se evalúa en el paso 3.

## Cómo distinguí precondición de regla de negocio

| | Precondición | Regla de negocio |
|---|---|---|
| Cuándo se mira | Antes de empezar | Durante la ejecución, sobre un dato concreto |
| Si no se cumple | SC-07 no puede iniciar | SC-07 inicia y termina por un flujo alterno, con un motivo |
| Ejemplo aquí | P3: hay una estrategia activa | RN-01: el drone tiene al menos 30 % de batería |

Que no haya drones disponibles **no** es una precondición: es una situación normal y esperada de la operación, por eso tiene su flujo alterno (A1) y no impide iniciar.

## Qué ya existe en el código v2 y qué falta
| Parte de SC-07 | Estado |
|---|---|
| Pasos 5, 7 y 8: estrategia, creación de la misión EN_VUELO y notificación a observadores | Existe: `GestorFlota.asignar` con las tres estrategias y los eventos MISION_ASIGNADA y SIN_DRONE_DISPONIBLE (Monferno 03) |
| Filtro de drones disponibles (`disponible` y estado DISPONIBLE) | Existe en `EstrategiaAsignacion.candidatos` |
| RN-01 (30 % de batería) | Parcial: solo `EstrategiaBateriaJusta` exige un mínimo; las otras dos no |
| RN-02 (CARGO y paquetes de menos de 100 g) y RN-04 (capacidad por tipo) | No existen |
| RN-03 (1 a 2000 g) | No existe: `SolicitudMision` solo exige peso mayor que 0 |
| Tipo EQUIPO y `paquete.tipo` | No existen: la solicitud v2 no tiene tipo de paquete |
| Formato D-XX del id y destino como enumeración | No se exigen: ambos son texto libre |
| Paso 3 (clima, RN-05, A2) y estado de la solicitud (ATENDIDA, RECHAZADA, A3) | No existen |
| Paso 6 y A4 (validación final contra el estado vigente) | No existe |

Esta plantilla describe el caso completo que se debe construir; lo que no existe es trabajo pendiente para los retos de implementación.

## Notas
- Las reglas RN-01 a RN-03 son de la misma familia que los validadores del MVP (`ValidadorBateria`, `ValidadorCarga`, `ValidadorDestino`). Al implementarlas en v2 se pueden reutilizar como cadena de validación.
- La regla de urgentes de la estrategia (paso 5) sale de la resolución de la tensión RF-07 / RF-08 en `MONFERNO06-RF-RNF-V2.md`.
