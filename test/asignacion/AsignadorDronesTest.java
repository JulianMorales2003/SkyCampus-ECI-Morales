package asignacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Optional;
import model.Drone;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import testutil.Datos;

class AsignadorDronesTest {

    private final List<Drone> flota = List.of(
            Datos.drone("D-01", 95, true, "Bloque B"),
            Datos.drone("D-02", 60, true, "Bloque A"));

    @Test
    @DisplayName("asignar: delega en la estrategia configurada (mayor batería)")
    void asignar_estrategiaMayorBateria_eligeElDeMayorBateria() {
        AsignadorDrones asignador = new AsignadorDrones(new EstrategiaMayorBateria());

        Optional<Drone> elegido = asignador.asignar(flota, "Bloque A");

        assertEquals("D-01", elegido.orElseThrow().id());
    }

    @Test
    @DisplayName("cambiarEstrategia: el asignador usa la nueva estrategia en tiempo de ejecución")
    void cambiarEstrategia_nuevaEstrategia_cambiaElResultado() {
        AsignadorDrones asignador = new AsignadorDrones(new EstrategiaMayorBateria());

        asignador.cambiarEstrategia(new EstrategiaDroneEnOrigen());

        assertEquals("D-02", asignador.asignar(flota, "Bloque A").orElseThrow().id());
    }

    @Test
    @DisplayName("constructor y cambiarEstrategia: una estrategia nula lanza IllegalArgumentException")
    void estrategiaNula_lanzaExcepcion() {
        AsignadorDrones asignador = new AsignadorDrones(new EstrategiaMayorBateria());

        assertThrows(IllegalArgumentException.class, () -> new AsignadorDrones(null));
        assertThrows(IllegalArgumentException.class, () -> asignador.cambiarEstrategia(null));
    }

    @Test
    @DisplayName("asignar: flota nula u origen vacío lanzan IllegalArgumentException")
    void asignar_argumentosInvalidos_lanzaExcepcion() {
        AsignadorDrones asignador = new AsignadorDrones(new EstrategiaMayorBateria());

        assertThrows(IllegalArgumentException.class, () -> asignador.asignar(null, "Bloque A"));
        assertThrows(IllegalArgumentException.class, () -> asignador.asignar(flota, ""));
    }
}
