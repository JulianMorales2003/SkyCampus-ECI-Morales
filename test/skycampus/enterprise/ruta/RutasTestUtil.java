package skycampus.enterprise.ruta;

import java.util.List;
import skycampus.enterprise.ruta.drone.FabricaDroneEtapa;
import skycampus.enterprise.ruta.evento.NotificadorRuta;
import skycampus.enterprise.ruta.evento.RegistroDeEtapas;

/** Datos compartidos por las pruebas de rutas: un mapa pequeño de la red. */
public final class RutasTestUtil {

    public static final Punto ECI = new Punto("ECI", 0, 0);
    public static final Punto BIBLIOTECA = new Punto("Biblioteca", 2, 0);
    public static final Punto EAFIT = new Punto("EAFIT", 14, 0);
    public static final Punto UNAL = new Punto("UNAL", 40, 0);
    public static final Punto UNIANDES = new Punto("Uniandes", 60, 0);
    public static final Punto ESTACION_NORTE = new Punto("Estación Norte", 20, 5);
    public static final Punto ESTACION_SUR = new Punto("Estación Sur", 20, -12);

    private RutasTestUtil() {
    }

    /** Ruta ECI -> Estación Norte (carga) -> UNAL. */
    public static Ruta eciUnalConCarga() {
        return new RutaCompuesta(List.of(
                new TramoSimple(ECI, ESTACION_NORTE),
                new ParadaDeCarga(ESTACION_NORTE, 20),
                new TramoSimple(ESTACION_NORTE, UNAL)));
    }

    public static ContextoEjecucion contexto(FabricaDroneEtapa fabrica, RegistroDeEtapas registro) {
        NotificadorRuta notificador = new NotificadorRuta();
        notificador.suscribir(registro);
        return new ContextoEjecucion(fabrica, notificador);
    }
}
