package asignacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import model.Drone;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import testutil.Datos;

class EstrategiaMayorBateriaTest {

    private final EstrategiaMayorBateria estrategia = new EstrategiaMayorBateria();

    @Test
    @DisplayName("seleccionar: elige el drone disponible con más batería")
    void seleccionar_variosDisponibles_eligeElDeMayorBateria() {
        List<Drone> flota = List.of(
                Datos.drone("D-01", 40, true, "Bloque A"),
                Datos.drone("D-02", 90, true, "Bloque B"),
                Datos.drone("D-03", 60, true, "Bloque C"));

        Optional<Drone> elegido = estrategia.seleccionar(flota, "Bloque A");

        assertEquals("D-02", elegido.orElseThrow().id());
    }

    @Test
    @DisplayName("seleccionar: ignora los drones no disponibles aunque tengan más batería")
    void seleccionar_conNoDisponibles_losIgnora() {
        List<Drone> flota = List.of(
                Datos.drone("D-01", 100, false, "Bloque A"),
                Datos.drone("D-02", 50, true, "Bloque B"));

        assertEquals("D-02", estrategia.seleccionar(flota, "Bloque A").orElseThrow().id());
    }

    @Test
    @DisplayName("seleccionar: sin drones disponibles retorna Optional vacío")
    void seleccionar_sinDisponibles_retornaVacio() {
        List<Drone> flota = List.of(Datos.drone("D-01", 100, false, "Bloque A"));

        assertTrue(estrategia.seleccionar(flota, "Bloque A").isEmpty());
    }

    @Test
    @DisplayName("seleccionar: una flota nula lanza IllegalArgumentException")
    void seleccionar_flotaNula_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> estrategia.seleccionar(null, "Bloque A"));
    }
}
