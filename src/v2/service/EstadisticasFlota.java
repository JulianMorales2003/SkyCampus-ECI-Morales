package v2.service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import util.Validaciones;
import v2.model.EstadoMision;
import v2.model.Mision;
import v2.model.TipoDrone;

public final class EstadisticasFlota {

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
}
