package asignacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import model.Drone;
import model.Mision;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import testutil.Datos;

class AsignacionTest {

    @Test
    @DisplayName("constructor: misión y drone coincidentes crean la asignación")
    void constructor_mismoDrone_creaLaAsignacion() {
        Drone drone = Datos.drone(80);
        Mision mision = Datos.mision(drone);

        Asignacion asignacion = new Asignacion(mision, drone);

        assertEquals(mision, asignacion.mision());
        assertEquals(drone, asignacion.drone());
    }

    @Test
    @DisplayName("constructor: misión o drone nulos lanzan IllegalArgumentException")
    void constructor_nulos_lanzaExcepcion() {
        Drone drone = Datos.drone(80);
        Mision mision = Datos.mision(drone);

        assertThrows(IllegalArgumentException.class, () -> new Asignacion(null, drone));
        assertThrows(IllegalArgumentException.class, () -> new Asignacion(mision, null));
    }

    @Test
    @DisplayName("constructor: un drone distinto al de la misión lanza IllegalArgumentException")
    void constructor_droneDistinto_lanzaExcepcion() {
        Mision mision = Datos.mision(Datos.drone("D-01", 80, true, "Bloque A"));
        Drone otro = Datos.drone("D-02", 80, true, "Bloque A");

        assertThrows(IllegalArgumentException.class, () -> new Asignacion(mision, otro));
    }
}
