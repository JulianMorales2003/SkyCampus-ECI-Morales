package util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class ValidacionesTest {

    @Test
    @DisplayName("exigirPresente: un valor presente no lanza excepción")
    void exigirPresente_valorPresente_noLanzaExcepcion() {
        assertDoesNotThrow(() -> Validaciones.exigirPresente("algo", "campo"));
    }

    @Test
    @DisplayName("exigirPresente: un valor nulo lanza IllegalArgumentException con el nombre del campo")
    void exigirPresente_valorNulo_lanzaExcepcionConNombreDelCampo() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> Validaciones.exigirPresente(null, "flota"));

        assertTrue(ex.getMessage().contains("flota"));
    }

    @Test
    @DisplayName("exigirTexto: un texto con contenido no lanza excepción")
    void exigirTexto_textoValido_noLanzaExcepcion() {
        assertDoesNotThrow(() -> Validaciones.exigirTexto("Bloque A", "origen"));
    }

    @ParameterizedTest(name = "texto \"{0}\" -> IllegalArgumentException")
    @NullSource
    @ValueSource(strings = {"", "   "})
    @DisplayName("exigirTexto: nulo, vacío o en blanco lanza IllegalArgumentException")
    void exigirTexto_textoNuloVacioOEnBlanco_lanzaExcepcion(String texto) {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> Validaciones.exigirTexto(texto, "origen"));

        assertTrue(ex.getMessage().contains("origen"));
    }
}
