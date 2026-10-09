package service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import model.Drone;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import testutil.Datos;

class ConsultasFlotaTest {

    private final List<Drone> flota = List.of(
            Datos.drone("D-01", 90, true, "Bloque A"),
            Datos.drone("D-02", 50, true, "Bloque B"),
            Datos.drone("D-03", 49, true, "Bloque A"),
            Datos.drone("D-04", 95, false, "Bloque C"),
            Datos.drone("D-05", 10, true, "Bloque B"));

    @Test
    @DisplayName("idsDisponiblesConBateriaSuficiente: solo disponibles con >=50%, de mayor a menor batería")
    void idsDisponibles_flotaMixta_filtraYOrdenaPorBateriaDescendente() {
        assertEquals(List.of("D-01", "D-02"), ConsultasFlota.idsDisponiblesConBateriaSuficiente(flota));
    }

    @Test
    @DisplayName("hayDisponibleEn: detecta si hay un drone disponible en la ubicación")
    void hayDisponibleEn_ubicaciones_respondeSegunDisponibilidad() {
        assertTrue(ConsultasFlota.hayDisponibleEn(flota, "Bloque A"));
        assertFalse(ConsultasFlota.hayDisponibleEn(flota, "Bloque C"));
        assertFalse(ConsultasFlota.hayDisponibleEn(flota, "Bloque Z"));
    }

    @Test
    @DisplayName("hayDisponibleEn: ubicación nula o en blanco lanza IllegalArgumentException")
    void hayDisponibleEn_ubicacionInvalida_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> ConsultasFlota.hayDisponibleEn(flota, null));
        assertThrows(IllegalArgumentException.class, () -> ConsultasFlota.hayDisponibleEn(flota, " "));
    }

    @Test
    @DisplayName("contarBateriaCritica: cuenta los drones con menos de 20% (el 20 exacto no cuenta)")
    void contarBateriaCritica_bordeEn20_cuentaSoloLosMenores() {
        List<Drone> f = List.of(Datos.drone("A", 19, true, "X"), Datos.drone("B", 20, true, "X"),
                Datos.drone("C", 5, false, "X"));

        assertEquals(2, ConsultasFlota.contarBateriaCritica(f));
    }

    @Test
    @DisplayName("resumenBateria: una línea 'id: n%' por drone")
    void resumenBateria_flota_formateaCadaDrone() {
        assertEquals(List.of("D-01: 90%", "D-02: 50%", "D-03: 49%", "D-04: 95%", "D-05: 10%"),
                ConsultasFlota.resumenBateria(flota));
    }

    @Test
    @DisplayName("todas las consultas: una flota nula lanza IllegalArgumentException")
    void consultas_flotaNula_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> ConsultasFlota.idsDisponiblesConBateriaSuficiente(null));
        assertThrows(IllegalArgumentException.class, () -> ConsultasFlota.hayDisponibleEn(null, "X"));
        assertThrows(IllegalArgumentException.class, () -> ConsultasFlota.contarBateriaCritica(null));
        assertThrows(IllegalArgumentException.class, () -> ConsultasFlota.resumenBateria(null));
    }
}
