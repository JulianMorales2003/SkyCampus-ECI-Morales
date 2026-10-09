# Monferno 14: SonarQube sobre la v2 completa

## Objetivo

Analizar la v2 completa con SonarQube y dejar el Quality Gate en verde con estas condiciones:

| Condición | Umbral |
|---|---|
| Bugs | 0 |
| Vulnerabilidades | 0 |
| Code smells (incluido el código de Strategy y Observer) | 0 |
| Deuda técnica | 30 min o menos |
| Cobertura | 85 % o más |

## Punto de partida

El análisis posterior a las correcciones del reto 13 dejó el proyecto así:

| Métrica | Valor |
|---|---|
| Bugs | 0 |
| Vulnerabilidades | 0 |
| Code smells | 16 |
| Deuda técnica | 109 min |
| Cobertura | 96,3 % |
| Cobertura de ramas | 100 % |

Cobertura y seguridad ya cumplían. Faltaban los 16 code smells, que sumaban 109 minutos de deuda y quedaban por encima del límite de 30 minutos.

## Las 16 issues y cómo se resolvieron

| Regla | Cantidad | Dónde | Corrección |
|---|---|---|---|
| `java:S120` (nombre de paquete) | 5 | `v2.service`, `v2.model`, `v2.asignacion`, `v2.clima`, `v2.eventos` | El primer segmento de un paquete no puede contener dígitos según la expresión `^[a-z_]+(\.[a-z_][a-z0-9_]*)*$`. Los paquetes pasaron a `skycampus.v2.*`, donde `v2` ya es un segundo segmento válido. |
| `java:S5778` (lambda con más de una llamada que puede lanzar excepción) | 7 | `EstrategiaPorPrioridadTest`, `MisionBuilderTest` | Los objetos se construyen antes del `assertThrows`; la lambda queda con una sola invocación (`builder::build`). |
| `java:S1192` (literal duplicado) | 3 | `GestorFlota` (`"ahora"`), `EstadisticasFlota` (`"misiones"`), `SolidApp` | Constantes `PARAMETRO_AHORA` y `PARAMETRO_MISIONES`; en `SolidApp` se usa la constante `ORIGEN` que ya existía. |
| `java:S6126` (concatenación en vez de bloque de texto) | 1 | `GeneradorReporteTest` | Se reemplazó por un text block de Java 21. |

Cambios que no son correcciones directas:

- `GestorFlotaSolidTest` revisa por reflexión que las dependencias de `GestorFlota` sean interfaces. Al agregar constantes `static final String`, ese recorrido las contaba como dependencias. Ahora ignora los campos estáticos, porque una constante no es una dependencia. La verificación sobre los campos de instancia sigue igual.
- El renombre de paquetes es mecánico: mismas clases, mismo comportamiento. Se actualizaron las referencias en `docs/`.

## Quality Gate propio

El Quality Gate por defecto de SonarQube (`Sonar way`) solo evalúa el código nuevo y no exige estas condiciones. Por eso se crea el gate **SkyCampus Monferno**, que evalúa el código completo (Overall Code):

| Métrica de Sonar | Falla si |
|---|---|
| Coverage | menor que 85 % |
| Bugs | mayor que 0 |
| Vulnerabilities | mayor que 0 |
| Code Smells | mayor que 0 |
| Technical Debt (`sqale_index`) | mayor que 30 min |
| Duplicated Lines (%) | mayor que 3 % |

Se crea y se asigna con el script `scripts/configurar-quality-gate.ps1` (usa la API de SonarQube; el token se lee de la variable de entorno `SONAR_TOKEN` y no se guarda en el repositorio). Si el script falla en tu versión de SonarQube, se puede crear a mano en **Quality Gates → Create**, con las mismas condiciones, y asignar al proyecto en **Project Settings → Quality Gate**.

## Gate del build (JaCoCo)

En `pom.xml` los umbrales subieron para igualar el objetivo del reto:

| Contador | Antes (reto 13) | Ahora |
|---|---|---|
| `LINE` | 80 % | 85 % |
| `BRANCH` | 70 % | 85 % |

Medición con JaCoCo 0.8.11 sobre las 241 pruebas, sin contar `app`: líneas 100 % (495 de 495) y ramas 100 % (170 de 170).

## Cómo reproducirlo

```powershell
mvn clean verify
$env:SONAR_TOKEN = "<tu token>"
.\scripts\configurar-quality-gate.ps1
mvn org.sonarsource.scanner.maven:sonar-maven-plugin:sonar "-Dsonar.host.url=http://localhost:9000" "-Dsonar.token=$env:SONAR_TOKEN" "-Dsonar.ws.timeout=600"
```

## Resultado del análisis final

| Métrica | Valor |
|---|---|
| Quality Gate | [PEGA: Passed o Failed] |
| Bugs | [PEGA] |
| Vulnerabilidades | [PEGA] |
| Code smells | [PEGA] |
| Deuda técnica | [PEGA] min |
| Cobertura | [PEGA] % |
| Duplicaciones | [PEGA] % |

Captura del panel: `docs/quality/sonar-v2-final.png`.
