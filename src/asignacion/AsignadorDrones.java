package asignacion;

import java.util.List;
import java.util.Optional;
import model.Drone;

public class AsignadorDrones {

    private EstrategiaAsignacion estrategia;

    public AsignadorDrones(EstrategiaAsignacion estrategia) {
        this.estrategia = validarEstrategia(estrategia);
    }

    public void cambiarEstrategia(EstrategiaAsignacion nuevaEstrategia) {
        this.estrategia = validarEstrategia(nuevaEstrategia);
    }

    public Optional<Drone> asignar(List<Drone> flota, String origen) {
        if (flota == null) {
            throw new IllegalArgumentException("La flota de drones no puede ser nula.");
        }
        if (origen == null || origen.isBlank()) {
            throw new IllegalArgumentException("El origen de la misión no puede ser nulo ni vacío.");
        }
        return estrategia.seleccionar(flota, origen);
    }

    private static EstrategiaAsignacion validarEstrategia(EstrategiaAsignacion estrategia) {
        if (estrategia == null) {
            throw new IllegalArgumentException("La estrategia de asignación no puede ser nula.");
        }
        return estrategia;
    }
}
