package skycampus.enterprise.aplicacion;

/** Lo que pide quien crea una misión. La prioridad omitida cuenta como NORMAL. */
public record ComandoMision(String origen, String destino, int pesoGramos, PrioridadMision prioridad) {

    public ComandoMision {
        exigirTexto(origen, "origen");
        exigirTexto(destino, "destino");
        if (origen.equals(destino)) {
            throw new IllegalArgumentException("El origen y el destino deben ser distintos.");
        }
        if (pesoGramos <= 0) {
            throw new IllegalArgumentException("El peso debe ser mayor que 0 g.");
        }
        prioridad = prioridad == null ? PrioridadMision.NORMAL : prioridad;
    }

    private static void exigirTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El campo " + campo + " es obligatorio.");
        }
    }
}
