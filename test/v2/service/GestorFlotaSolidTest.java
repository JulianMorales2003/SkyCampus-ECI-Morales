package v2.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static v2.testutil.DatosV2.AHORA;
import static v2.testutil.DatosV2.drone;
import static v2.testutil.DatosV2.droneNoDisponible;
import static v2.testutil.DatosV2.solicitud;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import v2.asignacion.EstrategiaAsignacion;
import v2.asignacion.EstrategiaBateriaJusta;
import v2.asignacion.EstrategiaMayorBateria;
import v2.asignacion.EstrategiaTipoSegunPaquete;
import v2.eventos.SistemaLog;
import v2.model.Drone;
import v2.model.Mision;
import v2.model.Prioridad;
import v2.model.SolicitudMision;
import v2.model.TipoDrone;

@DisplayName("SOLID en v2: GestorFlota con cualquier EstrategiaAsignacion")
class GestorFlotaSolidTest {

    private final List<Drone> flota = List.of(
            droneNoDisponible("D-00", TipoDrone.MINI, 99),
            drone("D-01", TipoDrone.MINI, 95),
            drone("D-02", TipoDrone.MINI, 40),
            drone("D-15", TipoDrone.EXPRESS, 55));
    private final SolicitudMision pedido = solicitud("M-1", 300, Prioridad.NORMAL);

    /** Estrategia escrita solo para esta prueba: GestorFlota no la conoce de antemano. */
    private static class PrimeroDeLaLista implements EstrategiaAsignacion {
        @Override
        public Optional<Drone> seleccionar(SolicitudMision solicitud, List<Drone> flota) {
            return EstrategiaAsignacion.candidatos(flota).findFirst();
        }
    }

    /** Estrategia espía: guarda con qué argumentos la llamó el gestor. */
    private static class Espia implements EstrategiaAsignacion {
        private SolicitudMision solicitudRecibida;
        private List<Drone> flotaRecibida;

        @Override
        public Optional<Drone> seleccionar(SolicitudMision solicitud, List<Drone> flota) {
            this.solicitudRecibida = solicitud;
            this.flotaRecibida = flota;
            return Optional.empty();
        }
    }

    static Stream<EstrategiaAsignacion> todasLasEstrategias() {
        return Stream.of(
                new EstrategiaMayorBateria(),
                new EstrategiaTipoSegunPaquete(),
                new EstrategiaBateriaJusta(),
                new PrimeroDeLaLista(),
                (solicitud, f) -> f.stream().filter(d -> d.id().equals("D-02")).findFirst());
    }

    @Test
    @DisplayName("asignar_estrategiaDefinidaEnLaPrueba_elGestorUsaSuEleccion")
    void asignar_estrategiaDefinidaEnLaPrueba_elGestorUsaSuEleccion() {
        GestorFlota gestor = new GestorFlota(new PrimeroDeLaLista());

        Mision mision = gestor.asignar(pedido, flota, AHORA).orElseThrow();

        assertEquals("D-01", mision.drone().id());
    }

    @Test
    @DisplayName("asignar_estrategiaComoLambda_elGestorUsaSuEleccion")
    void asignar_estrategiaComoLambda_elGestorUsaSuEleccion() {
        GestorFlota gestor = new GestorFlota((solicitud, f) -> Optional.of(f.get(3)));

        assertEquals("D-15", gestor.asignar(pedido, flota, AHORA).orElseThrow().drone().id());
    }

    @Test
    @DisplayName("asignar_estrategiaQueNuncaEncuentraDrone_devuelveVacioYAvisaQueNoHay")
    void asignar_estrategiaQueNuncaEncuentraDrone_devuelveVacioYAvisaQueNoHay() {
        GestorFlota gestor = new GestorFlota((solicitud, f) -> Optional.empty());
        SistemaLog log = new SistemaLog();
        gestor.suscribir(log);

        assertTrue(gestor.asignar(pedido, flota, AHORA).isEmpty());
        assertTrue(log.registros().get(0).contains("SIN_DRONE_DISPONIBLE"));
    }

    @Test
    @DisplayName("asignar_conEstrategiaEspia_delegaLaDecisionConLosMismosArgumentos")
    void asignar_conEstrategiaEspia_delegaLaDecisionConLosMismosArgumentos() {
        Espia espia = new Espia();
        GestorFlota gestor = new GestorFlota(espia);
        List<Drone> flotaModificable = new ArrayList<>(flota);

        gestor.asignar(pedido, flotaModificable, AHORA);

        assertSame(pedido, espia.solicitudRecibida);
        assertSame(flotaModificable, espia.flotaRecibida);
    }

    @ParameterizedTest(name = "estrategia #{index}")
    @MethodSource("todasLasEstrategias")
    @DisplayName("asignar_cualquierEstrategia_laMisionLlevaExactamenteElDroneQueEligioLaEstrategia")
    void asignar_cualquierEstrategia_laMisionLlevaExactamenteElDroneQueEligioLaEstrategia(
            EstrategiaAsignacion estrategia) {
        Optional<Drone> esperado = estrategia.seleccionar(pedido, flota);
        GestorFlota gestor = new GestorFlota(estrategia);

        Optional<Mision> mision = gestor.asignar(pedido, flota, AHORA);

        assertEquals(esperado, mision.map(Mision::drone));
    }

    @ParameterizedTest(name = "estrategia #{index}")
    @MethodSource("todasLasEstrategias")
    @DisplayName("seleccionar_cualquierEstrategia_cumpleElContrato_droneDeLaFlotaYDisponible")
    void seleccionar_cualquierEstrategia_cumpleElContrato_droneDeLaFlotaYDisponible(
            EstrategiaAsignacion estrategia) {
        Optional<Drone> elegido = estrategia.seleccionar(pedido, flota);

        elegido.ifPresent(drone -> {
            assertTrue(flota.contains(drone), "El drone elegido debe pertenecer a la flota.");
            assertTrue(drone.disponible(), "El drone elegido debe estar disponible.");
        });
    }

    @Test
    @DisplayName("cambiarEstrategia_unSoloGestorConVariasEstrategias_cadaUnaDecideASuManera")
    void cambiarEstrategia_unSoloGestorConVariasEstrategias_cadaUnaDecideASuManera() {
        GestorFlota gestor = new GestorFlota(new EstrategiaMayorBateria());
        List<String> elegidos = new ArrayList<>();

        for (EstrategiaAsignacion estrategia : List.of(new EstrategiaMayorBateria(),
                new EstrategiaBateriaJusta(), new PrimeroDeLaLista(),
                new EstrategiaTipoSegunPaquete())) {
            gestor.cambiarEstrategia(estrategia);
            elegidos.add(gestor.asignar(pedido, flota, AHORA).orElseThrow().drone().id());
        }

        assertEquals(List.of("D-01", "D-02", "D-01", "D-01"), elegidos);
    }

    @Test
    @DisplayName("gestorFlota_dependenciasDeclaradas_sonAbstraccionesYNoClasesConcretas")
    void gestorFlota_dependenciasDeclaradas_sonAbstraccionesYNoClasesConcretas() throws NoSuchFieldException {
        Constructor<?> constructor = GestorFlota.class.getConstructors()[0];
        Field estrategia = GestorFlota.class.getDeclaredField("estrategia");

        assertEquals(EstrategiaAsignacion.class, constructor.getParameterTypes()[0]);
        assertEquals(EstrategiaAsignacion.class, estrategia.getType());
        assertTrue(EstrategiaAsignacion.class.isInterface());
        for (Field campo : GestorFlota.class.getDeclaredFields()) {
            assertTrue(campo.getType().isInterface(),
                    "El campo " + campo.getName() + " debería ser una interfaz, es " + campo.getType());
        }
    }
}
