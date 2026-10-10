package skycampus.enterprise.dominio;

/** Drone de una sede de la red. Es un dato puro: no sabe de bases de datos ni de HTTP. */
public record Drone(String id, String sede, int bateria) {

    public static final int BATERIA_MINIMA = 0;
    public static final int BATERIA_MAXIMA = 100;

    public Drone {
        exigirTexto(id, "id");
        exigirTexto(sede, "sede");
        if (bateria < BATERIA_MINIMA || bateria > BATERIA_MAXIMA) {
            throw new IllegalArgumentException("La batería debe estar entre "
                    + BATERIA_MINIMA + " y " + BATERIA_MAXIMA + ".");
        }
    }

    private static void exigirTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El campo " + campo + " es obligatorio.");
        }
    }
}
