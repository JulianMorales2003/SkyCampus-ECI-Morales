package skycampus.enterprise.aplicacion;

import java.util.Objects;
import skycampus.enterprise.dominio.DecisionVuelo;
import skycampus.enterprise.dominio.MotivoRechazo;
import skycampus.enterprise.dominio.PlanVuelo;
import skycampus.enterprise.dominio.PoliticaVuelo;
import skycampus.enterprise.dominio.ServicioAerocivil;

/**
 * Caso de uso: decide si un vuelo entre sedes puede despegar. Primero aplica los límites propios
 * (restricción conocida, altura y radio efectivo) y solo después pide la autorización de la Aerocivil.
 * Ninguna prioridad, ni siquiera la urgente, se salta estas comprobaciones.
 */
public class AutorizadorVuelo {

    private final PoliticaVuelo politica;
    private final ServicioAerocivil aerocivil;

    public AutorizadorVuelo(PoliticaVuelo politica, ServicioAerocivil aerocivil) {
        this.politica = Objects.requireNonNull(politica, "politica");
        this.aerocivil = Objects.requireNonNull(aerocivil, "aerocivil");
    }

    public DecisionVuelo decidir(PlanVuelo plan) {
        Objects.requireNonNull(plan, "plan");
        if (!politica.tieneRestriccion(plan.sede())) {
            return DecisionVuelo.rechazada(MotivoRechazo.SIN_RESTRICCION_VIGENTE);
        }
        if (plan.alturaM() > politica.alturaMaximaM(plan.sede(), plan.zonaUrbana())) {
            return DecisionVuelo.rechazada(MotivoRechazo.ALTURA_EXCEDIDA);
        }
        if (plan.distanciaKm() > politica.radioEfectivoKm(plan.sede())) {
            return DecisionVuelo.rechazada(MotivoRechazo.RADIO_EXCEDIDO);
        }
        return consultarAerocivil(plan);
    }

    /** Falla segura (RNF-10): si la Aerocivil no responde o lanza un error, no se despega. */
    private DecisionVuelo consultarAerocivil(PlanVuelo plan) {
        try {
            return aerocivil.autoriza(plan)
                    ? DecisionVuelo.autorizada()
                    : DecisionVuelo.rechazada(MotivoRechazo.AEROCIVIL_RECHAZA);
        } catch (RuntimeException ignored) {
            // Sin respuesta confiable no se vuela: el resultado es AEROCIVIL_NO_RESPONDE.
            return DecisionVuelo.rechazada(MotivoRechazo.AEROCIVIL_NO_RESPONDE);
        }
    }
}
