package v2.service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import v2.clima.ApiMeteorologica;
import v2.eventos.EventoFlota;
import v2.eventos.ObservadorFlota;
import v2.eventos.TipoEvento;
import v2.model.Drone;
import v2.model.EstadoDrone;
import v2.model.Prioridad;
import v2.model.SolicitudMision;
import v2.model.TipoDrone;

/** Fase verde del TDD: lo mínimo para que pasen las 5 primeras pruebas. */
public class AsignadorMision {

    private final ApiMeteorologica clima;
    private final ObservadorFlota notificador;

    public AsignadorMision(ApiMeteorologica clima, ObservadorFlota notificador) {
        this.clima = clima;
        this.notificador = notificador;
    }

    public Optional<Drone> asignar(SolicitudMision solicitud, List<Drone> flota, Instant ahora) {
        if (solicitud.pesoPaqueteGramos() > 2000) {
            notificador.alOcurrir(new EventoFlota(TipoEvento.PAQUETE_EXCEDE_CAPACIDAD,
                    "El paquete de la misión " + solicitud.id() + " supera los 2000 g.", ahora));
            return Optional.empty();
        }
        if (!clima.esApto()) {
            notificador.alOcurrir(new EventoFlota(TipoEvento.CLIMA_ADVERSO,
                    "Clima adverso para la misión " + solicitud.id() + ".", ahora));
            return Optional.empty();
        }
        Optional<Drone> elegido = flota.stream()
                .filter(d -> d.disponible() && d.estado() == EstadoDrone.DISPONIBLE)
                .filter(d -> d.bateria() >= 30)
                .filter(d -> solicitud.prioridad() != Prioridad.URGENTE || d.tipo() == TipoDrone.EXPRESS)
                .max(Comparator.comparingInt(Drone::bateria));
        if (elegido.isEmpty()) {
            notificador.alOcurrir(new EventoFlota(TipoEvento.SIN_DRONE_DISPONIBLE,
                    "No hay drone disponible para la misión " + solicitud.id() + ".", ahora));
            return elegido;
        }
        notificador.alOcurrir(new EventoFlota(TipoEvento.MISION_ASIGNADA,
                "Misión " + solicitud.id() + " asignada al drone " + elegido.get().id() + ".", ahora));
        return elegido;
    }
}
