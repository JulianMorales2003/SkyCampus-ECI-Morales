# Infernape 09 — Textos para copiar en Jira

Proyecto `JM`. Estados del tablero: Por hacer, En Diseño, Aprobado.

## 1. Sprints

| Sprint | Nombre | Inicio | Fin | Meta |
|---|---|---|---|---|
| 1 | Enterprise S1 — Conectividad | 13/10/2026 | 23/10/2026 | Dos sedes comparten flota bajo las reglas de la Aerocivil. |
| 2 | Enterprise S2 — Rutas multi-etapa | 26/10/2026 | 06/11/2026 | Un drone viaja de la ECI a la UNAL con parada de carga y avisos por etapa. |
| 3 | Enterprise S3 — Analytics y panel | 09/11/2026 | 20/11/2026 | El superadmin ve la red completa y el coordinador su sede. |

## 2. Historias nuevas (tipo Historia, etiqueta `enterprise`, épica de Enterprise)

**HU-17 — Panel de superadmin de la red** · Story points: 8 · Prioridad: Should · Sprint 3
Como Superadmin, quiero un panel que muestre la red completa (sedes, estaciones y misiones activas) y me deje fijar el techo de radio, para supervisar toda la red desde un solo lugar.
Depende de: HU-10.

**HU-18 — Adaptador HTTP de la Aerocivil** · Story points: 3 · Prioridad: Must · Sprint 1
Como Operador de drones, quiero que el sistema pregunte a la Aerocivil real y no despegue si no responde en 3 segundos, para que la falla segura funcione con el servicio de verdad y no solo con un doble de prueba.
Bloquea a: HU-13.

**HU-19 — Reserva de estación y límite de 30 min** · Story points: 5 · Prioridad: Should · Sprint 2
Como Operador de drones, quiero que cada parada de carga reserve una estación libre y que el paquete no espere más de 30 minutos, para que una ruta multi-etapa no deje paquetes olvidados.
Depende de: HU-11.

## 3. Puntos y sprint de las historias que ya existen

| Historia | Puntos | Sprint inicial |
|---|---|---|
| HU-9 | 3 | 1 |
| HU-10 | 2 | 1 |
| HU-11 | 8 | 2 |
| HU-12 | 3 | 1 |
| HU-13 | 5 | 1 |
| HU-14 | 3 | 3 |
| HU-15 | 3 | 2 |
| HU-16 | 2 | 3 |

Después de la retrospectiva: HU-10 pasa al Sprint 2 y HU-15 al Sprint 3.
