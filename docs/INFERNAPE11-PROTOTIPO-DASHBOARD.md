# Infernape · Reto 11 — Prototipo navegable del dashboard Enterprise multi-sede

**Prototipo:** [`prototipo/dashboard-enterprise.html`](prototipo/dashboard-enterprise.html) (un solo archivo; se abre con doble clic en el navegador, sin servidor ni instalación). Capturas en [`prototipo/capturas/`](prototipo/capturas/).

## 1. Mock contra prototipo
Un mock (Chimchar) es una imagen: muestra cómo se vería. Este es un **prototipo**: tiene datos que cambian solos, clics, transiciones y todos los estados, de modo que un usuario recorre el flujo completo sin que nadie se lo explique.

## 2. Cómo cumple los 4 puntos del reto
| # | Se pide | Cómo se resuelve |
|---|---|---|
| 1 | Red de 4 sedes con la flota en tiempo real | Pestaña **Red de sedes**: ECI, UNAL, Uniandes y EAFIT con barra de estados, batería media y clima. Cada 2,5 s los drones cambian de estado y batería (indicador «En vivo», con botón **Pausar** y hora de la última actualización). Al elegir una sede se ve el detalle de sus drones. |
| 2 | Dos identidades (ECI y UNAL) solo cambiando tokens | El HTML **no define colores, fuentes ni radios**: usa `var(--color-primary)`, `--font-ui`, `--border-radius` de [`diseno/temas/eci.css`](diseno/temas/eci.css) y [`unal.css`](diseno/temas/unal.css) (reto 08). El selector **ECI / UNAL** solo cambia `data-sede` en `<html>`. Ver capturas `01` y `02`. |
| 3 | Planificación de ruta inter-sede con todos los estados de error | Pestaña **Planificar ruta**: 4 pasos (Ruta → Opción → Autorización → Vuelo). Estados en la sección 3. |
| 4 | Prueba con un compañero sin instrucciones | Protocolo y formato en la sección 5. **La prueba con una persona real queda por hacer** (ver 5). |

## 3. Estados del flujo de ruta (todos alcanzables y verificados)
| Estado | Cómo se llega | Qué ve el usuario y cómo sale |
|---|---|---|
| Faltan datos | Sin elegir origen o destino | Botón desactivado y texto «Elige origen y destino» |
| Origen = destino | Elegir la misma sede | Aviso en rojo junto al campo, botón desactivado (prevención del error) |
| Planificando | Pulsar «Planificar ruta» | Barra de progreso, texto de qué se revisa y botón **Cancelar** |
| Clima no apto | Origen UNAL (viento 38 km/h > 30) | Mensaje con la causa; «Volver a intentar» o «Elegir otra sede de origen» |
| Sin drone apto | Origen EAFIT (todos en carga/mantenimiento o < 30 %) | Mensaje con la causa; «Elegir otra sede» o «Ver drones de EAFIT» |
| Ruta directa / con parada | ECI→UNAL / ECI→EAFIT | Resumen en km, minutos y tramos; cambio de criterio con un clic |
| Aerocivil rechaza | ECI→EAFIT con «Menor duración» (31,4 km > 30 km) | Causa con cifras y botón **«Usar Menor distancia»** que lo corrige en un clic |
| Aerocivil no responde | Casilla de prueba: a los 3 s | Falla segura: el vuelo no despega; **Reintentar** |
| Etapa del vuelo falla | Casilla de prueba | La ruta se detiene, se avisa y se ofrece replanificar |
| Vuelo cancelado | Botón «Cancelar vuelo» durante el vuelo | Confirmación del estado y salida a planificar otra |
| Vuelo completado | Seguir el flujo feliz | Resumen y «Planificar otra ruta» |

Los datos (distancias entre sedes, estados de la flota, límites de 30 km, 30 km/h de viento y 30 % de batería) son de demostración; los límites siguen las reglas de RF-12, RF-15, RF-16, RNF-09 y RNF-10 del documento de trazabilidad.

## 4. Revisión contra las 10 heurísticas de Nielsen (hecha por quien diseñó el prototipo)
| Heurística | Dónde se ve |
|---|---|
| 1. Visibilidad del estado | «En vivo/Pausado» con hora, indicador de pasos, barras de progreso, etapa «en curso» |
| 2. Lenguaje del usuario | «Pedir autorización a la Aerocivil», sin códigos ni jerga |
| 3. Control y libertad | Cancelar en la planificación, la autorización y el vuelo; «Atrás» en cada paso |
| 4. Consistencia | Mismos componentes en ambas identidades; mismo patrón de error en todos los estados |
| 5. Prevención de errores | Botón desactivado con origen = destino; el drone y el clima se revisan antes de ofrecer la ruta |
| 6. Reconocer antes que recordar | Lista de sedes con nombre, criterios con explicación, distancia visible |
| 7. Flexibilidad | «Planificar ruta desde esta sede» precarga el origen |
| 8. Diseño minimalista | Un paso por pantalla; las condiciones de prueba van plegadas |
| 9. Ayuda con los errores | Cada error dice la **causa con cifras** y ofrece una **salida con botón** |
| 10. Ayuda y documentación | Texto de apoyo bajo cada campo y criterio; no hay manual porque no debería hacer falta |

Accesibilidad: el estado nunca depende solo del color (siempre ícono + texto), los errores usan `role="alert"` y llevan el foco, los cambios se anuncian en una región `aria-live`, controles de 44 px, foco visible y animaciones que respetan «reducir movimiento».

Esto es una revisión **experta**, no la prueba con usuario: confirma el diseño, pero no sustituye ver a alguien usarlo.

## 5. Prueba con un compañero (a completar)
**Protocolo** (10 min): 1) Un compañero que no haya visto el proyecto abre el HTML. 2) Se le da solo esta frase: *«Eres operador. Manda un drone de la ECI a EAFIT».* 3) Quien observa **no explica nada**, no señala ni responde; solo anota. 4) Cada vez que el compañero pregunta algo, duda más de 5 s o se equivoca, es un problema de usabilidad. 5) Al final, 3 preguntas: ¿qué fue lo más confuso?, ¿qué esperabas que pasara y no pasó?, ¿lo usarías sin manual?

| Dato | Resultado |
|---|---|
| Fecha / persona / identidad probada | _por completar_ |
| ¿Terminó la tarea sin ayuda? (sí/no) y tiempo | _por completar_ |
| Preguntas que hizo (literal) | _por completar_ |
| Heurística de Nielsen vulnerada por cada pregunta | _por completar_ |
| Cambio hecho en el prototipo | _por completar_ |
| ¿Se repitió la prueba tras el cambio? | _por completar_ |

**Dónde puede salir una duda** (hipótesis a validar, no resultados): que «Condiciones de prueba» confunda (heurística 8), que no se entienda por qué ECI→EAFIT con «Menor duración» es rechazado (9), o que el botón «Cancelar vuelo» no se note (3).

## 6. Verificación técnica realizada
Recorrido automatizado con un navegador real (Chromium): **13 comprobaciones, 13 correctas** y cero errores de JavaScript: 4 sedes visibles, cambio de tema (color primario `#00457C` → `#7B0000`), origen = destino, clima no apto, sin drone, ruta directa, ruta con parada, rechazo de la Aerocivil y su corrección, autorización, fallo de etapa, vuelo completo y falla segura de la Aerocivil. Las capturas de `prototipo/capturas/` salen de ese recorrido.

## 7. Límites
- Es un prototipo: los datos son simulados y no se conecta con el código Java.
- Las sedes UNAL y ECI son las dos identidades pedidas; Uniandes y EAFIT usan el mismo mecanismo (basta enlazar su `temas/*.css`).
- No existe aún el adaptador real de la Aerocivil; el rechazo y el límite de 3 s están simulados.
