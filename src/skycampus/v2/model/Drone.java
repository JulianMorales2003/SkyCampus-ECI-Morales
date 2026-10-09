package skycampus.v2.model;

import util.Validaciones;

public record Drone(String id, TipoDrone tipo, int bateria, boolean disponible, EstadoDrone estado) {

    public static final int BATERIA_MINIMA = 0;
    public static final int BATERIA_MAXIMA = 100;

    public Drone {
        Validaciones.exigirTexto(id, "id");
        Validaciones.exigirPresente(tipo, "tipo");
        Validaciones.exigirPresente(estado, "estado");
        if (bateria < BATERIA_MINIMA || bateria > BATERIA_MAXIMA) {
            throw new IllegalArgumentException("La batería debe estar entre "
                    + BATERIA_MINIMA + " y " + BATERIA_MAXIMA + ".");
        }
    }
}
