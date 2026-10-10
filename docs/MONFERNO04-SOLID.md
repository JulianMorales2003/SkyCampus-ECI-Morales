# Monferno · Reto 04 — SOLID: del `GestorDrone` del MVP a la arquitectura v2

Nota de nombres: el enunciado habla de `GestorMisiones` y `ObservadorDrone`. En el código v2 esas piezas se llaman `GestorFlota` y `ObservadorFlota`; son las mismas.

## 1. El punto de partida: `GestorDrone` (según el Reto 04 de Chimchar, `docs/RETO04-SOLID.md`)
Una sola clase que:
- asigna misiones,
- guarda en base de datos (abría la conexión con `DriverManager`, con usuario y contraseña escritos en el código),
- envía alertas por email,
- genera el reporte PDF,
- calcula la ruta con un `if/else` según el tipo.

## 2. Principio por principio

### SRP — una sola razón para cambiar
**(a) Antes.** `GestorDrone` tenía 5 razones para cambiar. Cambiar el servidor de correo obligaba a editar la misma clase que calcula rutas, con riesgo de romper lo que no se quería tocar.

**(b) Ahora.** Cada clase responde a una sola razón:

| Clase | Su única razón para cambiar |
|---|---|
| `EstrategiaMayorBateria`, `EstrategiaTipoSegunPaquete`, `EstrategiaBateriaJusta` | El criterio para elegir drone (una clase por criterio) |
| `SistemaLog` | Cómo se registran los eventos |
| `PanelOperador` | Cómo se muestran al operador |
| `AlertaTecnico` | A quién y cómo se avisa de un fallo |
| `ValidadorBateria`, `ValidadorCarga`, `ValidadorDestino` | Una regla de validación cada uno |
| `GestorFlota` | Coordinar el flujo: pedir un drone a la estrategia, armar la misión y publicar el evento |

Límite honesto: `GestorFlota` sigue construyendo la `Mision` además de coordinar. Es una responsabilidad pequeña y cohesionada, pero si la creación de misiones creciera se extraería a una fábrica.

### OCP — abierto a extensión, cerrado a modificación
**(a) Antes.** `calcularRuta` decidía con un `if/else` por tipo. Cada tipo nuevo obligaba a modificar una clase que ya funcionaba.

**(b) Ahora.** Un criterio de asignación nuevo es una clase nueva que implementa `EstrategiaAsignacion`. Un observador nuevo es una clase que implementa `ObservadorFlota`. `GestorFlota` no cambia en ninguno de los dos casos. Dos pruebas lo demuestran:
- `GestorFlotaSolidTest`: una estrategia escrita dentro de la propia prueba (clase y lambda) funciona sin tocar el gestor.
- `GestorFlotaTest.agregarCuartoObservador_sinTocarElGestor_recibeLosEventos`.

### DIP — depender de abstracciones
**(a) Antes.** La lógica de negocio creaba la conexión MySQL ella misma: dependía de un detalle concreto, y no se podía probar sin base de datos.

**(b) Ahora.** `GestorFlota` recibe por constructor una `EstrategiaAsignacion` (interfaz) y notifica a `ObservadorFlota` (interfaz). No conoce `PanelOperador`, `SistemaLog` ni las estrategias concretas. Los observadores reciben su salida como `Consumer<String>`, no escriben en `System.out` directamente. La prueba `gestorFlota_dependenciasDeclaradas_sonAbstraccionesYNoClasesConcretas` lo verifica por reflexión: el parámetro del constructor y todos los campos de `GestorFlota` son interfaces.

## 3. Resumen de patrones y principios

| Patrón en v2 | Principio | Por qué |
|---|---|---|
| Strategy (`EstrategiaAsignacion`) | OCP | Estrategia nueva = clase nueva; el gestor no cambia |
| Observer (`ObservadorFlota`) | DIP | El gestor depende de la interfaz, no de `PanelOperador` |
| Servicios separados | SRP | El log solo registra, el panel solo muestra, cada estrategia solo elige |
| Cadena de validadores | OCP + SRP | Validador nuevo = clase nueva; una regla por clase |

## 4. La prueba pedida: funciona con cualquier `EstrategiaAsignacion`
`test/v2/service/GestorFlotaSolidTest.java` (16 pruebas):
- Tres estrategias de producción, una clase de prueba (`PrimeroDeLaLista`) y una lambda pasan por las mismas pruebas parametrizadas.
- **Delegación:** una estrategia espía comprueba que el gestor le entrega exactamente la solicitud y la flota recibidas, sin copiarlas ni filtrarlas.
- **Resultado:** la misión que devuelve el gestor lleva exactamente el drone que eligió la estrategia, sea cual sea.
- **Contrato (sustitución de Liskov):** toda estrategia devuelve un drone que pertenece a la flota y está disponible, o vacío.
- **Cambio en ejecución:** un solo gestor alterna entre estrategias y cada una decide a su manera.
- **Sin dependencia concreta:** las dependencias declaradas de `GestorFlota` son interfaces.

Comprobación por mutación: si el gestor ignorara la estrategia que le pasan y usara una fija, fallan 7 pruebas; si guardara la estrategia en una clase concreta, fallan 8; si copiara la flota antes de pasarla, falla 1.

## 5. Lo que no se cubre
- **ISP:** `ObservadorFlota` tiene un solo método y `EstrategiaAsignacion` también, así que ningún implementador carga con métodos que no usa.
- **LSP:** aparte del contrato de la sección 4, no hay jerarquías de herencia en v2 que puedan romperlo.
- `GestorFlota` depende de los records concretos `Mision` y `Drone`. Es aceptable: son datos, no comportamiento que pueda cambiar de implementación.
