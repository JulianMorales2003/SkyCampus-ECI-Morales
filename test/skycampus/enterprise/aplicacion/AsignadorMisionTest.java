package skycampus.enterprise.aplicacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import skycampus.enterprise.dominio.Drone;
import skycampus.enterprise.dominio.EstrategiaAsignacion;
import skycampus.enterprise.dominio.EventoAsignacion;
import skycampus.enterprise.dominio.ObservadorDrone;
import skycampus.enterprise.dominio.RepositorioFlota;
import skycampus.enterprise.dominio.ServicioClima;
import skycampus.enterprise.dominio.SolicitudEntrega;
import skycampus.enterprise.dominio.TipoEventoAsignacion;

/**
 * La capa de aplicación se prueba con puertos falsos de Mockito: no hay HTTP, no hay base de datos
 * ni clase de infraestructura en el classpath de estas pruebas.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AsignadorMision (capa de aplicación, solo Mockito)")
class AsignadorMisionTest {

    private static final String ORIGEN = "ECI";
    private static final String DESTINO = "UNAL";
    private static final String OTRA_SEDE = "EAFIT";
    private static final SolicitudEntrega SOLICITUD = new SolicitudEntrega("M-1", ORIGEN, DESTINO);

    @Mock
    private RepositorioFlota repo;
    @Mock
    private ServicioClima clima;
    @Mock
    private ObservadorDrone notificador;
    @Mock
    private EstrategiaAsignacion estrategiaFalsa;

    private AsignadorMision asignador;

    @BeforeEach
    void crearAsignador() {
        asignador = new AsignadorMision(repo, clima, new EstrategiaMayorBateria(), notificador);
    }

    private EventoAsignacion eventoPublicado() {
        ArgumentCaptor<EventoAsignacion> captor = ArgumentCaptor.forClass(EventoAsignacion.class);
        verify(notificador).alOcurrir(captor.capture());
        return captor.getValue();
    }

    @Test
    @DisplayName("con clima apto asigna el drone con más batería de la sede de origen y lo notifica")
    void asigna_conClimaApto_eligeMayorBateria() {
        Drone bajo = new Drone("D-1", ORIGEN, 50);
        Drone alto = new Drone("D-2", ORIGEN, 90);
        when(clima.condicionesAptas(ORIGEN, DESTINO)).thenReturn(true);
        when(repo.findDisponibles(ORIGEN)).thenReturn(List.of(bajo, alto));

        Optional<Drone> resultado = asignador.asignar(SOLICITUD);

        assertEquals(Optional.of(alto), resultado);
        EventoAsignacion evento = eventoPublicado();
        assertEquals(TipoEventoAsignacion.MISION_ASIGNADA, evento.tipo());
        assertEquals("M-1", evento.solicitudId());
        assertTrue(evento.detalle().contains("D-2"));
        verifyNoMoreInteractions(repo, clima, notificador);
    }

    @Test
    @DisplayName("con clima no apto no consulta la flota y avisa CLIMA_ADVERSO")
    void asigna_conClimaNoApto_noTocaElRepositorio() {
        when(clima.condicionesAptas(ORIGEN, DESTINO)).thenReturn(false);

        Optional<Drone> resultado = asignador.asignar(SOLICITUD);

        assertTrue(resultado.isEmpty());
        assertEquals(TipoEventoAsignacion.CLIMA_ADVERSO, eventoPublicado().tipo());
        verifyNoMoreInteractions(repo);
    }

    @Test
    @DisplayName("si el servicio del clima falla, se trata como clima no apto (falla segura)")
    void asigna_siElClimaLanzaExcepcion_noVuela() {
        when(clima.condicionesAptas(ORIGEN, DESTINO)).thenThrow(new IllegalStateException("sin red"));

        Optional<Drone> resultado = asignador.asignar(SOLICITUD);

        assertTrue(resultado.isEmpty());
        assertEquals(TipoEventoAsignacion.CLIMA_ADVERSO, eventoPublicado().tipo());
        verifyNoMoreInteractions(repo);
    }

    @Test
    @DisplayName("sin drones en la sede avisa SIN_DRONE_DISPONIBLE")
    void asigna_sinDrones_avisaSinDrone() {
        when(clima.condicionesAptas(ORIGEN, DESTINO)).thenReturn(true);
        when(repo.findDisponibles(ORIGEN)).thenReturn(List.of());

        assertTrue(asignador.asignar(SOLICITUD).isEmpty());

        assertEquals(TipoEventoAsignacion.SIN_DRONE_DISPONIBLE, eventoPublicado().tipo());
    }

    @ParameterizedTest(name = "batería {0} -> asignado: {1}")
    @CsvSource({"29,false", "30,true", "31,true"})
    @DisplayName("exige una batería mínima de 30 % (el límite cuenta)")
    void asigna_respetaBateriaMinima(int bateria, boolean asignado) {
        when(clima.condicionesAptas(ORIGEN, DESTINO)).thenReturn(true);
        when(repo.findDisponibles(ORIGEN)).thenReturn(List.of(new Drone("D-1", ORIGEN, bateria)));

        assertEquals(asignado, asignador.asignar(SOLICITUD).isPresent());
    }

    @Test
    @DisplayName("la estrategia recibe solo los candidatos filtrados y su elección es la que se devuelve")
    void asigna_delegaLaEleccionEnLaEstrategia() {
        Drone apto = new Drone("D-1", ORIGEN, 60);
        Drone descargado = new Drone("D-2", ORIGEN, 10);
        AsignadorMision conFalsa = new AsignadorMision(repo, clima, estrategiaFalsa, notificador);
        when(clima.condicionesAptas(ORIGEN, DESTINO)).thenReturn(true);
        when(repo.findDisponibles(ORIGEN)).thenReturn(List.of(apto, descargado));
        when(estrategiaFalsa.seleccionar(SOLICITUD, List.of(apto))).thenReturn(Optional.of(apto));

        assertSame(apto, conFalsa.asignar(SOLICITUD).orElseThrow());

        verify(estrategiaFalsa).seleccionar(SOLICITUD, List.of(apto));
    }

    @Test
    @DisplayName("consulta la flota solo de la sede de origen de la solicitud")
    void asigna_consultaLaSedeDeOrigen() {
        SolicitudEntrega otra = new SolicitudEntrega("M-2", OTRA_SEDE, DESTINO);
        when(clima.condicionesAptas(OTRA_SEDE, DESTINO)).thenReturn(true);
        when(repo.findDisponibles(OTRA_SEDE)).thenReturn(List.of());

        asignador.asignar(otra);

        verify(repo).findDisponibles(OTRA_SEDE);
        verify(repo, never()).findDisponibles(ORIGEN);
    }

    @Test
    @DisplayName("rechaza solicitud nula sin tocar ningún puerto")
    void asigna_solicitudNula_lanzaExcepcion() {
        assertThrows(NullPointerException.class, () -> asignador.asignar(null));

        verifyNoMoreInteractions(repo, clima, notificador);
    }

    @Test
    @DisplayName("el constructor exige las cuatro dependencias")
    void constructor_exigeDependencias() {
        EstrategiaAsignacion estrategia = new EstrategiaMayorBateria();
        assertThrows(NullPointerException.class, () -> new AsignadorMision(null, clima, estrategia, notificador));
        assertThrows(NullPointerException.class, () -> new AsignadorMision(repo, null, estrategia, notificador));
        assertThrows(NullPointerException.class, () -> new AsignadorMision(repo, clima, null, notificador));
        assertThrows(NullPointerException.class, () -> new AsignadorMision(repo, clima, estrategia, null));
    }

    @Test
    @DisplayName("ningún puerto se invoca con argumentos cualquiera: solo con los datos de la solicitud")
    void asigna_noUsaOtrosDatos() {
        when(clima.condicionesAptas(ORIGEN, DESTINO)).thenReturn(false);

        asignador.asignar(SOLICITUD);

        verify(clima).condicionesAptas(ORIGEN, DESTINO);
        verify(clima, never()).condicionesAptas(DESTINO, ORIGEN);
        verify(repo, never()).findDisponibles(any());
    }
}
