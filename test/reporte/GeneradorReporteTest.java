package reporte;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalTime;
import java.util.List;
import model.EstadoMision;
import model.Mision;
import model.TipoCarga;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import testutil.Datos;

class GeneradorReporteTest {

    private final GeneradorReporte generador = new GeneradorReporte();

    @Test
    @DisplayName("generar: una lista nula lanza IllegalArgumentException")
    void generar_listaNula_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> generador.generar(null));
    }

    @Test
    @DisplayName("generar: una lista vacía produce el reporte de 'sin misiones'")
    void generar_listaVacia_reporteSinMisiones() {
        assertEquals("REPORTE DE MISIONES\nSin misiones registradas.", generador.generar(List.of()));
    }

    @Test
    @DisplayName("generar: con misiones incluye el total y una línea por misión")
    void generar_conMisiones_incluyeTotalYDetalle() {
        Mision m1 = new Mision("M-1", Datos.drone("D-01", 80, true, "Bloque A"), "Bloque A", "Biblioteca",
                TipoCarga.SOBRE, EstadoMision.PENDIENTE, 3, "", LocalTime.NOON);
        Mision m2 = new Mision("M-2", Datos.drone("D-02", 60, true, "Bloque B"), "Bloque B", "Bloque C",
                TipoCarga.LIBRO, EstadoMision.EN_VUELO, 3, "", LocalTime.NOON);

        String reporte = generador.generar(List.of(m1, m2));

        assertEquals("REPORTE DE MISIONES (2)\n"
                + "M-1 | D-01 | Bloque A -> Biblioteca | SOBRE | PENDIENTE\n"
                + "M-2 | D-02 | Bloque B -> Bloque C | LIBRO | EN_VUELO", reporte);
        assertTrue(reporte.contains("(2)"));
    }
}
