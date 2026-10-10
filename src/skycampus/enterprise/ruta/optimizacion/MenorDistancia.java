package skycampus.enterprise.ruta.optimizacion;

import java.util.Comparator;
import skycampus.enterprise.ruta.Ruta;

/** Prefiere la ruta de menos kilómetros; desempata por menos paradas y luego por menos minutos. */
public class MenorDistancia implements EstrategiaOptimizacionRuta {

    @Override
    public Comparator<Ruta> criterio() {
        return Comparator.comparingDouble(Ruta::distanciaKm)
                .thenComparingInt(Ruta::paradasDeCarga)
                .thenComparingInt(Ruta::duracionMinutos);
    }
}
