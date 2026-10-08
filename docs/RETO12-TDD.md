# Reto 12 — TDD con ValidadorMision (Red, Green, Refactor)

## 1. Qué se entrega

La clase `ValidadorMision` (`src/validacion/ValidadorMision.java`) con 3 métodos, y sus pruebas (`test/validacion/ValidadorMisionTest.java`) con JUnit 5, patrón AAA, `@DisplayName` y nombres `metodo_condicion_resultado`.

| Método | Qué hace |
|--------|----------|
| `boolean tieneBateriaSuficiente(Drone)` | Dice si la batería del drone alcanza el mínimo del 30% (RN-01) |
| `void validarDestino(String)` | No hace nada si el destino está en la lista; si no, lanza `DestinoInvalidoException` |
| `boolean droneEstaDisponible(Drone)` | Dice si el drone está libre, sin mirar su batería |

## 2. Las 9 pruebas (3 por método)

| Método | Caso | Prueba |
|--------|------|--------|
| `tieneBateriaSuficiente` | Normal | `tieneBateriaSuficiente_bateriaDel85PorCiento_retornaTrue` |
| `tieneBateriaSuficiente` | Límite | `tieneBateriaSuficiente_bateriaAlrededorDelLimite_respetaElMinimoDel30PorCiento` (30 alcanza, 29 no) |
| `tieneBateriaSuficiente` | Nulo | `tieneBateriaSuficiente_droneNulo_lanzaIllegalArgumentException` |
| `validarDestino` | Normal | `validarDestino_destinoExistente_noLanzaExcepcion` |
| `validarDestino` | Límite y negativo | `validarDestino_destinoFueraDeLaListaOCoincidenciaInexacta_lanzaDestinoInvalidoException` ("Bloque Z", "Bloque C " con un espacio y "bloque c" en minúscula) |
| `validarDestino` | Nulo | `validarDestino_destinoNulo_lanzaIllegalArgumentException` (y no `DestinoInvalidoException`) |
| `droneEstaDisponible` | Normal | `droneEstaDisponible_droneDisponible_retornaTrue` |
| `droneEstaDisponible` | Límite y negativo | `droneEstaDisponible_disponibilidadIndependienteDeLaBateria_retornaElValorDelDrone` (disponible con 0% de batería es true; no disponible con 100% es false) |
| `droneEstaDisponible` | Nulo | `droneEstaDisponible_droneNulo_lanzaIllegalArgumentException` |

Son 9 métodos de prueba. Como 3 son parametrizados, JUnit los ejecuta 13 veces en total.

## 3. La secuencia Red, Green, Refactor

Cada fase es un commit, y las pruebas van primero. Resultados medidos ejecutando las pruebas en cada commit:

| Fase | Commit | Resultado |
|------|--------|-----------|
| Preparación | `pom.xml` con JUnit 5 | Compila; todavía no hay pruebas |
| Preparación | Renombre de la clase base de la cadena (ver sección 4) | Compila; las demos del reto 03 dan la misma salida que antes |
| **Red** | Las 9 pruebas solas | Las pruebas no compilan: `ValidadorMision` y `DestinoInvalidoException` no existen (4 errores) |
| **Red** | Clases vacías que lanzan `UnsupportedOperationException` | Compilan; fallan las 13 ejecuciones |
| **Green** | `tieneBateriaSuficiente` | Pasan 4 de 13 ejecuciones (3 de 9 métodos) |
| **Green** | `validarDestino` | Pasan 9 de 13 (6 de 9 métodos) |
| **Green** | `droneEstaDisponible` | Pasan 13 de 13 (9 de 9 métodos) |
| **Refactor** | Reutilizar `ValidadorBateria` y `ValidadorDestino` | Siguen pasando 13 de 13 |

En la fase Green el código es el mínimo, y por eso repite el número 30 y la lista de destinos. Esa duplicación se elimina en el Refactor, no antes.

## 4. Cómo convive con la cadena del reto 03

- **El nombre chocaba.** La clase base abstracta de la cadena del reto 03 ya se llamaba `ValidadorMision`. La renombré a `ValidadorEnCadena` (es el eslabón de la cadena de responsabilidad), en un commit aparte antes de las pruebas, y así `ValidadorMision` queda libre con el significado que pide el reto 12. Actualicé `ValidadorBateria`, `ValidadorDestino`, `ValidadorCarga`, `PatronesApp` y el documento del reto 03. La salida de `PatronesApp` es idéntica antes y después.
- **No se duplica la regla del 30%.** El mínimo vive en `ValidadorBateria.BATERIA_MINIMA`, y `ValidadorBateria.esSuficiente(int)` lo aplica. La cadena y `ValidadorMision` llaman a ese mismo método.
- **No se duplica la regla de destino.** La coincidencia exacta y el mensaje de rechazo viven en `ValidadorDestino` (`esValido` y `mensajeDeRechazo`). `ValidadorMision` crea un `ValidadorDestino` con la lista que recibe por constructor, así que la lista tampoco queda escrita dentro de la clase.
- **Propósito distinto.** La cadena valida una `Mision` completa y devuelve un `ResultadoValidacion`. `ValidadorMision` responde preguntas sueltas sobre un drone o un destino, con `boolean` o con una excepción.

## 5. `DestinoInvalidoException`

Es **no comprobada** (hereda de `IllegalArgumentException`).
- Un destino que no existe es un argumento inválido, y el proyecto ya usa `IllegalArgumentException` con mensajes en español para eso.
- Una excepción comprobada obligaría a todos los llamadores a escribir `try/catch` o `throws`, sin que puedan hacer nada útil con ella.
- Al heredar de `IllegalArgumentException`, cualquier código que ya capture esa excepción sigue funcionando, y el nombre dice qué pasó.
- Un destino nulo no es lo mismo que un destino que no existe: lanza `IllegalArgumentException` y no `DestinoInvalidoException`. Una prueba lo comprueba.

## 6. ¿Las pruebas detectan errores?

Además de que pasen, comprobé que fallan cuando el código se rompe. Con 6 cambios deliberados al código final, las pruebas fallaron en todos:

| Cambio al código | ¿Lo detectan las pruebas? |
|------------------|---------------------------|
| Cambiar `>= 30` por `> 30` | Sí (falla la prueba del 30) |
| Aceptar desde 29% | Sí (falla la prueba del 29) |
| Comparar el destino sin distinguir mayúsculas | Sí |
| Recortar espacios del destino antes de comparar | Sí |
| Hacer que la disponibilidad exija 30% de batería | Sí |
| Quitar la comprobación de drone nulo | Sí |

## 7. Notas

- **Cómo ejecutarlas y resultado:** `mvn test` con el `pom.xml` del repo, o desde IntelliJ. Ejecuté `mvn test` en mi computador y dio `Tests run: 13, Failures: 0, Errors: 0, Skipped: 0` y `BUILD SUCCESS` (JUnit 5.10.2). Antes de eso, las pruebas se habían verificado paso a paso con JUnit 5.10.1 y su consola, porque el entorno donde se prepararon no tenía acceso a Maven Central.
- **Cobertura:** medirla con JaCoCo queda para el reto 13.
- **Datos repetidos:** la lista de destinos válidos sigue escrita en las demos (`PatronesApp`) y en las pruebas, porque se pasa por constructor. No es una regla duplicada en el dominio, pero habría que centralizarla en un solo lugar cuando exista una fuente de destinos.
