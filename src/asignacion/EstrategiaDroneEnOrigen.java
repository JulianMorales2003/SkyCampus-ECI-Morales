package asignacion;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import model.Drone;

public class EstrategiaDroneEnOrigen implements EstrategiaAsignacion {

    @Override
    public Optional<Drone> seleccionar(List<Drone> flota, String origen) {
        if (flota == null) {
            throw new IllegalArgumentException("La flota de drones no puede ser nula.");
        }
        if (origen == null || origen.isBlank()) {
            throw new IllegalArgumentException("El origen de la misión no puede ser nulo ni vacío.");
        }
        return flota.stream()
                .filter(Drone::disponible)
                .filter(drone -> origen.equals(drone.ubicacion()))
                .max(Comparator.comparingInt(Drone::bateria));
    }
}
