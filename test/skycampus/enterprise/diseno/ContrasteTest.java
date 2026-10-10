package skycampus.enterprise.diseno;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Contraste: razón WCAG 2.1 entre dos colores")
class ContrasteTest {

    private static final double DELTA = 0.001;
    private static final String NEGRO = "#000000";
    private static final String BLANCO = "#FFFFFF";

    @Test
    @DisplayName("el blanco tiene luminancia 1 y el negro 0")
    void luminancia_blancoYNegro_extremosDeLaEscala() {
        assertEquals(1.0, Contraste.luminancia(BLANCO), DELTA);
        assertEquals(0.0, Contraste.luminancia(NEGRO), DELTA);
    }

    @Test
    @DisplayName("negro sobre blanco es 21:1 y un color contra sí mismo es 1:1")
    void razon_extremos_veintiunoYUno() {
        assertEquals(21.0, Contraste.razon(NEGRO, BLANCO), DELTA);
        assertEquals(1.0, Contraste.razon("#336699", "#336699"), DELTA);
    }

    @Test
    @DisplayName("la razón no depende del orden de los colores")
    void razon_ordenInvertido_mismoResultado() {
        assertEquals(Contraste.razon("#00457C", BLANCO), Contraste.razon(BLANCO, "#00457C"), DELTA);
    }

    @ParameterizedTest(name = "{0} sobre blanco: {1}:1")
    @CsvSource({
        "#00457C, 9.803",
        "#7B0000, 11.399",
        "#0057A8, 7.170",
        "#2D6A4F, 6.391",
        "#E63946, 4.168",
        "#FFEE00, 1.201"
    })
    @DisplayName("valores de referencia calculados con la fórmula WCAG")
    void razon_coloresDeLasSedes_coincideConLaReferencia(String color, double esperada) {
        assertEquals(esperada, Contraste.razon(BLANCO, color), DELTA);
    }

    @ParameterizedTest(name = "{0} tiene luminancia {1}")
    @CsvSource({
        "#0A0A0A, 0.00304",
        "#0B0B0B, 0.00334"
    })
    @DisplayName("los dos tramos de la curva sRGB (lineal y potencia) dan la luminancia esperada")
    void luminancia_alrededorDelCorte_usaElTramoCorrecto(String color, double esperada) {
        assertEquals(esperada, Contraste.luminancia(color), 0.00001);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"#FFF", "FFFFFF", "#GGGGGG", "#FFFFFFF", "rgb(0,0,0)"})
    @DisplayName("rechaza lo que no es #RRGGBB")
    void luminancia_formatoInvalido_lanzaExcepcion(String color) {
        assertThrows(IllegalArgumentException.class, () -> Contraste.luminancia(color));
    }

    @Test
    @DisplayName("acepta minúsculas")
    void luminancia_hexEnMinusculas_igualQueMayusculas() {
        assertEquals(Contraste.luminancia("#E63946"), Contraste.luminancia("#e63946"), DELTA);
    }
}
