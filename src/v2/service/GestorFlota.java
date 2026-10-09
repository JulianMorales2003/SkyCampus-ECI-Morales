package v2.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import util.Validaciones;
import v2.asignacion.EstrategiaAsignacion;
import v2.eventos.EventoFlota;
import v2.eventos.ObservadorFlota;
import v2.eventos.TipoEvento;
import v2.model.Drone;
import v2.model.EstadoMision;
import v2.model.Mision;
import v2.model.SolicitudMision;

public class GestorFlota {

    private final List<ObservadorFlota> observadores = new ArrayList<>();
    private EstrategiaAsignacion estrategia;

    public GestorFlota(EstrategiaAsignacion estrategia) {
        cambiarEstrategia(estrategia);
    }

    public final void cambiarEstrategia(EstrategiaAsignacion nueva) {
        Validaciones.exigirPresente(nueva, "estrategia");
        this.estrategia = nueva;
    }

    public void suscribir(ObservadorFlota observador) {
        Validaciones.exigirPresente(observador, "observador");
        observadores.add(observador);
    }

    public void cancelar(ObservadorFlota observador) {
        observadores.remove(observador);
    }

    public Optional<Mision> asignar(SolicitudMision solicitud, List<Drone> flota, Instant ahora) {
        Validaciones.exigirPresente(solicitud, "solicitud");
        Validaciones.exigirPresente(flota, "flota");
        Validaciones.exigirPresente(ahora, "ahora");
        Optional<Mision> mision = estrategia.seleccionar(solicitud, flota)
                .map(drone -> new Mision(solicitud.id(), drone, solicitud.destino(),
                        solicitud.pesoPaqueteGramos(), solicitud.prioridad(),
                        EstadoMision.EN_VUELO, ahora));
        if (mision.isPresent()) {
            notificar(new EventoFlota(TipoEvento.MISION_ASIGNADA,
                    "Misión " + solicitud.id() + " asignada al drone "
                            + mision.get().drone().id() + " con destino " + solicitud.destino() + ".",
                    ahora));
        } else {
            notificar(new EventoFlota(TipoEvento.SIN_DRONE_DISPONIBLE,
                    "No hay drone disponible para la misión " + solicitud.id() + ".", ahora));
        }
        return mision;
    }

    public Mision completar(Mision mision, Instant ahora) {
        Validaciones.exigirPresente(mision, "mision");
        Validaciones.exigirPresente(ahora, "ahora");
        Mision completada = new Mision(mision.id(), mision.drone(), mision.destino(),
                mision.pesoPaqueteGramos(), mision.prioridad(), EstadoMision.COMPLETADA,
                mision.creadaEn());
        notificar(new EventoFlota(TipoEvento.MISION_COMPLETADA,
                "Misión " + mision.id() + " completada por el drone " + mision.drone().id() + ".",
                ahora));
        return completada;
    }

    public void reportarFallo(Drone drone, Instant ahora) {
        Validaciones.exigirPresente(drone, "drone");
        Validaciones.exigirPresente(ahora, "ahora");
        notificar(new EventoFlota(TipoEvento.FALLO_DRONE,
                "El drone " + drone.id() + " reportó un fallo.", ahora));
    }

    private void notificar(EventoFlota evento) {
        for (ObservadorFlota observador : List.copyOf(observadores)) {
            observador.alOcurrir(evento);
        }
    }
}
