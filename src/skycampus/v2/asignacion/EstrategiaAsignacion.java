package skycampus.v2.asignacion;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import skycampus.v2.model.Drone;
import skycampus.v2.model.EstadoDrone;
import skycampus.v2.model.SolicitudMision;

public interface EstrategiaAsignacion {

    Optional<Drone> seleccionar(SolicitudMision solicitud, List<Drone> flota);

    static Stream<Drone> candidatos(List<Drone> flota) {
        return flota.stream()
                .filter(Drone::disponible)
                .filter(drone -> drone.estado() == EstadoDrone.DISPONIBLE);
    }
}
