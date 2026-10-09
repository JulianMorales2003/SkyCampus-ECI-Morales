package skycampus.v2.service;

import java.time.Duration;
import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import util.Validaciones;
import skycampus.v2.model.Drone;
import skycampus.v2.model.EstadoMision;
import skycampus.v2.model.Mision;
import skycampus.v2.model.Prioridad;
import skycampus.v2.model.TipoDrone;

public final class EstadisticasFlota {

    public static final Duration UMBRAL_URGENTE_PENDIENTE = Duration.ofMinutes(10);

    private EstadisticasFlota() {
    }

    public static Map<TipoDrone, Long> completadasPorTipo(List<Mision> misiones) {
        Validaciones.exigirPresente(misiones, "misiones");
        return misiones.stream()
                .filter(mision -> mision.estado() == EstadoMision.COMPLETADA)
                .collect(Collectors.groupingBy(
                        mision -> mision.drone().tipo(),
                        () -> new EnumMap<>(TipoDrone.class),
                        Collectors.counting()));
    }

    public static Optional<Drone> droneConMasCompletadas(List<Mision> misiones) {
        Validaciones.exigirPresente(misiones, "misiones");
        Map<String, List<Mision>> completadasPorDrone = misiones.stream()
                .filter(mision -> mision.estado() == EstadoMision.COMPLETADA)
                .collect(Collectors.groupingBy(mision -> mision.drone().id()));
        return completadasPorDrone.entrySet().stream()
                .max(Comparator.comparingInt((Map.Entry<String, List<Mision>> grupo) -> grupo.getValue().size())
                        .thenComparing(Map.Entry::getKey, Comparator.reverseOrder()))
                .map(grupo -> grupo.getValue().get(0).drone());
    }

    public static double porcentajeFallidas(List<Mision> misiones) {
        Validaciones.exigirPresente(misiones, "misiones");
        if (misiones.isEmpty()) {
            return 0.0;
        }
        long fallidas = misiones.stream()
                .filter(mision -> mision.estado() == EstadoMision.FALLIDA)
                .count();
        return fallidas * 100.0 / misiones.size();
    }

    public static boolean hayUrgentePendienteMasDeDiezMinutos(List<Mision> misiones, Instant ahora) {
        Validaciones.exigirPresente(misiones, "misiones");
        Validaciones.exigirPresente(ahora, "ahora");
        return misiones.stream()
                .filter(mision -> mision.prioridad() == Prioridad.URGENTE)
                .filter(mision -> mision.estado() == EstadoMision.PENDIENTE)
                .anyMatch(mision -> Duration.between(mision.creadaEn(), ahora)
                        .compareTo(UMBRAL_URGENTE_PENDIENTE) > 0);
    }
}
