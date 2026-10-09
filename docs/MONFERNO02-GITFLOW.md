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

Verificación: compila y pasan las pruebas de ambas ramas juntas (`AsignadorMisionAutomaticoTest` y `AsignadorMisionNotificacionTest`).

## Uso de `git stash`
Mientras se escribía la asignación automática se necesitó revisar `develop` sin perder el trabajo sin commit:
```
[PEGA AQUÍ la salida de git stash, git checkout develop, git checkout <rama> y git stash pop]
```

## Historial: `git log --oneline --graph --all --decorate`
```
[PEGA AQUÍ la salida real]
```

## ¿Merge o rebase?
Se usó **merge** porque las dos ramas ya están publicadas en el repositorio compartido: preserva la historia real, con las dos ramas convergiendo, y no reescribe commits que otras personas pueden haber descargado. `rebase` sería válido solo en ramas locales que aún no se han publicado.
