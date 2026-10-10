package skycampus.enterprise.analytics;

import java.util.OptionalDouble;
import skycampus.enterprise.model.Sede;
import util.Validaciones;

/**
 * Métricas de eficiencia de una sede que tuvo al menos una misión.
 *
 * <p>Una sede sin misiones no tiene {@code EficienciaSede}: el análisis la representa
 * con un {@code Optional} vacío.
 */
public record EficienciaSede(Sede sede, long totalMisiones, long entregadas, long minutosEntregadas,
                             long urgentes, String droneMasUtilizado) {

    public EficienciaSede {
        Validaciones.exigirPresente(sede, "sede");
        Validaciones.exigirTexto(droneMasUtilizado, "droneMasUtilizado");
        if (totalMisiones <= 0) {
            throw new IllegalArgumentException("Una sede con métricas debe tener al menos una misión.");
        }
        if (entregadas < 0 || entregadas > totalMisiones) {
            throw new IllegalArgumentException("Las misiones entregadas deben estar entre 0 y el total.");
        }
        if (urgentes < 0 || urgentes > totalMisiones) {
            throw new IllegalArgumentException("Las misiones urgentes deben estar entre 0 y el total.");
        }
        if (minutosEntregadas < 0) {
            throw new IllegalArgumentException("Los minutos de entrega no pueden ser negativos.");
        }
    }

    /** Misiones entregadas sobre el total, entre 0.0 y 1.0. */
    public double tasaExito() {
        return (double) entregadas / totalMisiones;
    }

    /** Promedio en minutos solo de las misiones entregadas; vacío si ninguna se entregó. */
    public OptionalDouble tiempoPromedioEntregaMinutos() {
        if (entregadas == 0) {
            return OptionalDouble.empty();
        }
        return OptionalDouble.of((double) minutosEntregadas / entregadas);
    }

    /** Porcentaje de misiones urgentes sobre el total, entre 0.0 y 100.0. */
    public double porcentajeUrgentes() {
        return urgentes * 100.0 / totalMisiones;
    }
}
