package skycampus.v2.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static skycampus.v2.testutil.DatosV2.AHORA;
import static skycampus.v2.testutil.DatosV2.drone;
import static skycampus.v2.testutil.DatosV2.droneNoDisponible;
import static skycampus.v2.testutil.DatosV2.solicitud;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import skycampus.v2.clima.ApiMeteorologica;
import skycampus.v2.eventos.EventoFlota;
import skycampus.v2.eventos.ObservadorFlota;
import skycampus.v2.eventos.TipoEvento;
import skycampus.v2.model.Drone;
import skycampus.v2.model.Prioridad;
import skycampus.v2.model.SolicitudMision;
import skycampus.v2.model.TipoDrone;

@ExtendWith(MockitoExtension.class)
@DisplayName("AsignadorMision (TDD con Mockito)")
class AsignadorMisionTest {

    @Mock
    ApiMeteorologica clima;          // simulado: sistema externo
    @Mock
    ObservadorFlota notificador;     // simulado: quien recibe los avisos
    @Captor
    ArgumentCaptor<EventoFlota> evento;
    @InjectMocks
    AsignadorMision asignador;

    private List<TipoEvento> tiposPublicados() {
        verify(notificador, atLeastOnce()).alOcurrir(evento.capture());
        return evento.getAllValues().stream().map(EventoFlota::tipo).toList();
    }

    @Test
    @DisplayName("misionNormal_asignaDroneMayorBateria")
    void misionNormal_asignaDroneMayorBateria() {
        // ARRANGE
        when(clima.esApto()).thenReturn(true);
        List<Drone> flota = List.of(drone("D-01", TipoDrone.MINI, 85), drone("D-02", TipoDrone.MINI, 91),
                drone("D-03", TipoDrone.CARGO, 62));
        SolicitudMision pedido = solicitud("M-1", 200, Prioridad.NORMAL);
        // ACT
        Optional<Drone> asignado = asignador.asignar(pedido, flota, AHORA);
        // ASSERT
        assertTrue(asignado.isPresent());
        assertEquals("D-02", asignado.get().id());
        assertEquals(91, asignado.get().bateria());
        assertEquals(List.of(TipoEvento.MISION_ASIGNADA), tiposPublicados());
    }

    @Test
    @DisplayName("climaAdverso_retornaVacio")
    void climaAdverso_retornaVacio() {
        when(clima.esApto()).thenReturn(false);
        List<Drone> flota = List.of(drone("D-01", TipoDrone.MINI, 85));

        Optional<Drone> resultado = asignador.asignar(solicitud("M-1", 200, Prioridad.NORMAL), flota, AHORA);

        assertTrue(resultado.isEmpty());
        assertEquals(List.of(TipoEvento.CLIMA_ADVERSO), tiposPublicados());
    }

    @Test
    @DisplayName("sinDronesAptos_retornaVacio")
    void sinDronesAptos_retornaVacio() {
        when(clima.esApto()).thenReturn(true);
        List<Drone> flota = List.of(drone("D-01", TipoDrone.MINI, 29), droneNoDisponible("D-02", TipoDrone.MINI, 90));

        Optional<Drone> resultado = asignador.asignar(solicitud("M-1", 200, Prioridad.NORMAL), flota, AHORA);

        assertTrue(resultado.isEmpty());
        assertEquals(List.of(TipoEvento.SIN_DRONE_DISPONIBLE), tiposPublicados());
    }

    @Test
    @DisplayName("paqueteMuyPesado_retornaVacioSinConsultarElClima")
    void paqueteMuyPesado_retornaVacioSinConsultarElClima() {
        List<Drone> flota = List.of(drone("D-01", TipoDrone.CARGO, 90));

        Optional<Drone> resultado = asignador.asignar(solicitud("M-1", 2001, Prioridad.NORMAL), flota, AHORA);

        assertTrue(resultado.isEmpty());
        verify(clima, never()).esApto();
        assertEquals(List.of(TipoEvento.PAQUETE_EXCEDE_CAPACIDAD), tiposPublicados());
    }

    @Test
    @DisplayName("misionUrgente_asignaDroneExpressAunqueOtroTengaMasBateria")
    void misionUrgente_asignaDroneExpressAunqueOtroTengaMasBateria() {
        when(clima.esApto()).thenReturn(true);
        List<Drone> flota = List.of(drone("D-01", TipoDrone.MINI, 95), drone("D-15", TipoDrone.EXPRESS, 45));

        Optional<Drone> asignado = asignador.asignar(solicitud("M-1", 200, Prioridad.URGENTE), flota, AHORA);

        assertTrue(asignado.isPresent());
        assertEquals("D-15", asignado.get().id());
        assertEquals(TipoDrone.EXPRESS, asignado.get().tipo());
    }

    // ---------- Casos límite (segunda ronda del ciclo rojo-verde) ----------

    @ParameterizedTest(name = "bateria {0}% -> asignado: {1}")
    @CsvSource({"30,true", "29,false"})
    @DisplayName("bateriaMinima_30EsValidoY29No")
    void bateriaMinima_30EsValidoY29No(int bateria, boolean seAsigna) {
        when(clima.esApto()).thenReturn(true);
        List<Drone> flota = List.of(drone("D-01", TipoDrone.MINI, bateria));

        Optional<Drone> resultado = asignador.asignar(solicitud("M-1", 200, Prioridad.NORMAL), flota, AHORA);

        assertEquals(seAsigna, resultado.isPresent());
    }

    @Test
    @DisplayName("pesoExactoDelMaximo_2000SeAsignaAUnCargo")
    void pesoExactoDelMaximo_2000SeAsignaAUnCargo() {
        when(clima.esApto()).thenReturn(true);
        List<Drone> flota = List.of(drone("D-01", TipoDrone.MINI, 95), drone("D-02", TipoDrone.CARGO, 70));

        Optional<Drone> resultado = asignador.asignar(solicitud("M-1", 2000, Prioridad.NORMAL), flota, AHORA);

        assertEquals("D-02", resultado.orElseThrow().id());
    }

    @Test
    @DisplayName("pesoDentroDelSistemaPeroNingunDroneLoSoporta_esSinDroneDisponible")
    void pesoDentroDelSistemaPeroNingunDroneLoSoporta_esSinDroneDisponible() {
        when(clima.esApto()).thenReturn(true);
        List<Drone> flota = List.of(drone("D-01", TipoDrone.MINI, 95), drone("D-15", TipoDrone.EXPRESS, 90));

        Optional<Drone> resultado = asignador.asignar(solicitud("M-1", 1500, Prioridad.NORMAL), flota, AHORA);

        assertTrue(resultado.isEmpty());
        assertEquals(List.of(TipoEvento.SIN_DRONE_DISPONIBLE), tiposPublicados());
    }

    @ParameterizedTest(name = "paquete de {0} g en CARGO -> asignado: {1}")
    @CsvSource({"99,false", "100,true"})
    @DisplayName("cargoNoLlevaPaquetesDeMenosDe100g")
    void cargoNoLlevaPaquetesDeMenosDe100g(int peso, boolean seAsigna) {
        when(clima.esApto()).thenReturn(true);
        List<Drone> flota = List.of(drone("D-08", TipoDrone.CARGO, 90));

        Optional<Drone> resultado = asignador.asignar(solicitud("M-1", peso, Prioridad.NORMAL), flota, AHORA);

        assertEquals(seAsigna, resultado.isPresent());
    }

    @Test
    @DisplayName("apiDelClimaFalla_seTrataComoClimaAdversoYNoSeAsigna")
    void apiDelClimaFalla_seTrataComoClimaAdversoYNoSeAsigna() {
        when(clima.esApto()).thenThrow(new IllegalStateException("tiempo de espera agotado"));
        List<Drone> flota = List.of(drone("D-01", TipoDrone.MINI, 85));

        Optional<Drone> resultado = asignador.asignar(solicitud("M-1", 200, Prioridad.NORMAL), flota, AHORA);

        assertTrue(resultado.isEmpty());
        assertEquals(List.of(TipoEvento.CLIMA_ADVERSO), tiposPublicados());
    }

    @Test
    @DisplayName("urgenteSinExpressApto_usaMayorBateriaYAvisaAlOperador")
    void urgenteSinExpressApto_usaMayorBateriaYAvisaAlOperador() {
        when(clima.esApto()).thenReturn(true);
        List<Drone> flota = List.of(drone("D-01", TipoDrone.MINI, 95), drone("D-15", TipoDrone.EXPRESS, 20));

        Optional<Drone> resultado = asignador.asignar(solicitud("M-1", 200, Prioridad.URGENTE), flota, AHORA);

        assertEquals("D-01", resultado.orElseThrow().id());
        verify(notificador, times(2)).alOcurrir(evento.capture());
        assertEquals(List.of(TipoEvento.URGENTE_SIN_DRONE_RAPIDO, TipoEvento.MISION_ASIGNADA),
                evento.getAllValues().stream().map(EventoFlota::tipo).toList());
    }

    @Test
    @DisplayName("empateDeBateria_ganaElIdMenor")
    void empateDeBateria_ganaElIdMenor() {
        when(clima.esApto()).thenReturn(true);
        List<Drone> flota = List.of(drone("D-02", TipoDrone.MINI, 90), drone("D-01", TipoDrone.MINI, 90));

        Optional<Drone> resultado = asignador.asignar(solicitud("M-1", 200, Prioridad.NORMAL), flota, AHORA);

        assertEquals("D-01", resultado.orElseThrow().id());
    }

    @Test
    @DisplayName("unaSolaMisionAsignada_publicaUnSoloEvento")
    void unaSolaMisionAsignada_publicaUnSoloEvento() {
        when(clima.esApto()).thenReturn(true);

        asignador.asignar(solicitud("M-1", 200, Prioridad.NORMAL), List.of(drone("D-01", TipoDrone.MINI, 85)), AHORA);

        verify(notificador, times(1)).alOcurrir(org.mockito.ArgumentMatchers.any(EventoFlota.class));
    }

    @Test
    @DisplayName("constructor_dependenciasNulas_lanzaExcepcion")
    void constructor_dependenciasNulas_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> new AsignadorMision(null, notificador));
        assertThrows(IllegalArgumentException.class, () -> new AsignadorMision(clima, null));
    }

    @Test
    @DisplayName("asignar_argumentosNulos_lanzaExcepcion")
    void asignar_argumentosNulos_lanzaExcepcion() {
        SolicitudMision pedido = solicitud("M-1", 200, Prioridad.NORMAL);
        assertThrows(IllegalArgumentException.class, () -> asignador.asignar(null, List.of(), AHORA));
        assertThrows(IllegalArgumentException.class, () -> asignador.asignar(pedido, null, AHORA));
        assertThrows(IllegalArgumentException.class, () -> asignador.asignar(pedido, List.of(), null));
    }
}
