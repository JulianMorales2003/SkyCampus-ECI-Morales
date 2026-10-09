package v2.asignacion;

import java.util.List;
import java.util.Optional;
import util.Validaciones;
import v2.model.Drone;
import v2.model.Prioridad;
import v2.model.SolicitudMision;
import v2.model.TipoDrone;

public class EstrategiaTipoSegunPaquete implements EstrategiaAsignacion {

    public static final int PESO_MAXIMO_MINI_GRAMOS = 1000;

    private final EstrategiaAsignacion desempate = new EstrategiaMayorBateria();

    @Override
    public Optional<Drone> seleccionar(SolicitudMision solicitud, List<Drone> flota) {
        Validaciones.exigirPresente(solicitud, "solicitud");
        Validaciones.exigirPresente(flota, "flota");
        TipoDrone requerido = tipoRequerido(solicitud);
        List<Drone> delTipo = EstrategiaAsignacion.candidatos(flota)
                .filter(drone -> drone.tipo() == requerido)
                .toList();
        return desempate.seleccionar(solicitud, delTipo);
    }

    static TipoDrone tipoRequerido(SolicitudMision solicitud) {
        if (solicitud.prioridad() == Prioridad.URGENTE) {
            return TipoDrone.EXPRESS;
        }
        if (solicitud.pesoPaqueteGramos() > PESO_MAXIMO_MINI_GRAMOS) {
            return TipoDrone.CARGO;
        }
        return TipoDrone.MINI;
    }
}
