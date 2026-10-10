package skycampus.enterprise.ruta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static skycampus.enterprise.ruta.RutasTestUtil.BIBLIOTECA;
import static skycampus.enterprise.ruta.RutasTestUtil.ECI;
import static skycampus.enterprise.ruta.RutasTestUtil.ESTACION_NORTE;
import static skycampus.enterprise.ruta.RutasTestUtil.ESTACION_SUR;
import static skycampus.enterprise.ruta.RutasTestUtil.UNAL;
import static skycampus.enterprise.ruta.RutasTestUtil.contexto;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import skycampus.enterprise.ruta.drone.FabricaDroneEstandar;
import skycampus.enterprise.ruta.evento.AlertaFalloDeEtapa;
import skycampus.enterprise.ruta.evento.NotificadorRuta;
import skycampus.enterprise.ruta.evento.RegistroDeEtapas;
import skycampus.enterprise.ruta.evento.TipoEventoRuta;
import skycampus.enterprise.ruta.optimizacion.MenorDistancia;
import skycampus.enterprise.ruta.optimizacion.MenosParadasDeCarga;

@DisplayName("PlanificadorRutas y GestorMisiones")
class PlanificadorYGestorTest {

    private static final double ALCANCE_MAXIMO_KM = 30;
    private static final List<Punto> ESTACIONES = List.of(ESTACION_NORTE, ESTACION_SUR);

    private final PlanificadorRutas planificador = new PlanificadorRutas(new MenorDistancia(), ALCANCE_MAXIMO_KM);

    @Test
    @DisplayName("planificar_destinoCercano_eligeLaRutaDirecta")
    void planificar_destinoCercano_eligeLaRutaDirecta() {
        Ruta ruta = planificador.planificar(ECI, BIBLIOTECA, ESTACIONES).orElseThrow();

        assertEquals(List.of(ECI, BIBLIOTECA), ruta.puntos());
        assertEquals(0, ruta.paradasDeCarga());
    }

    @Test
    @DisplayName("planificar_destinoMasLejosQueElAlcance_usaLaEstacionMasCorta")
    void planificar_destinoMasLejosQueElAlcance_usaLaEstacionMasCorta() {
        Ruta ruta = planificador.planificar(ECI, UNAL, ESTACIONES).orElseThrow();

        assertEquals(List.of(ECI, ESTACION_NORTE, UNAL), ruta.puntos());
        assertEquals(1, ruta.paradasDeCarga());
    }

    @Test
    @DisplayName("planificar_otraEstrategia_puedeCambiarLaEleccion")
    void planificar_otraEstrategia_puedeCambiarLaEleccion() {
        Punto destino = new Punto("Cerca", 20, 0);
        PlanificadorRutas sinCargas = new PlanificadorRutas(new MenosParadasDeCarga(), ALCANCE_MAXIMO_KM);

        assertEquals(0, sinCargas.planificar(ECI, destino, ESTACIONES).orElseThrow().paradasDeCarga());
    }

    @Test
    @DisplayName("planificar_ningunaRutaCabeEnElAlcance_devuelveVacio")
    void planificar_ningunaRutaCabeEnElAlcance_devuelveVacio() {
        assertTrue(planificador.planificar(ECI, UNAL, List.of()).isEmpty());
    }

    @Test
    @DisplayName("planificar_tramoIgualAlAlcanceMaximo_seConsideraPosible")
    void planificar_tramoIgualAlAlcanceMaximo_seConsideraPosible() {
        Punto justoEnElLimite = new Punto("Límite", ALCANCE_MAXIMO_KM, 0);

        Ruta ruta = planificador.planificar(ECI, justoEnElLimite, List.of()).orElseThrow();

        assertEquals(List.of(ECI, justoEnElLimite), ruta.puntos());
    }

    @Test
    @DisplayName("planificar_estacionIgualAlOrigenOAlDestino_laIgnora")
    void planificar_estacionIgualAlOrigenOAlDestino_laIgnora() {
        List<Punto> estaciones = List.of(ECI, UNAL, ESTACION_NORTE);

        Ruta ruta = planificador.planificar(ECI, UNAL, estaciones).orElseThrow();

        assertEquals(List.of(ECI, ESTACION_NORTE, UNAL), ruta.puntos());
    }

    @Test
    @DisplayName("planificar_datosInvalidos_lanzaExcepcion")
    void planificar_datosInvalidos_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> planificador.planificar(null, UNAL, ESTACIONES));
        assertThrows(IllegalArgumentException.class, () -> planificador.planificar(ECI, null, ESTACIONES));
        assertThrows(IllegalArgumentException.class, () -> planificador.planificar(ECI, UNAL, null));
        assertThrows(IllegalArgumentException.class, () -> planificador.planificar(ECI, ECI, ESTACIONES));
        assertThrows(IllegalArgumentException.class, () -> new PlanificadorRutas(null, 10));
        assertThrows(IllegalArgumentException.class, () -> new PlanificadorRutas(new MenorDistancia(), 0));
    }

    @Test
    @DisplayName("despachar_viajeEntreSedes_planificaEjecutaYAvisaCadaEtapa")
    void despachar_viajeEntreSedes_planificaEjecutaYAvisaCadaEtapa() {
        RegistroDeEtapas registro = new RegistroDeEtapas();
        GestorMisiones gestor = new GestorMisiones(planificador, contexto(new FabricaDroneEstandar(), registro));

        ResultadoEjecucion resultado = gestor.despachar(ECI, UNAL, ESTACIONES);

        assertEquals(ResultadoEjecucion.exito(3), resultado);
        assertEquals(6, registro.eventos().size());
    }

    @Test
    @DisplayName("despachar_rutaSimpleYCompuesta_seEjecutanDeLaMismaManera")
    void despachar_rutaSimpleYCompuesta_seEjecutanDeLaMismaManera() {
        RegistroDeEtapas registro = new RegistroDeEtapas();
        GestorMisiones gestor = new GestorMisiones(planificador, contexto(new FabricaDroneEstandar(), registro));

        ResultadoEjecucion simple = gestor.ejecutar(new TramoSimple(ECI, BIBLIOTECA));
        ResultadoEjecucion compuesta = gestor.ejecutar(RutasTestUtil.eciUnalConCarga());

        assertEquals(ResultadoEjecucion.exito(1), simple);
        assertEquals(ResultadoEjecucion.exito(3), compuesta);
    }

    @Test
    @DisplayName("despachar_sinRutaPosible_lanzaExcepcion")
    void despachar_sinRutaPosible_lanzaExcepcion() {
        GestorMisiones gestor = new GestorMisiones(planificador,
                contexto(new FabricaDroneEstandar(), new RegistroDeEtapas()));
        List<Punto> sinEstaciones = List.of();

        assertThrows(IllegalStateException.class, () -> gestor.despachar(ECI, UNAL, sinEstaciones));
    }

    @Test
    @DisplayName("ejecutar_etapaFallida_losObservadoresRecibenLaAlertaSinCambiarLaRuta")
    void ejecutar_etapaFallida_losObservadoresRecibenLaAlertaSinCambiarLaRuta() {
        List<String> avisos = new ArrayList<>();
        NotificadorRuta notificador = new NotificadorRuta();
        notificador.suscribir(new AlertaFalloDeEtapa(avisos::add));
        RegistroDeEtapas registro = new RegistroDeEtapas();
        notificador.suscribir(registro);
        GestorMisiones gestor = new GestorMisiones(planificador,
                new ContextoEjecucion(new FabricaDroneEstandar(), notificador));

        ResultadoEjecucion resultado = gestor.ejecutar(new TramoSimple(ECI, new Punto("Lejísimos", 99, 0)));

        assertEquals(ResultadoEjecucion.fallo(0), resultado);
        assertEquals(1, avisos.size());
        assertEquals(TipoEventoRuta.ETAPA_FALLIDA, registro.eventos().get(1).tipo());
    }

    @Test
    @DisplayName("gestor_datosInvalidos_lanzaExcepcion")
    void gestor_datosInvalidos_lanzaExcepcion() {
        ContextoEjecucion contexto = contexto(new FabricaDroneEstandar(), new RegistroDeEtapas());
        GestorMisiones gestor = new GestorMisiones(planificador, contexto);

        assertThrows(IllegalArgumentException.class, () -> new GestorMisiones(null, contexto));
        assertThrows(IllegalArgumentException.class, () -> new GestorMisiones(planificador, null));
        assertThrows(IllegalArgumentException.class, () -> gestor.ejecutar(null));
    }
}
