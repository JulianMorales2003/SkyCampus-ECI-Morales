package util;

public final class Validaciones {

    private Validaciones() {
    }

    public static void exigirPresente(Object valor, String campo) {
        if (valor == null) {
            throw new IllegalArgumentException("El campo '" + campo + "' no puede ser nulo.");
        }
    }

    public static void exigirTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El campo '" + campo + "' no puede ser nulo ni vacío.");
        }
    }
}
