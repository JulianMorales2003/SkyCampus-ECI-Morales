package skycampus.enterprise.ruta.optimizacion;

import java.util.Comparator;
import skycampus.enterprise.ruta.Ruta;

/** Prefiere la ruta con menos paradas de carga; desempata por distancia y luego por minutos. */
public class MenosParadasDeCarga implements EstrategiaOptimizacionRuta {

    @Override
    public Comparator<Ruta> criterio() {
        return Comparator.comparingInt(Ruta::paradasDeCarga)
                .thenComparingDouble(Ruta::distanciaKm)
                .thenComparingInt(Ruta::duracionMinutos);
    }
}
