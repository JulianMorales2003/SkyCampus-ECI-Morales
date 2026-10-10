package skycampus.enterprise.analytics;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import skycampus.enterprise.model.MisionRed;
import skycampus.enterprise.model.Sede;
import util.Validaciones;

/** Analytics de eficiencia de la red de sedes para el panel del superadministrador. */
public final class AnalyticsRed {

    private static final String PARAMETRO_SEDES = "sedes";
    private static final String PARAMETRO_MISIONES = "misiones";

    private static final Comparator<Map.Entry<Sede, Optional<EficienciaSede>>> POR_TASA_DE_EXITO =
            Comparator.comparingDouble(
                    (Map.Entry<Sede, Optional<EficienciaSede>> e) -> e.getValue().map(EficienciaSede::tasaExito).orElse(-1.0))
                    .reversed();

    private static final Comparator<Map.Entry<Sede, Optional<EficienciaSede>>> POR_ENTREGADAS =
            Comparator.comparingLong(
                    (Map.Entry<Sede, Optional<EficienciaSede>> e) -> e.getValue().map(EficienciaSede::entregadas).orElse(-1L))
                    .reversed();

    private static final Comparator<Map.Entry<Sede, Optional<EficienciaSede>>> RANKING =
            POR_TASA_DE_EXITO
                    .thenComparing(POR_ENTREGADAS)
                    .thenComparing(e -> e.getKey().nombre());

    private AnalyticsRed() {
    }

    /**
     * Calcula, recorriendo las misiones una sola vez, las métricas de cada sede.
     * Devuelve todas las sedes recibidas, en el mismo orden y sin repetidas; las que no tuvieron
     * misiones quedan con {@code Optional.empty()}.
     *
     * @throws IllegalArgumentException si alguna misión pertenece a una sede que no está en la lista
     */
    public static Map<Sede, Optional<EficienciaSede>> eficienciaPorSede(List<Sede> sedes, List<MisionRed> misiones) {
        Validaciones.exigirPresente(sedes, PARAMETRO_SEDES);
        Validaciones.exigirPresente(misiones, PARAMETRO_MISIONES);

        Map<Sede, EficienciaSede> conActividad = misiones.stream()
                .collect(Collectors.groupingBy(MisionRed::sede, AcumuladorSede.recolector()));

        if (!Set.copyOf(sedes).containsAll(conActividad.keySet())) {
            throw new IllegalArgumentException("Hay misiones de una sede que no pertenece a la red.");
        }

        Map<Sede, Optional<EficienciaSede>> resultado = new LinkedHashMap<>();
        new LinkedHashSet<>(sedes).forEach(sede -> resultado.put(sede, Optional.ofNullable(conActividad.get(sede))));
        return resultado;
    }

    /**
     * Ordena las sedes por tasa de éxito (mayor primero), luego por misiones entregadas (mayor primero)
     * y por último por nombre. Las sedes sin actividad quedan al final.
     */
    public static List<Sede> ranking(List<Sede> sedes, List<MisionRed> misiones) {
        return eficienciaPorSede(sedes, misiones).entrySet().stream()
                .sorted(RANKING)
                .map(Map.Entry::getKey)
                .toList();
    }
}
