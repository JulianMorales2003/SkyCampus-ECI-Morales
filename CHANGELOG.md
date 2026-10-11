# Changelog

Todos los cambios relevantes de SkyCampus se documentan en este archivo.
El formato sigue [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/) y el versionado es [semántico](https://semver.org/lang/es/).

## [Sin publicar]

### Añadido

- Rutas multi-etapa entre sedes con Composite, estrategias de optimización intercambiables (menor distancia, menor duración y menos paradas de carga) y un gestor de misiones que avisa cada etapa (`skycampus.enterprise.ruta`).
- Autorización de la Aerocivil con falla segura y política de vuelo por sede, con radio configurado y radio efectivo.
- Caso de uso `CrearMision`, puerto `CatalogoSedes`, adaptadores en memoria y endpoint `POST /api/v3/misiones`.
- Design tokens por sede (ECI y UNAL) y prototipo navegable del dashboard Enterprise.
- Matriz de trazabilidad de requisitos, protegida por una prueba que falla si desaparece una prueba referenciada.
- Regla de JaCoCo en el build: ningún método puede superar la complejidad ciclomática de 10.

### Cambiado

- `RutaCompuesta` pasó a ser un `record`.
- `scripts/configurar-quality-gate.ps1` recibe el nivel (`-Nivel Monferno` o `-Nivel Infernape`) y `-PerfilComplejidad`.
- Se corrigieron 18 issues de SonarQube sin suprimirlas (deuda técnica de 146 a 0 minutos).

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

[Sin publicar]: https://github.com/JulianMorales2003/SkyCampus-ECI-Morales/compare/v3.0.0...develop
[3.0.0]: https://github.com/JulianMorales2003/SkyCampus-ECI-Morales/releases/tag/v3.0.0
