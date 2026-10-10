package skycampus.enterprise.ruta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static skycampus.enterprise.ruta.RutasTestUtil.ECI;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import skycampus.enterprise.ruta.drone.FabricaDroneEstandar;
import skycampus.enterprise.ruta.evento.NotificadorRuta;

@DisplayName("Punto, ResultadoEjecucion y ContextoEjecucion")
class PuntoYModeloTest {

    @Test
    @DisplayName("distanciaKm_dosPuntos_usaLaDistanciaEnLineaRecta")
    void distanciaKm_dosPuntos_usaLaDistanciaEnLineaRecta() {
        assertEquals(5.0, new Punto("A", 0, 0).distanciaKm(new Punto("B", 3, 4)), 1e-9);
    }

    @Test
    @DisplayName("punto_datosInvalidos_lanzaExcepcion")
    void punto_datosInvalidos_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> new Punto(" ", 0, 0));
        assertThrows(IllegalArgumentException.class, () -> ECI.distanciaKm(null));
    }

    @Test
    @DisplayName("resultadoEjecucion_exitoYFallo_guardanLasEtapasCompletadas")
    void resultadoEjecucion_exitoYFallo_guardanLasEtapasCompletadas() {
        assertTrue(ResultadoEjecucion.exito(3).completada());
        assertFalse(ResultadoEjecucion.fallo(1).completada());
        assertEquals(1, ResultadoEjecucion.fallo(1).etapasCompletadas());
        assertThrows(IllegalArgumentException.class, () -> ResultadoEjecucion.exito(-1));
    }

    @Test
    @DisplayName("contextoEjecucion_colaboradoresNulos_lanzaExcepcion")
    void contextoEjecucion_colaboradoresNulos_lanzaExcepcion() {
        NotificadorRuta notificador = new NotificadorRuta();
        FabricaDroneEstandar fabrica = new FabricaDroneEstandar();

        assertThrows(IllegalArgumentException.class, () -> new ContextoEjecucion(null, notificador));
        assertThrows(IllegalArgumentException.class, () -> new ContextoEjecucion(fabrica, null));
    }
}
