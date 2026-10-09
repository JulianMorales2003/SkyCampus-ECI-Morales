package v2.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static v2.testutil.DatosV2.drone;
import static v2.testutil.DatosV2.mision;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import v2.model.Drone;
import v2.model.EstadoMision;
import v2.model.Mision;
import v2.model.TipoDrone;

@DisplayName("EstadisticasFlota")
class EstadisticasFlotaTest {

    private final Drone mini = drone("D-01", TipoDrone.MINI);
    private final Drone cargo = drone("D-02", TipoDrone.CARGO);

    @Test
    @DisplayName("completadasPorTipo_variasMisiones_cuentaSoloLasCompletadasDeCadaTipo")
    void completadasPorTipo_variasMisiones_cuentaSoloLasCompletadasDeCadaTipo() {
        List<Mision> misiones = List.of(
                mision("M-1", mini, EstadoMision.COMPLETADA),
                mision("M-2", mini, EstadoMision.COMPLETADA),
                mision("M-3", mini, EstadoMision.FALLIDA),
                mision("M-4", cargo, EstadoMision.COMPLETADA),
                mision("M-5", cargo, EstadoMision.EN_VUELO));

        Map<TipoDrone, Long> resultado = EstadisticasFlota.completadasPorTipo(misiones);

        assertEquals(Map.of(TipoDrone.MINI, 2L, TipoDrone.CARGO, 1L), resultado);
    }

    @Test
    @DisplayName("completadasPorTipo_sinCompletadas_devuelveMapaVacio")
    void completadasPorTipo_sinCompletadas_devuelveMapaVacio() {
        List<Mision> misiones = List.of(mision("M-1", mini, EstadoMision.PENDIENTE));

        assertTrue(EstadisticasFlota.completadasPorTipo(misiones).isEmpty());
    }

    @Test
    @DisplayName("completadasPorTipo_listaNula_lanzaIllegalArgumentException")
    void completadasPorTipo_listaNula_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> EstadisticasFlota.completadasPorTipo(null));
    }
}
