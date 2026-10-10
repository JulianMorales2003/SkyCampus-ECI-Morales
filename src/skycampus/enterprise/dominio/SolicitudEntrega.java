package skycampus.enterprise.dominio;

/** Pedido de llevar un paquete de la sede de origen a la sede de destino. */
public record SolicitudEntrega(String id, String origen, String destino) {

    public SolicitudEntrega {
        exigirTexto(id, "id");
        exigirTexto(origen, "origen");
        exigirTexto(destino, "destino");
        if (origen.equals(destino)) {
            throw new IllegalArgumentException("El origen y el destino deben ser distintos.");
        }
    }

    private static void exigirTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El campo " + campo + " es obligatorio.");
        }
    }
}
