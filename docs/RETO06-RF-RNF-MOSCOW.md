# Reto 06 — RF, RNF y priorización MoSCoW (SkyCampus MVP)

## Requerimientos funcionales (qué hace el sistema)

- **RF-01.** El sistema debe permitir al Operador de drones consultar los drones disponibles, mostrando de cada uno su id, su batería actual y su ubicación.
  - *Resultado observable:* aparece una lista con solo los drones que tienen `disponible = true`.
- **RF-02.** El sistema debe permitir al Solicitante registrar una solicitud de reparto indicando origen, destino y tipo de carga (SOBRE, CARPETA o LIBRO).
  - *Resultado observable:* el sistema muestra el código de la misión generada, con estado PENDIENTE.
- **RF-03.** El sistema debe permitir al Operador de drones asignar un drone disponible a una misión PENDIENTE.
  - *Resultado observable:* la misión pasa a estado EN_VUELO y ese drone deja de aparecer en la lista del RF-01.

## Requerimientos no funcionales (cómo debe ser el sistema)

Cada uno lleva un número, una condición y una forma de verificarlo.

| Código | Atributo | Requerimiento | Número | Condición | Cómo se verifica |
|--------|----------|---------------|--------|-----------|------------------|
| RNF-01 | Rendimiento | La consulta de drones disponibles (RF-01) debe mostrar su resultado rápido | Menos de 2 segundos | Con una flota de 5 drones, en al menos 95 % de las consultas | Medir el tiempo de 20 consultas seguidas; al menos 19 deben cumplir |
| RNF-02 | Usabilidad | Un operador sin capacitación previa debe poder asignar un drone a una misión pendiente (RF-03) sin ayuda | Menos de 2 minutos | Usando el panel del operador, con 3 personas que no conozcan el sistema | Prueba con cronómetro; las 3 personas deben terminar dentro del tiempo |
| RNF-03 | Mantenibilidad | El código del dominio y de los servicios debe estar probado y limpio | Al menos 80 % de cobertura de líneas y 0 problemas Blocker o Critical | En el build del proyecto | Reportes de JaCoCo y SonarQube |

## Priorización MoSCoW

| Código | Requerimiento | Categoría | Justificación |
|--------|---------------|-----------|---------------|
| RF-01 | Consultar drones disponibles | Must Have | Sin ver qué drones están libres, el operador no puede elegir uno, así que el flujo de reparto no arranca. |
| RF-02 | Registrar solicitud de reparto | Must Have | Sin solicitud no existe misión que asignar, así que el sistema no tendría nada que repartir. |
| RF-03 | Asignar drone a misión pendiente | Must Have | Es el núcleo del MVP: sin asignación el drone nunca sale y no hay entrega. |
| RNF-01 | Consulta en menos de 2 segundos | Should Have | Importa para que el operador trabaje con fluidez, pero con 5 drones el MVP sigue funcionando aunque tarde un poco más. |
| RNF-02 | Asignación en menos de 2 minutos sin ayuda | Could Have | Es deseable, pero medirla exige una prueba con usuarios y su incumplimiento no impide entregar el MVP. |
| RNF-03 | 80 % de cobertura y 0 Blocker o Critical | Should Have | El curso lo evalúa y protege la calidad del código, pero un MVP puede mostrarse al operador sin que esa medición esté completa. |

## Notas y puntos abiertos
- **Won't Have (según el enunciado):** la asignación automática y la ruta optimizada quedan fuera del MVP; van en Monferno.
- **Antes del reto 07:** el modelo `Mision` exige un drone al crearse (Builder del reto 03). Por eso aquí separé RF-02 (solicitud, hecha por el Solicitante) de RF-03 (asignación, hecha por el Operador). En el reto 07 hay que decidir si SC-01 "Registrar misión de reparto" junta esos dos pasos y qué actor lo ejecuta; el ejemplo del enunciado usa Operador de drones.
