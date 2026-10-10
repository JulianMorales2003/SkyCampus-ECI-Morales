package skycampus.enterprise.analytics;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collector;
import skycampus.enterprise.model.MisionRed;
import skycampus.enterprise.model.Sede;

/**
 * Acumulador mutable que calcula todas las métricas de una sede en una sola pasada.
 * Se usa como colector downstream de {@code groupingBy}, por lo que cada misión se visita una vez.
 */
final class AcumuladorSede {

    private Sede sede;
    private long total;
    private long entregadas;
    private long minutosEntregadas;
    private long urgentes;
    private final Map<String, Long> misionesPorDrone = new HashMap<>();

    static Collector<MisionRed, AcumuladorSede, EficienciaSede> recolector() {
        return Collector.of(AcumuladorSede::new, AcumuladorSede::agregar,
                AcumuladorSede::combinar, AcumuladorSede::resultado);
    }

    void agregar(MisionRed mision) {
        sede = mision.sede();
        total++;
        if (mision.entregada()) {
            entregadas++;
            minutosEntregadas += mision.tiempoEntregaMinutos();
        }
        if (mision.urgente()) {
            urgentes++;
        }
        misionesPorDrone.merge(mision.droneId(), 1L, Long::sum);
    }

    AcumuladorSede combinar(AcumuladorSede otro) {
        if (sede == null) {
            sede = otro.sede;
        }
        total += otro.total;
        entregadas += otro.entregadas;
        minutosEntregadas += otro.minutosEntregadas;
        urgentes += otro.urgentes;
        otro.misionesPorDrone.forEach((drone, cantidad) -> misionesPorDrone.merge(drone, cantidad, Long::sum));
        return this;
    }

    EficienciaSede resultado() {
        // Más misiones gana; ante un empate se elige el id de drone menor, para que el resultado sea estable
        String droneMasUtilizado = misionesPorDrone.entrySet().stream()
                .max(Map.Entry.<String, Long>comparingByValue()
                        .thenComparing(Map.Entry.comparingByKey(Comparator.reverseOrder())))
                .map(Map.Entry::getKey)
                .orElse("");
        return new EficienciaSede(sede, total, entregadas, minutosEntregadas, urgentes, droneMasUtilizado);
    }
}
