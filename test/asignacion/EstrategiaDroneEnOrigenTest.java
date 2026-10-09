package asignacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import model.Drone;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import testutil.Datos;

class EstrategiaDroneEnOrigenTest {

    private final EstrategiaDroneEnOrigen estrategia = new EstrategiaDroneEnOrigen();

    @Test
    @DisplayName("seleccionar: elige, entre los disponibles en el origen, el de más batería")
    void seleccionar_variosEnOrigen_eligeElDeMayorBateria() {
        List<Drone> flota = List.of(
                Datos.drone("D-01", 40, true, "Bloque A"),
                Datos.drone("D-02", 70, true, "Bloque A"),
                Datos.drone("D-03", 99, true, "Bloque B"));

        assertEquals("D-02", estrategia.seleccionar(flota, "Bloque A").orElseThrow().id());
    }

    @Test
    @DisplayName("seleccionar: ignora drones no disponibles en el origen")
    void seleccionar_noDisponibleEnOrigen_loIgnora() {
        List<Drone> flota = List.of(
                Datos.drone("D-01", 90, false, "Bloque A"),
                Datos.drone("D-02", 30, true, "Bloque A"));

        assertEquals("D-02", estrategia.seleccionar(flota, "Bloque A").orElseThrow().id());
    }

    @Test
    @DisplayName("seleccionar: sin drones en el origen retorna Optional vacío")
    void seleccionar_ningunoEnOrigen_retornaVacio() {
        List<Drone> flota = List.of(Datos.drone("D-01", 90, true, "Bloque B"));

        assertTrue(estrategia.seleccionar(flota, "Bloque A").isEmpty());
    }

    @Test
    @DisplayName("seleccionar: flota nula u origen vacío lanzan IllegalArgumentException")
    void seleccionar_argumentosInvalidos_lanzaExcepcion() {
        List<Drone> flota = List.of(Datos.drone(80));

        assertThrows(IllegalArgumentException.class, () -> estrategia.seleccionar(null, "Bloque A"));
        assertThrows(IllegalArgumentException.class, () -> estrategia.seleccionar(flota, " "));
        assertThrows(IllegalArgumentException.class, () -> estrategia.seleccionar(flota, null));
    }
}
