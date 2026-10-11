# SkyCampus-ECI-Morales

Sistema de drones de reparto de la Escuela Colombiana de Ingeniería. El proyecto creció en tres niveles, y el código y la documentación de cada uno siguen en el repositorio:

| Nivel | Qué es | Dónde está el código |
|---|---|---|
| **Chimchar** | MVP: una sede, registro de misiones y flota básica | Paquetes sin prefijo (`model`, `asignacion`, `service`, `builder`, ...) |
| **Monferno** | v2: tipos de drone, prioridades, asignación automática con Strategy y Observer, alertas | `skycampus.v2.*` |
| **Infernape** | Enterprise: red multi-sede, rutas multi-etapa con autorización de la Aerocivil, creación de misiones por API | `skycampus.enterprise.*` |

## Cómo compilar y probar

Requiere JDK 21 y Maven. El código está en `src/` y las pruebas en `test/`.

```bash
mvn clean verify
```

Ejecuta todas las pruebas y aplica las reglas de calidad del build con JaCoCo: cobertura mínima del 85 % en líneas y en ramas, y ningún método con complejidad ciclomática mayor a 10. El informe queda en `target/site/jacoco/index.html`.

Las demos están en `src/app/` (clases con `main`, se ejecutan desde el IDE). Las demos no se miden en la cobertura.

## Calidad con SonarQube

El análisis y el Quality Gate de cada nivel están explicados en [`docs/MONFERNO14-SONARQUBE-V2.md`](docs/MONFERNO14-SONARQUBE-V2.md) (nivel v2) y en [`docs/INFERNAPE13-QUALITY-GATE-ENTERPRISE.md`](docs/INFERNAPE13-QUALITY-GATE-ENTERPRISE.md) y [`docs/INFERNAPE14-SONARQUBE-ENTERPRISE.md`](docs/INFERNAPE14-SONARQUBE-ENTERPRISE.md) (nivel Enterprise). El gate se crea con:

```powershell
.\scripts\configurar-quality-gate.ps1 -Nivel Infernape -PerfilComplejidad
```

El token de SonarQube se toma de la variable de entorno `SONAR_TOKEN` y nunca se guarda en el repositorio.

## Documentación

Todo lo entregado por reto está en [`docs/`](docs/README.md), con un índice por nivel.

## Flujo de ramas (GitFlow)

- `main`: versión estable. Nunca se hace push directo.
- `develop`: integración. Aquí se hace merge de las ramas feature.
- `feature/MoralesZambrano-*`: una rama por funcionalidad, creada desde `develop` y mergeada con `--no-ff`.
- Los cambios relevantes de cada versión están en [`CHANGELOG.md`](CHANGELOG.md).
