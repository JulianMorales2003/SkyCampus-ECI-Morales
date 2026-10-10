# Prompt de los 3 estados del panel de monitoreo (reto 11)

Este es el prompt completo con el que se generan los 3 estados del panel. Sigue la plantilla del material (sistema, pantalla, estilo, actor, datos por drone, acciones y estados) y lleva la identidad escrita dentro: la IA solo usa lo que está en el prompt. La versión 1 del mock del reto 08 no salió de un prompt escrito; esta versión de 3 estados sí se generó siguiendo este texto.

## Prompt

```text
Actúa como diseñador UX/UI senior de sistemas de control.

SISTEMA: SkyCampus, panel de control de la flota de drones de reparto de la Escuela Colombiana de Ingeniería (ECI).
PANTALLA: Panel de monitoreo de la flota (vista principal del operador). Entrega 3 variantes de la MISMA pantalla, una por estado, con el mismo tamaño y la misma estructura.
ACTOR: Operador de drones, un profesional que necesita tomar decisiones rápidas.

ESTILO (obligatorio, no inventes otros colores ni fuentes). Fondo oscuro tipo dashboard técnico:
- Fondo de la aplicación #0B1220. Cabecera #0F1B33 con la marca "SkyCampus" y el nombre de la pantalla.
- Filas y tarjetas: gris pizarra #1E293B. Bordes y fondo de las barras: #334155.
- Texto principal #F1F5F9. Texto secundario #94A3B8.
- Acción principal (botón "Asignar misión" habilitado): morado técnico #7C3AED con texto blanco. El morado no se usa en ningún estado.
- Botón deshabilitado: fondo #334155 y texto #94A3B8.
- Tipografía: Inter (400 y 600) para la interfaz y JetBrains Mono para IDs, porcentajes y códigos. Si no están disponibles, DejaVu Sans y DejaVu Sans Mono.
- Colores de estado del drone, cada uno con UN solo significado: Disponible #22C55E, En vuelo #3B82F6, En carga #EAB308, Fallo #EF4444. El estado va en un chip con su NOMBRE en texto #0B1220; nunca solo con color.
- El rojo #EF4444 solo significa Fallo; el amarillo solo En carga; el azul solo En vuelo; el verde solo Disponible. Los mensajes informativos usan colores neutros.

DATOS A MOSTRAR POR DRONE: ID (formato D-XX), batería en % (barra con marca vertical en el 30%, que es el mínimo para asignar una misión), estado (Disponible / En vuelo / En carga / Fallo), ubicación actual. No muestres nada más.
ACCIONES DEL OPERADOR: Seleccionar drone (la fila seleccionada lleva un borde morado), Asignar misión (solo habilitado si el drone está Disponible y tiene al menos 30% de batería) y Ver detalle.
LEYENDA: los 4 estados con punto de color y nombre, siempre visible.
FRANJA DE ESTADO: debajo del título hay una franja, siempre de la misma altura, con un mensaje principal y, si hace falta, una segunda línea más pequeña.
BATERÍA BAJO EL MÍNIMO: no usa ningún color de estado; se marca con un triángulo de advertencia y la etiqueta "Bajo mínimo" en #F1F5F9.
RAZÓN DEL BLOQUEO: debajo de cada fila con "Asignar misión" deshabilitado, una línea con la razón. Excepción: en el estado Vacío todas las filas comparten la misma razón y la explica la franja, así que no se repite por fila.

ESTADOS DE LA PANTALLA:
1. NORMAL. Flota con drones en distintos estados y ninguno en fallo:
   D-01 | Disponible | 85% | Bloque A (seleccionado)
   D-02 | En vuelo   | 42% | Biblioteca
   D-03 | Disponible | 91% | Bloque C
   D-04 | Disponible | 18% | Bloque B (bloqueado por batería: "El drone D-04 tiene batería insuficiente (18%). Mínimo requerido: 30%.")
   D-05 | En carga   | 67% | Bloque D
   Franja: "Flota: 3 disponibles, 1 en vuelo, 1 en carga. Asignables ahora: 2." (neutra)
2. ALERTA. Igual que Normal (D-01 sigue seleccionado), pero D-03 está en FALLO y se destaca visualmente: chip rojo, borde rojo de la fila, "Asignar misión" deshabilitado con la razón y "Ver detalle" resaltado como acción útil. La franja pasa a una alerta con borde rojo y conserva el resumen de la flota: línea 1 "Alerta: el drone D-03 reporta un fallo. Requiere atención antes de volver a usarlo."; línea 2 "Flota: 2 disponibles, 1 en vuelo, 1 en carga, 1 en fallo. Asignables ahora: 1."
3. VACÍO. Todos los drones están en misión a la vez, sin ninguno disponible:
   D-01 | En vuelo | 61% | Bloque A
   D-02 | En vuelo | 42% | Biblioteca
   D-03 | En vuelo | 77% | Bloque C
   D-04 | En vuelo | 54% | Bloque B
   D-05 | En vuelo | 38% | Bloque D
   Todos los botones "Asignar misión" deshabilitados. Franja neutra: línea 1 "No hay drones disponibles para asignar: los 5 drones están en misión."; línea 2 "Usa Ver detalle para revisar cada misión en curso."

NIELSEN que debe cumplir cada estado: visibilidad del estado (#1), minimalismo (#8), prevención de errores (#5).

FORMATO DE SALIDA: tres archivos SVG de 1280 x 760 px, sin imágenes externas, todo el texto en español.
```
