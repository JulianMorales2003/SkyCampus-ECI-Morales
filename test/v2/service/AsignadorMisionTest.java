package v2.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static v2.testutil.DatosV2.AHORA;
import static v2.testutil.DatosV2.drone;
import static v2.testutil.DatosV2.droneNoDisponible;
import static v2.testutil.DatosV2.solicitud;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import v2.clima.ApiMeteorologica;
import v2.eventos.EventoFlota;
import v2.eventos.ObservadorFlota;
import v2.eventos.TipoEvento;
import v2.model.Drone;
import v2.model.Prioridad;
import v2.model.SolicitudMision;
import v2.model.TipoDrone;

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
}
