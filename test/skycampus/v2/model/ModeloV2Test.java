package skycampus.v2.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Modelo v2: Drone y Mision")
class ModeloV2Test {

    private static final Drone DRONE = new Drone("D-01", TipoDrone.MINI, 80, true, EstadoDrone.DISPONIBLE);
    private static final Instant CREADA = Instant.parse("2026-10-09T15:00:00Z");

    @Test
    @DisplayName("drone_bateriaMayorACien_lanzaIllegalArgumentException")
    void drone_bateriaMayorACien_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> new Drone("D-01", TipoDrone.MINI, 101, true, EstadoDrone.DISPONIBLE));
    }

    @Test
    @DisplayName("drone_bateriaNegativa_lanzaIllegalArgumentException")
    void drone_bateriaNegativa_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> new Drone("D-01", TipoDrone.MINI, -1, true, EstadoDrone.DISPONIBLE));
    }

    @Test
    @DisplayName("drone_tipoNulo_lanzaIllegalArgumentExceptionConElNombreDelCampo")
    void drone_tipoNulo_lanzaIllegalArgumentExceptionConElNombreDelCampo() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> new Drone("D-01", null, 50, true, EstadoDrone.DISPONIBLE));
        assertEquals("El campo 'tipo' no puede ser nulo.", error.getMessage());
    }

    @Test
    @DisplayName("mision_pesoCero_lanzaIllegalArgumentException")
    void mision_pesoCero_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> new Mision("M-1", DRONE, "Biblioteca", 0, Prioridad.NORMAL, EstadoMision.PENDIENTE, CREADA));
    }

    @Test
    @DisplayName("mision_creadaEnNula_lanzaIllegalArgumentExceptionConElNombreDelCampo")
    void mision_creadaEnNula_lanzaIllegalArgumentExceptionConElNombreDelCampo() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> new Mision("M-1", DRONE, "Biblioteca", 300, Prioridad.NORMAL, EstadoMision.PENDIENTE, null));
        assertEquals("El campo 'creadaEn' no puede ser nulo.", error.getMessage());
    }

    @Test
    @DisplayName("mision_datosValidos_creaLaMisionConLosValoresRecibidos")
    void mision_datosValidos_creaLaMisionConLosValoresRecibidos() {
        Mision mision = new Mision("M-1", DRONE, "Biblioteca", 300, Prioridad.URGENTE, EstadoMision.PENDIENTE, CREADA);

        assertEquals(Prioridad.URGENTE, mision.prioridad());
        assertEquals(300, mision.pesoPaqueteGramos());
    }
}
