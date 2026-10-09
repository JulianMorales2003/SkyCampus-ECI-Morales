package testutil;

import java.time.LocalTime;
import model.Drone;
import model.EstadoMision;
import model.Mision;
import model.TipoCarga;

/** Fábricas de datos de prueba compartidas (no es una prueba, Surefire la ignora). */
public final class Datos {

    private Datos() {
    }

    public static Drone drone(String id, int bateria, boolean disponible, String ubicacion) {
        return new Drone(id, "DJI Mini 3", bateria, disponible, ubicacion);
    }

    public static Drone drone(int bateria) {
        return drone("D-01", bateria, true, "Bloque A");
    }

    public static Mision mision(Drone drone, String destino, TipoCarga carga, EstadoMision estado) {
        return new Mision("M-01", drone, "Bloque A", destino, carga, estado, 3, "", LocalTime.NOON);
    }

    public static Mision mision(Drone drone) {
        return mision(drone, "Biblioteca", TipoCarga.SOBRE, EstadoMision.PENDIENTE);
    }
}
