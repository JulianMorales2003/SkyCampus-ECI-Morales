package v2.service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import util.Validaciones;
import v2.clima.ApiMeteorologica;
import v2.eventos.EventoFlota;
import v2.eventos.ObservadorFlota;
import v2.eventos.TipoEvento;
import v2.model.Drone;
import v2.model.EstadoDrone;
import v2.model.Prioridad;
import v2.model.SolicitudMision;
import v2.model.TipoDrone;

/** Fase verde (segunda ronda): cubre los casos límite. Todavía con todo mezclado en un método. */
public class AsignadorMision {

    private final ApiMeteorologica clima;
    private final ObservadorFlota notificador;

    public AsignadorMision(ApiMeteorologica clima, ObservadorFlota notificador) {
        Validaciones.exigirPresente(clima, "clima");
        Validaciones.exigirPresente(notificador, "notificador");
        this.clima = clima;
        this.notificador = notificador;
    }

    public Optional<Drone> asignar(SolicitudMision solicitud, List<Drone> flota, Instant ahora) {
        Validaciones.exigirPresente(solicitud, "solicitud");
        Validaciones.exigirPresente(flota, "flota");
        Validaciones.exigirPresente(ahora, "ahora");
        if (solicitud.pesoPaqueteGramos() > 2000) {
            notificador.alOcurrir(new EventoFlota(TipoEvento.PAQUETE_EXCEDE_CAPACIDAD,
                    "El paquete de la misión " + solicitud.id() + " supera los 2000 g.", ahora));
            return Optional.empty();
        }
        boolean apto;
        try {
            apto = clima.esApto();
        } catch (RuntimeException e) {
            apto = false;
        }
        if (!apto) {
            notificador.alOcurrir(new EventoFlota(TipoEvento.CLIMA_ADVERSO,
                    "Clima adverso o no disponible para la misión " + solicitud.id() + ".", ahora));
            return Optional.empty();
        }
        List<Drone> aptos = flota.stream()
                .filter(d -> d.disponible() && d.estado() == EstadoDrone.DISPONIBLE)
                .filter(d -> d.bateria() >= 30)
                .filter(d -> d.tipo() == TipoDrone.CARGO ? d.tipo() == TipoDrone.CARGO && solicitud.pesoPaqueteGramos() >= 100
                        && solicitud.pesoPaqueteGramos() <= 2000 : solicitud.pesoPaqueteGramos() <= 1000)
                .toList();
        Comparator<Drone> mayorBateria = Comparator.comparingInt(Drone::bateria)
                .thenComparing(Drone::id, Comparator.reverseOrder());
        Optional<Drone> elegido = Optional.empty();
        boolean urgenteSinRapido = false;
        if (solicitud.prioridad() == Prioridad.URGENTE) {
            elegido = aptos.stream().filter(d -> d.tipo() == TipoDrone.EXPRESS).max(mayorBateria);
            if (elegido.isEmpty()) {
                elegido = aptos.stream().max(mayorBateria);
                urgenteSinRapido = elegido.isPresent();
            }
        } else {
            elegido = aptos.stream().max(mayorBateria);
        }
        if (elegido.isEmpty()) {
            notificador.alOcurrir(new EventoFlota(TipoEvento.SIN_DRONE_DISPONIBLE,
                    "No hay drone disponible para la misión " + solicitud.id() + ".", ahora));
            return elegido;
        }
        if (urgenteSinRapido) {
            notificador.alOcurrir(new EventoFlota(TipoEvento.URGENTE_SIN_DRONE_RAPIDO,
                    "La misión urgente " + solicitud.id() + " no pudo atenderse con un drone EXPRESS.", ahora));
        }
        notificador.alOcurrir(new EventoFlota(TipoEvento.MISION_ASIGNADA,
                "Misión " + solicitud.id() + " asignada al drone " + elegido.get().id() + ".", ahora));
        return elegido;
    }
}
