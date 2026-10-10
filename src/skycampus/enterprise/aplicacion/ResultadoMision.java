package skycampus.enterprise.aplicacion;

import skycampus.enterprise.dominio.Drone;

/** Misión creada (con su drone) o rechazada (con su motivo). */
public record ResultadoMision(String misionId, Drone drone, MotivoMision motivo) {

    public ResultadoMision {
        if ((motivo == null) == (drone == null)) {
            throw new IllegalArgumentException("Una misión creada lleva drone y una rechazada lleva motivo.");
        }
    }

    public static ResultadoMision creada(String misionId, Drone drone) {
        return new ResultadoMision(misionId, drone, null);
    }

    public static ResultadoMision rechazada(MotivoMision motivo) {
        return new ResultadoMision(null, null, motivo);
    }

    public boolean creada() {
        return motivo == null;
    }
}
