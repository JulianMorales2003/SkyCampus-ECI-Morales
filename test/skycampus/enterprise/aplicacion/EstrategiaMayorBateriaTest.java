package skycampus.enterprise.aplicacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import skycampus.enterprise.dominio.Drone;
import skycampus.enterprise.dominio.SolicitudEntrega;

@DisplayName("EstrategiaMayorBateria")
class EstrategiaMayorBateriaTest {

    private static final SolicitudEntrega SOLICITUD = new SolicitudEntrega("M-1", "ECI", "UNAL");
    private final EstrategiaMayorBateria estrategia = new EstrategiaMayorBateria();

    @Test
    @DisplayName("elige el de mayor batería")
    void seleccionar_eligeMayorBateria() {
        List<Drone> candidatos = List.of(new Drone("D-1", "ECI", 40), new Drone("D-2", "ECI", 95),
                new Drone("D-3", "ECI", 70));

        assertEquals(Optional.of(candidatos.get(1)), estrategia.seleccionar(SOLICITUD, candidatos));
    }

    @Test
    @DisplayName("ante empate de batería gana el id menor, sin importar el orden de la lista")
    void seleccionar_empate_desempataPorId() {
        Drone d1 = new Drone("D-1", "ECI", 80);
        Drone d2 = new Drone("D-2", "ECI", 80);

        assertEquals(Optional.of(d1), estrategia.seleccionar(SOLICITUD, List.of(d1, d2)));
        assertEquals(Optional.of(d1), estrategia.seleccionar(SOLICITUD, List.of(d2, d1)));
    }

    @Test
    @DisplayName("sin candidatos devuelve vacío")
    void seleccionar_sinCandidatos_devuelveVacio() {
        assertTrue(estrategia.seleccionar(SOLICITUD, List.of()).isEmpty());
    }
}
