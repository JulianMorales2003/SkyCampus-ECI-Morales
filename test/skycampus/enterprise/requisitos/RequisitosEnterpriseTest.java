package skycampus.enterprise.requisitos;

import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import skycampus.enterprise.ruta.PlanificadorRutas;
import skycampus.enterprise.ruta.Punto;
import skycampus.enterprise.ruta.Ruta;
import skycampus.enterprise.ruta.optimizacion.EstrategiaOptimizacionRuta;
import skycampus.enterprise.ruta.optimizacion.MenorDistancia;
import skycampus.enterprise.ruta.optimizacion.MenorDuracion;
import skycampus.enterprise.ruta.optimizacion.MenosParadasDeCarga;

@DisplayName("Requisitos no funcionales medibles de Enterprise")
class RequisitosEnterpriseTest {

    private static final Punto ORIGEN = new Punto("ECI", 0, 0);
    private static final Punto DESTINO = new Punto("Uniandes", 60, 0);
    private static final double ALCANCE_KM = 40;

    @Test
    @DisplayName("RNF-08: planificar una ruta entre sedes con 20 estaciones tarda menos de 500 ms con cada criterio")
    void rnf08_planificarConVeinteEstaciones_tardaMenosDe500ms() {
        List<Punto> estaciones = new ArrayList<>();
        IntStream.range(0, 20).forEach(i -> estaciones.add(new Punto("Estación " + i, 3.0 * i, (i % 5) - 2.0)));
        List<EstrategiaOptimizacionRuta> criterios =
                List.of(new MenorDistancia(), new MenorDuracion(), new MenosParadasDeCarga());

        for (EstrategiaOptimizacionRuta criterio : criterios) {
            PlanificadorRutas planificador = new PlanificadorRutas(criterio, ALCANCE_KM);
            Optional<Ruta> ruta = assertTimeout(Duration.ofMillis(500),
                    () -> planificador.planificar(ORIGEN, DESTINO, estaciones));
            assertTrue(ruta.isPresent());
        }
    }
}
