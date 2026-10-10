package skycampus.enterprise.dominio;

import java.util.List;
import java.util.Optional;

/** Regla para elegir un drone entre los candidatos. */
public interface EstrategiaAsignacion {

    Optional<Drone> seleccionar(SolicitudEntrega solicitud, List<Drone> candidatos);
}
