package skycampus.enterprise.model;

import skycampus.v2.model.EstadoMision;
import skycampus.v2.model.Prioridad;
import util.Validaciones;

/**
 * Misión de la red de sedes.
 *
 * <p>{@code tiempoEntregaMinutos} solo es significativo cuando la misión está
 * {@link EstadoMision#COMPLETADA}; en los demás estados se ignora en las métricas.
 */
public record MisionRed(String id, Sede sede, String droneId, EstadoMision estado,
                        Prioridad prioridad, long tiempoEntregaMinutos) {

    public MisionRed {
        Validaciones.exigirTexto(id, "id");
        Validaciones.exigirPresente(sede, "sede");
        Validaciones.exigirTexto(droneId, "droneId");
        Validaciones.exigirPresente(estado, "estado");
        Validaciones.exigirPresente(prioridad, "prioridad");
        if (tiempoEntregaMinutos < 0) {
            throw new IllegalArgumentException("El tiempo de entrega no puede ser negativo.");
        }
    }

    public boolean entregada() {
        return estado == EstadoMision.COMPLETADA;
    }

    public boolean urgente() {
        return prioridad == Prioridad.URGENTE;
    }
}
