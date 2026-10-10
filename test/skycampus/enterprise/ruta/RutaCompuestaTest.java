package skycampus.enterprise.ruta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static skycampus.enterprise.ruta.RutasTestUtil.BIBLIOTECA;
import static skycampus.enterprise.ruta.RutasTestUtil.EAFIT;
import static skycampus.enterprise.ruta.RutasTestUtil.ECI;
import static skycampus.enterprise.ruta.RutasTestUtil.ESTACION_NORTE;
import static skycampus.enterprise.ruta.RutasTestUtil.UNAL;
import static skycampus.enterprise.ruta.RutasTestUtil.contexto;
import static skycampus.enterprise.ruta.RutasTestUtil.eciUnalConCarga;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import skycampus.enterprise.ruta.drone.FabricaDroneEstandar;
import skycampus.enterprise.ruta.drone.FabricaDroneUrgente;
import skycampus.enterprise.ruta.evento.EventoRuta;
import skycampus.enterprise.ruta.evento.RegistroDeEtapas;
import skycampus.enterprise.ruta.evento.TipoEventoRuta;

@DisplayName("Composite: rutas simples y compuestas")
class RutaCompuestaTest {

    private static final double DELTA = 1e-9;

    @Test
    @DisplayName("tramoSimple_calculos_distanciaDuracionYSinParadas")
    void tramoSimple_calculos_distanciaDuracionYSinParadas() {
        TramoSimple tramo = new TramoSimple(ECI, BIBLIOTECA);

        assertEquals(2.0, tramo.distanciaKm(), DELTA);
        assertEquals(4, tramo.duracionMinutos());
        assertEquals(0, tramo.paradasDeCarga());
        assertEquals(2.0, tramo.tramoMasLargoKm(), DELTA);
        assertEquals(List.of(ECI, BIBLIOTECA), tramo.puntos());
        assertEquals("Tramo ECI -> Biblioteca", tramo.descripcion());
    }

    @Test
    @DisplayName("tramoSimple_duracion_redondeaHaciaArribaALosMinutosCompletos")
    void tramoSimple_duracion_redondeaHaciaArribaALosMinutosCompletos() {
        assertEquals(1, new TramoSimple(ECI, new Punto("X", 0.1, 0)).duracionMinutos());
    }

    @Test
    @DisplayName("tramoSimple_datosInvalidos_lanzaExcepcion")
    void tramoSimple_datosInvalidos_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> new TramoSimple(null, ECI));
        assertThrows(IllegalArgumentException.class, () -> new TramoSimple(ECI, null));
        assertThrows(IllegalArgumentException.class, () -> new TramoSimple(ECI, ECI));
    }

    @Test
    @DisplayName("paradaDeCarga_calculos_noSumaDistanciaYCuentaUnaParada")
    void paradaDeCarga_calculos_noSumaDistanciaYCuentaUnaParada() {
        ParadaDeCarga parada = new ParadaDeCarga(ESTACION_NORTE, 20);

        assertEquals(0.0, parada.distanciaKm(), DELTA);
        assertEquals(20, parada.duracionMinutos());
        assertEquals(1, parada.paradasDeCarga());
        assertEquals(0.0, parada.tramoMasLargoKm(), DELTA);
        assertEquals(List.of(ESTACION_NORTE), parada.puntos());
        assertEquals(ESTACION_NORTE, parada.inicio());
        assertEquals(ESTACION_NORTE, parada.fin());
        assertEquals("Carga en Estación Norte", parada.descripcion());
    }

    @Test
    @DisplayName("paradaDeCarga_datosInvalidos_lanzaExcepcion")
    void paradaDeCarga_datosInvalidos_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> new ParadaDeCarga(null, 10));
        assertThrows(IllegalArgumentException.class, () -> new ParadaDeCarga(ESTACION_NORTE, 0));
    }

    @Test
    @DisplayName("rutaCompuesta_tresEtapas_sumaDistanciaDuracionYParadas")
    void rutaCompuesta_tresEtapas_sumaDistanciaDuracionYParadas() {
        Ruta ruta = eciUnalConCarga();
        double tramo = Math.hypot(20, 5);

        assertEquals(2 * tramo, ruta.distanciaKm(), DELTA);
        assertEquals(2 * (int) Math.ceil(tramo * 2) + 20, ruta.duracionMinutos());
        assertEquals(1, ruta.paradasDeCarga());
        assertEquals(tramo, ruta.tramoMasLargoKm(), DELTA);
        assertEquals(ECI, ruta.inicio());
        assertEquals(UNAL, ruta.fin());
        assertEquals("Ruta ECI -> UNAL (3 etapas)", ruta.descripcion());
    }

    @Test
    @DisplayName("rutaCompuesta_puntos_noRepiteLaEstacionEnLaParada")
    void rutaCompuesta_puntos_noRepiteLaEstacionEnLaParada() {
        assertEquals(List.of(ECI, ESTACION_NORTE, UNAL), eciUnalConCarga().puntos());
    }

    @Test
    @DisplayName("rutaCompuesta_anidada_tieneTresNivelesYSeComportaComoUnaSola")
    void rutaCompuesta_anidada_tieneTresNivelesYSeComportaComoUnaSola() {
        Ruta primera = eciUnalConCarga();
        Ruta segunda = new TramoSimple(UNAL, new Punto("Uniandes", 60, 0));
        RutaCompuesta viaje = new RutaCompuesta(List.of(primera, segunda));

        assertEquals(2, viaje.etapas().size());
        assertEquals(primera.distanciaKm() + 20, viaje.distanciaKm(), DELTA);
        assertEquals(1, viaje.paradasDeCarga());
        assertEquals(Math.hypot(20, 5), viaje.tramoMasLargoKm(), DELTA);
        assertEquals(4, viaje.puntos().size());
        assertEquals("Uniandes", viaje.fin().nombre());
    }

    @Test
    @DisplayName("rutaCompuesta_datosInvalidos_lanzaExcepcion")
    void rutaCompuesta_datosInvalidos_lanzaExcepcion() {
        List<Ruta> vacia = List.of();
        List<Ruta> conNulo = Arrays.asList(new TramoSimple(ECI, BIBLIOTECA), null);
        List<Ruta> sinContinuidad = List.of(new TramoSimple(ECI, BIBLIOTECA), new TramoSimple(EAFIT, UNAL));

        assertThrows(IllegalArgumentException.class, () -> new RutaCompuesta(null));
        assertThrows(IllegalArgumentException.class, () -> new RutaCompuesta(vacia));
        assertThrows(IllegalArgumentException.class, () -> new RutaCompuesta(conNulo));
        assertThrows(IllegalArgumentException.class, () -> new RutaCompuesta(sinContinuidad));
    }

    @Test
    @DisplayName("rutaCompuesta_etapas_noSePuedenModificarDesdeAfuera")
    void rutaCompuesta_etapas_noSePuedenModificarDesdeAfuera() {
        List<Ruta> original = new ArrayList<>(List.of(new TramoSimple(ECI, BIBLIOTECA)));
        RutaCompuesta ruta = new RutaCompuesta(original);
        original.add(new TramoSimple(BIBLIOTECA, EAFIT));

        assertEquals(1, ruta.etapas().size());
    }

    @Test
    @DisplayName("ejecutar_rutaCompuesta_avisaIniciadaYCompletadaDeCadaEtapaEnOrden")
    void ejecutar_rutaCompuesta_avisaIniciadaYCompletadaDeCadaEtapaEnOrden() {
        RegistroDeEtapas registro = new RegistroDeEtapas();

        ResultadoEjecucion resultado = eciUnalConCarga().ejecutar(contexto(new FabricaDroneEstandar(), registro));

        assertEquals(ResultadoEjecucion.exito(3), resultado);
        List<EventoRuta> eventos = registro.eventos();
        assertEquals(6, eventos.size());
        assertEquals(new EventoRuta(TipoEventoRuta.ETAPA_INICIADA, "Tramo ECI -> Estación Norte", ""), eventos.get(0));
        assertEquals(new EventoRuta(TipoEventoRuta.ETAPA_COMPLETADA, "Tramo ECI -> Estación Norte", "drone CARGO"), eventos.get(1));
        assertEquals("Carga en Estación Norte", eventos.get(2).etapa());
        assertEquals("20 min", eventos.get(3).detalle());
        assertEquals(TipoEventoRuta.ETAPA_COMPLETADA, eventos.get(5).tipo());
    }

    @Test
    @DisplayName("ejecutar_tramoSinDroneQueLoCubra_avisaFalloYSeDetieneSinEjecutarElResto")
    void ejecutar_tramoSinDroneQueLoCubra_avisaFalloYSeDetieneSinEjecutarElResto() {
        RegistroDeEtapas registro = new RegistroDeEtapas();

        ResultadoEjecucion resultado = eciUnalConCarga().ejecutar(contexto(new FabricaDroneUrgente(), registro));

        assertEquals(ResultadoEjecucion.fallo(0), resultado);
        List<EventoRuta> eventos = registro.eventos();
        assertEquals(2, eventos.size());
        assertEquals(TipoEventoRuta.ETAPA_FALLIDA, eventos.get(1).tipo());
    }

    @Test
    @DisplayName("ejecutar_fallaEnUnaEtapaIntermedia_cuentaLasEtapasYaCompletadas")
    void ejecutar_fallaEnUnaEtapaIntermedia_cuentaLasEtapasYaCompletadas() {
        RegistroDeEtapas registro = new RegistroDeEtapas();
        Ruta ruta = new RutaCompuesta(List.of(
                new TramoSimple(ECI, BIBLIOTECA),
                new TramoSimple(BIBLIOTECA, UNAL)));

        ResultadoEjecucion resultado = ruta.ejecutar(contexto(new FabricaDroneEstandar(), registro));

        assertEquals(ResultadoEjecucion.fallo(1), resultado);
    }

    @Test
    @DisplayName("ejecutar_contextoNulo_lanzaExcepcion")
    void ejecutar_contextoNulo_lanzaExcepcion() {
        Ruta tramo = new TramoSimple(ECI, BIBLIOTECA);
        Ruta parada = new ParadaDeCarga(ESTACION_NORTE, 5);
        Ruta compuesta = eciUnalConCarga();

        assertThrows(IllegalArgumentException.class, () -> tramo.ejecutar(null));
        assertThrows(IllegalArgumentException.class, () -> parada.ejecutar(null));
        assertThrows(IllegalArgumentException.class, () -> compuesta.ejecutar(null));
    }
}
