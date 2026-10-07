# Reto 07 — Plantilla DOSW: SC-01 Registrar misión de reparto de documento

| Campo | Valor |
|-------|-------|
| Código | SC-01 |
| Nombre | Registrar misión de reparto de documento |
| Actor | Operador de drones |
| Requerimiento que implementa | RF-03 (reto 06) |

## Precondiciones (existen ANTES de ejecutar)

Si alguna falta, SC-01 no puede iniciar.

- **P1.** Existe al menos una solicitud de reparto pendiente, registrada por un Solicitante en el RF-02, con su código de seguimiento.
- **P2.** Existe al menos un drone disponible (los que muestra la consulta del RF-01).
- **P3.** Existe la lista de destinos válidos configurada por el Admin (Bloque A, Bloque B, Bloque C, Bloque D y Biblioteca).

## Datos de entrada

**Decisión: origen, destino y tipo de carga NO son datos de entrada de SC-01.** Los datos de entrada son solo el código de la solicitud y el id del drone elegido. El sistema lee origen, destino y tipo de carga de la solicitud, donde ya quedaron guardados en el RF-02, y la batería y la ubicación del drone de su estado vigente. Razones:
- Hay una sola fuente de esos datos. Si el Operador los digitara otra vez, podrían diferir de lo que pidió el Solicitante.
- Se evitan errores de digitación del Operador.
- La batería y la ubicación del drone no las trae el Operador: si viajaran con el dato de entrada podrían llegar desactualizadas.
- Es coherente con el RF-03, que registra la misión "a partir de una solicitud pendiente".

| Campo | Tipo | Descripción |
|-------|------|-------------|
| codigoSolicitud | String | Código de seguimiento de la solicitud pendiente que el Operador elige |
| idDrone | String | Id del drone que el Operador elige de la lista de drones disponibles (por ejemplo "D-03") |

Datos que el sistema recupera de la solicitud (el Operador no los digita, solo los ve para confirmar):

| Campo | Tipo |
|-------|------|
| origen | String |
| destino | String |
| tipoCarga | Enum(SOBRE,CARPETA,LIBRO) |

Datos que el sistema lee del estado vigente del drone elegido (el Operador no los digita):

| Campo | Tipo |
|-------|------|
| bateria | int |
| ubicacion | String |

## Datos de salida

| Campo | Tipo | Descripción |
|-------|------|-------------|
| codigoMision | String | Código único de la misión generada |
| estadoMision | Enum(PENDIENTE,EN_VUELO,ENTREGADA,FALLIDA) | Siempre EN_VUELO al terminar bien |

Datos que el sistema muestra durante el flujo:

| Campo | Tipo |
|-------|------|
| solicitudesPendientes | List<Solicitud(codigo:String, origen:String, destino:String, tipoCarga:Enum(SOBRE,CARPETA,LIBRO))> |
| dronesDisponibles | List<Drone(id:String, bateria:int, ubicacion:String)> |

Datos de salida cuando ocurre A2:

| Campo | Tipo |
|-------|------|
| estadoSolicitud | Enum(PENDIENTE,RECHAZADA) |
| motivoRechazo | String |

## Flujo básico

1. El Operador pide registrar una misión y el sistema muestra la lista `solicitudesPendientes`.
2. El Operador elige una solicitud; el sistema comprueba RN-02 (destino) y, si es válido, muestra la lista `dronesDisponibles`.
3. El Operador elige un drone y confirma el registro.
4. El sistema comprueba RN-01 (batería) sobre el drone elegido.
5. El sistema crea la misión en estado EN_VUELO, marca el drone como no disponible, deja la solicitud como no pendiente y muestra al Operador el `codigoMision`.

## Flujos alternos

- **A1. Drone sin batería suficiente (en el paso 4, RN-01).**
  1. El sistema rechaza el registro y muestra el motivo: la batería del drone elegido y el mínimo exigido (30 %).
  2. No se crea ninguna misión y la solicitud sigue pendiente.
  3. El Operador elige otro drone y el flujo vuelve al paso 3, o cancela el registro y SC-01 termina con la solicitud todavía pendiente (por ejemplo, cuando ningún drone disponible llega al 30 %).
- **A2. Destino inválido (en el paso 2, RN-02).**
  1. El sistema rechaza la solicitud elegida y muestra al Operador el motivo: el destino no existe, junto con los destinos válidos.
  2. El sistema pasa la solicitud al estado RECHAZADA y guarda el motivo. La solicitud deja de aparecer en `solicitudesPendientes`, así que no bloquea la lista ni se puede volver a elegir.
  3. No se pide ningún drone y no se crea ninguna misión. El Operador vuelve al paso 1 para elegir otra solicitud.
  4. El Solicitante verá el estado RECHAZADA y el motivo cuando consulte su solicitud con el código de seguimiento, y podrá registrar una solicitud nueva con un destino válido.

Las dos reglas ya no se comprueban juntas: RN-02 se comprueba en el paso 2 y RN-01 en el paso 4. Así un destino inválido se detecta antes de pedirle un drone al Operador.

## Reglas de negocio (aplican DURANTE la ejecución)

- **RN-01.** El drone elegido debe tener una batería de al menos 30 %. Se evalúa en el paso 4, sobre el drone que el Operador eligió.
- **RN-02.** El destino de la solicitud debe ser uno de los destinos válidos configurados. Se evalúa en el paso 2, al elegir la solicitud y antes de pedir un drone, aunque la solicitud ya se registró con ese destino, porque el Admin puede cambiar la lista de destinos entre el RF-02 y SC-01.

## Cómo distinguí precondición de regla de negocio

| | Precondición | Regla de negocio |
|---|--------------|------------------|
| Cuándo se mira | Antes de empezar | Durante la ejecución, sobre lo que el Operador eligió |
| Qué describe | Algo que debe existir | Una restricción del dominio sobre un dato concreto |
| Si no se cumple | SC-01 no puede iniciar | SC-01 inicia y rechaza el registro con un motivo |
| Ejemplo aquí | P2: existe al menos un drone disponible | RN-01: el drone elegido tiene al menos 30 % de batería |

## Notas
- RN-01 y RN-02 corresponden a `ValidadorBateria` y `ValidadorDestino` de la cadena del reto 03. Esa cadena valida la misión completa en un solo paso; esta plantilla comprueba el destino antes (paso 2), sobre la solicitud, para no pedirle un drone al Operador en vano. Al implementarlo, `ValidadorDestino` tendrá que poder usarse sobre una solicitud y no solo sobre una `Mision`.
- **Regla fuera del alcance de este reto:** la cadena real también rechaza una carga que supera la capacidad del drone (`ValidadorCarga`). Esta plantilla no la describe, así que por sí sola permitiría registrar una misión con una carga demasiado pesada; esa regla y su flujo alterno quedan pendientes.
- Mostrarle al Solicitante el estado de su solicitud (A2, paso 4) requiere una consulta de la solicitud que no está entre los 3 RF del reto 06. Queda anotada como requerimiento pendiente (Could Have) y es el candidato a segundo caso de uso del Solicitante en el reto 10.
- Con la decisión de los datos de entrada, en la flecha 1 del diagrama del reto 05 lo que viaja es el código de la solicitud y el drone, no un "id de misión": la misión todavía no existe antes de registrarla.
