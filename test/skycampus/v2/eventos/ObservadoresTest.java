package skycampus.v2.eventos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static skycampus.v2.testutil.DatosV2.AHORA;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Observadores de la flota")
class ObservadoresTest {

    private static EventoFlota evento(TipoEvento tipo) {
        return new EventoFlota(tipo, "detalle de prueba", AHORA);
    }

    @Test
    @DisplayName("panelOperador_cualquierEvento_muestraTipoYDetalle")
    void panelOperador_cualquierEvento_muestraTipoYDetalle() {
        List<String> pantalla = new ArrayList<>();

        new PanelOperador(pantalla::add).alOcurrir(evento(TipoEvento.MISION_ASIGNADA));

        assertEquals(List.of("[PANEL] MISION_ASIGNADA: detalle de prueba"), pantalla);
    }

    @Test
    @DisplayName("sistemaLog_variosEventos_losGuardaEnOrden")
    void sistemaLog_variosEventos_losGuardaEnOrden() {
        SistemaLog log = new SistemaLog();

        log.alOcurrir(evento(TipoEvento.MISION_ASIGNADA));
        log.alOcurrir(evento(TipoEvento.FALLO_DRONE));

        assertEquals(2, log.registros().size());
        assertTrue(log.registros().get(0).contains("MISION_ASIGNADA"));
        assertTrue(log.registros().get(1).contains("FALLO_DRONE"));
        assertTrue(log.registros().get(0).startsWith(AHORA.toString()));
    }

    @Test
    @DisplayName("sistemaLog_registrosExpuestos_noSePuedenModificarDesdeAfuera")
    void sistemaLog_registrosExpuestos_noSePuedenModificarDesdeAfuera() {
        List<String> registros = new SistemaLog().registros();

        assertThrows(UnsupportedOperationException.class, () -> registros.add("manipulado"));
    }

    @Test
    @DisplayName("alertaTecnico_falloDeDrone_enviaAlertaYLaCuenta")
    void alertaTecnico_falloDeDrone_enviaAlertaYLaCuenta() {
        List<String> canal = new ArrayList<>();
        AlertaTecnico alerta = new AlertaTecnico(canal::add);

        alerta.alOcurrir(evento(TipoEvento.FALLO_DRONE));

        assertEquals(1, alerta.alertasEnviadas());
        assertEquals(List.of("[TÉCNICO] Revisar de inmediato: detalle de prueba"), canal);
    }

    @Test
    @DisplayName("alertaTecnico_eventosQueNoSonFallo_noEnviaNada")
    void alertaTecnico_eventosQueNoSonFallo_noEnviaNada() {
        List<String> canal = new ArrayList<>();
        AlertaTecnico alerta = new AlertaTecnico(canal::add);

        alerta.alOcurrir(evento(TipoEvento.MISION_ASIGNADA));
        alerta.alOcurrir(evento(TipoEvento.MISION_COMPLETADA));
        alerta.alOcurrir(evento(TipoEvento.SIN_DRONE_DISPONIBLE));

        assertEquals(0, alerta.alertasEnviadas());
        assertTrue(canal.isEmpty());
    }

    @Test
    @DisplayName("observadores_argumentosNulos_lanzaIllegalArgumentException")
    void observadores_argumentosNulos_lanzaIllegalArgumentException() {
        PanelOperador panel = new PanelOperador(linea -> { });
        SistemaLog log = new SistemaLog();
        AlertaTecnico alerta = new AlertaTecnico(linea -> { });

        assertThrows(IllegalArgumentException.class, () -> new PanelOperador(null));
        assertThrows(IllegalArgumentException.class, () -> new AlertaTecnico(null));
        assertThrows(IllegalArgumentException.class, () -> panel.alOcurrir(null));
        assertThrows(IllegalArgumentException.class, () -> log.alOcurrir(null));
        assertThrows(IllegalArgumentException.class, () -> alerta.alOcurrir(null));
    }

    @Test
    @DisplayName("eventoFlota_camposInvalidos_lanzaIllegalArgumentException")
    void eventoFlota_camposInvalidos_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new EventoFlota(null, "x", AHORA));
        assertThrows(IllegalArgumentException.class, () -> new EventoFlota(TipoEvento.FALLO_DRONE, " ", AHORA));
        assertThrows(IllegalArgumentException.class, () -> new EventoFlota(TipoEvento.FALLO_DRONE, "x", null));
    }
}
