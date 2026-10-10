package skycampus.enterprise.aplicacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import skycampus.enterprise.dominio.DecisionVuelo;
import skycampus.enterprise.dominio.MotivoRechazo;
import skycampus.enterprise.dominio.PlanVuelo;
import skycampus.enterprise.dominio.PoliticaVuelo;
import skycampus.enterprise.dominio.RestriccionAerea;
import skycampus.enterprise.dominio.ServicioAerocivil;

/** La Aerocivil se reemplaza por un doble de Mockito: no hay llamada HTTP real. */
@ExtendWith(MockitoExtension.class)
@DisplayName("AutorizadorVuelo (RF-16, RNF-09, RNF-10)")
class AutorizadorVueloTest {

    private static final String SEDE = "ECI";

    @Mock
    private ServicioAerocivil aerocivil;

    private PoliticaVuelo politica;
    private AutorizadorVuelo autorizador;

    @BeforeEach
    void preparar() {
        politica = new PoliticaVuelo(20);
        politica.registrarRestriccion(SEDE, new RestriccionAerea(5, 150));
        autorizador = new AutorizadorVuelo(politica, aerocivil);
    }

    private static PlanVuelo plan(double km, int alturaM, boolean urbana, boolean urgente) {
        return new PlanVuelo("M-1", SEDE, km, alturaM, urbana, urgente);
    }

    @Test
    @DisplayName("RF-16: con límites cumplidos y la Aerocivil de acuerdo, el vuelo se autoriza")
    void decidir_planValidoYAerocivilAutoriza_autoriza() {
        PlanVuelo plan = plan(4, 100, true, false);
        when(aerocivil.autoriza(plan)).thenReturn(true);

        assertEquals(DecisionVuelo.autorizada(), autorizador.decidir(plan));

        verify(aerocivil).autoriza(plan);
    }

    @Test
    @DisplayName("RF-16: si la Aerocivil rechaza, el vuelo no sale")
    void decidir_aerocivilRechaza_noSale() {
        PlanVuelo plan = plan(4, 100, true, false);
        when(aerocivil.autoriza(plan)).thenReturn(false);

        assertEquals(MotivoRechazo.AEROCIVIL_RECHAZA, autorizador.decidir(plan).motivo());
    }

    @Test
    @DisplayName("RNF-09: más de 120 m en zona urbana se rechaza sin molestar a la Aerocivil")
    void decidir_alturaSobre120EnZonaUrbana_rechaza() {
        DecisionVuelo decision = autorizador.decidir(plan(4, 121, true, false));

        assertEquals(MotivoRechazo.ALTURA_EXCEDIDA, decision.motivo());
        verifyNoMoreInteractions(aerocivil);
    }

    @Test
    @DisplayName("RNF-09: exactamente 120 m en zona urbana es válido (el límite cuenta)")
    void decidir_alturaIgualA120EnZonaUrbana_seConsultaALaAerocivil() {
        PlanVuelo plan = plan(4, 120, true, false);
        when(aerocivil.autoriza(plan)).thenReturn(true);

        assertEquals(DecisionVuelo.autorizada(), autorizador.decidir(plan));
    }

    @Test
    @DisplayName("fuera de zona urbana rige la altura de la Aerocivil (150 m)")
    void decidir_alturaFueraDeZonaUrbana_usaElLimiteDeLaAerocivil() {
        PlanVuelo valido = plan(4, 150, false, false);
        when(aerocivil.autoriza(valido)).thenReturn(true);

        assertEquals(DecisionVuelo.autorizada(), autorizador.decidir(valido));
        assertEquals(MotivoRechazo.ALTURA_EXCEDIDA, autorizador.decidir(plan(4, 151, false, false)).motivo());
    }

    @Test
    @DisplayName("C-01: el coordinador configuró 15 km pero la Aerocivil solo permite 5: un vuelo de 8 km se rechaza")
    void decidir_radioConfiguradoPorElCoordinadorQueViolaLaAerocivil_seRechaza() {
        politica.configurarRadio(SEDE, 15);

        DecisionVuelo decision = autorizador.decidir(plan(8, 100, true, false));

        assertEquals(MotivoRechazo.RADIO_EXCEDIDO, decision.motivo());
        verifyNoMoreInteractions(aerocivil);
    }

    @Test
    @DisplayName("el radio efectivo cuenta como válido justo en su límite")
    void decidir_distanciaIgualAlRadioEfectivo_seConsultaALaAerocivil() {
        PlanVuelo plan = plan(5, 100, true, false);
        when(aerocivil.autoriza(plan)).thenReturn(true);

        assertEquals(DecisionVuelo.autorizada(), autorizador.decidir(plan));
        assertEquals(MotivoRechazo.RADIO_EXCEDIDO, autorizador.decidir(plan(5.1, 100, true, false)).motivo());
    }

    @ParameterizedTest(name = "urgente = {0}")
    @ValueSource(booleans = {false, true})
    @DisplayName("C-03: la urgencia no salta ni la altura ni el radio")
    void decidir_urgenteONo_losLimitesDeSeguridadSeAplicanIgual(boolean urgente) {
        assertEquals(MotivoRechazo.ALTURA_EXCEDIDA, autorizador.decidir(plan(4, 130, true, urgente)).motivo());
        assertEquals(MotivoRechazo.RADIO_EXCEDIDO, autorizador.decidir(plan(9, 100, true, urgente)).motivo());
        verifyNoMoreInteractions(aerocivil);
    }

    @ParameterizedTest(name = "urgente = {0}")
    @ValueSource(booleans = {false, true})
    @DisplayName("C-03: la urgencia tampoco se salta la autorización de la Aerocivil")
    void decidir_urgenteONo_siemprePideLaAutorizacionDeLaAerocivil(boolean urgente) {
        PlanVuelo plan = plan(4, 100, true, urgente);
        when(aerocivil.autoriza(plan)).thenReturn(false);

        assertEquals(MotivoRechazo.AEROCIVIL_RECHAZA, autorizador.decidir(plan).motivo());

        verify(aerocivil).autoriza(plan);
    }

    @Test
    @DisplayName("RNF-10: si la Aerocivil falla o no responde, no se despega (20 de 20 intentos)")
    void decidir_aerocivilFalla_noSeDespega() {
        PlanVuelo plan = plan(4, 100, true, true);
        when(aerocivil.autoriza(plan)).thenThrow(new IllegalStateException("tiempo de espera agotado"));

        for (int intento = 0; intento < 20; intento++) {
            assertEquals(MotivoRechazo.AEROCIVIL_NO_RESPONDE, autorizador.decidir(plan).motivo());
        }
    }

    @Test
    @DisplayName("sin restricción registrada para la sede no se autoriza ningún vuelo")
    void decidir_sedeSinRestriccion_rechazaSinConsultar() {
        PlanVuelo plan = new PlanVuelo("M-2", "UNAL", 3, 50, true, false);

        assertEquals(MotivoRechazo.SIN_RESTRICCION_VIGENTE, autorizador.decidir(plan).motivo());
        verifyNoMoreInteractions(aerocivil);
    }

    @Test
    @DisplayName("el constructor y decidir exigen sus argumentos")
    void datosNulos_lanzaExcepcion() {
        assertThrows(NullPointerException.class, () -> new AutorizadorVuelo(null, aerocivil));
        assertThrows(NullPointerException.class, () -> new AutorizadorVuelo(politica, null));
        assertThrows(NullPointerException.class, () -> autorizador.decidir(null));
    }
}
