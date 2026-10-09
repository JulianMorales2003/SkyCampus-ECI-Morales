package v2.asignacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static v2.testutil.DatosV2.drone;
import static v2.testutil.DatosV2.solicitud;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import v2.model.Drone;
import v2.model.Prioridad;
import v2.model.TipoDrone;

@DisplayName("EstrategiaPorPrioridad (RF-07 + RF-08)")
class EstrategiaPorPrioridadTest {

    private final EstrategiaPorPrioridad estrategia = new EstrategiaPorPrioridad();
    private final List<Drone> flota = List.of(drone("D-01", TipoDrone.MINI, 95), drone("D-14", TipoDrone.EXPRESS, 40),
            drone("D-15", TipoDrone.EXPRESS, 60));

    @Test
    @DisplayName("seleccionar_urgente_eligeElExpressConMasBateria")
    void seleccionar_urgente_eligeElExpressConMasBateria() {
        assertEquals("D-15", estrategia.seleccionar(solicitud("M-1", 200, Prioridad.URGENTE), flota).orElseThrow().id());
    }

    @Test
    @DisplayName("seleccionar_urgenteSinExpress_eligeLaMayorBateria")
    void seleccionar_urgenteSinExpress_eligeLaMayorBateria() {
        List<Drone> soloMini = List.of(drone("D-01", TipoDrone.MINI, 95), drone("D-02", TipoDrone.MINI, 50));
        assertEquals("D-01", estrategia.seleccionar(solicitud("M-1", 200, Prioridad.URGENTE), soloMini).orElseThrow().id());
    }

    @Test
    @DisplayName("seleccionar_noUrgente_eligeLaMayorBateriaSinImportarElTipo")
    void seleccionar_noUrgente_eligeLaMayorBateriaSinImportarElTipo() {
        assertEquals("D-01", estrategia.seleccionar(solicitud("M-1", 200, Prioridad.NORMAL), flota).orElseThrow().id());
        assertEquals("D-01", estrategia.seleccionar(solicitud("M-2", 200, Prioridad.BAJO), flota).orElseThrow().id());
    }

    @Test
    @DisplayName("seleccionar_flotaVacia_devuelveVacio")
    void seleccionar_flotaVacia_devuelveVacio() {
        assertTrue(estrategia.seleccionar(solicitud("M-1", 200, Prioridad.URGENTE), List.of()).isEmpty());
    }

    @Test
    @DisplayName("seleccionar_argumentosNulos_lanzaExcepcion")
    void seleccionar_argumentosNulos_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> estrategia.seleccionar(null, flota));
        assertThrows(IllegalArgumentException.class,
                () -> estrategia.seleccionar(solicitud("M-1", 200, Prioridad.NORMAL), null));
    }
}
