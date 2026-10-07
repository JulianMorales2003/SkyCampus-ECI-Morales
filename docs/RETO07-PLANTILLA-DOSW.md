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
- **P2.** Existe al menos un drone con `disponible = true`.
- **P3.** Existe la lista de destinos válidos configurada por el Admin (Bloque A, Bloque B, Bloque C, Bloque D y Biblioteca).

## Datos de entrada

**Decisión: origen, destino y tipo de carga NO son datos de entrada de SC-01.** Los datos de entrada son solo el código de la solicitud y el drone elegido. El sistema lee origen, destino y tipo de carga de la solicitud, donde ya quedaron guardados en el RF-02. Razones:
- Hay una sola fuente de esos datos. Si el Operador los digitara otra vez, podrían diferir de lo que pidió el Solicitante.
- Se evitan errores de digitación del Operador.
- Es coherente con el RF-03, que registra la misión "a partir de una solicitud pendiente".

| Campo | Tipo | Descripción |
|-------|------|-------------|
| codigoSolicitud | String | Código de seguimiento de la solicitud pendiente que el Operador elige |
| droneAsignado | Drone(id:String, bateria:int, ubicacion:String) | Drone que el Operador elige de la lista de drones disponibles |

Datos que el sistema recupera de la solicitud (el Operador no los digita, solo los ve para confirmar):

| Campo | Tipo |
|-------|------|
| origen | String |
| destino | String |
| tipoCarga | Enum(SOBRE,CARPETA,LIBRO) |

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

## Flujo básico

1. El Operador pide registrar una misión y el sistema muestra la lista `solicitudesPendientes`.
2. El Operador elige una solicitud y el sistema muestra la lista `dronesDisponibles`.
3. El Operador elige un drone y confirma el registro.
4. El sistema comprueba las reglas de negocio RN-01 (batería) y RN-02 (destino), en ese orden.
5. El sistema crea la misión en estado EN_VUELO, marca el drone como no disponible, deja la solicitud como no pendiente y muestra al Operador el `codigoMision`.

## Flujos alternos

- **A1. Drone sin batería suficiente (en el paso 4, RN-01).**
  1. El sistema rechaza el registro y muestra el motivo: la batería del drone elegido y el mínimo exigido (30 %).
  2. No se crea ninguna misión y la solicitud sigue pendiente.
  3. El flujo vuelve al paso 3 para que el Operador elija otro drone.
- **A2. Destino inválido (en el paso 4, RN-02).**
  1. El sistema rechaza el registro y muestra el motivo: el destino de la solicitud no existe, junto con los destinos válidos.
  2. No se crea ninguna misión y la solicitud sigue pendiente.
  3. SC-01 termina, porque el destino pertenece a la solicitud y el Operador no lo puede corregir aquí.

Si ambas reglas fallan a la vez, solo se informa A1, porque RN-01 se comprueba primero.

## Reglas de negocio (aplican DURANTE la ejecución)

- **RN-01.** El drone elegido debe tener una batería de al menos 30 %. Se evalúa en el paso 4, sobre el drone que el Operador eligió.
- **RN-02.** El destino de la solicitud debe ser uno de los destinos válidos configurados. Se evalúa en el paso 4, aunque la solicitud ya se registró con ese destino, porque el Admin puede cambiar la lista de destinos entre el RF-02 y SC-01.

## Cómo distinguí precondición de regla de negocio

| | Precondición | Regla de negocio |
|---|--------------|------------------|
| Cuándo se mira | Antes de empezar | Durante la ejecución, sobre lo que el Operador eligió |
| Qué describe | Algo que debe existir | Una restricción del dominio sobre un dato concreto |
| Si no se cumple | SC-01 no puede iniciar | SC-01 inicia y rechaza el registro con un motivo |
| Ejemplo aquí | P2: existe al menos un drone disponible | RN-01: el drone elegido tiene al menos 30 % de batería |

## Notas
- RN-01 y RN-02 corresponden a `ValidadorBateria` y `ValidadorDestino` de la cadena del reto 03. Esa cadena también tiene `ValidadorCarga` (peso de la carga frente a la capacidad del drone); no se incluye aquí porque este reto pide dos reglas y las dos de los flujos alternos solicitados son estas.
- Con la decisión de los datos de entrada, en la flecha 1 del diagrama del reto 05 lo que viaja es el código de la solicitud y el drone, no un "id de misión": la misión todavía no existe antes de registrarla.
