package skycampus.enterprise.analytics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static skycampus.v2.model.EstadoMision.COMPLETADA;
import static skycampus.v2.model.EstadoMision.EN_VUELO;
import static skycampus.v2.model.EstadoMision.FALLIDA;
import static skycampus.v2.model.Prioridad.BAJO;
import static skycampus.v2.model.Prioridad.NORMAL;
import static skycampus.v2.model.Prioridad.URGENTE;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import skycampus.enterprise.model.MisionRed;
import skycampus.enterprise.model.Sede;
import skycampus.v2.model.EstadoMision;
import skycampus.v2.model.Prioridad;

@DisplayName("AnalyticsRed")
class AnalyticsRedTest {

    private static final double DELTA = 1e-9;

    private static final Sede ECI = new Sede("ECI");
    private static final Sede UNAL = new Sede("UNAL");
    private static final Sede UNIANDES = new Sede("Uniandes");
    private static final Sede EAFIT = new Sede("EAFIT");

    private static MisionRed mision(String id, Sede sede, String drone, EstadoMision estado,
                                    Prioridad prioridad, long minutos) {
        return new MisionRed(id, sede, drone, estado, prioridad, minutos);
    }

    /** Resultado esperado de una sede con actividad. */
    private record Esperado(long total, long entregadas, double tasaExito, Double tiempoPromedio,
                            String droneMasUtilizado, double porcentajeUrgentes) {
    }

    private record Escenario(String nombre, List<Sede> sedes, List<MisionRed> misiones,
                             Map<Sede, Esperado> esperados, List<Sede> ranking) {
        @Override
        public String toString() {
            return nombre;
        }
    }

    static Stream<Arguments> escenarios() {
        Escenario sedeVacia = new Escenario("sede vacía: sin misiones no hay métricas",
                List.of(ECI), List.of(), Map.of(), List.of(ECI));

        Escenario unaMision = new Escenario("sede con 1 misión: todas las métricas salen de ese dato",
                List.of(ECI),
                List.of(mision("M-1", ECI, "D-01", COMPLETADA, URGENTE, 25)),
                Map.of(ECI, new Esperado(1, 1, 1.0, 25.0, "D-01", 100.0)),
                List.of(ECI));

        Escenario empate = new Escenario("empate entre sedes: desempata por entregadas y luego por nombre",
                List.of(ECI, EAFIT, UNAL),
                List.of(
                        mision("M-1", UNAL, "D-10", COMPLETADA, NORMAL, 30),
                        mision("M-2", UNAL, "D-10", COMPLETADA, NORMAL, 50),
                        mision("M-3", ECI, "D-01", COMPLETADA, NORMAL, 30),
                        mision("M-4", EAFIT, "D-20", COMPLETADA, NORMAL, 30)),
                Map.of(
                        UNAL, new Esperado(2, 2, 1.0, 40.0, "D-10", 0.0),
                        ECI, new Esperado(1, 1, 1.0, 30.0, "D-01", 0.0),
                        EAFIT, new Esperado(1, 1, 1.0, 30.0, "D-20", 0.0)),
                List.of(UNAL, EAFIT, ECI));

        Escenario sinEntregas = new Escenario("sede sin entregas: tasa 0 y sin tiempo promedio",
                List.of(UNIANDES),
                List.of(
                        mision("M-1", UNIANDES, "D-30", FALLIDA, NORMAL, 0),
                        mision("M-2", UNIANDES, "D-31", EN_VUELO, URGENTE, 0)),
                Map.of(UNIANDES, new Esperado(2, 0, 0.0, null, "D-30", 50.0)),
                List.of(UNIANDES));

        Escenario redCompleta = new Escenario("red completa: 4 sedes, estados y prioridades mezclados",
                List.of(ECI, UNAL, UNIANDES, EAFIT),
                List.of(
                        mision("M-1", ECI, "D-01", COMPLETADA, URGENTE, 20),
                        mision("M-2", ECI, "D-01", COMPLETADA, NORMAL, 30),
                        mision("M-3", ECI, "D-02", FALLIDA, NORMAL, 0),
                        mision("M-4", ECI, "D-01", EN_VUELO, BAJO, 0),
                        mision("M-5", UNAL, "D-10", COMPLETADA, NORMAL, 40),
                        mision("M-6", UNAL, "D-11", COMPLETADA, URGENTE, 20),
                        mision("M-7", UNAL, "D-10", COMPLETADA, BAJO, 60),
                        mision("M-8", UNIANDES, "D-21", FALLIDA, URGENTE, 0),
                        mision("M-9", UNIANDES, "D-20", FALLIDA, NORMAL, 0)),
                Map.of(
                        ECI, new Esperado(4, 2, 0.5, 25.0, "D-01", 25.0),
                        UNAL, new Esperado(3, 3, 1.0, 40.0, "D-10", 100.0 / 3),
                        UNIANDES, new Esperado(2, 0, 0.0, null, "D-20", 50.0)),
                List.of(UNAL, ECI, UNIANDES, EAFIT));

        return Stream.of(sedeVacia, unaMision, empate, sinEntregas, redCompleta).map(Arguments::of);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("escenarios")
    @DisplayName("eficienciaPorSede_escenarios_calculaLasCuatroMetricasPorSede")
    void eficienciaPorSede_escenarios_calculaLasCuatroMetricasPorSede(Escenario escenario) {
        Map<Sede, Optional<EficienciaSede>> resultado =
                AnalyticsRed.eficienciaPorSede(escenario.sedes(), escenario.misiones());

        assertEquals(escenario.sedes(), List.copyOf(resultado.keySet()));
        for (Sede sede : escenario.sedes()) {
            Esperado esperado = escenario.esperados().get(sede);
            Optional<EficienciaSede> obtenido = resultado.get(sede);
            if (esperado == null) {
                assertTrue(obtenido.isEmpty(), "La sede " + sede.nombre() + " no debería tener métricas");
            } else {
                verificar(esperado, obtenido.orElseThrow());
            }
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("escenarios")
    @DisplayName("ranking_escenarios_ordenaPorTasaLuegoEntregadasLuegoNombre")
    void ranking_escenarios_ordenaPorTasaLuegoEntregadasLuegoNombre(Escenario escenario) {
        assertEquals(escenario.ranking(), AnalyticsRed.ranking(escenario.sedes(), escenario.misiones()));
    }

    private static void verificar(Esperado esperado, EficienciaSede obtenido) {
        assertEquals(esperado.total(), obtenido.totalMisiones());
        assertEquals(esperado.entregadas(), obtenido.entregadas());
        assertEquals(esperado.tasaExito(), obtenido.tasaExito(), DELTA);
        assertEquals(esperado.droneMasUtilizado(), obtenido.droneMasUtilizado());
        assertEquals(esperado.porcentajeUrgentes(), obtenido.porcentajeUrgentes(), DELTA);
        OptionalDouble tiempo = obtenido.tiempoPromedioEntregaMinutos();
        if (esperado.tiempoPromedio() == null) {
            assertTrue(tiempo.isEmpty());
        } else {
            assertEquals(esperado.tiempoPromedio(), tiempo.orElseThrow(), DELTA);
        }
    }

    @Test
    @DisplayName("eficienciaPorSede_sedesRepetidas_lasDevuelveUnaSolaVezEnElOrdenRecibido")
    void eficienciaPorSede_sedesRepetidas_lasDevuelveUnaSolaVezEnElOrdenRecibido() {
        Map<Sede, Optional<EficienciaSede>> resultado =
                AnalyticsRed.eficienciaPorSede(List.of(UNAL, ECI, UNAL), List.of());

        assertEquals(List.of(UNAL, ECI), List.copyOf(resultado.keySet()));
    }

    @Test
    @DisplayName("eficienciaPorSede_misionDeSedeAjena_lanzaExcepcion")
    void eficienciaPorSede_misionDeSedeAjena_lanzaExcepcion() {
        List<Sede> sedes = List.of(ECI);
        List<MisionRed> misiones = List.of(mision("M-1", UNAL, "D-10", COMPLETADA, NORMAL, 10));

        assertThrows(IllegalArgumentException.class, () -> AnalyticsRed.eficienciaPorSede(sedes, misiones));
    }

    @Test
    @DisplayName("eficienciaPorSede_argumentosNulos_lanzaExcepcion")
    void eficienciaPorSede_argumentosNulos_lanzaExcepcion() {
        List<Sede> sedes = List.of(ECI);
        List<MisionRed> misiones = List.of();

        assertThrows(IllegalArgumentException.class, () -> AnalyticsRed.eficienciaPorSede(null, misiones));
        assertThrows(IllegalArgumentException.class, () -> AnalyticsRed.eficienciaPorSede(sedes, null));
    }

    @Test
    @DisplayName("tiempoPromedio_soloCuentaLasMisionesEntregadas")
    void tiempoPromedio_soloCuentaLasMisionesEntregadas() {
        List<MisionRed> misiones = List.of(
                mision("M-1", ECI, "D-01", COMPLETADA, NORMAL, 10),
                mision("M-2", ECI, "D-01", FALLIDA, NORMAL, 500),
                mision("M-3", ECI, "D-01", COMPLETADA, NORMAL, 20));

        EficienciaSede eficiencia = AnalyticsRed.eficienciaPorSede(List.of(ECI), misiones).get(ECI).orElseThrow();

        assertEquals(15.0, eficiencia.tiempoPromedioEntregaMinutos().orElseThrow(), DELTA);
    }

    @Test
    @DisplayName("recolector_combinarDosMitades_daElMismoResultadoQueLaListaCompleta")
    void recolector_combinarDosMitades_daElMismoResultadoQueLaListaCompleta() {
        List<MisionRed> misiones = List.of(
                mision("M-1", ECI, "D-01", COMPLETADA, URGENTE, 20),
                mision("M-2", ECI, "D-02", COMPLETADA, NORMAL, 30),
                mision("M-3", ECI, "D-02", FALLIDA, NORMAL, 0),
                mision("M-4", ECI, "D-02", COMPLETADA, BAJO, 10));
        var recolector = AcumuladorSede.recolector();

        AcumuladorSede primera = recolector.supplier().get();
        AcumuladorSede segunda = recolector.supplier().get();
        misiones.subList(0, 2).forEach(m -> recolector.accumulator().accept(primera, m));
        misiones.subList(2, 4).forEach(m -> recolector.accumulator().accept(segunda, m));
        EficienciaSede combinada = recolector.finisher().apply(recolector.combiner().apply(primera, segunda));

        EficienciaSede completa = misiones.stream().collect(recolector);
        assertEquals(completa, combinada);
        assertEquals("D-02", combinada.droneMasUtilizado());
    }

    @Test
    @DisplayName("recolector_combinarConAcumuladorVacio_tomaLaSedeDelOtro")
    void recolector_combinarConAcumuladorVacio_tomaLaSedeDelOtro() {
        var recolector = AcumuladorSede.recolector();
        AcumuladorSede vacio = recolector.supplier().get();
        AcumuladorSede conDatos = recolector.supplier().get();
        recolector.accumulator().accept(conDatos, mision("M-1", UNAL, "D-10", COMPLETADA, NORMAL, 5));

        EficienciaSede resultado = recolector.finisher().apply(recolector.combiner().apply(vacio, conDatos));

        assertEquals(UNAL, resultado.sede());
        assertEquals(1, resultado.totalMisiones());
    }

    @Test
    @DisplayName("ranking_unaSolaPasada_funcionaConStreamParalelo")
    void ranking_unaSolaPasada_funcionaConStreamParalelo() {
        List<MisionRed> misiones = Stream.iterate(1, n -> n + 1).limit(2000)
                .map(n -> mision("M-" + n, n % 2 == 0 ? ECI : UNAL, "D-" + (n % 7),
                        n % 5 == 0 ? FALLIDA : COMPLETADA, n % 3 == 0 ? URGENTE : NORMAL, n % 40))
                .toList();

        EficienciaSede secuencial = misiones.stream().filter(m -> m.sede().equals(ECI))
                .collect(AcumuladorSede.recolector());
        EficienciaSede paralelo = misiones.parallelStream().filter(m -> m.sede().equals(ECI))
                .collect(AcumuladorSede.recolector());

        assertEquals(secuencial, paralelo);
    }
}
