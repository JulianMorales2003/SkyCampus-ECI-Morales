package validacion;

public record ResultadoValidacion(boolean valido, String motivo) {

    public static ResultadoValidacion aprobada() {
        return new ResultadoValidacion(true, "");
    }

    public static ResultadoValidacion rechazada(String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("Un rechazo debe incluir el motivo.");
        }
        return new ResultadoValidacion(false, motivo);
    }
}
