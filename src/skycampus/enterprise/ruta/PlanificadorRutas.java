package skycampus.enterprise.ruta;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import skycampus.enterprise.ruta.optimizacion.EstrategiaOptimizacionRuta;
import util.Validaciones;

/**
 * Contexto del Strategy: arma las rutas posibles (directa o con una parada de carga) y deja que la
 * estrategia elija. Descarta las que tengan un tramo más largo que el alcance máximo.
 */
public final class PlanificadorRutas {

    public static final int MINUTOS_DE_CARGA = 20;

    private final EstrategiaOptimizacionRuta estrategia;
    private final double alcanceMaximoKm;

    public PlanificadorRutas(EstrategiaOptimizacionRuta estrategia, double alcanceMaximoKm) {
        Validaciones.exigirPresente(estrategia, "estrategia");
        if (alcanceMaximoKm <= 0) {
            throw new IllegalArgumentException("El alcance máximo debe ser mayor que 0 km.");
        }
        this.estrategia = estrategia;
        this.alcanceMaximoKm = alcanceMaximoKm;
    }

    /** Devuelve la mejor ruta según la estrategia, o vacío si ninguna se puede volar con el alcance dado. */
    public Optional<Ruta> planificar(Punto origen, Punto destino, List<Punto> estaciones) {
        Validaciones.exigirPresente(origen, "origen");
        Validaciones.exigirPresente(destino, "destino");
        Validaciones.exigirPresente(estaciones, "estaciones");
        if (origen.equals(destino)) {
            throw new IllegalArgumentException("El origen y el destino no pueden ser el mismo punto.");
        }

        Stream<Ruta> directa = Stream.of(new TramoSimple(origen, destino));
        Stream<Ruta> conCarga = estaciones.stream()
                .filter(estacion -> !estacion.equals(origen) && !estacion.equals(destino))
                .map(estacion -> viaEstacion(origen, destino, estacion));

        List<Ruta> candidatas = Stream.concat(directa, conCarga)
                .filter(ruta -> ruta.tramoMasLargoKm() <= alcanceMaximoKm)
                .toList();

        if (candidatas.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(estrategia.elegir(candidatas));
    }

    private static Ruta viaEstacion(Punto origen, Punto destino, Punto estacion) {
        return new RutaCompuesta(List.of(
                new TramoSimple(origen, estacion),
                new ParadaDeCarga(estacion, MINUTOS_DE_CARGA),
                new TramoSimple(estacion, destino)));
    }
}
