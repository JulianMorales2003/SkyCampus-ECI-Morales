package skycampus.enterprise.aplicacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import skycampus.enterprise.dominio.Drone;
import skycampus.enterprise.dominio.EventoAsignacion;
import skycampus.enterprise.dominio.PlanVuelo;
import skycampus.enterprise.dominio.PoliticaVuelo;
import skycampus.enterprise.dominio.RestriccionAerea;
import skycampus.enterprise.dominio.ServicioAerocivil;
import skycampus.enterprise.dominio.ServicioClima;
import skycampus.enterprise.dominio.TipoEventoAsignacion;
import skycampus.enterprise.infraestructura.CatalogoSedesEnMemoria;
import skycampus.enterprise.infraestructura.RepositorioFlotaEnMemoria;

/**
 * Capa 2 (integración): aplicación + dominio + adaptadores reales en memoria juntos. Solo se simulan
 * los dos servicios externos (clima y Aerocivil). No hay HTTP: eso lo prueba la capa 3.
 */
@DisplayName("Creación de misión · integración (capa 2)")
class MisionIntegrationTest {

    private ServicioClima clima;
    private ServicioAerocivil aerocivil;
    private CatalogoSedesEnMemoria sedes;
    private final List<EventoAsignacion> eventos = new ArrayList<>();
    private final AtomicInteger contador = new AtomicInteger();
    private CrearMision caso;

    @BeforeEach
    void ensamblar() {
        clima = mock(ServicioClima.class);
        aerocivil = mock(ServicioAerocivil.class);
        sedes = new CatalogoSedesEnMemoria().activar("ECI").activar("UNAL").activar("UNIANDES")
                .distancia("ECI", "UNAL", 9).distancia("ECI", "UNIANDES", 45);
        RepositorioFlotaEnMemoria flota = new RepositorioFlotaEnMemoria(List.of(
                new Drone("ECI-D1", "ECI", 50), new Drone("ECI-D2", "ECI", 90),
                new Drone("UNAL-D1", "UNAL", 99)));
        PoliticaVuelo politica = new PoliticaVuelo(30);
        politica.registrarRestriccion("ECI", new RestriccionAerea(50, 120));
        caso = new CrearMision(sedes, flota, clima, new EstrategiaMayorBateria(), eventos::add,
                new AutorizadorVuelo(politica, aerocivil), () -> "M-" + contador.incrementAndGet());
        when(clima.condicionesAptas("ECI", "UNAL")).thenReturn(true);
        when(aerocivil.autoriza(any(PlanVuelo.class))).thenReturn(true);
    }

    private ResultadoMision crear(String destino, int peso, PrioridadMision prioridad) {
        return caso.crear(new ComandoMision("ECI", destino, peso, prioridad));
    }

    @Test
    @DisplayName("clima apto: crea la misión con el drone de la sede de origen (no el de otra sede) y avisa")
    void crearMision_climaApto_retornaCreadaConDroneAsignado() {
        ResultadoMision resultado = crear("UNAL", 300, PrioridadMision.NORMAL);

        assertTrue(resultado.creada());
        assertEquals("ECI-D2", resultado.drone().id());
        assertEquals(List.of(TipoEventoAsignacion.MISION_ASIGNADA), eventos.stream().map(EventoAsignacion::tipo).toList());
        verify(aerocivil).autoriza(any(PlanVuelo.class));
    }

    @Test
    @DisplayName("flujo alterno · clima adverso")
    void crearMision_climaAdverso() {
        when(clima.condicionesAptas("ECI", "UNAL")).thenReturn(false);

        assertEquals(MotivoMision.CLIMA_ADVERSO, crear("UNAL", 300, PrioridadMision.NORMAL).motivo());
        assertEquals(TipoEventoAsignacion.CLIMA_ADVERSO, eventos.get(0).tipo());
        verifyNoInteractions(aerocivil);
    }

    @Test
    @DisplayName("flujo alterno · sin drones disponibles con batería suficiente")
    void crearMision_sinDrones() {
        RepositorioFlotaEnMemoria vacia = new RepositorioFlotaEnMemoria(List.of(new Drone("ECI-D9", "ECI", 29)));
        PoliticaVuelo politica = new PoliticaVuelo(30);
        politica.registrarRestriccion("ECI", new RestriccionAerea(50, 120));
        CrearMision sinDrones = new CrearMision(sedes, vacia, clima, new EstrategiaMayorBateria(), eventos::add,
                new AutorizadorVuelo(politica, aerocivil), () -> "M-X");

        ResultadoMision resultado = sinDrones.crear(new ComandoMision("ECI", "UNAL", 300, null));

        assertEquals(MotivoMision.SIN_DRONE_DISPONIBLE, resultado.motivo());
        assertEquals(TipoEventoAsignacion.SIN_DRONE_DISPONIBLE, eventos.get(0).tipo());
        verifyNoInteractions(aerocivil);
    }

    @Test
    @DisplayName("flujo alterno · paquete pesado: ni se consulta el clima")
    void crearMision_paquetePesado() {
        assertEquals(MotivoMision.PAQUETE_EXCEDE_PESO, crear("UNAL", 2500, PrioridadMision.NORMAL).motivo());
        verifyNoInteractions(clima, aerocivil);
    }

    @Test
    @DisplayName("flujo alterno · la Aerocivil rechaza")
    void crearMision_aerocivilRechaza() {
        when(aerocivil.autoriza(any(PlanVuelo.class))).thenReturn(false);

        assertEquals(MotivoMision.AEROCIVIL_RECHAZA, crear("UNAL", 300, PrioridadMision.NORMAL).motivo());
    }

    @Test
    @DisplayName("flujo alterno · sede inactiva")
    void crearMision_sedeInactiva() {
        sedes.desactivar("UNAL");

        assertEquals(MotivoMision.SEDE_INACTIVA, crear("UNAL", 300, PrioridadMision.NORMAL).motivo());
        verifyNoInteractions(clima, aerocivil);
    }

    @Test
    @DisplayName("la Aerocivil no responde: el vuelo no despega")
    void crearMision_aerocivilNoResponde() {
        when(aerocivil.autoriza(any(PlanVuelo.class))).thenThrow(new IllegalStateException("timeout"));

        assertEquals(MotivoMision.AEROCIVIL_NO_RESPONDE, crear("UNAL", 300, PrioridadMision.NORMAL).motivo());
    }

    @Test
    @DisplayName("una ruta más larga que el techo de la red (30 km) se rechaza aunque la Aerocivil permita 50")
    void crearMision_radioExcedido() {
        when(clima.condicionesAptas("ECI", "UNIANDES")).thenReturn(true);

        assertEquals(MotivoMision.RADIO_EXCEDIDO, crear("UNIANDES", 300, PrioridadMision.NORMAL).motivo());
        verify(aerocivil, never()).autoriza(any(PlanVuelo.class));
    }

    @Test
    @DisplayName("la misión urgente también pasa por la Aerocivil y su urgencia llega en el plan")
    void crearMision_urgente_sigueLasMismasReglas() {
        when(aerocivil.autoriza(any(PlanVuelo.class))).thenReturn(false);

        assertEquals(MotivoMision.AEROCIVIL_RECHAZA, crear("UNAL", 300, PrioridadMision.URGENTE).motivo());
        ArgumentCaptor<PlanVuelo> plan = ArgumentCaptor.forClass(PlanVuelo.class);
        verify(aerocivil).autoriza(plan.capture());
        assertTrue(plan.getValue().urgente());
    }

    @Test
    @DisplayName("dos misiones seguidas reciben ids distintos")
    void crearMision_idsSecuenciales() {
        assertEquals("M-1", crear("UNAL", 300, PrioridadMision.NORMAL).misionId());
        assertEquals("M-2", crear("UNAL", 300, PrioridadMision.NORMAL).misionId());
        verify(aerocivil, times(2)).autoriza(any(PlanVuelo.class));
    }
}
