package skycampus.enterprise.ruta.drone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static skycampus.enterprise.ruta.RutasTestUtil.ECI;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import skycampus.enterprise.ruta.Punto;
import skycampus.enterprise.ruta.TramoSimple;
import skycampus.v2.model.TipoDrone;

@DisplayName("Factory Method: fábricas de drone por etapa")
class FabricasDroneTest {

    private static TramoSimple tramoDe(double km) {
        return new TramoSimple(ECI, new Punto("Destino", km, 0));
    }

    @ParameterizedTest(name = "estándar, tramo de {0} km -> {1}")
    @CsvSource({"0.5, MINI", "3, MINI", "3.1, EXPRESS", "15, EXPRESS", "15.1, CARGO", "30, CARGO"})
    @DisplayName("estandar_distintasDistancias_creaElDroneMasPequenoQueAlcance")
    void estandar_distintasDistancias_creaElDroneMasPequenoQueAlcance(double km, TipoDrone esperado) {
        assertEquals(esperado, new FabricaDroneEstandar().prepararPara(tramoDe(km)).tipo());
    }

    @Test
    @DisplayName("estandar_tramoMasLargoQueTodosLosAlcances_lanzaExcepcion")
    void estandar_tramoMasLargoQueTodosLosAlcances_lanzaExcepcion() {
        FabricaDroneEstandar fabrica = new FabricaDroneEstandar();
        TramoSimple tramo = tramoDe(30.1);

        assertThrows(TramoSinDroneException.class, () -> fabrica.prepararPara(tramo));
    }

    @ParameterizedTest(name = "urgente, tramo de {0} km -> EXPRESS")
    @CsvSource({"1", "3.1", "15"})
    @DisplayName("urgente_tramosDentroDelAlcance_siempreCreaExpress")
    void urgente_tramosDentroDelAlcance_siempreCreaExpress(double km) {
        assertEquals(TipoDrone.EXPRESS, new FabricaDroneUrgente().prepararPara(tramoDe(km)).tipo());
    }

    @Test
    @DisplayName("urgente_tramoMayorAlAlcanceDeExpress_lanzaExcepcion")
    void urgente_tramoMayorAlAlcanceDeExpress_lanzaExcepcion() {
        FabricaDroneUrgente fabrica = new FabricaDroneUrgente();
        TramoSimple tramo = tramoDe(15.1);

        assertThrows(TramoSinDroneException.class, () -> fabrica.prepararPara(tramo));
    }

    @Test
    @DisplayName("prepararPara_fabricaQueDevuelveUnDroneQueNoAlcanza_lanzaExcepcion")
    void prepararPara_fabricaQueDevuelveUnDroneQueNoAlcanza_lanzaExcepcion() {
        FabricaDroneEtapa fabricaDefectuosa = new FabricaDroneEtapa() {
            @Override
            protected DroneEtapa crearDrone(TramoSimple tramo) {
                return DroneEtapa.de(TipoDrone.MINI);
            }
        };
        TramoSimple tramoLargo = tramoDe(10);

        assertThrows(TramoSinDroneException.class, () -> fabricaDefectuosa.prepararPara(tramoLargo));
        assertThrows(IllegalArgumentException.class, () -> fabricaDefectuosa.prepararPara(null));
    }

    @Test
    @DisplayName("droneEtapa_datosInvalidos_lanzaExcepcion")
    void droneEtapa_datosInvalidos_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> new DroneEtapa(null, 5));
        assertThrows(IllegalArgumentException.class, () -> new DroneEtapa(TipoDrone.MINI, 0));
        assertThrows(IllegalArgumentException.class, () -> DroneEtapa.de(null));
    }
}
