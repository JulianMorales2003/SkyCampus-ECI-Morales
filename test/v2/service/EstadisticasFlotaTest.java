package v2.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static v2.testutil.DatosV2.drone;
import static v2.testutil.DatosV2.mision;

import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    @Test
    @DisplayName("droneConMasCompletadas_variosDrones_devuelveElQueMasCompleto")
    void droneConMasCompletadas_variosDrones_devuelveElQueMasCompleto() {
        List<Mision> misiones = List.of(
                mision("M-1", mini, EstadoMision.COMPLETADA),
                mision("M-2", cargo, EstadoMision.COMPLETADA),
                mision("M-3", cargo, EstadoMision.COMPLETADA),
                mision("M-4", mini, EstadoMision.FALLIDA),
                mision("M-5", mini, EstadoMision.FALLIDA));

        assertEquals(Optional.of(cargo), EstadisticasFlota.droneConMasCompletadas(misiones));
    }

    @Test
    @DisplayName("droneConMasCompletadas_empate_devuelveElDeIdMenor")
    void droneConMasCompletadas_empate_devuelveElDeIdMenor() {
        List<Mision> misiones = List.of(
                mision("M-1", cargo, EstadoMision.COMPLETADA),
                mision("M-2", mini, EstadoMision.COMPLETADA));

        assertEquals(Optional.of(mini), EstadisticasFlota.droneConMasCompletadas(misiones));
    }

    @Test
    @DisplayName("droneConMasCompletadas_sinCompletadas_devuelveVacio")
    void droneConMasCompletadas_sinCompletadas_devuelveVacio() {
        List<Mision> misiones = List.of(mision("M-1", mini, EstadoMision.FALLIDA));

        assertTrue(EstadisticasFlota.droneConMasCompletadas(misiones).isEmpty());
    }

    @Test
    @DisplayName("droneConMasCompletadas_listaNula_lanzaIllegalArgumentException")
    void droneConMasCompletadas_listaNula_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> EstadisticasFlota.droneConMasCompletadas(null));
    }

    @Test
    @DisplayName("porcentajeFallidas_unaDeCuatro_devuelveVeinticincoPorCiento")
    void porcentajeFallidas_unaDeCuatro_devuelveVeinticincoPorCiento() {
        List<Mision> misiones = List.of(
                mision("M-1", mini, EstadoMision.FALLIDA),
                mision("M-2", mini, EstadoMision.COMPLETADA),
                mision("M-3", cargo, EstadoMision.COMPLETADA),
                mision("M-4", cargo, EstadoMision.PENDIENTE));

        assertEquals(25.0, EstadisticasFlota.porcentajeFallidas(misiones), 0.0001);
    }

    @Test
    @DisplayName("porcentajeFallidas_listaVacia_devuelveCero")
    void porcentajeFallidas_listaVacia_devuelveCero() {
        assertEquals(0.0, EstadisticasFlota.porcentajeFallidas(List.of()), 0.0001);
    }

    @Test
    @DisplayName("porcentajeFallidas_listaNula_lanzaIllegalArgumentException")
    void porcentajeFallidas_listaNula_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> EstadisticasFlota.porcentajeFallidas(null));
    }
}
