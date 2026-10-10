package skycampus.enterprise.dominio;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Registros del dominio")
class ModeloDominioTest {

    private static final String DETALLE = "detalle";

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  "})
    @DisplayName("Drone exige id y sede con texto")
    void drone_textoInvalido_lanzaExcepcion(String texto) {
        assertThrows(IllegalArgumentException.class, () -> new Drone(texto, "ECI", 50));
        assertThrows(IllegalArgumentException.class, () -> new Drone("D-1", texto, 50));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 101})
    @DisplayName("Drone rechaza baterías fuera de 0..100")
    void drone_bateriaFueraDeRango_lanzaExcepcion(int bateria) {
        assertThrows(IllegalArgumentException.class, () -> new Drone("D-1", "ECI", bateria));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 100})
    @DisplayName("Drone acepta los límites 0 y 100")
    void drone_limitesValidos(int bateria) {
        assertDoesNotThrow(() -> new Drone("D-1", "ECI", bateria));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  "})
    @DisplayName("SolicitudEntrega exige id, origen y destino con texto")
    void solicitud_textoInvalido_lanzaExcepcion(String texto) {
        assertThrows(IllegalArgumentException.class, () -> new SolicitudEntrega(texto, "ECI", "UNAL"));
        assertThrows(IllegalArgumentException.class, () -> new SolicitudEntrega("M-1", texto, "UNAL"));
        assertThrows(IllegalArgumentException.class, () -> new SolicitudEntrega("M-1", "ECI", texto));
    }

    @Test
    @DisplayName("SolicitudEntrega exige origen distinto del destino")
    void solicitud_mismoOrigenYDestino_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> new SolicitudEntrega("M-1", "ECI", "ECI"));
    }

    @Test
    @DisplayName("EventoAsignacion exige tipo e id de solicitud")
    void evento_datosInvalidos_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> new EventoAsignacion(null, "M-1", DETALLE));
        assertThrows(IllegalArgumentException.class,
                () -> new EventoAsignacion(TipoEventoAsignacion.MISION_ASIGNADA, null, DETALLE));
        assertThrows(IllegalArgumentException.class,
                () -> new EventoAsignacion(TipoEventoAsignacion.MISION_ASIGNADA, " ", DETALLE));
    }
}
