package asignacion;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import model.Drone;
import util.Validaciones;

public class EstrategiaDroneEnOrigen implements EstrategiaAsignacion {

    @Override
    public Optional<Drone> seleccionar(List<Drone> flota, String origen) {
        Validaciones.exigirPresente(flota, "flota");
        Validaciones.exigirTexto(origen, "origen");
        return flota.stream()
                .filter(Drone::disponible)
                .filter(drone -> origen.equals(drone.ubicacion()))
                .max(Comparator.comparingInt(Drone::bateria));
    }
}
