# Reto 06 — RF, RNF y priorización MoSCoW (SkyCampus MVP)

## Requerimientos funcionales (qué hace el sistema)

- **RF-01.** El sistema debe permitir al Operador de drones consultar los drones disponibles, mostrando de cada uno su id, su batería actual y su ubicación.
  - *Resultado observable:* aparece una lista con solo los drones que tienen `disponible = true`.
- **RF-02.** El sistema debe permitir al Solicitante registrar una solicitud de reparto indicando origen, destino y tipo de carga (SOBRE, CARPETA o LIBRO).
  - *Resultado observable:* el sistema muestra el código de la misión generada, con estado PENDIENTE.
- **RF-03.** El sistema debe permitir al Operador de drones asignar un drone disponible a una misión PENDIENTE.
  - *Resultado observable:* la misión pasa a estado EN_VUELO y ese drone deja de aparecer en la lista del RF-01.
