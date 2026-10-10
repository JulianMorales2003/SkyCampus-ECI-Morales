package skycampus.enterprise.ruta;

import java.util.List;

/**
 * Componente del patrón Composite: una ruta simple y una ruta compuesta se calculan y se
 * ejecutan exactamente igual desde el cliente.
 */
public interface Ruta {

    String descripcion();

    Punto inicio();

    Punto fin();

    double distanciaKm();

    int duracionMinutos();

    int paradasDeCarga();

    /** Distancia del tramo más largo que habría que volar sin recargar. */
    double tramoMasLargoKm();

    /** Puntos que recorre la ruta en orden, sin repetir dos veces seguidas el mismo punto. */
    List<Punto> puntos();

    ResultadoEjecucion ejecutar(ContextoEjecucion contexto);
}
