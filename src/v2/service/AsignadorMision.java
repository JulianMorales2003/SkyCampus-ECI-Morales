package v2.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import util.Validaciones;
import v2.asignacion.EstrategiaAsignacion;
import v2.asignacion.EstrategiaPorPrioridad;
import v2.clima.ApiMeteorologica;
import v2.eventos.EventoFlota;
import v2.eventos.ObservadorFlota;
import v2.eventos.TipoEvento;
import v2.model.Drone;
import v2.model.Prioridad;
import v2.model.SolicitudMision;
import v2.model.TipoDrone;

/**
 * Caso SC-07: valida el paquete, consulta el clima, filtra los drones aptos y elige uno.
 * No decide con qué drone: eso lo hace la estrategia. Cada resultado se avisa por un evento.
 */
public class AsignadorMision {

    public static final int PESO_MAXIMO_GRAMOS = 2000;
    public static final int BATERIA_MINIMA = 30;

    private final ApiMeteorologica clima;
    private final ObservadorFlota notificador;
    private final EstrategiaAsignacion estrategia = new EstrategiaPorPrioridad();

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
        String id = solicitud.id();

        if (solicitud.pesoPaqueteGramos() > PESO_MAXIMO_GRAMOS) {
            publicar(TipoEvento.PAQUETE_EXCEDE_CAPACIDAD,
                    "El paquete de la misión " + id + " supera los " + PESO_MAXIMO_GRAMOS + " g.", ahora);
            return Optional.empty();
        }
        if (!climaPermiteVolar()) {
            publicar(TipoEvento.CLIMA_ADVERSO,
                    "Clima adverso o no disponible para la misión " + id + ".", ahora);
            return Optional.empty();
        }
        Optional<Drone> elegido = estrategia.seleccionar(solicitud, dronesAptos(solicitud, flota));
        if (elegido.isEmpty()) {
            publicar(TipoEvento.SIN_DRONE_DISPONIBLE, "No hay drone disponible para la misión " + id + ".", ahora);
            return elegido;
        }
        Drone drone = elegido.get();
        if (solicitud.prioridad() == Prioridad.URGENTE && drone.tipo() != TipoDrone.EXPRESS) {
            publicar(TipoEvento.URGENTE_SIN_DRONE_RAPIDO,
                    "La misión urgente " + id + " no pudo atenderse con un drone EXPRESS.", ahora);
        }
        publicar(TipoEvento.MISION_ASIGNADA, "Misión " + id + " asignada al drone " + drone.id() + ".", ahora);
        return elegido;
    }

    /** Falla segura (RNF-06): si la API lanza una excepción, se trata como clima no apto. */
    private boolean climaPermiteVolar() {
        try {
            return clima.esApto();
        } catch (RuntimeException e) {
            return false;
        }
    }

    /** RN-01 (batería), RN-02 y RN-04 (peso admitido por el tipo) sobre los drones libres. */
    private static List<Drone> dronesAptos(SolicitudMision solicitud, List<Drone> flota) {
        return EstrategiaAsignacion.candidatos(flota)
                .filter(drone -> drone.bateria() >= BATERIA_MINIMA)
                .filter(drone -> drone.tipo().admitePeso(solicitud.pesoPaqueteGramos()))
                .toList();
    }

    private void publicar(TipoEvento tipo, String detalle, Instant ahora) {
        notificador.alOcurrir(new EventoFlota(tipo, detalle, ahora));
    }
}
