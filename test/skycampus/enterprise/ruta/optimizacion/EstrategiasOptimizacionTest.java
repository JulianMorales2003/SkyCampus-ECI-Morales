package skycampus.enterprise.ruta.optimizacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static skycampus.enterprise.ruta.RutasTestUtil.ECI;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import skycampus.enterprise.ruta.ParadaDeCarga;
import skycampus.enterprise.ruta.Punto;
import skycampus.enterprise.ruta.Ruta;
import skycampus.enterprise.ruta.RutaCompuesta;
import skycampus.enterprise.ruta.TramoSimple;

@DisplayName("Strategy: criterios de optimización de ruta")
class EstrategiasOptimizacionTest {

    private static final Punto DESTINO = new Punto("Destino", 20, 0);
    private static final Punto ESTACION_CERCA = new Punto("Cerca", 10, 1);
    private static final Punto ESTACION_LEJOS = new Punto("Lejos", 10, 8);

    private final Ruta directa = new TramoSimple(ECI, DESTINO);
    private final Ruta viaCerca = via(ESTACION_CERCA, 20);
    private final Ruta viaLejos = via(ESTACION_LEJOS, 5);

    private static Ruta via(Punto estacion, int minutosDeCarga) {
        return new RutaCompuesta(List.of(
                new TramoSimple(ECI, estacion),
                new ParadaDeCarga(estacion, minutosDeCarga),
                new TramoSimple(estacion, DESTINO)));
    }

    @Test
    @DisplayName("menorDistancia_candidatas_eligeLaDeMenosKilometros")
    void menorDistancia_candidatas_eligeLaDeMenosKilometros() {
        assertSame(directa, new MenorDistancia().elegir(List.of(viaLejos, viaCerca, directa)));
        assertSame(viaCerca, new MenorDistancia().elegir(List.of(viaLejos, viaCerca)));
    }

    @Test
    @DisplayName("menorDuracion_candidatas_eligeLaQueMenosTardaIncluyendoLaCarga")
    void menorDuracion_candidatas_eligeLaQueMenosTardaIncluyendoLaCarga() {
        // viaLejos es más larga pero carga solo 5 min, así que tarda menos que viaCerca (20 min de carga)
        assertSame(viaLejos, new MenorDuracion().elegir(List.of(viaCerca, viaLejos)));
        assertSame(directa, new MenorDuracion().elegir(List.of(viaCerca, viaLejos, directa)));
    }

    @Test
    @DisplayName("menosParadas_candidatas_eligeLaDeMenosCargasYDesempataPorDistancia")
    void menosParadas_candidatas_eligeLaDeMenosCargasYDesempataPorDistancia() {
        assertSame(directa, new MenosParadasDeCarga().elegir(List.of(viaCerca, directa)));
        assertSame(viaCerca, new MenosParadasDeCarga().elegir(List.of(viaLejos, viaCerca)));
    }

    @Test
    @DisplayName("criterios_empateTotal_eligeLaPrimeraDeLaLista")
    void criterios_empateTotal_eligeLaPrimeraDeLaLista() {
        Ruta primera = via(ESTACION_CERCA, 20);
        Ruta igual = via(ESTACION_CERCA, 20);

        assertSame(primera, new MenorDistancia().elegir(List.of(primera, igual)));
        assertSame(primera, new MenorDuracion().elegir(List.of(primera, igual)));
        assertSame(primera, new MenosParadasDeCarga().elegir(List.of(primera, igual)));
    }

    @Test
    @DisplayName("menorDistancia_mismaDistanciaDistintasParadas_desempataPorParadas")
    void menorDistancia_mismaDistanciaDistintasParadas_desempataPorParadas() {
        Ruta conParada = new RutaCompuesta(List.of(
                new TramoSimple(ECI, new Punto("En medio", 10, 0)),
                new ParadaDeCarga(new Punto("En medio", 10, 0), 20),
                new TramoSimple(new Punto("En medio", 10, 0), DESTINO)));

        assertSame(directa, new MenorDistancia().elegir(List.of(conParada, directa)));
    }

    @Test
    @DisplayName("elegir_sinCandidatasONulas_lanzaExcepcion")
    void elegir_sinCandidatasONulas_lanzaExcepcion() {
        MenorDistancia estrategia = new MenorDistancia();
        List<Ruta> vacia = List.of();

        assertThrows(IllegalArgumentException.class, () -> estrategia.elegir(vacia));
        assertThrows(IllegalArgumentException.class, () -> estrategia.elegir(null));
    }

    @Test
    @DisplayName("menorDistancia_mismaDistanciaYMismaDuracion_desempataPorMenosParadas")
    void menorDistancia_mismaDistanciaYMismaDuracion_desempataPorMenosParadas() {
        Punto fin = new Punto("Fin", 20.5, 0);
        Punto medio = new Punto("Medio", 10.25, 0);
        Ruta dosTramosSinCarga = new RutaCompuesta(List.of(
                new TramoSimple(ECI, medio), new TramoSimple(medio, fin)));
        TramoSimple directo = new TramoSimple(ECI, fin);
        int minutosParaEmpatar = dosTramosSinCarga.duracionMinutos() - directo.duracionMinutos();
        Ruta directoConCarga = new RutaCompuesta(List.of(directo, new ParadaDeCarga(fin, minutosParaEmpatar)));

        // El enunciado de la prueba exige distancia y duración iguales; solo cambia el número de paradas
        assertEquals(directoConCarga.distanciaKm(), dosTramosSinCarga.distanciaKm(), 0.0);
        assertEquals(directoConCarga.duracionMinutos(), dosTramosSinCarga.duracionMinutos());
        assertSame(dosTramosSinCarga, new MenorDistancia().elegir(List.of(directoConCarga, dosTramosSinCarga)));
    }
}
