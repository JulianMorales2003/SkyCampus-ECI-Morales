# Infernape 06: matriz de trazabilidad de SkyCampus Enterprise (RF sin ambigüedades ni contradicciones)

## Qué se pide

Construir la matriz de trazabilidad de SkyCampus Enterprise con 8 RF y 4 RNF: para cada RF su código, nombre, MoSCoW, caso de uso, historia de usuario en Jira y la prueba que lo valida; detectar y resolver la tensión entre RF-12 y RNF-09 y cualquier otra contradicción; y demostrar que ningún RF queda sin caso de uso ni sin prueba.

## 1. Numeración

Continúa los documentos anteriores: el MVP tiene RF-01 a RF-03 y RNF-01 a RNF-03 ([`RETO06-RF-RNF-MOSCOW.md`](RETO06-RF-RNF-MOSCOW.md)), y la v2 tiene RF-07 a RF-11 y RNF-04 a RNF-07 ([`MONFERNO06-RF-RNF-V2.md`](MONFERNO06-RF-RNF-V2.md)). Enterprise sigue desde ahí: **RF-12 a RF-19** y **RNF-08 a RNF-11**, que es lo que el enunciado da por hecho al hablar de RF-12 y RNF-09. Los casos de uso siguen desde SC-07 (SC-08 a SC-14) y las historias desde HU-8 (HU-9 a HU-16).

## 2. Requerimientos (texto final, ya sin contradicciones)

### Funcionales
- **RF-12.** El coordinador de cada sede puede configurar el radio máximo de vuelo de los drones de su sede, **dentro de los límites que fijan el superadmin (RF-13) y la Aerocivil (RNF-09)**. Si pide un radio mayor, el sistema lo guarda, informa cuál radio rige y aplica siempre el menor.
  - *Resultado observable:* con la Aerocivil en 5 km, el coordinador pide 15 km; el sistema responde "configurado 15 km, efectivo 5 km" y un vuelo de 8 km se rechaza.
- **RF-13.** El superadmin define el techo de radio de toda la red. Ningún coordinador puede superarlo y ninguno de los dos puede superar a la Aerocivil.
  - *Resultado observable:* con el techo de la red en 10 km y un coordinador en 15 km, rige 10 km.
- **RF-14.** El sistema planifica la ruta entre sedes, directa o con parada de carga, según un criterio elegible (menor distancia, menor duración o menos paradas).
  - *Resultado observable:* si el destino queda más lejos que el alcance del drone, la ruta pasa por la estación de carga más corta.
- **RF-15.** El sistema asigna a cada misión **que no sea urgente** el drone con más batería disponible en la sede de origen. *(Ver C-04.)*
  - *Resultado observable:* con drones de 50 % y 90 % en la sede de origen y clima apto, se asigna el de 90 %; los de menos de 30 % no se consideran.
- **RF-16.** Ningún vuelo entre sedes despega sin la autorización de la Aerocivil, **ni siquiera los urgentes**. *(Ver C-03.)*
  - *Resultado observable:* con la Aerocivil rechazando, el vuelo no sale aunque sea urgente.
- **RF-17.** El superadmin ve el ranking de eficiencia de las sedes de la red.
  - *Resultado observable:* las sedes salen ordenadas por tasa de éxito, luego por entregas y luego por nombre.
- **RF-18.** El sistema avisa el inicio, la finalización y el fallo de cada etapa de una ruta.
  - *Resultado observable:* una ruta de 3 etapas publica 3 avisos de inicio y 3 de finalización en orden; si una etapa falla, los observadores reciben la alerta y la ruta se detiene.
- **RF-19.** El coordinador consulta la eficiencia de su propia sede.
  - *Resultado observable:* tasa de éxito, tiempo promedio de las entregas, misiones urgentes y drone más utilizado de su sede; una sede sin misiones queda sin métricas en vez de en cero.

### No funcionales
| Código | Atributo | Requerimiento | Condición de medición | Cómo se verifica | Estado |
|---|---|---|---|---|---|
| RNF-08 | Rendimiento | Planificar una ruta entre sedes tarda menos de 500 ms. | Con 20 estaciones de carga y cada uno de los 3 criterios | `assertTimeout(Duration.ofMillis(500), …)` | Verificado: `RequisitosEnterpriseTest#rnf08_…` |
| RNF-09 | Cumplimiento normativo | Las rutas entre sedes respetan el espacio aéreo de la Aerocivil y ningún drone vuela a más de 120 m en zona urbana. | Ningún plan con altura mayor que el límite o distancia mayor que el radio efectivo se autoriza, aunque el coordinador haya configurado otra cosa | Pruebas de `AutorizadorVuelo` y `PoliticaVuelo`, incluidos los límites exactos (120 m, radio exacto) | Verificado en dominio y aplicación; el adaptador real de la Aerocivil no existe |
| RNF-10 | Seguridad operativa | Si la Aerocivil no responde en 3 segundos o devuelve un error, el vuelo no despega (falla segura). | Con un doble de la Aerocivil que lanza error, en 20 de 20 intentos | Mockito `thenThrow` | Verificado con el doble; el límite de 3 s lo tendrá el adaptador HTTP, que aún no existe |
| RNF-11 | Mantenibilidad | El dominio no depende de ningún framework ni librería externa. | Los `import` del dominio son solo `java.*` y su propio paquete | `ArquitecturaCapasTest` (lee los `import`) | Verificado |

Todos los RNF tienen número, condición y forma de verificación; ninguno usa "rápido" o "seguro" sin cifra.

## 3. Casos de uso

| Código | Caso de uso | Actor | Requisitos |
|---|---|---|---|
| SC-08 | Configurar el radio máximo de vuelo de una sede | Coordinador de sede | RF-12, RNF-09 |
| SC-09 | Definir el techo de radio de la red | Superadmin | RF-13 |
| SC-10 | Planificar una ruta entre sedes | Operador de drones | RF-14, RNF-08 |
| SC-11 | Asignar drone de la sede de origen a una misión | Sistema (lo dispara una solicitud) | RF-15, RNF-11 |
| SC-12 | Autorizar un vuelo entre sedes | Sistema (lo dispara una misión) | RF-16, RNF-09, RNF-10, RNF-11 |
| SC-13 | Consultar la eficiencia de la red o de una sede | Superadmin y Coordinador de sede | RF-17, RF-19 |
| SC-14 | Seguir las etapas de una ruta | Operador de drones | RF-18 |

## 4. Historias de usuario

Una por RF, de HU-9 a HU-16. Los textos para crearlas en Jira están en `JIRA-INFERNAPE06-COPIAR.md`.

| Historia | Texto | Requerimiento |
|---|---|---|
| HU-9 | Como Coordinador de sede, quiero configurar el radio máximo de vuelo de los drones de mi sede, para adaptar la operación a mi sede sin salirme de lo permitido. | RF-12 |
| HU-10 | Como Superadmin, quiero fijar el radio máximo de toda la red, para que ninguna sede supere el alcance que la organización considera seguro. | RF-13 |
| HU-11 | Como Operador de drones, quiero que el sistema planifique la ruta entre sedes, directa o con parada de carga, para llegar a la sede destino sin calcularla a mano. | RF-14 |
| HU-12 | Como Operador de drones, quiero que cada misión no urgente se asigne al drone con más batería de la sede de origen, para no comparar la flota a mano. | RF-15 |
| HU-13 | Como Operador de drones, quiero que un vuelo entre sedes solo salga cuando la Aerocivil lo autorice, para volar siempre dentro de la norma. | RF-16 |
| HU-14 | Como Superadmin, quiero ver el ranking de eficiencia de las sedes, para saber cuáles rinden mejor y cuáles necesitan apoyo. | RF-17 |
| HU-15 | Como Operador de drones, quiero que el sistema avise cada etapa de la ruta, para enterarme enseguida de que una falla. | RF-18 |
| HU-16 | Como Coordinador de sede, quiero consultar la eficiencia de mi sede, para saber cómo va frente al resto de la red. | RF-19 |

Los RNF no tienen historia propia: son criterios de aceptación de las historias que acotan (RNF-08 en HU-11, RNF-09 y RNF-10 en HU-13 y HU-9, RNF-11 en HU-12 y HU-13).

## 5. Priorización MoSCoW

| Código | Categoría | Justificación (qué pasa si falta) |
|---|---|---|
| RF-12 | Should | Si falta, el radio lo fija solo el superadmin para toda la red: menos ajuste por sede, pero la red opera. |
| RF-13 | Should | Si falta, rigen la Aerocivil y lo que configure cada coordinador; sigue siendo seguro porque RNF-09 manda sobre los dos. |
| RF-14 | Must | Sin planificación de rutas entre sedes no hay red: es el motivo del nivel Enterprise. |
| RF-15 | Must | Si falta, el operador vuelve a elegir el drone a mano en cada misión. |
| RF-16 | Must | Es un requisito legal y de seguridad: sin él no se puede volar entre sedes. |
| RF-17 | Could | Si falta, los datos siguen en las misiones; solo se pierde el panel de comparación. |
| RF-18 | Should | Si falta, la ruta se ejecuta igual pero un fallo de etapa se descubre tarde. |
| RF-19 | Could | Si falta, el coordinador puede pedirle los datos de su sede al superadmin. |
| RNF-08 | Should | Con la red actual (4 sedes) aunque tarde más el sistema funciona; importa al crecer. |
| RNF-09 | Must | Es la norma de la Aerocivil: no se negocia. |
| RNF-10 | Must | Va unido a RF-16: de nada sirve exigir la autorización si, ante un error, el sistema vuela de todos modos. |
| RNF-11 | Should | Si falta, el sistema funciona pero cada cambio de tecnología obliga a tocar las reglas. |

**Won't Have en esta versión:** los flujos del diagrama de contexto con el ERP universitario (19 y 20) y con la Plataforma de Analytics (21) no tienen RF en este reto; se quedan para cuando se pida esa integración.

**Coherencia entre prioridades:** ningún Must depende de un Should. RF-16 (Must) depende de RNF-09 y RNF-10 (Must). RF-12 (Should) está acotado por RNF-09 (Must), que es lo correcto: lo opcional se limita por lo obligatorio, no al revés.

## 6. La matriz de trazabilidad

| Código | Nombre | MoSCoW | Caso de uso | HU (Jira) | Prueba que lo valida | Conflicto |
|---|---|---|---|---|---|---|
| RF-12 | El coordinador de cada sede puede configurar el radio máximo de vuelo de los drones de su sede | Should | SC-08 | HU-9 [PEGA: clave] | `PoliticaVueloTest#configurarRadio_pedidoMayorQueLaAerocivil_rigeElLimiteDeLaAerocivil`<br>`PoliticaVueloTest#configurarRadio_pedidoDentroDeLosLimites_rigeElPedido` | C-01, C-02 |
| RF-13 | El superadmin define el techo de radio de toda la red | Should | SC-09 | HU-10 [PEGA: clave] | `PoliticaVueloTest#definirTechoRed_menorQueElRadioDelCoordinador_rigeElTecho` | C-02 |
| RF-14 | El sistema planifica la ruta entre sedes, directa o con parada de carga, según un criterio elegible | Must | SC-10 | HU-11 [PEGA: clave] | `PlanificadorYGestorTest#planificar_destinoMasLejosQueElAlcance_usaLaEstacionMasCorta`<br>`RutaCompuestaTest#rutaCompuesta_anidada_tieneTresNivelesYSeComportaComoUnaSola` | - |
| RF-15 | El sistema asigna a cada misión que no sea urgente el drone con más batería disponible en la sede de origen | Must | SC-11 | HU-12 [PEGA: clave] | `AsignadorMisionTest#asigna_conClimaApto_eligeMayorBateria`<br>`AsignadorMisionTest#asigna_consultaLaSedeDeOrigen` | C-04 |
| RF-16 | Ningún vuelo entre sedes despega sin la autorización de la Aerocivil | Must | SC-12 | HU-13 [PEGA: clave] | `AutorizadorVueloTest#decidir_planValidoYAerocivilAutoriza_autoriza`<br>`AutorizadorVueloTest#decidir_urgenteONo_siemprePideLaAutorizacionDeLaAerocivil` | C-03 |
| RF-17 | El superadmin ve el ranking de eficiencia de las sedes de la red | Could | SC-13 | HU-14 [PEGA: clave] | `AnalyticsRedTest#ranking_escenarios_ordenaPorTasaLuegoEntregadasLuegoNombre` | - |
| RF-18 | El sistema avisa el inicio, la finalización y el fallo de cada etapa de una ruta | Should | SC-14 | HU-15 [PEGA: clave] | `RutaCompuestaTest#ejecutar_rutaCompuesta_avisaIniciadaYCompletadaDeCadaEtapaEnOrden`<br>`PlanificadorYGestorTest#ejecutar_etapaFallida_losObservadoresRecibenLaAlertaSinCambiarLaRuta` | - |
| RF-19 | El coordinador consulta la eficiencia de su propia sede | Could | SC-13 | HU-16 [PEGA: clave] | `AnalyticsRedTest#eficienciaPorSede_escenarios_calculaLasCuatroMetricasPorSede` | - |
| RNF-08 | Planificar una ruta entre sedes con 20 estaciones tarda menos de 500 ms | Should | SC-10 | HU-11 [PEGA: clave] | `RequisitosEnterpriseTest#rnf08_planificarConVeinteEstaciones_tardaMenosDe500ms` | - |
| RNF-09 | Las rutas entre sedes respetan el espacio aéreo de la Aerocivil y ningún drone vuela a más de 120 m en zona urbana | Must | SC-12, SC-08 | HU-13 [PEGA: clave]<br>HU-9 [PEGA: clave] | `AutorizadorVueloTest#decidir_alturaSobre120EnZonaUrbana_rechaza`<br>`AutorizadorVueloTest#decidir_radioConfiguradoPorElCoordinadorQueViolaLaAerocivil_seRechaza`<br>`PoliticaVueloTest#radioEfectivo_laAerocivilReduceElLimiteDespues_seSigueCumpliendo` | C-01, C-03 |
| RNF-10 | Si la Aerocivil no responde o falla, el vuelo no despega | Must | SC-12 | HU-13 [PEGA: clave] | `AutorizadorVueloTest#decidir_aerocivilFalla_noSeDespega` | - |
| RNF-11 | El dominio no depende de ningún framework ni librería externa | Should | SC-11, SC-12 | HU-12 [PEGA: clave]<br>HU-13 [PEGA: clave] | `ArquitecturaCapasTest#dominio_noImportaFrameworksNiLibrerias` | - |

Los datos están también en [`trazabilidad/matriz-enterprise.csv`](trazabilidad/matriz-enterprise.csv) y [`trazabilidad/casos-de-uso-enterprise.csv`](trazabilidad/casos-de-uso-enterprise.csv).

### Cómo se demuestra que ningún RF queda sin caso de uso ni sin prueba
`MatrizTrazabilidadTest` lee esos dos archivos en cada `mvn verify` y falla si:
- falta alguno de los 8 RF (RF-12 a RF-19) o de los 4 RNF (RNF-08 a RNF-11), o hay uno repetido;
- un requisito no tiene caso de uso, o el caso de uso no existe en el catálogo;
- un requisito no tiene historia (`HU-n`) o tiene un MoSCoW inválido;
- un requisito no tiene prueba, o la prueba que dice tener **no existe de verdad**: se comprueba por reflexión que la clase existe y que el método está anotado como prueba;
- un caso de uso queda huérfano (ningún requisito lo usa).

La prueba `verificador_detectaLosProblemas` alimenta al verificador con una matriz con esos errores y comprueba que los detecta todos, para que la regla no pase en vacío. Si mañana alguien borra o renombra una prueba, la matriz deja de cumplirse y el build falla.

## 7. Contradicciones detectadas y cómo se resolvieron

### C-01: RF-12 contra RNF-09 (la del enunciado)
**Dónde chocan.** RF-12 dice que el coordinador configura el radio de su sede, sin tope. RNF-09 dice que la Aerocivil define restricciones que no se pueden ignorar. Si el coordinador pide un radio mayor que lo que la Aerocivil permite, las dos reglas se aplican a la vez y se contradicen: una dice "se puede", la otra "no".

**Cómo se resuelve.** Una regla de precedencia: **RNF-09 manda sobre RF-12**. El coordinador configura, pero siempre dentro del límite de la Aerocivil. El texto de RF-12 se corrigió (sección 2) para decirlo.

**Por qué se guarda lo que pidió el coordinador y no se rechaza.** Hay dos formas de hacer cumplir el RNF. Una es rechazar la configuración cuando viola el límite. La otra, la que se eligió, es guardar lo pedido, informar cuál radio rige (`ConfiguracionRadio` con `configuradoKm`, `efectivoKm` y `ajustado()`) y aplicar el menor cada vez que se consulta. Se eligió la segunda porque la Aerocivil puede cambiar sus restricciones **después** de que el coordinador configuró: si el límite solo se comprobara al configurar, un cambio posterior se ignoraría y el sistema volvería a incumplir. La prueba `radioEfectivo_laAerocivilReduceElLimiteDespues_seSigueCumpliendo` lo demuestra, y `decidir_radioConfiguradoPorElCoordinadorQueViolaLaAerocivil_seRechaza` comprueba que un vuelo de 8 km se rechaza con el coordinador en 15 km y la Aerocivil en 5.

**Qué incluye RNF-09 en el modelo.** El enunciado dice "espacio aéreo controlado" sin forma ni cifras, salvo los 120 m en zona urbana. Lo modelé como un radio máximo y una altura máxima por sede que fija la Aerocivil (`RestriccionAerea`); en zona urbana la altura nunca pasa de 120 m aunque la Aerocivil permita más. Las zonas prohibidas con forma geométrica concreta no están modeladas.

### C-02: RF-12 contra RF-13 (dos autoridades para el mismo número)
**Dónde chocan.** Una política de vuelo de la red (RF-13) y la configuración de cada sede (RF-12) tocan el mismo parámetro, el radio. Sin una regla, no se sabe cuál gana si el superadmin dice 10 km y el coordinador 15.

**Cómo se resuelve.** Jerarquía fija de tres niveles: **Aerocivil > superadmin > coordinador**. El radio que rige es el menor de los tres. Está en el texto de RF-12 y RF-13, y la prueba `definirTechoRed_menorQueElRadioDelCoordinador_rigeElTecho` lo comprueba: con la Aerocivil en 30, el coordinador en 15 y el techo de la red en 10, rige 10.

### C-03: RF-08 de la v2 contra RF-16 y RNF-09 (urgente contra autorización)
**Dónde chocan.** RF-08 de la v2 da "prioridad absoluta" a las misiones urgentes. Si "absoluta" se leyera como "se salta lo que haga falta", una misión urgente entre sedes podría despegar sin la autorización de la Aerocivil o a más de 120 m, que es exactamente lo que RNF-09 prohíbe.

**Cómo se resuelve.** La urgencia solo decide **qué drone se asigna y en qué orden se atiende**; nunca exime de los límites de seguridad ni de la autorización. RF-16 lo dice expresamente ("ni siquiera los urgentes"). `PlanVuelo` lleva el campo `urgente` justamente para poder demostrarlo: las pruebas `decidir_urgenteONo_losLimitesDeSeguridadSeAplicanIgual` y `decidir_urgenteONo_siemprePideLaAutorizacionDeLaAerocivil` dan el mismo resultado con `urgente` verdadero y falso, y un mutante que deja pasar los urgentes hace fallar 2 pruebas.

### C-04: RF-15 contra RF-08 de la v2 (la misma tensión de la v2, repetida)
**Dónde chocan.** RF-15, tal como se redactó primero ("asigna a cada misión el drone con más batería"), repite el problema que la v2 ya resolvió entre RF-07 y RF-08: "cada misión" incluye las urgentes, para las que RF-08 pide el drone más rápido, no el de más batería.

**Cómo se resuelve.** Se hereda la solución de la v2: RF-15 aplica a las misiones **que no sean urgentes** (texto corregido en la sección 2).

**Lo que queda pendiente.** El código Enterprise todavía no distingue urgencia: `SolicitudEntrega` no tiene prioridad, así que el `AsignadorMision` de Enterprise trata todo igual. Como la estrategia es una dependencia inyectada (`EstrategiaAsignacion`), el cambio es añadir la prioridad a la solicitud y una estrategia para urgentes, sin tocar el asignador. No se hace en este reto.

### Revisión de coherencia del resto
Se revisaron las demás parejas y no hay más contradicciones: RNF-10 (falla segura) es coherente con RF-16 (los dos son Must y dicen lo mismo desde dos ángulos), RNF-08 mide RF-14 sin pedir nada incompatible, y RF-17 y RF-19 usan la misma operación (`eficienciaPorSede`) para actores distintos, sin pisarse.

## 8. Qué está construido y qué no

| Requisito | Estado |
|---|---|
| RF-12, RF-13, RF-16, RNF-09, RNF-10 | Código nuevo de este reto: `PoliticaVuelo`, `RestriccionAerea`, `ConfiguracionRadio`, `PlanVuelo`, `DecisionVuelo`, `MotivoRechazo`, el puerto `ServicioAerocivil` y el caso de uso `AutorizadorVuelo`. La Aerocivil se prueba con un doble de Mockito; **no existe aún el adaptador HTTP real**, que será quien aplique el límite de 3 s de RNF-10. |
| RF-14, RNF-08 | Rutas de Infernape 03; RNF-08 se mide con prueba nueva. |
| RF-15, RNF-11 | `AsignadorMision` y arquitectura por capas de Infernape 04. |
| RF-17, RF-18, RF-19 | Analytics (Infernape 01) y eventos de ruta (Infernape 03). |
| Interfaz de usuario | Ningún RF tiene todavía pantalla: el coordinador y el superadmin se modelan en las reglas, no en una interfaz. |

## 9. Verificación

- 39 pruebas nuevas: `PoliticaVueloTest`, `AutorizadorVueloTest`, `RequisitosEnterpriseTest` y `MatrizTrazabilidadTest`.
- Cobertura del código nuevo: 100 % de líneas y de ramas (medida en mi entorno; la oficial es la de `mvn verify`).
- Mutación manual: 21 mutantes sobre el código nuevo (por ejemplo cambiar `min` por el valor del coordinador, `>` por `>=` en la altura y el radio, dejar pasar a los urgentes, invertir la respuesta de la Aerocivil), los 21 detectados.
