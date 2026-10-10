package skycampus.v2.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static skycampus.v2.testutil.DatosV2.AHORA;
import static skycampus.v2.testutil.DatosV2.drone;
import static skycampus.v2.testutil.DatosV2.solicitud;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import skycampus.v2.asignacion.EstrategiaAsignacion;
import skycampus.v2.asignacion.EstrategiaBateriaJusta;
import skycampus.v2.asignacion.EstrategiaMayorBateria;
import skycampus.v2.asignacion.EstrategiaTipoSegunPaquete;
import skycampus.v2.eventos.ObservadorFlota;
import skycampus.v2.model.Drone;
import skycampus.v2.model.Prioridad;
import skycampus.v2.model.SolicitudMision;
import skycampus.v2.model.TipoDrone;

@DisplayName("Requisitos v2 verificables (RNF-04, RNF-05 y la tensión RF-07/RF-08)")
class RequisitosV2Test {

    private static final int DRONES_EN_FLOTA = 50;

    private static List<Drone> flotaDe(int cantidad) {
        List<Drone> flota = new ArrayList<>();
        for (int i = 1; i <= cantidad; i++) {
            TipoDrone tipo = TipoDrone.values()[i % TipoDrone.values().length];
            flota.add(drone(String.format("D-%02d", i), tipo, 20 + (i * 7) % 81));
        }
        return flota;
    }

    @Test
    @DisplayName("rnf04_asignacionAutomaticaConFlotaDe50_cadaEstrategiaTardaMenosDe500ms")
    void rnf04_asignacionAutomaticaConFlotaDe50_cadaEstrategiaTardaMenosDe500ms() {
        List<Drone> flota = flotaDe(DRONES_EN_FLOTA);
        SolicitudMision pedido = solicitud("M-1", 300, Prioridad.NORMAL);

        for (EstrategiaAsignacion estrategia : List.of(new EstrategiaMayorBateria(),
                new EstrategiaTipoSegunPaquete(), new EstrategiaBateriaJusta())) {
            GestorFlota gestor = new GestorFlota(estrategia);

            assertTimeout(Duration.ofMillis(500), () -> gestor.asignar(pedido, flota, AHORA),
                    "La asignación con " + estrategia.getClass().getSimpleName() + " superó 500 ms.");
        }
    }

    @Test
    @DisplayName("rnf05_cienFallosDeDrone_llegaCadaUnoATodosLosObservadoresEnMenosDeUnSegundo")
    void rnf05_cienFallosDeDrone_llegaCadaUnoATodosLosObservadoresEnMenosDeUnSegundo() {
        GestorFlota gestor = new GestorFlota(new EstrategiaMayorBateria());
        int[] recibidos = new int[3];
        for (int i = 0; i < recibidos.length; i++) {
            int posicion = i;
            ObservadorFlota observador = evento -> recibidos[posicion]++;
            gestor.suscribir(observador);
        }
        Drone averiado = drone("D-01", TipoDrone.MINI);

        assertTimeout(Duration.ofSeconds(1), () -> {
            for (int i = 0; i < 100; i++) {
                gestor.reportarFallo(averiado, AHORA);
            }
        });

        assertEquals(List.of(100, 100, 100), List.of(recibidos[0], recibidos[1], recibidos[2]));
    }

    @Test
    @DisplayName("rf07YRf08_misionUrgente_lasDosReglasEligenDronesDistintos")
    void rf07YRf08_misionUrgente_lasDosReglasEligenDronesDistintos() {
        List<Drone> flota = List.of(drone("D-01", TipoDrone.MINI, 95), drone("D-15", TipoDrone.EXPRESS, 45));
        SolicitudMision urgente = solicitud("M-2", 200, Prioridad.URGENTE);

        Optional<Drone> segunRf07 = new EstrategiaMayorBateria().seleccionar(urgente, flota);
        Optional<Drone> segunRf08 = new EstrategiaTipoSegunPaquete().seleccionar(urgente, flota);

        assertEquals("D-01", segunRf07.orElseThrow().id());
        assertEquals("D-15", segunRf08.orElseThrow().id());
        assertNotEquals(segunRf07, segunRf08);
    }
}
