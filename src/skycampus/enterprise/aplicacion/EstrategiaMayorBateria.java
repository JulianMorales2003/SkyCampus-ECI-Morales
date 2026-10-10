package skycampus.enterprise.aplicacion;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import skycampus.enterprise.dominio.Drone;
import skycampus.enterprise.dominio.EstrategiaAsignacion;
import skycampus.enterprise.dominio.SolicitudEntrega;

/** Elige el drone con más batería; si hay empate, el de id menor para que el resultado sea estable. */
public class EstrategiaMayorBateria implements EstrategiaAsignacion {

    private static final Comparator<Drone> POR_BATERIA_Y_ID = Comparator
            .comparingInt(Drone::bateria)
            .thenComparing(Drone::id, Comparator.reverseOrder());

    @Override
    public Optional<Drone> seleccionar(SolicitudEntrega solicitud, List<Drone> candidatos) {
        return candidatos.stream().max(POR_BATERIA_Y_ID);
    }
}
