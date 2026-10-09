package v2.asignacion;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import util.Validaciones;
import v2.model.Drone;
import v2.model.SolicitudMision;

public class EstrategiaMayorBateria implements EstrategiaAsignacion {

    private static final Comparator<Drone> POR_BATERIA_Y_ID_MENOR =
            Comparator.comparingInt(Drone::bateria)
                    .thenComparing(Drone::id, Comparator.reverseOrder());

    @Override
    public Optional<Drone> seleccionar(SolicitudMision solicitud, List<Drone> flota) {
        Validaciones.exigirPresente(solicitud, "solicitud");
        Validaciones.exigirPresente(flota, "flota");
        return EstrategiaAsignacion.candidatos(flota).max(POR_BATERIA_Y_ID_MENOR);
    }
}
