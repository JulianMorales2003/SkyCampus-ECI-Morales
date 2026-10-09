# Monferno · Reto 02 — Conflictos de merge en el equipo de SkyCampus v2
Estudiante: Julian Felipe Morales Zambrano

## Escenario
Dos funcionalidades salen del mismo commit de `develop` y modifican `src/asignacion/AsignadorMision.java`:
- **Asignación automática** (rol de Juan), rama `feature/MoralesZambrano-asignacion-automatica`: agrega `asignarAutomaticamente(...)`, que elige el drone disponible con más batería y lo asigna.
- **Alertas** (rol de María), rama `feature/MoralesZambrano-alertas-asignacion`: agrega `asignarYNotificar(...)`, que asigna y avisa al operador.

Las dos ramas agregan código en los mismos dos lugares del archivo: el bloque de `import` y el final de la clase. Por eso Git no puede combinarlas solo.

## Integración
La integración se hizo en `feature/MoralesZambrano-monferno-02-integracion`, creada desde el mismo commit base:
1. `git merge --no-ff feature/MoralesZambrano-asignacion-automatica` → sin conflicto (es la primera en entrar).
2. `git merge --no-ff feature/MoralesZambrano-alertas-asignacion` → **conflicto en `AsignadorMision.java`**, en dos zonas.

## El conflicto
| Zona | Lado `HEAD` (asignación automática) | Lado de la otra rama (alertas) |
|------|-------------------------------------|--------------------------------|
| Imports | `Comparator`, `List`, `Optional` | `alerta.AlertaOperador` |
| Final de la clase | `asignarAutomaticamente` y `misionDelDrone` | `asignarYNotificar` |

## Resolución
Se conservaron **ambos** cambios, sin borrar nada de ninguno:
- Imports: `alerta.AlertaOperador` primero, luego los tres de `java.util`, luego los de `model`, en orden alfabético.
- Final de la clase: los dos métodos nuevos, uno tras otro.
- Se eliminaron las marcas `<<<<<<<`, `=======` y `>>>>>>>`.

Verificación: el resultado se contrastó con una versión de referencia del archivo resuelto, no quedaron marcas de conflicto, compila y pasan las pruebas de ambas ramas juntas (`AsignadorMisionAutomaticoTest` y `AsignadorMisionNotificacionTest`): `mvn clean verify` dio 130 pruebas, 0 fallos y `BUILD SUCCESS`.

## Uso de `git stash`
Mientras se escribía la asignación automática se necesitó revisar `develop` sin perder el trabajo sin commit:
```
git stash
Saved working directory and index state WIP on feature/MoralesZambrano-asignacion-automatica: c9c635a merge: integra feature/MoralesZambrano-sonarqube en develop (reto 14 aprobado)

git status -sb
## feature/MoralesZambrano-asignacion-automatica

git checkout develop
Switched to branch 'develop'
Your branch is up to date with 'origin/develop'.

git checkout feature/MoralesZambrano-asignacion-automatica
Switched to branch 'feature/MoralesZambrano-asignacion-automatica'

git stash pop
On branch feature/MoralesZambrano-asignacion-automatica
Changes not staged for commit:
        modified:   src/asignacion/AsignadorMision.java
Dropped refs/stash@{0} (dd4837f96a7290968720b9633302b09035f7195d)
```

## Historial: `git log --oneline --graph --all --decorate`
```
*   5262a7d (HEAD -> feature/MoralesZambrano-monferno-02-integracion) merge: integra asignacion automatica y sistema de alertas
|\
| * e102c34 (origin/feature/MoralesZambrano-alertas-asignacion, feature/MoralesZambrano-alertas-asignacion) test: agrega pruebas de asignar y notificar
| * c142e24 feat: agrega asignarYNotificar que asigna la mision y avisa al operador
* |   d78aef8 merge: integra la asignacion automatica en la rama de integracion
|\ \
| |/
|/|
| * 68a7000 (origin/feature/MoralesZambrano-asignacion-automatica, feature/MoralesZambrano-asignacion-automatica) test: agrega pruebas de la asignacion automatica
| * 6c75b56 feat: agrega asignarAutomaticamente que elige el drone disponible con mas bateria
|/
*   c9c635a (origin/develop, develop) merge: integra feature/MoralesZambrano-sonarqube en develop (reto 14 aprobado)
```

## ¿Merge o rebase?
Se usó **merge** porque las dos ramas ya están publicadas en el repositorio compartido: preserva la historia real, con las dos ramas convergiendo, y no reescribe commits que otras personas pueden haber descargado. `rebase` sería válido solo en ramas locales que aún no se han publicado.
