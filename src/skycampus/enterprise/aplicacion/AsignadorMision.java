package skycampus.enterprise.aplicacion;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import skycampus.enterprise.dominio.Drone;
import skycampus.enterprise.dominio.EstrategiaAsignacion;
import skycampus.enterprise.dominio.EventoAsignacion;
import skycampus.enterprise.dominio.ObservadorDrone;
import skycampus.enterprise.dominio.RepositorioFlota;
import skycampus.enterprise.dominio.ServicioClima;
import skycampus.enterprise.dominio.SolicitudEntrega;
import skycampus.enterprise.dominio.TipoEventoAsignacion;

/**
 * Caso de uso de la red: asignar un drone a una entrega.
 * Solo conoce las interfaces del dominio; quién guarda los drones o consulta el clima
 * lo decide quien lo construye (inyección por constructor).
 */
public class AsignadorMision {

    public static final int BATERIA_MINIMA = 30;

    private final RepositorioFlota repo;
    private final ServicioClima clima;
    private final EstrategiaAsignacion estrategia;
    private final ObservadorDrone notificador;

    public AsignadorMision(RepositorioFlota repo, ServicioClima clima,
            EstrategiaAsignacion estrategia, ObservadorDrone notificador) {
        this.repo = Objects.requireNonNull(repo, "repo");
        this.clima = Objects.requireNonNull(clima, "clima");
        this.estrategia = Objects.requireNonNull(estrategia, "estrategia");
        this.notificador = Objects.requireNonNull(notificador, "notificador");
    }

    public Optional<Drone> asignar(SolicitudEntrega solicitud) {
        Objects.requireNonNull(solicitud, "solicitud");
        if (!climaPermiteVolar(solicitud)) {
            publicar(TipoEventoAsignacion.CLIMA_ADVERSO, solicitud,
                    "Clima adverso o no disponible entre " + solicitud.origen() + " y " + solicitud.destino() + ".");
            return Optional.empty();
        }
        List<Drone> aptos = repo.findDisponibles(solicitud.origen()).stream()
                .filter(drone -> drone.bateria() >= BATERIA_MINIMA)
                .toList();
        Optional<Drone> elegido = estrategia.seleccionar(solicitud, aptos);
        if (elegido.isEmpty()) {
            publicar(TipoEventoAsignacion.SIN_DRONE_DISPONIBLE, solicitud,
                    "No hay drone disponible en " + solicitud.origen() + ".");
            return elegido;
        }
        publicar(TipoEventoAsignacion.MISION_ASIGNADA, solicitud,
                "Misión " + solicitud.id() + " asignada al drone " + elegido.get().id() + ".");
        return elegido;
    }

    /** Falla segura: si el servicio del clima lanza una excepción, no se vuela. */
    private boolean climaPermiteVolar(SolicitudEntrega solicitud) {
        try {
            return clima.condicionesAptas(solicitud.origen(), solicitud.destino());
        } catch (RuntimeException ignored) {
            // Sin respuesta confiable del clima no se despega; se publica CLIMA_ADVERSO.
            return false;
        }
    }

    private void publicar(TipoEventoAsignacion tipo, SolicitudEntrega solicitud, String detalle) {
        notificador.alOcurrir(new EventoAsignacion(tipo, solicitud.id(), detalle));
    }
}
