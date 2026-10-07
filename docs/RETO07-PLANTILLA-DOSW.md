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
