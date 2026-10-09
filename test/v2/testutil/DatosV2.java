package v2.testutil;

import java.time.Instant;
import v2.model.Drone;
import v2.model.EstadoDrone;
import v2.model.EstadoMision;
import v2.model.Mision;
import v2.model.Prioridad;
import v2.model.TipoDrone;

public final class DatosV2 {

    public static final Instant AHORA = Instant.parse("2026-10-09T15:00:00Z");

    private DatosV2() {
    }

    public static Drone drone(String id, TipoDrone tipo) {
        return new Drone(id, tipo, 80, true, EstadoDrone.DISPONIBLE);
    }

    public static Mision mision(String id, Drone drone, EstadoMision estado) {
        return mision(id, drone, estado, Prioridad.NORMAL, AHORA);
    }

    public static Mision mision(String id, Drone drone, EstadoMision estado,
                                Prioridad prioridad, Instant creadaEn) {
        return new Mision(id, drone, "Biblioteca", 300, prioridad, estado, creadaEn);
    }
}
