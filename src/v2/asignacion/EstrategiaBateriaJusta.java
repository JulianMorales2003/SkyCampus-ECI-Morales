package v2.asignacion;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import util.Validaciones;
import v2.model.Drone;
import v2.model.SolicitudMision;

public class EstrategiaBateriaJusta implements EstrategiaAsignacion {

    public static final int BATERIA_MINIMA_POR_DEFECTO = 30;

    private static final Comparator<Drone> POR_BATERIA_Y_ID =
            Comparator.comparingInt(Drone::bateria).thenComparing(Drone::id);

    private final int bateriaMinima;

    public EstrategiaBateriaJusta() {
        this(BATERIA_MINIMA_POR_DEFECTO);
    }

    public EstrategiaBateriaJusta(int bateriaMinima) {
        if (bateriaMinima < Drone.BATERIA_MINIMA || bateriaMinima > Drone.BATERIA_MAXIMA) {
            throw new IllegalArgumentException("La batería mínima debe estar entre "
                    + Drone.BATERIA_MINIMA + " y " + Drone.BATERIA_MAXIMA + ".");
        }
        this.bateriaMinima = bateriaMinima;
    }

    @Override
    public Optional<Drone> seleccionar(SolicitudMision solicitud, List<Drone> flota) {
        Validaciones.exigirPresente(solicitud, "solicitud");
        Validaciones.exigirPresente(flota, "flota");
        return EstrategiaAsignacion.candidatos(flota)
                .filter(drone -> drone.bateria() >= bateriaMinima)
                .min(POR_BATERIA_Y_ID);
    }
}
