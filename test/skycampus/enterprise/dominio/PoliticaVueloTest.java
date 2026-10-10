package skycampus.enterprise.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("PoliticaVuelo: jerarquía Aerocivil > superadmin > coordinador")
class PoliticaVueloTest {

    private static final String SEDE = "ECI";
    private static final double DELTA = 0.0001;

    private PoliticaVuelo politica;

    @BeforeEach
    void crearPolitica() {
        politica = new PoliticaVuelo(20);
        politica.registrarRestriccion(SEDE, new RestriccionAerea(5, 120));
    }

    @Test
    @DisplayName("RF-12 / RNF-09: el coordinador pide más radio del que permite la Aerocivil y rige el de la Aerocivil")
    void configurarRadio_pedidoMayorQueLaAerocivil_rigeElLimiteDeLaAerocivil() {
        ConfiguracionRadio resultado = politica.configurarRadio(SEDE, 15);

        assertEquals(15, resultado.configuradoKm(), DELTA);
        assertEquals(5, resultado.efectivoKm(), DELTA);
        assertTrue(resultado.ajustado());
        assertEquals(5, politica.radioEfectivoKm(SEDE), DELTA);
    }

    @Test
    @DisplayName("RF-12: si el radio del coordinador cabe en los límites, rige tal cual")
    void configurarRadio_pedidoDentroDeLosLimites_rigeElPedido() {
        ConfiguracionRadio resultado = politica.configurarRadio(SEDE, 3);

        assertEquals(3, resultado.efectivoKm(), DELTA);
        assertFalse(resultado.ajustado());
    }

    @Test
    @DisplayName("RF-12: pedir exactamente el límite de la Aerocivil no cuenta como ajuste")
    void configurarRadio_pedidoIgualAlLimite_noEsAjuste() {
        assertFalse(politica.configurarRadio(SEDE, 5).ajustado());
    }

    @Test
    @DisplayName("RF-12: sin configuración del coordinador rige el menor entre el techo de la red y la Aerocivil")
    void radioEfectivo_sinConfiguracionDelCoordinador_usaLosLimitesSuperiores() {
        assertEquals(5, politica.radioEfectivoKm(SEDE), DELTA);
        politica.definirTechoRed(4);
        assertEquals(4, politica.radioEfectivoKm(SEDE), DELTA);
    }

    @Test
    @DisplayName("RF-13 / C-02: el techo que fija el superadmin pesa más que el radio del coordinador")
    void definirTechoRed_menorQueElRadioDelCoordinador_rigeElTecho() {
        politica.registrarRestriccion(SEDE, new RestriccionAerea(30, 120));
        politica.configurarRadio(SEDE, 15);
        assertEquals(15, politica.radioEfectivoKm(SEDE), DELTA);

        politica.definirTechoRed(10);

        assertEquals(10, politica.radioEfectivoKm(SEDE), DELTA);
    }

    @Test
    @DisplayName("C-01: si la Aerocivil reduce su límite después de que el coordinador configuró, el nuevo límite rige")
    void radioEfectivo_laAerocivilReduceElLimiteDespues_seSigueCumpliendo() {
        politica.registrarRestriccion(SEDE, new RestriccionAerea(30, 120));
        politica.configurarRadio(SEDE, 15);
        assertEquals(15, politica.radioEfectivoKm(SEDE), DELTA);

        politica.registrarRestriccion(SEDE, new RestriccionAerea(6, 120));

        assertEquals(6, politica.radioEfectivoKm(SEDE), DELTA);
    }

    @Test
    @DisplayName("sin restricción de la Aerocivil registrada, la sede no puede volar (radio 0, altura 0)")
    void sinRestriccion_noSePuedeVolar() {
        assertFalse(politica.tieneRestriccion("UNAL"));
        assertEquals(0, politica.radioEfectivoKm("UNAL"), DELTA);
        assertEquals(0, politica.alturaMaximaM("UNAL", true));
        assertEquals(0, politica.alturaMaximaM("UNAL", false));
        assertEquals(0, politica.configurarRadio("UNAL", 8).efectivoKm(), DELTA);
        assertTrue(politica.tieneRestriccion(SEDE));
    }

    @ParameterizedTest(name = "zona urbana, la Aerocivil permite {0} m -> {1} m")
    @CsvSource({"200,120", "120,120", "100,100"})
    @DisplayName("RNF-09: en zona urbana la altura nunca pasa de 120 m")
    void alturaMaxima_zonaUrbana_noPasaDe120(int permitidaAerocivil, int esperada) {
        politica.registrarRestriccion(SEDE, new RestriccionAerea(5, permitidaAerocivil));

        assertEquals(esperada, politica.alturaMaximaM(SEDE, true));
    }

    @Test
    @DisplayName("fuera de zona urbana rige la altura que fija la Aerocivil")
    void alturaMaxima_zonaNoUrbana_usaLaDeLaAerocivil() {
        politica.registrarRestriccion(SEDE, new RestriccionAerea(5, 200));

        assertEquals(200, politica.alturaMaximaM(SEDE, false));
    }

    @ParameterizedTest
    @ValueSource(doubles = {0, -1})
    @DisplayName("los radios deben ser mayores que 0")
    void radiosInvalidos_lanzaExcepcion(double km) {
        assertThrows(IllegalArgumentException.class, () -> new PoliticaVuelo(km));
        assertThrows(IllegalArgumentException.class, () -> politica.definirTechoRed(km));
        assertThrows(IllegalArgumentException.class, () -> politica.configurarRadio(SEDE, km));
        assertThrows(IllegalArgumentException.class, () -> new RestriccionAerea(km, 120));
    }

    @Test
    @DisplayName("la altura de la restricción debe ser mayor que 0")
    void restriccionAerea_alturaInvalida_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> new RestriccionAerea(5, 0));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  "})
    @DisplayName("la sede es obligatoria en todas las operaciones")
    void sedeInvalida_lanzaExcepcion(String sede) {
        RestriccionAerea restriccion = new RestriccionAerea(5, 120);
        assertThrows(IllegalArgumentException.class, () -> politica.registrarRestriccion(sede, restriccion));
        assertThrows(IllegalArgumentException.class, () -> politica.configurarRadio(sede, 3));
        assertThrows(IllegalArgumentException.class, () -> politica.tieneRestriccion(sede));
        assertThrows(IllegalArgumentException.class, () -> politica.radioEfectivoKm(sede));
        assertThrows(IllegalArgumentException.class, () -> politica.alturaMaximaM(sede, true));
    }

    @Test
    @DisplayName("la restricción es obligatoria")
    void registrarRestriccion_nula_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> politica.registrarRestriccion(SEDE, null));
    }

    @Test
    @DisplayName("PlanVuelo y DecisionVuelo validan sus datos")
    void planYDecision_datosInvalidos_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> new PlanVuelo(" ", SEDE, 3, 50, true, false));
        assertThrows(IllegalArgumentException.class, () -> new PlanVuelo(null, SEDE, 3, 50, true, false));
        assertThrows(IllegalArgumentException.class, () -> new PlanVuelo("M-1", null, 3, 50, true, false));
        assertThrows(IllegalArgumentException.class, () -> new PlanVuelo("M-1", " ", 3, 50, true, false));
        assertThrows(IllegalArgumentException.class, () -> new PlanVuelo("M-1", SEDE, 0, 50, true, false));
        assertThrows(IllegalArgumentException.class, () -> new PlanVuelo("M-1", SEDE, 3, 0, true, false));
        assertThrows(IllegalArgumentException.class, () -> new DecisionVuelo(true, MotivoRechazo.RADIO_EXCEDIDO));
        assertThrows(IllegalArgumentException.class, () -> new DecisionVuelo(false, null));
        assertTrue(DecisionVuelo.autorizada().autorizado());
        assertEquals(MotivoRechazo.ALTURA_EXCEDIDA, DecisionVuelo.rechazada(MotivoRechazo.ALTURA_EXCEDIDA).motivo());
    }
}
