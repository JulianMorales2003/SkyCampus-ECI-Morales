package skycampus.enterprise.aplicacion;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import skycampus.enterprise.dominio.CatalogoSedes;
import skycampus.enterprise.dominio.DecisionVuelo;
import skycampus.enterprise.dominio.Drone;
import skycampus.enterprise.dominio.EstrategiaAsignacion;
import skycampus.enterprise.dominio.ObservadorDrone;
import skycampus.enterprise.dominio.PlanVuelo;
import skycampus.enterprise.dominio.RepositorioFlota;
import skycampus.enterprise.dominio.ServicioClima;
import skycampus.enterprise.dominio.SolicitudEntrega;
import skycampus.enterprise.dominio.TipoEventoAsignacion;

/**
 * Caso de uso completo de "crear una misión entre sedes": comprueba las sedes y el peso, asigna el
 * drone (con {@link AsignadorMision}) y pide la autorización del vuelo (con {@link AutorizadorVuelo}).
 * La urgencia solo viaja en el plan: ninguna regla de seguridad se salta por ella.
 */
public class CrearMision {

    public static final int PESO_MAXIMO_G = 2000;
    public static final int ALTURA_CRUCERO_M = 100;

    private final CatalogoSedes sedes;
    private final RepositorioFlota repo;
    private final ServicioClima clima;
    private final EstrategiaAsignacion estrategia;
    private final ObservadorDrone notificador;
    private final AutorizadorVuelo autorizador;
    private final Supplier<String> generadorId;

    public CrearMision(CatalogoSedes sedes, RepositorioFlota repo, ServicioClima clima,
            EstrategiaAsignacion estrategia, ObservadorDrone notificador,
            AutorizadorVuelo autorizador, Supplier<String> generadorId) {
        this.sedes = Objects.requireNonNull(sedes, "sedes");
        this.repo = Objects.requireNonNull(repo, "repo");
        this.clima = Objects.requireNonNull(clima, "clima");
        this.estrategia = Objects.requireNonNull(estrategia, "estrategia");
        this.notificador = Objects.requireNonNull(notificador, "notificador");
        this.autorizador = Objects.requireNonNull(autorizador, "autorizador");
        this.generadorId = Objects.requireNonNull(generadorId, "generadorId");
    }

    public ResultadoMision crear(ComandoMision comando) {
        Objects.requireNonNull(comando, "comando");
        if (!sedes.estaActiva(comando.origen()) || !sedes.estaActiva(comando.destino())) {
            return ResultadoMision.rechazada(MotivoMision.SEDE_INACTIVA);
        }
        if (comando.pesoGramos() > PESO_MAXIMO_G) {
            return ResultadoMision.rechazada(MotivoMision.PAQUETE_EXCEDE_PESO);
        }
        OptionalDouble km = sedes.distanciaKm(comando.origen(), comando.destino());
        if (km.isEmpty()) {
            return ResultadoMision.rechazada(MotivoMision.RUTA_NO_DEFINIDA);
        }
        String id = generadorId.get();
        AtomicReference<TipoEventoAsignacion> ultimoEvento = new AtomicReference<>();
        ObservadorDrone espia = evento -> {
            ultimoEvento.set(evento.tipo());
            notificador.alOcurrir(evento);
        };
        Optional<Drone> drone = new AsignadorMision(repo, clima, estrategia, espia)
                .asignar(new SolicitudEntrega(id, comando.origen(), comando.destino()));
        if (drone.isEmpty()) {
            return ResultadoMision.rechazada(ultimoEvento.get() == TipoEventoAsignacion.CLIMA_ADVERSO
                    ? MotivoMision.CLIMA_ADVERSO
                    : MotivoMision.SIN_DRONE_DISPONIBLE);
        }
        PlanVuelo plan = new PlanVuelo(id, comando.origen(), km.getAsDouble(), ALTURA_CRUCERO_M, true,
                comando.prioridad() == PrioridadMision.URGENTE);
        DecisionVuelo decision = autorizador.decidir(plan);
        if (!decision.autorizado()) {
            return ResultadoMision.rechazada(MotivoMision.valueOf(decision.motivo().name()));
        }
        return ResultadoMision.creada(id, drone.get());
    }
}
