# Monferno · Reto 06 — RF, RNF y MoSCoW de SkyCampus v2

Continúa el documento del MVP ([`RETO06-RF-RNF-MOSCOW.md`](RETO06-RF-RNF-MOSCOW.md), que tiene RF-01 a RF-03 y RNF-01 a RNF-03). El enunciado numera la v2 desde RF-07, así que RF-04 a RF-06 quedan sin usar. RNF-03 (80 % de cobertura y 0 Blocker o Critical) sigue valiendo para la v2.

Son 5 RF y no 4: el enunciado trae RF-07 y RF-08 como dos requisitos de la misma funcionalidad (asignación automática), y los necesito separados para la tensión de la sección 4. Las cuatro funcionalidades pedidas quedan cubiertas: asignación automática (RF-07 y RF-08), alertas de fallo (RF-09), consulta meteorológica (RF-10) y gestión por técnico (RF-11).

## 1. Requerimientos funcionales de la v2

- **RF-07.** El sistema debe asignar automáticamente el drone de mayor batería disponible para cualquier misión.
  - *Resultado observable:* con D-01 (95 %) y D-02 (40 %) disponibles, la misión queda EN_VUELO con D-01. Si hay empate de batería, gana el id menor. Si no hay ningún drone disponible, no se crea la misión y el sistema avisa "sin drone disponible".
- **RF-08.** Las misiones URGENTES tienen prioridad absoluta: deben recibir el drone más rápido disponible, independientemente de la batería.
  - *Resultado observable:* una misión URGENTE recibe un drone de tipo EXPRESS aunque haya otro con más batería. *(Ver la tensión con RF-07 y el texto corregido en la sección 4.)*
- **RF-09.** Cuando un drone entra en estado FALLO, el sistema debe notificarlo al técnico de mantenimiento por el Sistema de Alertas y mostrarlo en el panel del operador, con el id del drone y la hora del fallo.
  - *Resultado observable:* tras reportar el fallo de D-01, el panel muestra el aviso, el registro guarda el evento y se envía una alerta técnica, una sola por fallo.
- **RF-10.** Antes de lanzar una misión, el sistema debe consultar la API Meteorológica y no lanzarla si el viento supera el límite configurado o hay lluvia.
  - *Resultado observable:* con viento de 35 km/h y límite de 30 km/h, la misión no sale y el Operador ve el motivo "clima no apto". Con el viento por debajo del límite y sin lluvia, la misión sale normal.
  - *Supuesto:* el enunciado dice "viento fuerte" sin cifra. Propongo 30 km/h como valor inicial configurable; hay que confirmarlo con quien opere los drones.
- **RF-11.** El sistema debe permitir al Técnico de mantenimiento ver los drones en estado FALLO y registrar su reparación para devolverlos a la flota.
  - *Resultado observable:* D-01 en FALLO aparece en la lista del técnico; al marcarlo como reparado pasa a DISPONIBLE y vuelve a ser candidato para RF-07.

## 2. Requerimientos no funcionales de la v2

| Código | Atributo | Requerimiento | Condición de medición | Cómo se verifica | Estado |
|---|---|---|---|---|---|
| RNF-04 | Rendimiento | La asignación automática (RF-07) debe seleccionar el drone en menos de 500 ms. | Con una flota de hasta 50 drones, con cada una de las tres estrategias | `assertTimeout(Duration.ofMillis(500), …)` en JUnit 5 | Verificado: `RequisitosV2Test.rnf04_…` |
| RNF-05 | Confiabilidad | Todo evento de fallo (RF-09) debe llegar a todos los observadores suscritos, sin perder ninguno. | 100 fallos seguidos con 3 observadores, en menos de 1 segundo en total | JUnit 5: contadores por observador y `assertTimeout(Duration.ofSeconds(1), …)` | Verificado: `RequisitosV2Test.rnf05_…` |
| RNF-06 | Seguridad operativa | Si la API Meteorológica no responde en 3 segundos o devuelve error, el sistema no debe lanzar la misión y debe mostrar "clima no disponible" al Operador (falla segura). | Con un doble de prueba que simula timeout y error, en 20 de 20 intentos | Prueba automatizada con el doble de la API | Pendiente: la integración con el clima aún no existe |
| RNF-07 | Usabilidad | Un técnico sin capacitación previa debe poder registrar una reparación (RF-11) en menos de 1 minuto, sin ayuda. | Con 3 personas que no conozcan el sistema, usando el panel del técnico | Prueba con cronómetro; las 3 personas deben terminar a tiempo | Pendiente: el panel del técnico aún no existe |

Todos los RNF de esta tabla tienen un número, una condición y una forma de verificarlo. Ninguno usa palabras como "rápido" o "fácil" sin cifra.

## 3. Priorización MoSCoW

| Código | Requerimiento | Categoría | Justificación |
|---|---|---|---|
| RF-07 | Asignación automática por mayor batería | Must Have | Es el núcleo de la v2: sin ella el Operador sigue eligiendo a mano y la v2 no aporta lo que la distingue del MVP. |
| RF-08 | Prioridad absoluta para urgentes | Should Have | Si falta, las urgentes se atienden con la misma regla que las demás: más lento para ellas, pero el sistema sigue entregando. |
| RF-09 | Alerta de fallo al técnico | Must Have | Si falta, un drone averiado puede quedar sin atender, y se descubriría con el drone ya parado o perdido. Es un tema de seguridad. |
| RF-10 | Consulta meteorológica antes de lanzar | Should Have | Si falta, el Operador puede mirar el clima por su cuenta antes de lanzar; es riesgoso pero hay alternativa manual. |
| RF-11 | Gestión de reparaciones por el técnico | Could Have | Si falta, el Admin puede devolver el drone a la flota con la configuración que ya existe (flujo 5 del diagrama), aunque con más pasos. |
| RNF-04 | Asignación en menos de 500 ms | Should Have | Con una flota que hoy ronda las 5 unidades, aunque tarde más el sistema sigue funcionando; importa al crecer la flota. |
| RNF-05 | Ningún evento de fallo se pierde | Must Have | Es lo que hace confiable el RF-09: de nada sirve la alerta si a veces no llega. |
| RNF-06 | Falla segura si el clima no responde | Should Have | Va unido al RF-10: es Should porque el RF-10 lo es; si el RF-10 pasara a Must, este también. |
| RNF-07 | Registro de reparación en menos de 1 minuto | Could Have | Si falta, el técnico tarda más en reportar, pero la reparación en sí no se detiene. |

**Won't Have en esta versión:** el control autónomo multi-sede (corresponde al nivel Infernape). Los flujos 10 y 11 del diagrama de contexto (registro y autorización del Control Aéreo) tampoco tienen un RF en este reto; quedan para cuando se pida esa integración.

## 4. La tensión entre RF-07 y RF-08

### Dónde choca
RF-07 dice "cualquier misión" y eso incluye las URGENTES. RF-08 dice que, para las urgentes, importa el drone más rápido "independientemente de la batería". Para una misión urgente las dos reglas se aplican a la vez y pueden elegir drones distintos. Ejemplo con D-01 (MINI, 95 %) y D-15 (EXPRESS, 45 %): RF-07 elige D-01 y RF-08 elige D-15. La prueba `RequisitosV2Test.rf07YRf08_misionUrgente_lasDosReglasEligenDronesDistintos` lo demuestra con las estrategias actuales.

### Tres problemas escondidos dentro de RF-08
1. **"Más rápido" no está definido.** El modelo v2 no tiene velocidad ni ubicación del drone. Hoy el código usa el tipo EXPRESS como sustituto de "rápido". Habrá que decidir si "rápido" es el tipo de drone o el tiempo estimado de llegada.
2. **"Independientemente de la batería" es inseguro.** Probado con el código actual: con D-01 (MINI, 95 %) y D-15 (EXPRESS, 5 %), una misión urgente se asigna a D-15 con 5 % de batería. Un drone así no completa la ruta.
3. **Si no hay EXPRESS disponible, RF-08 no dice qué pasa.** El código actual devuelve "sin drone" aunque haya un MINI libre, y la misión urgente se queda esperando.

### Cómo resolverlo (propuesta, a confirmar con quien opere los drones)
**Regla de precedencia:** RF-08 es un caso particular de RF-07. Para las misiones URGENTES manda RF-08; para el resto, RF-07. Así no hay dos reglas peleando por la misma misión.

**Texto corregido:**
- **RF-07.** El sistema debe asignar automáticamente el drone de mayor batería disponible para cualquier misión **que no sea URGENTE**.
- **RF-08.** Las misiones URGENTES tienen prioridad absoluta: deben recibir el drone más rápido disponible **cuya batería supere el mínimo seguro (30 %, configurable)**. Si ningún drone rápido cumple ese mínimo, se asigna el de mayor batería (regla de RF-07) y se avisa al Operador de que la misión urgente no pudo atenderse con el más rápido.

**Por qué esta salida:** respeta la intención de RF-08 (las urgentes van primero y con el drone rápido) sin renunciar a que el drone llegue. El mínimo de 30 % coincide con el que ya usa `EstrategiaBateriaJusta` por defecto. Además, el MoSCoW da el mismo orden: RF-07 es Must y RF-08 es Should, así que si una urgente no se puede atender con el criterio rápido, cae en el Must y no se queda sin drone.

### Lo que implicaría en el código (no se hace en este reto)
- Una estrategia que elija según la prioridad: URGENTE con la regla de RF-08, el resto con `EstrategiaMayorBateria`.
- Ajustar `EstrategiaTipoSegunPaquete` para que exija la batería mínima y tenga el respaldo descrito arriba.
- Pruebas nuevas para los dos casos: urgente con EXPRESS sin batería suficiente, y urgente sin EXPRESS disponible.

## 5. Pruebas que respaldan este documento
`test/v2/service/RequisitosV2Test.java` (3 pruebas): RNF-04, RNF-05 y la demostración de la tensión. Se comprobaron con mutaciones manuales: una estrategia que tarda 700 ms hace fallar RNF-04, un gestor que solo avisa al primer observador hace fallar 5 pruebas (entre ellas RNF-05), y una estrategia que ignora la urgencia hace fallar 3.
