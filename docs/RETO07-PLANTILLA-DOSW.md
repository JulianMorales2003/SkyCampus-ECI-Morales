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
