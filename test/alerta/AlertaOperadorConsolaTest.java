package alerta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AlertaOperadorConsolaTest {

    private final PrintStream salidaOriginal = System.out;
    private ByteArrayOutputStream captura;
    private final AlertaOperadorConsola alerta = new AlertaOperadorConsola();

    @BeforeEach
    void capturarSalida() {
        captura = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captura, true, StandardCharsets.UTF_8));
    }

    @AfterEach
    void restaurarSalida() {
        System.setOut(salidaOriginal);
    }

    @Test
    @DisplayName("enviar: imprime la alerta con el operador y el mensaje")
    void enviar_datosValidos_imprimeLaAlerta() {
        alerta.enviar("Laura", "Batería crítica en D-01");

        assertEquals("[ALERTA para Laura] Batería crítica en D-01" + System.lineSeparator(),
                captura.toString(StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("enviar: operador nulo o en blanco lanza IllegalArgumentException")
    void enviar_operadorInvalido_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> alerta.enviar(null, "msg"));
        assertThrows(IllegalArgumentException.class, () -> alerta.enviar("  ", "msg"));
    }

    @Test
    @DisplayName("enviar: mensaje nulo o en blanco lanza IllegalArgumentException")
    void enviar_mensajeInvalido_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> alerta.enviar("Laura", null));
        assertThrows(IllegalArgumentException.class, () -> alerta.enviar("Laura", ""));
    }
}
