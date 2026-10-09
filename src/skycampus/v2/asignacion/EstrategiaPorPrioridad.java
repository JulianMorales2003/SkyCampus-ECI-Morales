package skycampus.v2.asignacion;

import java.util.List;
import java.util.Optional;
import util.Validaciones;
import skycampus.v2.model.Drone;
import skycampus.v2.model.Prioridad;
import skycampus.v2.model.SolicitudMision;
import skycampus.v2.model.TipoDrone;

/**
 * RF-07 y RF-08 con la regla de precedencia del reto 06: las misiones URGENTES van al drone
 * EXPRESS con más batería; si no hay ninguno, al de mayor batería. El resto, al de mayor batería.
 */
public class EstrategiaPorPrioridad implements EstrategiaAsignacion {

    private final EstrategiaAsignacion mayorBateria = new EstrategiaMayorBateria();

    @Override
    public Optional<Drone> seleccionar(SolicitudMision solicitud, List<Drone> flota) {
        Validaciones.exigirPresente(solicitud, "solicitud");
        Validaciones.exigirPresente(flota, "flota");
        if (solicitud.prioridad() == Prioridad.URGENTE) {
            List<Drone> rapidos = flota.stream().filter(drone -> drone.tipo() == TipoDrone.EXPRESS).toList();
            Optional<Drone> rapido = mayorBateria.seleccionar(solicitud, rapidos);
            if (rapido.isPresent()) {
                return rapido;
            }
        }
        return mayorBateria.seleccionar(solicitud, flota);
    }
}
