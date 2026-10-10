# Changelog

Todos los cambios relevantes de SkyCampus se documentan en este archivo.
El formato sigue [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/) y el versionado es [semántico](https://semver.org/lang/es/).

## [3.0.0] - 2026-10-09

### Añadido

- Analytics de eficiencia por sede en una sola pasada de Stream: tasa de éxito, tiempo promedio de entrega, drone más utilizado y porcentaje de misiones urgentes, con `Optional` para sedes sin actividad y ranking de sedes (`skycampus.enterprise`).
- Modelo v2 de la flota: tipos de drone, prioridades y misiones con peso (`skycampus.v2.model`).
- Estrategias de asignación intercambiables (mayor batería, batería justa, tipo según paquete y por prioridad) y observadores de eventos de la flota (Strategy y Observer).
- `AsignadorMision` con la API meteorológica como dependencia, desarrollado con TDD y Mockito.
- Quality Gate del build con JaCoCo (85 % en líneas y 85 % en ramas) y análisis con SonarQube.

### Cambiado

- Los paquetes de la v2 pasaron de `v2.*` a `skycampus.v2.*` (regla S120 de Sonar).
- La salida por consola pasa por `util.Consola` (regla S106 de Sonar).

[3.0.0]: https://github.com/JulianMorales2003/SkyCampus-ECI-Morales/releases/tag/v3.0.0
