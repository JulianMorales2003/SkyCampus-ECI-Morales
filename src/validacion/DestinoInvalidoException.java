package validacion;

public class DestinoInvalidoException extends IllegalArgumentException {

    private static final long serialVersionUID = 1L;

    public DestinoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
