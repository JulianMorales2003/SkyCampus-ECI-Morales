# Reto 08 — Manual de identidad y mock del panel de monitoreo de la flota

![Manual de identidad de SkyCampus](ux/manual-identidad.png)

## 1. Paleta de marca e interfaz

| Nombre | Hex | Uso | Por qué |
|--------|-----|-----|---------|
| Azul noche | `#0F1B33` | Cabecera y marca | Transmite tecnología y confianza, como una app de control de sistemas |
| Fondo | `#0B1220` | Fondo de la aplicación | Un fondo oscuro reduce el reflejo en una sala de control y hace resaltar los colores de estado |
| Gris pizarra | `#1E293B` | Filas y tarjetas | Separa la información del fondo sin añadir un color nuevo |
| Morado técnico | `#7C3AED` | Acción principal ("Asignar misión") | Color reservado a las acciones: no se usa en ningún estado, así que un botón nunca se confunde con un estado |
| Texto principal | `#F1F5F9` | Texto sobre fondos oscuros | Máxima legibilidad |
| Texto secundario | `#94A3B8` | Etiquetas y ayudas | Baja el peso visual sin perder legibilidad |
| Borde | `#334155` | Divisores y fondo de las barras | |
| Deshabilitado | `#64748B` | Botones sin acción posible | |

Decisión clave: el azul de marca es muy oscuro y el azul del estado "En vuelo" (`#3B82F6`) es más claro y solo aparece en ese estado. Así cada color tiene un único significado.

## 2. Colores de estado del drone (críticos)

| Estado | Hex | Significado | Texto sobre el chip | Contraste |
|--------|-----|-------------|---------------------|-----------|
| Disponible | `#22C55E` | Puede recibir una misión | `#0B1220` | 8,22:1 |
| En vuelo | `#3B82F6` | Misión en curso | `#0B1220` | 5,09:1 |
| En carga | `#EAB308` | Espera; también marca una batería bajo el mínimo (30%) | `#0B1220` | 9,76:1 |
| Fallo | `#EF4444` | Requiere atención | `#0B1220` | 4,98:1 |

Regla: el estado nunca se comunica solo con el color. El chip siempre lleva el nombre del estado en texto, para quien no distingue bien los colores.

Contrastes del resto de la paleta (calculados con la fórmula de WCAG; el mínimo AA para texto es 4,5:1):

| Combinación | Contraste |
|-------------|-----------|
| Texto principal sobre fondo | 17,09:1 |
| Texto principal sobre gris pizarra | 13,35:1 |
| Texto secundario sobre gris pizarra | 5,71:1 |
| Blanco sobre morado técnico | 5,70:1 |
| Blanco sobre azul noche | 17,14:1 |

## 3. Tipografía

| Uso | Fuente | Pesos | Por qué |
|-----|--------|-------|---------|
| Interfaz | Inter (sans-serif; respaldo: Segoe UI, Arial) | 400 para texto, 600 para títulos, etiquetas y botones | Sans-serif clara a tamaños pequeños |
| IDs de drones, códigos de misión y porcentajes | JetBrains Mono (respaldo: Consolas, monospace) | 400 | El ancho fijo alinea los caracteres y distingue 0/O y 1/l |

Tamaños: títulos 24, cuerpo entre 15 y 17, etiquetas de columna 12 en mayúsculas con espaciado.

## 4. Tono de voz

Técnico pero claro: el operador es un profesional. Cada mensaje dice qué pasó con el dato concreto y cuál es el límite o qué hacer. Sin exclamaciones ni palabras de programación.

| Sí | No |
|----|----|
| El drone D-04 tiene batería insuficiente (18%). Mínimo requerido: 30%. | Error de asignación |
| El destino "Bloque Z" no existe. Destinos válidos: Bloque A, Bloque B, Bloque C, Bloque D, Biblioteca. | Destino inválido |
| No hay solicitudes pendientes. | ¡Todo al día! |
