package util;

/**
 * Única salida por consola del proyecto. Las demos (paquete app) y el adaptador de alertas por
 * consola imprimen aquí, así que el uso de System.out queda en un solo lugar.
 */
public final class Consola {

    private Consola() {
    }

    @SuppressWarnings("java:S106") // Imprimir en la consola es la función de esta clase.
    public static void imprimir(String texto) {
        System.out.println(texto);
    }
}
