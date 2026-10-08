package validacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import model.Drone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class ValidadorMisionTest {

    private static final List<String> DESTINOS_VALIDOS =
            List.of("Bloque A", "Bloque B", "Bloque C", "Bloque D", "Biblioteca");

    private ValidadorMision validador;

    @BeforeEach
    void crearValidador() {
        validador = new ValidadorMision(DESTINOS_VALIDOS);
    }

    private static Drone crearDrone(int bateria, boolean disponible) {
        return new Drone("D-01", "DJI Mini 3", bateria, disponible, "Bloque A");
    }

    // ---------- tieneBateriaSuficiente ----------

    @Test
    @DisplayName("tieneBateriaSuficiente: un drone con 85% de batería tiene batería suficiente")
    void tieneBateriaSuficiente_bateriaDel85PorCiento_retornaTrue() {
        // Arrange
        Drone drone = crearDrone(85, true);

        // Act
        boolean resultado = validador.tieneBateriaSuficiente(drone);

        // Assert
        assertTrue(resultado);
    }

    @ParameterizedTest(name = "batería {0}% -> suficiente: {1}")
    @CsvSource({"30,true", "29,false"})
    @DisplayName("tieneBateriaSuficiente: el mínimo es 30% (30 alcanza, 29 no)")
    void tieneBateriaSuficiente_bateriaAlrededorDelLimite_respetaElMinimoDel30PorCiento(
            int bateria, boolean esperado) {
        // Arrange
        Drone drone = crearDrone(bateria, true);

        // Act
        boolean resultado = validador.tieneBateriaSuficiente(drone);

        // Assert
        assertEquals(esperado, resultado);
    }

    @Test
    @DisplayName("tieneBateriaSuficiente: un drone nulo lanza IllegalArgumentException")
    void tieneBateriaSuficiente_droneNulo_lanzaIllegalArgumentException() {
        // Arrange
        Drone drone = null;

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> validador.tieneBateriaSuficiente(drone));
    }

    // ---------- validarDestino ----------

    @Test
    @DisplayName("validarDestino: un destino de la lista no lanza excepción")
    void validarDestino_destinoExistente_noLanzaExcepcion() {
        // Arrange
        String destino = "Bloque C";

        // Act + Assert
        assertDoesNotThrow(() -> validador.validarDestino(destino));
    }

    @ParameterizedTest(name = "destino \"{0}\" -> DestinoInvalidoException")
    @ValueSource(strings = {"Bloque Z", "Bloque C ", "bloque c"})
    @DisplayName("validarDestino: un destino que no coincide exactamente con ninguno de la lista lanza DestinoInvalidoException")
    void validarDestino_destinoSinCoincidenciaExacta_lanzaDestinoInvalidoException(
            String destino) {
        // Arrange (el destino llega por parámetro)

        // Act + Assert
        assertThrows(DestinoInvalidoException.class, () -> validador.validarDestino(destino));
    }

    @Test
    @DisplayName("validarDestino: un destino nulo lanza IllegalArgumentException y no DestinoInvalidoException")
    void validarDestino_destinoNulo_lanzaIllegalArgumentException() {
        // Arrange
        String destino = null;

        // Act
        IllegalArgumentException excepcion =
                assertThrows(IllegalArgumentException.class, () -> validador.validarDestino(destino));

        // Assert
        assertFalse(excepcion instanceof DestinoInvalidoException);
    }

    // ---------- droneEstaDisponible ----------

    @Test
    @DisplayName("droneEstaDisponible: un drone disponible retorna true")
    void droneEstaDisponible_droneDisponible_retornaTrue() {
        // Arrange
        Drone drone = crearDrone(85, true);

        // Act
        boolean resultado = validador.droneEstaDisponible(drone);

        // Assert
        assertTrue(resultado);
    }

    @ParameterizedTest(name = "disponible: {0}, batería {1}% -> {2}")
    @CsvSource({"true,0,true", "false,100,false"})
    @DisplayName("droneEstaDisponible: la disponibilidad no depende de la batería")
    void droneEstaDisponible_disponibilidadIndependienteDeLaBateria_retornaElValorDelDrone(
            boolean disponible, int bateria, boolean esperado) {
        // Arrange
        Drone drone = crearDrone(bateria, disponible);

        // Act
        boolean resultado = validador.droneEstaDisponible(drone);

        // Assert
        assertEquals(esperado, resultado);
    }

    @Test
    @DisplayName("droneEstaDisponible: un drone nulo lanza IllegalArgumentException")
    void droneEstaDisponible_droneNulo_lanzaIllegalArgumentException() {
        // Arrange
        Drone drone = null;

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> validador.droneEstaDisponible(drone));
    }
}
