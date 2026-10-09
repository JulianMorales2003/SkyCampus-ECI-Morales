package asignacion;

import alerta.AlertaOperador;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import model.Drone;
import model.EstadoMision;
import model.Mision;
import util.Validaciones;

public class AsignadorMision {

    public Asignacion asignar(Mision mision, Drone droneActual) {
        Validaciones.exigirPresente(mision, "mision");
        Validaciones.exigirPresente(droneActual, "droneActual");
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

    public Optional<Asignacion> asignarAutomaticamente(Mision mision, List<Drone> flota) {
        Validaciones.exigirPresente(mision, "mision");
        Validaciones.exigirPresente(flota, "flota");
        return flota.stream()
                .filter(Drone::disponible)
                .max(Comparator.comparingInt(Drone::bateria))
                .map(elegido -> asignar(misionDelDrone(mision, elegido), elegido));
    }

    private static Mision misionDelDrone(Mision mision, Drone drone) {
        return new Mision(mision.id(), drone, mision.origen(), mision.destino(), mision.tipoCarga(),
                mision.estado(), mision.prioridad(), mision.notas(), mision.horaMaximaEntrega());
    }

    public Asignacion asignarYNotificar(Mision mision, Drone droneActual, AlertaOperador alerta, String operador) {
        Validaciones.exigirPresente(alerta, "alerta");
        Asignacion asignacion = asignar(mision, droneActual);
        alerta.enviar(operador, "Misión asignada al drone " + droneActual.id()
                + " con destino " + mision.destino() + ".");
        return asignacion;
    }
}
