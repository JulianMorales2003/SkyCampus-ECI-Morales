package skycampus.enterprise.dominio;

/** Respuesta a una solicitud de vuelo: autorizado, o rechazado con su motivo. */
public record DecisionVuelo(boolean autorizado, MotivoRechazo motivo) {

    public DecisionVuelo {
        if (autorizado != (motivo == null)) {
            throw new IllegalArgumentException("Un vuelo autorizado no lleva motivo y uno rechazado sí.");
        }
    }

    public static DecisionVuelo autorizada() {
        return new DecisionVuelo(true, null);
    }

    public static DecisionVuelo rechazada(MotivoRechazo motivo) {
        return new DecisionVuelo(false, motivo);
    }
}
