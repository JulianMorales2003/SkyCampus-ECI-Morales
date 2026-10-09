package util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Consola")
class ConsolaTest {

    private final PrintStream salidaOriginal = System.out;
    private final ByteArrayOutputStream captura = new ByteArrayOutputStream();

    @BeforeEach
    void capturarSalida() {
        System.setOut(new PrintStream(captura, true, StandardCharsets.UTF_8));
    }

    @AfterEach
    void restaurarSalida() {
        System.setOut(salidaOriginal);
    }

    @Test
    @DisplayName("imprimir_texto_loEscribeEnLaSalidaEstandarConSaltoDeLinea")
    void imprimir_texto_loEscribeEnLaSalidaEstandarConSaltoDeLinea() {
        Consola.imprimir("Misión asignada");

        assertEquals("Misión asignada" + System.lineSeparator(), captura.toString(StandardCharsets.UTF_8));
    }
}
