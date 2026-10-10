package validacion;

public record ResultadoValidacion(boolean valido, String motivo) {

    public ResultadoValidacion {
        if (motivo == null) {
            throw new IllegalArgumentException("El motivo no puede ser nulo; usa una cadena vacía en una aprobación.");
        }
        if (!valido && motivo.isBlank()) {
            throw new IllegalArgumentException("Un rechazo debe incluir el motivo.");
        }
    }

    public static ResultadoValidacion aprobada() {
        return new ResultadoValidacion(true, "");
    }

    public static ResultadoValidacion rechazada(String motivo) {
        return new ResultadoValidacion(false, motivo);
    }
}
