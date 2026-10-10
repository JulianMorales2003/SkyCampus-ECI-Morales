package asignacion;

import model.Drone;
import model.Mision;

public record Asignacion(Mision mision, Drone drone) {

    public Asignacion {
        if (mision == null || drone == null) {
            throw new IllegalArgumentException("Una asignación necesita la misión y el drone reservado.");
        }
        if (!mision.drone().id().equals(drone.id())) {
            throw new IllegalArgumentException("El drone reservado debe ser el mismo drone de la misión.");
        }
    }
}
