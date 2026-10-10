package skycampus.enterprise.ruta.drone;

/** Ningún drone de la fábrica puede cubrir el tramo pedido. */
public class TramoSinDroneException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public TramoSinDroneException(String mensaje) {
        super(mensaje);
    }
}
