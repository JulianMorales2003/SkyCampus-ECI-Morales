package skycampus.enterprise.aplicacion;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.OptionalDouble;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import skycampus.enterprise.dominio.CatalogoSedes;
import skycampus.enterprise.dominio.Drone;
import skycampus.enterprise.dominio.EventoAsignacion;
import skycampus.enterprise.dominio.MotivoRechazo;
import skycampus.enterprise.dominio.ObservadorDrone;
import skycampus.enterprise.dominio.PlanVuelo;
import skycampus.enterprise.dominio.PoliticaVuelo;
import skycampus.enterprise.dominio.RepositorioFlota;
import skycampus.enterprise.dominio.RestriccionAerea;
import skycampus.enterprise.dominio.ServicioAerocivil;
import skycampus.enterprise.dominio.ServicioClima;
import skycampus.enterprise.dominio.TipoEventoAsignacion;

/** Capa 1 (unitarias): el caso de uso completo con todos sus puertos falsos y los 5 flujos alternos. */
@DisplayName("CrearMision · unitarias (capa 1)")
class CrearMisionTest {

    private CatalogoSedes sedes;
    private RepositorioFlota repo;
    private ServicioClima clima;
    private ServicioAerocivil aerocivil;
    private ObservadorDrone notificador;
    private PoliticaVuelo politica;
    private CrearMision caso;

    @BeforeEach
    void preparar() {
        sedes = mock(CatalogoSedes.class);
        repo = mock(RepositorioFlota.class);
        clima = mock(ServicioClima.class);
        aerocivil = mock(ServicioAerocivil.class);
        notificador = mock(ObservadorDrone.class);
        politica = new PoliticaVuelo(30);
        politica.registrarRestriccion("ECI", new RestriccionAerea(30, 120));
        caso = new CrearMision(sedes, repo, clima, new EstrategiaMayorBateria(), notificador,
                new AutorizadorVuelo(politica, aerocivil), () -> "M-1");

        when(sedes.estaActiva(anyString())).thenReturn(true);
        when(sedes.distanciaKm("ECI", "UNAL")).thenReturn(OptionalDouble.of(9));
        when(clima.condicionesAptas("ECI", "UNAL")).thenReturn(true);
        when(repo.findDisponibles("ECI")).thenReturn(List.of(new Drone("D-1", "ECI", 50), new Drone("D-2", "ECI", 90)));
        when(aerocivil.autoriza(any(PlanVuelo.class))).thenReturn(true);
    }

    private static ComandoMision comando(int peso, PrioridadMision prioridad) {
        return new ComandoMision("ECI", "UNAL", peso, prioridad);
    }

    private ResultadoMision crearNormal() {
        return caso.crear(comando(300, PrioridadMision.NORMAL));
    }

    @Test
    @DisplayName("camino feliz: asigna el drone con más batería y pide el plan correcto a la Aerocivil")
    void creada_conDatosValidos() {
        ResultadoMision resultado = crearNormal();

        assertTrue(resultado.creada());
        assertEquals("M-1", resultado.misionId());
        assertEquals("D-2", resultado.drone().id());
        ArgumentCaptor<PlanVuelo> plan = ArgumentCaptor.forClass(PlanVuelo.class);
        verify(aerocivil).autoriza(plan.capture());
        assertEquals("M-1", plan.getValue().misionId());
        assertEquals("ECI", plan.getValue().sede());
        assertEquals(9.0, plan.getValue().distanciaKm());
        assertEquals(CrearMision.ALTURA_CRUCERO_M, plan.getValue().alturaM());
        assertTrue(plan.getValue().zonaUrbana());
        assertFalse(plan.getValue().urgente());
    }

    @Test
    @DisplayName("la urgencia viaja en el plan de vuelo")
    void urgente_viajaEnElPlan() {
        caso.crear(comando(300, PrioridadMision.URGENTE));

        ArgumentCaptor<PlanVuelo> plan = ArgumentCaptor.forClass(PlanVuelo.class);
        verify(aerocivil).autoriza(plan.capture());
        assertTrue(plan.getValue().urgente());
    }

    @Test
    @DisplayName("la urgencia no se salta a la Aerocivil: si rechaza, no hay misión")
    void urgente_noSeSaltaLaAutorizacion() {
        when(aerocivil.autoriza(any(PlanVuelo.class))).thenReturn(false);

        ResultadoMision resultado = caso.crear(comando(300, PrioridadMision.URGENTE));

        assertEquals(MotivoMision.AEROCIVIL_RECHAZA, resultado.motivo());
    }

    @Test
    @DisplayName("flujo alterno · sede de origen inactiva: no toca flota, clima ni Aerocivil")
    void sedeInactiva_origen() {
        when(sedes.estaActiva("ECI")).thenReturn(false);

        assertEquals(MotivoMision.SEDE_INACTIVA, crearNormal().motivo());
        verifyNoInteractions(repo, clima, aerocivil);
    }

    @Test
    @DisplayName("flujo alterno · sede de destino inactiva")
    void sedeInactiva_destino() {
        when(sedes.estaActiva("UNAL")).thenReturn(false);

        assertEquals(MotivoMision.SEDE_INACTIVA, crearNormal().motivo());
        verifyNoInteractions(repo, clima, aerocivil);
    }

    @ParameterizedTest(name = "peso {0} g -> creada: {1}")
    @CsvSource({"1,true", "2000,true", "2001,false", "5000,false"})
    @DisplayName("flujo alterno · paquete pesado: el límite de 2000 g cuenta")
    void peso_limite(int peso, boolean creada) {
        ResultadoMision resultado = caso.crear(comando(peso, PrioridadMision.NORMAL));

        assertEquals(creada, resultado.creada());
        if (!creada) {
            assertEquals(MotivoMision.PAQUETE_EXCEDE_PESO, resultado.motivo());
            verifyNoInteractions(repo, clima, aerocivil);
        }
    }

    @Test
    @DisplayName("flujo alterno · clima adverso: no consulta la Aerocivil")
    void climaAdverso() {
        when(clima.condicionesAptas("ECI", "UNAL")).thenReturn(false);

        assertEquals(MotivoMision.CLIMA_ADVERSO, crearNormal().motivo());
        verify(aerocivil, never()).autoriza(any(PlanVuelo.class));
    }

    @Test
    @DisplayName("si el servicio del clima falla, se rechaza por clima (falla segura)")
    void climaLanzaExcepcion() {
        when(clima.condicionesAptas("ECI", "UNAL")).thenThrow(new IllegalStateException("sin red"));

        assertEquals(MotivoMision.CLIMA_ADVERSO, crearNormal().motivo());
    }

    @Test
    @DisplayName("flujo alterno · sin drones disponibles")
    void sinDrones() {
        when(repo.findDisponibles("ECI")).thenReturn(List.of());

        assertEquals(MotivoMision.SIN_DRONE_DISPONIBLE, crearNormal().motivo());
        verify(aerocivil, never()).autoriza(any(PlanVuelo.class));
    }

    @Test
    @DisplayName("flujo alterno · la Aerocivil rechaza")
    void aerocivilRechaza() {
        when(aerocivil.autoriza(any(PlanVuelo.class))).thenReturn(false);

        assertEquals(MotivoMision.AEROCIVIL_RECHAZA, crearNormal().motivo());
    }

    @Test
    @DisplayName("la Aerocivil no responde: no despega (RNF-10)")
    void aerocivilNoResponde() {
        when(aerocivil.autoriza(any(PlanVuelo.class))).thenThrow(new IllegalStateException("timeout"));

        assertEquals(MotivoMision.AEROCIVIL_NO_RESPONDE, crearNormal().motivo());
    }

    @Test
    @DisplayName("una ruta más larga que el radio efectivo se rechaza sin llamar a la Aerocivil")
    void radioExcedido() {
        when(sedes.distanciaKm("ECI", "UNAL")).thenReturn(OptionalDouble.of(35));

        assertEquals(MotivoMision.RADIO_EXCEDIDO, crearNormal().motivo());
        verify(aerocivil, never()).autoriza(any(PlanVuelo.class));
    }

    @Test
    @DisplayName("una altura de crucero sobre el máximo de la sede se rechaza")
    void alturaExcedida() {
        politica.registrarRestriccion("ECI", new RestriccionAerea(30, 80));

        assertEquals(MotivoMision.ALTURA_EXCEDIDA, crearNormal().motivo());
    }

    @Test
    @DisplayName("una sede sin restricción vigente de la Aerocivil no puede despegar")
    void sinRestriccionVigente() {
        when(sedes.distanciaKm("UNAL", "ECI")).thenReturn(OptionalDouble.of(9));
        when(clima.condicionesAptas("UNAL", "ECI")).thenReturn(true);
        when(repo.findDisponibles("UNAL")).thenReturn(List.of(new Drone("U-1", "UNAL", 80)));

        ResultadoMision resultado = caso.crear(new ComandoMision("UNAL", "ECI", 300, null));

        assertEquals(MotivoMision.SIN_RESTRICCION_VIGENTE, resultado.motivo());
    }

    @Test
    @DisplayName("sin ruta definida entre las sedes no se asigna nada")
    void rutaNoDefinida() {
        when(sedes.distanciaKm("ECI", "UNAL")).thenReturn(OptionalDouble.empty());

        assertEquals(MotivoMision.RUTA_NO_DEFINIDA, crearNormal().motivo());
        verifyNoInteractions(repo, clima, aerocivil);
    }

    @Test
    @DisplayName("el notificador recibe los eventos del asignador")
    void notificador_recibeElEvento() {
        crearNormal();

        ArgumentCaptor<EventoAsignacion> evento = ArgumentCaptor.forClass(EventoAsignacion.class);
        verify(notificador).alOcurrir(evento.capture());
        assertEquals(TipoEventoAsignacion.MISION_ASIGNADA, evento.getValue().tipo());
        assertEquals("M-1", evento.getValue().solicitudId());
    }

    @Test
    @DisplayName("el comando y el constructor exigen sus datos")
    void validaciones() {
        assertThrows(NullPointerException.class, () -> caso.crear(null));
        assertThrows(NullPointerException.class, () -> new CrearMision(null, repo, clima,
                new EstrategiaMayorBateria(), notificador, new AutorizadorVuelo(politica, aerocivil), () -> "x"));
        assertThrows(NullPointerException.class, () -> new CrearMision(sedes, repo, clima,
                new EstrategiaMayorBateria(), notificador, new AutorizadorVuelo(politica, aerocivil), null));
        assertThrows(IllegalArgumentException.class, () -> new ComandoMision(null, "UNAL", 100, null));
        assertThrows(IllegalArgumentException.class, () -> new ComandoMision(" ", "UNAL", 100, null));
        assertThrows(IllegalArgumentException.class, () -> new ComandoMision("ECI", null, 100, null));
        assertThrows(IllegalArgumentException.class, () -> new ComandoMision("ECI", "ECI", 100, null));
        assertThrows(IllegalArgumentException.class, () -> new ComandoMision("ECI", "UNAL", 0, null));
        assertThrows(IllegalArgumentException.class, () -> new ComandoMision("ECI", "UNAL", -5, null));
        assertEquals(PrioridadMision.NORMAL, new ComandoMision("ECI", "UNAL", 100, null).prioridad());
    }

    @Test
    @DisplayName("ResultadoMision: creada lleva drone, rechazada lleva motivo, nunca los dos ni ninguno")
    void resultado_invariantes() {
        Drone drone = new Drone("D-1", "ECI", 80);

        assertThrows(IllegalArgumentException.class, () -> new ResultadoMision("M-1", null, null));
        assertThrows(IllegalArgumentException.class,
                () -> new ResultadoMision("M-1", drone, MotivoMision.CLIMA_ADVERSO));
        assertTrue(ResultadoMision.creada("M-1", drone).creada());
        assertFalse(ResultadoMision.rechazada(MotivoMision.CLIMA_ADVERSO).creada());
    }

    @Test
    @DisplayName("todo motivo de rechazo de la Aerocivil tiene su motivo de misión y todos tienen mensaje")
    void motivos_cubrenLosDelDominio() {
        for (MotivoRechazo motivo : MotivoRechazo.values()) {
            assertDoesNotThrow(() -> MotivoMision.valueOf(motivo.name()));
        }
        for (MotivoMision motivo : MotivoMision.values()) {
            assertFalse(motivo.mensaje().isBlank());
        }
    }
}
