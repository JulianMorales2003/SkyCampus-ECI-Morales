package asignacion;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import model.Drone;
import util.Validaciones;

public class EstrategiaMayorBateria implements EstrategiaAsignacion {

    @Override
    public Optional<Drone> seleccionar(List<Drone> flota, String origen) {
        Validaciones.exigirPresente(flota, "flota");
        return flota.stream()
                .filter(Drone::disponible)
                .max(Comparator.comparingInt(Drone::bateria));
    }
}
