package skycampus.enterprise.ruta.optimizacion;

import java.util.Comparator;
import java.util.List;
import skycampus.enterprise.ruta.Ruta;
import util.Validaciones;

/**
 * Estrategia del patrón Strategy: cada implementación define qué significa "mejor" ruta.
 * El método {@link #elegir} es común; el criterio es lo que cambia.
 */
public interface EstrategiaOptimizacionRuta {

    /** Orden de preferencia: la ruta menor según este criterio es la elegida. */
    Comparator<Ruta> criterio();

    default Ruta elegir(List<Ruta> candidatas) {
        Validaciones.exigirPresente(candidatas, "candidatas");
        return candidatas.stream()
                .min(criterio())
                .orElseThrow(() -> new IllegalArgumentException("No hay rutas candidatas para elegir."));
    }
}
