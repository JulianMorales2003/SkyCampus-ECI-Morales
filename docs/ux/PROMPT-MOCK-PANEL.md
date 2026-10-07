# Prompt del mock "Panel de monitoreo de la flota" (versión 2)

Este es el prompt completo con el que se genera la versión 2 del mock. La identidad (paleta, estados, tipografía y tono) viaja dentro del prompt, porque la IA solo usa lo que está escrito en él. La versión 1 del mock no salió de un prompt escrito: se generó a partir del enunciado del reto. La versión 2 se generó siguiendo este texto.

## Prompt

```text
Rol: eres un diseñador de interfaces. Diseña el mock de UNA pantalla: "Panel de monitoreo de la flota" de SkyCampus, un sistema de drones de reparto de la Escuela Colombiana de Ingeniería. El usuario es el Operador de drones, un profesional: necesita ver de un vistazo qué drones puede asignar.

Formato de salida: un solo archivo SVG de 1280 x 700 px, sin imágenes externas. Todo el texto en español.

IDENTIDAD (obligatoria, no inventes otros colores ni fuentes):
- Fondo de la aplicación #0B1220. Cabecera #0F1B33 (altura 72 px) con una marca "SkyCampus" y a la derecha el nombre de la pantalla.
- Filas y tarjetas: gris pizarra #1E293B. Bordes, divisores y fondo de las barras: #334155.
- Texto principal #F1F5F9. Texto secundario #94A3B8.
- Acción principal (botón "Asignar misión" habilitado): morado técnico #7C3AED con texto blanco. Este morado no se usa para ningún estado.
- Colores de estado del drone, cada uno con UN solo significado:
  Disponible #22C55E, En vuelo #3B82F6, En carga #EAB308, Fallo #EF4444.
  El estado se muestra como un chip redondeado con el NOMBRE del estado en texto #0B1220. Nunca uses solo el color.
- Botón deshabilitado: fondo #334155 y texto #94A3B8.
- Tipografía: Inter (pesos 400 y 600) para la interfaz; JetBrains Mono para IDs de drones, porcentajes de batería y códigos. Si no están disponibles, usa DejaVu Sans y DejaVu Sans Mono.
- Tono de voz: técnico pero claro. Mensajes directos con el dato concreto, sin exclamaciones.

CONTENIDO (muestra exactamente estos 5 drones, en este orden):
- D-01 | Disponible | 85% | Bloque A
- D-02 | En vuelo   | 42% | Biblioteca
- D-03 | Fallo      | 91% | Bloque C
- D-04 | Disponible | 18% | Bloque B
- D-05 | En carga   | 67% | Bloque D

DISEÑO:
- Título "Flota de drones" y una leyenda con los 4 estados (punto de color + nombre) siempre visible.
- Una fila por drone con las columnas: ID, estado, batería (barra + porcentaje), ubicación y acción. No muestres nada más que eso.
- En cada barra de batería dibuja una marca vertical en el 30% exacto: es el mínimo para asignar una misión. Explícalo en una nota al pie.
- El botón "Asignar misión" está habilitado solo si el drone está Disponible y tiene al menos 30% de batería. En los demás casos va deshabilitado.
- La batería bajo el 30% NO usa ningún color de estado (ni el amarillo ni el rojo): márcala con un icono de triángulo de advertencia y la etiqueta "Bajo mínimo", ambos con el color de texto principal #F1F5F9, junto al porcentaje.
- Debajo de cada fila con botón deshabilitado, escribe la razón en una línea:
  D-02: "No se puede asignar: el drone está en vuelo."
  D-03: "No se puede asignar: el drone reporta un fallo."
  D-04: "El drone D-04 tiene batería insuficiente (18%). Mínimo requerido: 30%." (con el icono de advertencia al inicio)
  D-05: "No se puede asignar: el drone está en carga."
```
