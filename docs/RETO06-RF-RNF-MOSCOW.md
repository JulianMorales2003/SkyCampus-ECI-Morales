# Reto 06 — RF, RNF y priorización MoSCoW (SkyCampus MVP)

## Requerimientos funcionales (qué hace el sistema)

- **RF-01.** El sistema debe permitir al Operador de drones consultar los drones disponibles, mostrando de cada uno su id, su batería actual y su ubicación.
  - *Resultado observable:* aparece una lista con solo los drones que tienen `disponible = true`.
- **RF-02.** El sistema debe permitir al Solicitante registrar una solicitud de reparto indicando origen, destino y tipo de carga (SOBRE, CARPETA o LIBRO).
  - *Resultado observable:* el sistema muestra un código de seguimiento de la solicitud, que queda pendiente de que un Operador le asigne un drone.
- **RF-03 (SC-01 Registrar misión de reparto).** El sistema debe permitir al Operador de drones registrar una misión de reparto a partir de una solicitud pendiente, eligiendo un drone disponible.
  - *Resultado observable:* el sistema crea la misión con su código y estado EN_VUELO, la solicitud deja de estar pendiente y el drone elegido deja de aparecer en la lista del RF-01.

## Requerimientos no funcionales (cómo debe ser el sistema)

Cada uno lleva un número, una condición y una forma de verificarlo.

| Código | Atributo | Requerimiento | Condición de medición | Cómo se verifica |
|--------|----------|---------------|-----------------------|------------------|
| RNF-01 | Rendimiento | La consulta de drones disponibles (RF-01) debe mostrar su resultado en menos de 2 segundos. | Con una flota de 5 drones, en al menos 95 % de las consultas | Medir el tiempo de 20 consultas seguidas; al menos 19 deben cumplir |
| RNF-02 | Usabilidad | Un operador sin capacitación previa debe poder registrar una misión de reparto (RF-03) en menos de 2 minutos, sin ayuda. | Usando el panel del operador, con 3 personas que no conozcan el sistema | Prueba con cronómetro; las 3 personas deben terminar dentro del tiempo |
| RNF-03 | Mantenibilidad | El código del dominio y de los servicios debe tener al menos 80 % de cobertura de líneas y 0 problemas de severidad Blocker o Critical. | En el build del proyecto | Reportes de JaCoCo y SonarQube |

## Priorización MoSCoW

| Código | Requerimiento | Categoría | Justificación |
|--------|---------------|-----------|---------------|
| RF-01 | Consultar drones disponibles | Must Have | Sin ver qué drones están libres, el operador no puede elegir uno, así que el flujo de reparto no arranca. |
| RF-02 | Registrar solicitud de reparto | Must Have | Sin solicitudes el Operador no tiene qué repartir, así que el flujo de reparto no tendría entrada. |
| RF-03 | Registrar misión de reparto (SC-01) | Must Have | Es el núcleo del MVP: sin una misión registrada con un drone, nada sale a repartir y no hay entrega. |
| RNF-01 | Consulta en menos de 2 segundos | Should Have | Importa para que el operador trabaje con fluidez, pero con 5 drones el MVP sigue funcionando aunque tarde un poco más. |
| RNF-02 | Registro de misión en menos de 2 minutos sin ayuda | Could Have | Si falta, el operador puede registrar misiones igual, pero con una pantalla menos clara tarda más y puede equivocarse de drone; la entrega del MVP no se detiene, por eso es deseable y no necesaria. |
| RNF-03 | 80 % de cobertura y 0 Blocker o Critical | Should Have | Si falta, el MVP puede operar, pero queda más riesgo de que un error pase sin detectarse (por ejemplo, asignar un drone con batería baja) y el operador lo descubriría con el drone ya en el aire; no bloquea la primera entrega, pero conviene no dejarlo sin cubrir. |

## Decisiones cerradas y notas
- **Solicitud y misión son cosas distintas.** RF-02 produce una solicitud con su propio código de seguimiento, no una `Mision`. La `Mision` solo existe cuando el Operador le asigna un drone (RF-03), como exige el modelo: el Builder pide drone y `Mision` rechaza un drone nulo. El modelo actual no tiene una clase para la solicitud; si el equipo la implementa, será una clase aparte (por ejemplo `SolicitudReparto`) y no se toca `Mision`.
- **Actor de SC-01 cerrado: Operador de drones.** SC-01 "Registrar misión de reparto" es el RF-03, ejecutado por el Operador, como en el ejemplo del enunciado del reto 07 (el drone asignado es un dato de entrada). El Solicitante conserva el RF-02 y en el reto 10 tendrá su propio caso de uso ("Registrar solicitud de reparto").
- **Diagrama del reto 05.** Las flechas 1 (el Operador asigna y confirma) y 3 (el Solicitante registra la solicitud) siguen valiendo. En la flecha 4, lo que recibe el Solicitante es el código de seguimiento de su solicitud; la etiqueta "código de la misión" puede ajustarse a ese texto.
- **Won't Have (según el enunciado):** la asignación automática y la ruta optimizada quedan fuera del MVP; van en Monferno.
