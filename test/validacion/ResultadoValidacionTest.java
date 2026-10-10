package validacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ResultadoValidacionTest {

    @Test
    @DisplayName("aprobada: es válida y su motivo está vacío")
    void aprobada_resultado_esValidoConMotivoVacio() {
        ResultadoValidacion r = ResultadoValidacion.aprobada();

        assertTrue(r.valido());
        assertEquals("", r.motivo());
    }

    @Test
    @DisplayName("rechazada: no es válida y conserva el motivo")
    void rechazada_conMotivo_noEsValidaYConservaElMotivo() {
        ResultadoValidacion r = ResultadoValidacion.rechazada("Batería baja");

        assertFalse(r.valido());
        assertEquals("Batería baja", r.motivo());
    }

    @Test
    @DisplayName("constructor: un motivo nulo lanza IllegalArgumentException")
    void constructor_motivoNulo_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> new ResultadoValidacion(true, null));
    }

    @Test
    @DisplayName("constructor: un rechazo sin motivo (en blanco) lanza IllegalArgumentException")
    void constructor_rechazoSinMotivo_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> ResultadoValidacion.rechazada("  "));
    }
}
