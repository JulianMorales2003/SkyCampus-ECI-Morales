package skycampus.enterprise.diseno;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("TemaSede: los 4 tokens que cambian entre sedes")
class TemaSedeTest {

    private static final String SEDE = "eci";
    private static final String PRIMARIO = "#00457C";
    private static final String ALERTA = "#E63946";
    private static final String FUENTE = "\"Space Grotesk\", system-ui, sans-serif";
    private static final String RADIO = "10px";
    private static final String TOKENS_VALIDOS = "--color-primary: #00457C; --color-alert: #E63946; "
            + "--font-ui: \"Space Grotesk\", system-ui, sans-serif; --border-radius: 10px;";

    private static String tema(String sede, String declaraciones) {
        return "[data-sede=\"" + sede + "\"] { " + declaraciones + " }";
    }

    private static String mensaje(String css) {
        return assertThrows(IllegalArgumentException.class, () -> TemaSede.desdeCss(css)).getMessage();
    }

    @Test
    @DisplayName("lee un archivo de tema completo, con comentarios")
    void desdeCss_temaCompleto_devuelveLosCuatroTokens() {
        TemaSede tema = TemaSede.desdeCss("/* ECI */\n" + tema(SEDE, TOKENS_VALIDOS));

        assertEquals(new TemaSede(SEDE, PRIMARIO, ALERTA, FUENTE, RADIO), tema);
    }

    @Test
    @DisplayName("el orden de los tokens y los saltos de línea no importan")
    void desdeCss_otroOrdenYSaltos_mismoResultado() {
        String css = "[data-sede=\"eci\"] {\n  --border-radius: 10px;\n  --font-ui: \"Space Grotesk\", system-ui, "
                + "sans-serif;\n  --color-alert: #E63946;\n  --color-primary: #00457C;\n}\n";

        assertEquals(SEDE, TemaSede.desdeCss(css).sede());
        assertEquals(PRIMARIO, TemaSede.desdeCss(css).colorPrimario());
    }

    @Test
    @DisplayName("sin bloque de sede no hay tema")
    void desdeCss_sinBloque_lanzaExcepcion() {
        assertTrue(mensaje("/* vacío */").contains("bloque"));
    }

    @Test
    @DisplayName("un archivo no puede definir dos sedes")
    void desdeCss_dosSedes_lanzaExcepcion() {
        String css = tema(SEDE, TOKENS_VALIDOS) + "\n" + tema("unal", TOKENS_VALIDOS);

        assertTrue(mensaje(css).contains("una sola sede"));
    }

    @Test
    @DisplayName("si falta un token se nombra cuál")
    void desdeCss_faltaUnToken_loNombra() {
        String css = tema(SEDE, "--color-primary: #00457C; --color-alert: #E63946; --font-ui: Inter, sans-serif;");

        assertTrue(mensaje(css).contains("Faltan tokens: [--border-radius]"));
    }

    @Test
    @DisplayName("una sede no puede definir tokens extra: la lista de 4 es cerrada")
    void desdeCss_tokenDeMas_loNombra() {
        String css = tema(SEDE, TOKENS_VALIDOS + " --color-extra: #000000;");

        assertTrue(mensaje(css).contains("[--color-extra]"));
    }

    @Test
    @DisplayName("un token repetido es un error, no gana el último")
    void desdeCss_tokenRepetido_loNombra() {
        String css = tema(SEDE, TOKENS_VALIDOS + " --border-radius: 4px;");

        assertTrue(mensaje(css).contains("Token repetido: --border-radius"));
    }

    @Test
    @DisplayName("el nombre de la sede va en minúsculas")
    void desdeCss_sedeConMayusculas_lanzaExcepcion() {
        assertTrue(mensaje(tema("ECI", TOKENS_VALIDOS)).contains("minúsculas"));
    }

    @Test
    @DisplayName("el rojo de alerta es el mismo en todas las sedes, en mayúsculas o minúsculas")
    void crear_alertaDistintaDeLaComun_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> new TemaSede(SEDE, PRIMARIO, "#FF0000", FUENTE, RADIO));
        assertEquals("#e63946", new TemaSede(SEDE, PRIMARIO, "#e63946", FUENTE, RADIO).colorAlerta());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"azul", "#00457", "00457C"})
    @DisplayName("el color primario debe ser #RRGGBB")
    void crear_primarioInvalido_lanzaExcepcion(String primario) {
        assertThrows(IllegalArgumentException.class, () -> new TemaSede(SEDE, primario, ALERTA, FUENTE, RADIO));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"e", "E", "ECI", "e1", "aaaaaaaaaaaaaaaaaaaaa"})
    @DisplayName("la sede son de 2 a 20 letras minúsculas")
    void crear_sedeInvalida_lanzaExcepcion(String sede) {
        assertThrows(IllegalArgumentException.class, () -> new TemaSede(sede, PRIMARIO, ALERTA, FUENTE, RADIO));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"Inter", "sans-serif", "Inter, system-ui", "  , sans-serif", "Inter, monospace"})
    @DisplayName("toda fuente lleva al menos un respaldo y termina en sans-serif o serif")
    void crear_fuenteSinRespaldo_lanzaExcepcion(String fuente) {
        assertThrows(IllegalArgumentException.class, () -> new TemaSede(SEDE, PRIMARIO, ALERTA, fuente, RADIO));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Inter, sans-serif", "Merriweather, Georgia, serif", "\"Source Sans 3\", system-ui, sans-serif"})
    @DisplayName("fuentes con respaldo genérico válidas")
    void crear_fuenteConRespaldo_seAcepta(String fuente) {
        assertEquals(fuente, new TemaSede(SEDE, PRIMARIO, ALERTA, fuente, RADIO).fuenteUi());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"10", "100px", "10em", "-4px", "1.5px"})
    @DisplayName("el radio va en píxeles, de 1 o 2 cifras")
    void crear_radioInvalido_lanzaExcepcion(String radio) {
        assertThrows(IllegalArgumentException.class, () -> new TemaSede(SEDE, PRIMARIO, ALERTA, FUENTE, radio));
    }

    @Test
    @DisplayName("accesibilidad: el texto blanco sobre el primario necesita 4,5:1")
    void cumpleAccesibilidad_primarioOscuroYClaro_separa() {
        TemaSede oscuro = new TemaSede(SEDE, PRIMARIO, ALERTA, FUENTE, RADIO);
        TemaSede claro = new TemaSede(SEDE, "#FFEE00", ALERTA, FUENTE, RADIO);

        assertTrue(oscuro.cumpleAccesibilidad());
        assertFalse(claro.cumpleAccesibilidad());
        assertEquals(9.803, oscuro.contrasteTextoSobrePrimario(), 0.001);
    }

    @Test
    @DisplayName("el rojo de alerta se distingue del fondo blanco como elemento gráfico (3:1), no como texto")
    void contrasteAlertaSobreFondo_alertaComun_superaElMinimoGraficoPeroNoElDeTexto() {
        double razon = new TemaSede(SEDE, PRIMARIO, ALERTA, FUENTE, RADIO).contrasteAlertaSobreFondo();

        assertTrue(razon >= TemaSede.MINIMO_GRAFICO);
        assertTrue(razon < TemaSede.MINIMO_TEXTO);
    }

    @Test
    @DisplayName("el límite exacto: 4,5:1 cumple y por debajo no")
    void cumpleAccesibilidad_enElLimite_incluyeLaIgualdad() {
        // #767676 sobre blanco es 4,54:1 (AA); #777777 es 4,48:1 (no cumple).
        assertTrue(new TemaSede(SEDE, "#767676", ALERTA, FUENTE, RADIO).cumpleAccesibilidad());
        assertFalse(new TemaSede(SEDE, "#777777", ALERTA, FUENTE, RADIO).cumpleAccesibilidad());
    }
}
