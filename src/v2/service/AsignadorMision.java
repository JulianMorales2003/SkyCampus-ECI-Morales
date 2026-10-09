package v2.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import v2.clima.ApiMeteorologica;
import v2.eventos.ObservadorFlota;
import v2.model.Drone;
import v2.model.SolicitudMision;

/** Esqueleto del ciclo TDD (fase roja): compila, pero todavía no hace nada. */
public class AsignadorMision {

    private final ApiMeteorologica clima;
    private final ObservadorFlota notificador;

    public AsignadorMision(ApiMeteorologica clima, ObservadorFlota notificador) {
        this.clima = clima;
        this.notificador = notificador;
    }

    public Optional<Drone> asignar(SolicitudMision solicitud, List<Drone> flota, Instant ahora) {
        throw new UnsupportedOperationException("Aún no implementado (TDD: fase roja)");
    }
}
