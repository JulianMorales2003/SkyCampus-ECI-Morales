package validacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DestinoInvalidoExceptionTest {

    @Test
    @DisplayName("es una IllegalArgumentException y conserva el mensaje")
    void excepcion_conMensaje_esIllegalArgumentExceptionConMensaje() {
        DestinoInvalidoException ex = new DestinoInvalidoException("destino malo");

        assertInstanceOf(IllegalArgumentException.class, ex);
        assertEquals("destino malo", ex.getMessage());
    }
}
