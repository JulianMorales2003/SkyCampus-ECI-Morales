package skycampus.enterprise.analytics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import skycampus.enterprise.model.Sede;

@DisplayName("EficienciaSede")
class EficienciaSedeTest {

    private static final Sede ECI = new Sede("ECI");

    @Test
    @DisplayName("metricasDerivadas_conDatosValidos_calculaTasaPromedioYPorcentaje")
    void metricasDerivadas_conDatosValidos_calculaTasaPromedioYPorcentaje() {
        EficienciaSede e = new EficienciaSede(ECI, 4, 3, 90, 1, "D-01");

        assertEquals(0.75, e.tasaExito(), 1e-9);
        assertEquals(30.0, e.tiempoPromedioEntregaMinutos().orElseThrow(), 1e-9);
        assertEquals(25.0, e.porcentajeUrgentes(), 1e-9);
    }

    @Test
    @DisplayName("tiempoPromedio_sinEntregas_estaVacio")
    void tiempoPromedio_sinEntregas_estaVacio() {
        assertTrue(new EficienciaSede(ECI, 2, 0, 0, 0, "D-01").tiempoPromedioEntregaMinutos().isEmpty());
    }

    @Test
    @DisplayName("constructor_datosInconsistentes_lanzaExcepcion")
    void constructor_datosInconsistentes_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> new EficienciaSede(null, 1, 1, 1, 0, "D-01"));
        assertThrows(IllegalArgumentException.class, () -> new EficienciaSede(ECI, 1, 1, 1, 0, " "));
        assertThrows(IllegalArgumentException.class, () -> new EficienciaSede(ECI, 0, 0, 0, 0, "D-01"));
        assertThrows(IllegalArgumentException.class, () -> new EficienciaSede(ECI, 2, 3, 1, 0, "D-01"));
        assertThrows(IllegalArgumentException.class, () -> new EficienciaSede(ECI, 2, -1, 1, 0, "D-01"));
        assertThrows(IllegalArgumentException.class, () -> new EficienciaSede(ECI, 2, 1, 1, 3, "D-01"));
        assertThrows(IllegalArgumentException.class, () -> new EficienciaSede(ECI, 2, 1, 1, -1, "D-01"));
        assertThrows(IllegalArgumentException.class, () -> new EficienciaSede(ECI, 2, 1, -1, 0, "D-01"));
    }
}
