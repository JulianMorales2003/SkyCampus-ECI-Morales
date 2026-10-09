package asignacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import model.Drone;
import model.EstadoMision;
import model.Mision;
import model.TipoCarga;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import testutil.Datos;

class AsignadorMisionTest {

    private final AsignadorMision asignador = new AsignadorMision();

    @Test
    @DisplayName("asignar: misión PENDIENTE con drone disponible queda EN_VUELO y el drone reservado")
    void asignar_misionPendienteDroneDisponible_poneEnVueloYReservaElDrone() {
        Drone drone = Datos.drone(80);
        Mision mision = Datos.mision(drone);

        Asignacion resultado = asignador.asignar(mision, drone);

        assertEquals(EstadoMision.EN_VUELO, resultado.mision().estado());
        assertFalse(resultado.drone().disponible());
        assertEquals(drone.id(), resultado.drone().id());
        assertEquals(mision.id(), resultado.mision().id());
        assertEquals(mision.destino(), resultado.mision().destino());
    }

    @Test
    @DisplayName("asignar: no modifica los objetos originales (inmutabilidad)")
    void asignar_exitoso_noMutaLosOriginales() {
        Drone drone = Datos.drone(80);
        Mision mision = Datos.mision(drone);

        asignador.asignar(mision, drone);

        assertEquals(EstadoMision.PENDIENTE, mision.estado());
        assertEquals(true, drone.disponible());
    }

    @Test
    @DisplayName("asignar: misión o drone nulos lanzan IllegalArgumentException")
    void asignar_nulos_lanzaIllegalArgumentException() {
        Drone drone = Datos.drone(80);
        Mision mision = Datos.mision(drone);

        assertThrows(IllegalArgumentException.class, () -> asignador.asignar(null, drone));
        assertThrows(IllegalArgumentException.class, () -> asignador.asignar(mision, null));
    }

    @Test
    @DisplayName("asignar: un drone distinto al de la misión lanza IllegalArgumentException")
    void asignar_droneDistinto_lanzaIllegalArgumentException() {
        Mision mision = Datos.mision(Datos.drone("D-01", 80, true, "Bloque A"));
        Drone otro = Datos.drone("D-02", 80, true, "Bloque A");

        assertThrows(IllegalArgumentException.class, () -> asignador.asignar(mision, otro));
    }

    @Test
    @DisplayName("asignar: una misión que no está PENDIENTE lanza IllegalStateException")
    void asignar_misionNoPendiente_lanzaIllegalStateException() {
        Drone drone = Datos.drone(80);
        Mision enVuelo = Datos.mision(drone, "Biblioteca", TipoCarga.SOBRE, EstadoMision.EN_VUELO);

        assertThrows(IllegalStateException.class, () -> asignador.asignar(enVuelo, drone));
    }

    @Test
    @DisplayName("asignar: un drone no disponible lanza IllegalStateException")
    void asignar_droneNoDisponible_lanzaIllegalStateException() {
        Drone ocupado = Datos.drone("D-01", 80, false, "Bloque A");
        Mision mision = Datos.mision(ocupado);

        assertThrows(IllegalStateException.class, () -> asignador.asignar(mision, ocupado));
    }
}
