package skycampus.enterprise.aplicacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import skycampus.enterprise.dominio.Drone;
import skycampus.enterprise.dominio.EventoAsignacion;
import skycampus.enterprise.dominio.SolicitudEntrega;
import skycampus.enterprise.dominio.TipoEventoAsignacion;

/** Capa 1 (unitarias): casos de borde del asignador con puertos escritos como lambdas, sin Mockito. */
@DisplayName("AsignadorMision · casos de borde (capa 1)")
class AsignadorMisionBordesTest {

    private static final SolicitudEntrega SOLICITUD = new SolicitudEntrega("M-1", "ECI", "UNAL");

    private final List<EventoAsignacion> eventos = new ArrayList<>();

    private AsignadorMision asignador(List<Drone> flota, boolean climaApto) {
        return new AsignadorMision(sede -> flota, (origen, destino) -> climaApto,
                new EstrategiaMayorBateria(), eventos::add);
    }

    @Test
    @DisplayName("el aviso de clima adverso nombra el origen y el destino")
    void climaAdverso_detalleNombraLasSedes() {
        asignador(List.of(new Drone("D-1", "ECI", 90)), false).asignar(SOLICITUD);

        EventoAsignacion evento = eventos.get(0);
        assertEquals(TipoEventoAsignacion.CLIMA_ADVERSO, evento.tipo());
        assertTrue(evento.detalle().contains("ECI") && evento.detalle().contains("UNAL"));
    }

    @Test
    @DisplayName("si todos los drones están bajo el 30 % avisa SIN_DRONE_DISPONIBLE y nombra la sede")
    void todosBajoElMinimo_sinDrone() {
        Optional<Drone> resultado = asignador(
                List.of(new Drone("D-1", "ECI", 29), new Drone("D-2", "ECI", 5)), true).asignar(SOLICITUD);

        assertTrue(resultado.isEmpty());
        assertEquals(TipoEventoAsignacion.SIN_DRONE_DISPONIBLE, eventos.get(0).tipo());
        assertTrue(eventos.get(0).detalle().contains("ECI"));
    }

    @Test
    @DisplayName("con empate de batería gana el id menor, así el resultado es estable")
    void empate_ganaElIdMenor() {
        Optional<Drone> resultado = asignador(
                List.of(new Drone("D-2", "ECI", 80), new Drone("D-1", "ECI", 80)), true).asignar(SOLICITUD);

        assertEquals("D-1", resultado.orElseThrow().id());
    }

    @Test
    @DisplayName("cada asignación publica exactamente un evento, sea cual sea el resultado")
    void unSoloEventoPorAsignacion() {
        List<Drone> flota = List.of(new Drone("D-1", "ECI", 90));
        asignador(flota, true).asignar(SOLICITUD);
        asignador(flota, false).asignar(SOLICITUD);
        asignador(List.of(), true).asignar(SOLICITUD);

        assertEquals(List.of(TipoEventoAsignacion.MISION_ASIGNADA, TipoEventoAsignacion.CLIMA_ADVERSO,
                TipoEventoAsignacion.SIN_DRONE_DISPONIBLE), eventos.stream().map(EventoAsignacion::tipo).toList());
    }
}
