package asignacion;

import java.util.List;
import java.util.Optional;
import model.Drone;

public interface EstrategiaAsignacion {

    Optional<Drone> seleccionar(List<Drone> flota, String origen);
}
