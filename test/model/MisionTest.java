package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import testutil.Datos;

class MisionTest {

    private final Drone drone = Datos.drone(80);

    private Mision crear(String id, Drone d, String origen, String destino, TipoCarga carga,
                         EstadoMision estado, int prioridad, String notas, LocalTime hora) {
        return new Mision(id, d, origen, destino, carga, estado, prioridad, notas, hora);
    }

    @Test
    @DisplayName("constructor: con datos válidos conserva todos los campos")
    void constructor_datosValidos_conservaLosCampos() {
        Mision mision = crear("M-1", drone, "Bloque A", "Biblioteca", TipoCarga.LIBRO,
                EstadoMision.PENDIENTE, 4, "frágil", LocalTime.of(10, 30));

        assertEquals("M-1", mision.id());
        assertEquals(drone, mision.drone());
        assertEquals("Biblioteca", mision.destino());
        assertEquals(4, mision.prioridad());
        assertEquals("frágil", mision.notas());
    }

    @ParameterizedTest(name = "prioridad {0} -> válida: {1}")
    @CsvSource({"0,false", "1,true", "3,true", "5,true", "6,false", "-1,false"})
    @DisplayName("esPrioridadValida: el rango válido es 1 a 5")
    void esPrioridadValida_valoresAlrededorDelRango_respetaLimites(int prioridad, boolean esperado) {
        assertEquals(esperado, Mision.esPrioridadValida(prioridad));
    }

    @ParameterizedTest(name = "prioridad {0} -> IllegalArgumentException")
    @ValueSource(ints = {0, 6})
    @DisplayName("constructor: una prioridad fuera de rango lanza IllegalArgumentException")
    void constructor_prioridadFueraDeRango_lanzaExcepcion(int prioridad) {
        assertThrows(IllegalArgumentException.class, () -> crear("M-1", drone, "A", "B",
                TipoCarga.SOBRE, EstadoMision.PENDIENTE, prioridad, "", LocalTime.NOON));
    }

    @ParameterizedTest(name = "id \"{0}\" -> IllegalArgumentException")
    @ValueSource(strings = {"", "  "})
    @DisplayName("constructor: un id vacío o en blanco lanza IllegalArgumentException")
    void constructor_idVacio_lanzaExcepcion(String id) {
        assertThrows(IllegalArgumentException.class, () -> crear(id, drone, "A", "B",
                TipoCarga.SOBRE, EstadoMision.PENDIENTE, 3, "", LocalTime.NOON));
    }

    @Test
    @DisplayName("constructor: cada campo obligatorio nulo lanza IllegalArgumentException")
    void constructor_camposNulos_lanzanExcepcion() {
        LocalTime h = LocalTime.NOON;
        assertThrows(IllegalArgumentException.class, () -> crear(null, drone, "A", "B", TipoCarga.SOBRE, EstadoMision.PENDIENTE, 3, "", h));
        assertThrows(IllegalArgumentException.class, () -> crear("M", null, "A", "B", TipoCarga.SOBRE, EstadoMision.PENDIENTE, 3, "", h));
        assertThrows(IllegalArgumentException.class, () -> crear("M", drone, null, "B", TipoCarga.SOBRE, EstadoMision.PENDIENTE, 3, "", h));
        assertThrows(IllegalArgumentException.class, () -> crear("M", drone, "A", null, TipoCarga.SOBRE, EstadoMision.PENDIENTE, 3, "", h));
        assertThrows(IllegalArgumentException.class, () -> crear("M", drone, "A", "B", null, EstadoMision.PENDIENTE, 3, "", h));
        assertThrows(IllegalArgumentException.class, () -> crear("M", drone, "A", "B", TipoCarga.SOBRE, null, 3, "", h));
        assertThrows(IllegalArgumentException.class, () -> crear("M", drone, "A", "B", TipoCarga.SOBRE, EstadoMision.PENDIENTE, 3, null, h));
        assertThrows(IllegalArgumentException.class, () -> crear("M", drone, "A", "B", TipoCarga.SOBRE, EstadoMision.PENDIENTE, 3, "", null));
    }

    @Test
    @DisplayName("constantes: la prioridad por defecto es válida y SIN_HORA_LIMITE es el fin del día")
    void constantes_valoresPorDefecto_sonCoherentes() {
        assertTrue(Mision.esPrioridadValida(Mision.PRIORIDAD_POR_DEFECTO));
        assertFalse(Mision.SIN_HORA_LIMITE.isBefore(LocalTime.of(23, 59)));
    }
}
