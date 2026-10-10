package skycampus.enterprise.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import skycampus.v2.model.EstadoMision;
import skycampus.v2.model.Prioridad;

@DisplayName("Modelo de la red")
class ModeloRedTest {

    private static final Sede ECI = new Sede("ECI");

    @Test
    @DisplayName("sede_nombreVacio_lanzaExcepcion")
    void sede_nombreVacio_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> new Sede(" "));
        assertEquals("ECI", ECI.nombre());
    }

    @Test
    @DisplayName("misionRed_entregadaYUrgente_dependenDeEstadoYPrioridad")
    void misionRed_entregadaYUrgente_dependenDeEstadoYPrioridad() {
        MisionRed entregadaUrgente = new MisionRed("M-1", ECI, "D-01", EstadoMision.COMPLETADA, Prioridad.URGENTE, 10);
        MisionRed fallidaNormal = new MisionRed("M-2", ECI, "D-01", EstadoMision.FALLIDA, Prioridad.NORMAL, 0);

        assertTrue(entregadaUrgente.entregada());
        assertTrue(entregadaUrgente.urgente());
        assertFalse(fallidaNormal.entregada());
        assertFalse(fallidaNormal.urgente());
    }

    @Test
    @DisplayName("misionRed_datosInvalidos_lanzaExcepcion")
    void misionRed_datosInvalidos_lanzaExcepcion() {
        EstadoMision estado = EstadoMision.COMPLETADA;
        Prioridad prioridad = Prioridad.NORMAL;

        assertThrows(IllegalArgumentException.class, () -> new MisionRed("", ECI, "D-01", estado, prioridad, 1));
        assertThrows(IllegalArgumentException.class, () -> new MisionRed("M-1", null, "D-01", estado, prioridad, 1));
        assertThrows(IllegalArgumentException.class, () -> new MisionRed("M-1", ECI, " ", estado, prioridad, 1));
        assertThrows(IllegalArgumentException.class, () -> new MisionRed("M-1", ECI, "D-01", null, prioridad, 1));
        assertThrows(IllegalArgumentException.class, () -> new MisionRed("M-1", ECI, "D-01", estado, null, 1));
        assertThrows(IllegalArgumentException.class, () -> new MisionRed("M-1", ECI, "D-01", estado, prioridad, -1));
    }
}
