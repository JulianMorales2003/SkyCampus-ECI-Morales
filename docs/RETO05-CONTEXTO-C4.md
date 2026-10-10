# Reto 05 — Diagrama de contexto C4 (nivel 1) de SkyCampus MVP

![Diagrama de contexto de SkyCampus MVP](c4/contexto-skycampus.png)

Archivos: [`contexto-skycampus.drawio`](c4/contexto-skycampus.drawio) (editable en app.diagrams.net), [`contexto-skycampus.svg`](c4/contexto-skycampus.svg) y [`contexto-skycampus.png`](c4/contexto-skycampus.png).

## Actores y sistemas
| Elemento | Tipo | Qué hace |
|----------|------|----------|
| Operador de drones | Persona | Asigna misiones y monitorea la flota |
| Solicitante | Persona | Pide el reparto de un documento |
| Admin | Persona | Configura destinos y flota |
| SkyCampus MVP | Sistema de software | Gestiona la flota, las misiones y su entrega dentro del campus de la ECI |
| Sistemas externos | — | Ninguno en el MVP |

## Flujos de datos (qué viaja y en qué dirección)
| # | Dirección | Datos |
|---|-----------|-------|
| 1 | Operador → SkyCampus | Al registrar la misión: código de la solicitud elegida + id del drone elegido; al confirmar la entrega: código de la misión |
| 2 | SkyCampus → Operador | Solicitudes pendientes (código, origen, destino, tipo de carga); drones disponibles con batería y ubicación; misiones con su estado (PENDIENTE, EN_VUELO, ENTREGADA, FALLIDA) |
| 3 | Solicitante → SkyCampus | Solicitud de reparto: origen, destino y tipo de carga (SOBRE, CARPETA, LIBRO) |
| 4 | SkyCampus → Solicitante | Código de seguimiento de la solicitud y su estado (pendiente, rechazada con su motivo, cancelada, o el estado de la misión) |
| 5 | Admin → SkyCampus | Destinos válidos (Bloque A-D, Biblioteca) y flota de drones (id, modelo) |
| 6 | SkyCampus → Admin | Confirmación de la configuración y listado vigente de destinos y drones |

## Supuestos y decisiones
- **Sin sistemas externos.** El enunciado dice que los drones se controlan directamente, sin API ni integración de red, así que no dibujé ningún sistema externo. Tampoco dibujé base de datos, correo ni PDF: son detalles de implementación (nivel 2 de C4) y en el rediseño del reto 04 son interfaces con implementaciones en memoria y en consola.
- **Flecha 4 (código de seguimiento al solicitante).** El enunciado deja la pregunta abierta. Decidí que sí vuelve, para que el solicitante pueda saber qué pasó con su pedido. Como la misión solo existe cuando el Operador asigna un drone, lo que recibe al registrar es el código de seguimiento de su solicitud (decisión de los retos 06 y 07).
- **Quién registra qué.** El Solicitante registra la solicitud (flecha 3) y el Operador registra la misión a partir de ella (flecha 1). Decisión cerrada en los retos 06 y 07.
- **Flecha 5.** En el MVP los destinos son una lista fija y la flota son 5 drones; el Admin configura esa lista y esa flota, no un catálogo abierto.
- **Evolución.** El enunciado indica que esto cambia en Monferno (monitoreo remoto) e Infernape (control autónomo multi-sede), donde sí aparecerían sistemas externos.

## Cómo editarlo
Abre `docs/c4/contexto-skycampus.drawio` en [app.diagrams.net](https://app.diagrams.net) (Archivo → Abrir desde → Dispositivo) y exporta con Archivo → Exportar como → PNG o SVG.
