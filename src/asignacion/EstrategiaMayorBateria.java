package asignacion;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import model.Drone;

public class EstrategiaMayorBateria implements EstrategiaAsignacion {

    @Override
    public Optional<Drone> seleccionar(List<Drone> flota, String origen) {
        if (flota == null) {
            throw new IllegalArgumentException("La flota de drones no puede ser nula.");
        }
        return flota.stream()
                .filter(Drone::disponible)
                .max(Comparator.comparingInt(Drone::bateria));
    }
}
