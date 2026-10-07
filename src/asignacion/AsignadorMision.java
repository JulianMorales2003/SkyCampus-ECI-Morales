package asignacion;

import model.Drone;
import model.EstadoMision;
import model.Mision;

public class AsignadorMision {

    public Asignacion asignar(Mision mision, Drone droneActual) {
        if (mision == null) {
            throw new IllegalArgumentException("La misión a asignar no puede ser nula.");
        }
        if (droneActual == null) {
            throw new IllegalArgumentException("El drone a asignar no puede ser nulo.");
        }
        if (!mision.drone().id().equals(droneActual.id())) {
            throw new IllegalArgumentException("La misión es del drone " + mision.drone().id()
                    + " y no puede asignarse al drone " + droneActual.id() + ".");
        }
        if (mision.estado() != EstadoMision.PENDIENTE) {
            throw new IllegalStateException("Solo se puede asignar una misión PENDIENTE; estado actual: "
                    + mision.estado() + ".");
        }
        if (!droneActual.disponible()) {
            throw new IllegalStateException("El drone " + droneActual.id() + " no está disponible.");
        }
        Drone droneReservado = reservar(droneActual);
        return new Asignacion(misionEnVuelo(mision, droneReservado), droneReservado);
    }

    private static Drone reservar(Drone drone) {
        return new Drone(drone.id(), drone.modelo(), drone.bateria(), false, drone.ubicacion());
    }

    private static Mision misionEnVuelo(Mision mision, Drone droneReservado) {
        return new Mision(mision.id(), droneReservado, mision.origen(), mision.destino(), mision.tipoCarga(),
                EstadoMision.EN_VUELO, mision.prioridad(), mision.notas(), mision.horaMaximaEntrega());
    }
}
