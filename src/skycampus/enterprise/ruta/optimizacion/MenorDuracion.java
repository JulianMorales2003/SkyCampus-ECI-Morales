package skycampus.enterprise.ruta.optimizacion;

import java.util.Comparator;
import skycampus.enterprise.ruta.Ruta;

/** Prefiere la ruta que menos tarda (vuelo y cargas); desempata por distancia y luego por paradas. */
public class MenorDuracion implements EstrategiaOptimizacionRuta {

    @Override
    public Comparator<Ruta> criterio() {
        return Comparator.comparingInt(Ruta::duracionMinutos)
                .thenComparingDouble(Ruta::distanciaKm)
                .thenComparingInt(Ruta::paradasDeCarga);
    }
}
