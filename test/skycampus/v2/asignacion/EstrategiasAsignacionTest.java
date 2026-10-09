package skycampus.v2.asignacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static skycampus.v2.testutil.DatosV2.drone;
import static skycampus.v2.testutil.DatosV2.droneNoDisponible;
import static skycampus.v2.testutil.DatosV2.solicitud;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import skycampus.v2.model.Drone;
import skycampus.v2.model.EstadoDrone;
import skycampus.v2.model.Prioridad;
import skycampus.v2.model.SolicitudMision;
import skycampus.v2.model.TipoDrone;

@DisplayName("Estrategias de asignación v2")
class EstrategiasAsignacionTest {

    private final SolicitudMision normal = solicitud("M-1", 300, Prioridad.NORMAL);

    @Test
    @DisplayName("mayorBateria_variosDisponibles_eligeElDeMasBateria")
    void mayorBateria_variosDisponibles_eligeElDeMasBateria() {
        List<Drone> flota = List.of(drone("D-01", TipoDrone.MINI, 50), drone("D-02", TipoDrone.MINI, 90));

        Optional<Drone> elegido = new EstrategiaMayorBateria().seleccionar(normal, flota);

        assertEquals("D-02", elegido.orElseThrow().id());
    }

    @Test
    @DisplayName("mayorBateria_empateDeBateria_eligeElIdMenor")
    void mayorBateria_empateDeBateria_eligeElIdMenor() {
        List<Drone> flota = List.of(drone("D-09", TipoDrone.MINI, 80), drone("D-03", TipoDrone.MINI, 80));

        assertEquals("D-03", new EstrategiaMayorBateria().seleccionar(normal, flota).orElseThrow().id());
    }

    @Test
    @DisplayName("mayorBateria_ignoraNoDisponiblesYEstadoInconsistente_eligeSoloCandidatosReales")
    void mayorBateria_ignoraNoDisponiblesYEstadoInconsistente_eligeSoloCandidatosReales() {
        Drone enVuelo = droneNoDisponible("D-01", TipoDrone.MINI, 99);
        Drone conFallo = new Drone("D-02", TipoDrone.MINI, 98, true, EstadoDrone.FALLO);
        Drone bueno = drone("D-03", TipoDrone.MINI, 40);

        assertEquals("D-03", new EstrategiaMayorBateria()
                .seleccionar(normal, List.of(enVuelo, conFallo, bueno)).orElseThrow().id());
    }

    @Test
    @DisplayName("mayorBateria_sinCandidatos_devuelveVacio")
    void mayorBateria_sinCandidatos_devuelveVacio() {
        assertTrue(new EstrategiaMayorBateria()
                .seleccionar(normal, List.of(droneNoDisponible("D-01", TipoDrone.MINI, 90))).isEmpty());
    }

    @Test
    @DisplayName("mayorBateria_argumentosNulos_lanzaIllegalArgumentException")
    void mayorBateria_argumentosNulos_lanzaIllegalArgumentException() {
        EstrategiaMayorBateria estrategia = new EstrategiaMayorBateria();
        List<Drone> flota = List.of();

        assertThrows(IllegalArgumentException.class, () -> estrategia.seleccionar(null, flota));
        assertThrows(IllegalArgumentException.class, () -> estrategia.seleccionar(normal, null));
    }

    @Test
    @DisplayName("tipoSegunPaquete_urgente_eligeExpressAunqueHayaOtrosConMasBateria")
    void tipoSegunPaquete_urgente_eligeExpressAunqueHayaOtrosConMasBateria() {
        List<Drone> flota = List.of(drone("D-01", TipoDrone.MINI, 99), drone("D-15", TipoDrone.EXPRESS, 40));

        Optional<Drone> elegido = new EstrategiaTipoSegunPaquete()
                .seleccionar(solicitud("M-2", 200, Prioridad.URGENTE), flota);

        assertEquals("D-15", elegido.orElseThrow().id());
    }

    @Test
    @DisplayName("tipoSegunPaquete_paquetePesado_eligeCargo")
    void tipoSegunPaquete_paquetePesado_eligeCargo() {
        List<Drone> flota = List.of(drone("D-01", TipoDrone.MINI, 99), drone("D-11", TipoDrone.CARGO, 50));

        Optional<Drone> elegido = new EstrategiaTipoSegunPaquete()
                .seleccionar(solicitud("M-3", 1001, Prioridad.NORMAL), flota);

        assertEquals("D-11", elegido.orElseThrow().id());
    }

    @Test
    @DisplayName("tipoSegunPaquete_pesoExactoEnElLimite_sigueSiendoMini")
    void tipoSegunPaquete_pesoExactoEnElLimite_sigueSiendoMini() {
        List<Drone> flota = List.of(drone("D-01", TipoDrone.MINI, 60), drone("D-11", TipoDrone.CARGO, 90));

        Optional<Drone> elegido = new EstrategiaTipoSegunPaquete()
                .seleccionar(solicitud("M-4", EstrategiaTipoSegunPaquete.PESO_MAXIMO_MINI_GRAMOS, Prioridad.BAJO), flota);

        assertEquals("D-01", elegido.orElseThrow().id());
    }

    @Test
    @DisplayName("tipoSegunPaquete_noHayDroneDelTipoRequerido_devuelveVacio")
    void tipoSegunPaquete_noHayDroneDelTipoRequerido_devuelveVacio() {
        List<Drone> flota = List.of(drone("D-01", TipoDrone.MINI, 90));

        assertTrue(new EstrategiaTipoSegunPaquete()
                .seleccionar(solicitud("M-5", 200, Prioridad.URGENTE), flota).isEmpty());
    }

    @Test
    @DisplayName("tipoSegunPaquete_dosDelTipoRequerido_eligeElDeMasBateria")
    void tipoSegunPaquete_dosDelTipoRequerido_eligeElDeMasBateria() {
        List<Drone> flota = List.of(drone("D-01", TipoDrone.MINI, 30), drone("D-02", TipoDrone.MINI, 70));

        assertEquals("D-02", new EstrategiaTipoSegunPaquete().seleccionar(normal, flota).orElseThrow().id());
    }

    @Test
    @DisplayName("tipoSegunPaquete_argumentosNulos_lanzaIllegalArgumentException")
    void tipoSegunPaquete_argumentosNulos_lanzaIllegalArgumentException() {
        EstrategiaTipoSegunPaquete estrategia = new EstrategiaTipoSegunPaquete();
        List<Drone> flota = List.of();

        assertThrows(IllegalArgumentException.class, () -> estrategia.seleccionar(null, flota));
        assertThrows(IllegalArgumentException.class, () -> estrategia.seleccionar(normal, null));
    }

    @Test
    @DisplayName("bateriaJusta_variosSuficientes_eligeElDeMenorBateriaQueCumple")
    void bateriaJusta_variosSuficientes_eligeElDeMenorBateriaQueCumple() {
        List<Drone> flota = List.of(drone("D-01", TipoDrone.MINI, 95), drone("D-02", TipoDrone.MINI, 45),
                drone("D-03", TipoDrone.MINI, 20));

        assertEquals("D-02", new EstrategiaBateriaJusta().seleccionar(normal, flota).orElseThrow().id());
    }

    @Test
    @DisplayName("bateriaJusta_bateriaIgualAlMinimo_secuentaComoSuficiente")
    void bateriaJusta_bateriaIgualAlMinimo_secuentaComoSuficiente() {
        List<Drone> flota = List.of(drone("D-01", TipoDrone.MINI, 30), drone("D-02", TipoDrone.MINI, 29));

        assertEquals("D-01", new EstrategiaBateriaJusta(30).seleccionar(normal, flota).orElseThrow().id());
    }

    @Test
    @DisplayName("bateriaJusta_ningunoLlegaAlMinimo_devuelveVacio")
    void bateriaJusta_ningunoLlegaAlMinimo_devuelveVacio() {
        assertTrue(new EstrategiaBateriaJusta(50)
                .seleccionar(normal, List.of(drone("D-01", TipoDrone.MINI, 49))).isEmpty());
    }

    @Test
    @DisplayName("bateriaJusta_empateDeBateria_eligeElIdMenor")
    void bateriaJusta_empateDeBateria_eligeElIdMenor() {
        List<Drone> flota = List.of(drone("D-07", TipoDrone.MINI, 60), drone("D-02", TipoDrone.MINI, 60));

        assertEquals("D-02", new EstrategiaBateriaJusta().seleccionar(normal, flota).orElseThrow().id());
    }

    @Test
    @DisplayName("bateriaJusta_minimoFueraDeRango_lanzaIllegalArgumentException")
    void bateriaJusta_minimoFueraDeRango_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new EstrategiaBateriaJusta(-1));
        assertThrows(IllegalArgumentException.class, () -> new EstrategiaBateriaJusta(101));
    }

    @Test
    @DisplayName("bateriaJusta_argumentosNulos_lanzaIllegalArgumentException")
    void bateriaJusta_argumentosNulos_lanzaIllegalArgumentException() {
        EstrategiaBateriaJusta estrategia = new EstrategiaBateriaJusta();
        List<Drone> flota = List.of();

        assertThrows(IllegalArgumentException.class, () -> estrategia.seleccionar(null, flota));
        assertThrows(IllegalArgumentException.class, () -> estrategia.seleccionar(normal, null));
    }

    @Test
    @DisplayName("estrategias_mismaFlotaYSolicitud_puedenElegirDronesDistintos")
    void estrategias_mismaFlotaYSolicitud_puedenElegirDronesDistintos() {
        List<Drone> flota = List.of(drone("D-01", TipoDrone.MINI, 95), drone("D-02", TipoDrone.MINI, 40));

        String mayor = new EstrategiaMayorBateria().seleccionar(normal, flota).orElseThrow().id();
        String justa = new EstrategiaBateriaJusta().seleccionar(normal, flota).orElseThrow().id();

        assertEquals("D-01", mayor);
        assertEquals("D-02", justa);
    }
}
