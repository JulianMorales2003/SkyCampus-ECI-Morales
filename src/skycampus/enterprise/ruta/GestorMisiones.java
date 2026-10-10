package skycampus.enterprise.ruta;

import java.util.List;
import util.Validaciones;

/**
 * Cliente de las rutas: planifica y ejecuta sin saber si la ruta es simple o compuesta.
 */
public final class GestorMisiones {

    private final PlanificadorRutas planificador;
    private final ContextoEjecucion contexto;

    public GestorMisiones(PlanificadorRutas planificador, ContextoEjecucion contexto) {
        Validaciones.exigirPresente(planificador, "planificador");
        Validaciones.exigirPresente(contexto, "contexto");
        this.planificador = planificador;
        this.contexto = contexto;
    }

    /** Planifica la mejor ruta entre los dos puntos y la ejecuta. */
    public ResultadoEjecucion despachar(Punto origen, Punto destino, List<Punto> estaciones) {
        Ruta ruta = planificador.planificar(origen, destino, estaciones)
                .orElseThrow(() -> new IllegalStateException("No hay una ruta posible entre "
                        + origen.nombre() + " y " + destino.nombre() + " con el alcance de los drones."));
        return ejecutar(ruta);
    }

    /** Ejecuta una ruta ya armada (simple o compuesta) de la misma manera. */
    public ResultadoEjecucion ejecutar(Ruta ruta) {
        Validaciones.exigirPresente(ruta, "ruta");
        return ruta.ejecutar(contexto);
    }
}
