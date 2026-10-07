package asignacion;

import model.Drone;
import model.EstadoMision;
import model.Mision;

public class AsignadorMision {

    public Mision asignar(Mision mision, Drone drone) {
        if (mision == null) {
            throw new IllegalArgumentException("La misión a asignar no puede ser nula.");
        }
        if (drone == null) {
            throw new IllegalArgumentException("El drone a asignar no puede ser nulo.");
        }
        if (mision.estado() != EstadoMision.PENDIENTE) {
            throw new IllegalStateException("Solo se puede asignar una misión PENDIENTE; estado actual: "
                    + mision.estado() + ".");
        }
        if (!drone.disponible()) {
            throw new IllegalStateException("El drone " + drone.id() + " no está disponible.");
        }
        return new Mision(mision.id(), drone, mision.origen(), mision.destino(), mision.tipoCarga(),
                EstadoMision.EN_VUELO, mision.prioridad(), mision.notas(), mision.horaMaximaEntrega());
    }
}
