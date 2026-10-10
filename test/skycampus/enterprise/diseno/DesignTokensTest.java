package skycampus.enterprise.diseno;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Comprueba que el sistema de diseño de {@code docs/diseno} cumple lo que promete: los componentes no
 * contienen valores de ninguna sede, las sedes solo definen sus 4 tokens y la estructura HTML de cada
 * componente es la misma en todas las identidades.
 */
@DisplayName("Design tokens de SkyCampus Enterprise")
class DesignTokensTest {

    private static final Path DISENO = Path.of("docs/diseno");
    private static final Path TEMAS = DISENO.resolve("temas");
    private static final Path COMPONENTES = DISENO.resolve("componentes.css");
    private static final Path TARJETAS = DISENO.resolve("tarjeta-drone.html");
    private static final Path PANELES = DISENO.resolve("panel-mision-activa.html");
    private static final List<Path> CSS_COMPARTIDO = List.of(DISENO.resolve("base.css"), COMPONENTES,
            DISENO.resolve("demo.css"));
    private static final Set<String> SEDES_DEL_RETO = Set.of("eci", "unal", "uniandes", "eafit");
    private static final List<String> TOKENS = List.of("--color-primary", "--color-alert", "--font-ui",
            "--border-radius");
    private static final String INICIO_TARJETA = "<article class=\"tarjeta-drone\"";
    private static final String FIN_TARJETA = "</article>";
    private static final String INICIO_PANEL = "<section class=\"panel-mision\"";
    private static final String FIN_PANEL = "</section>";
    private static final Pattern COLOR_LITERAL = Pattern.compile("#[0-9A-Fa-f]{3,8}\\b|\\b(?:rgb|rgba|hsl|hsla)\\(");
    private static final Pattern DECLARACION = Pattern.compile("([a-z-]+)\\s*:\\s*([^;{}]+);");
    private static final Pattern COLOR_CON_NOMBRE = Pattern.compile(
            "\\b(?:white|black|red|blue|green|gray|grey|yellow|orange|purple|navy|maroon)\\b");
    private static final Pattern ETIQUETA = Pattern.compile("<(/?)([a-z0-9]+)([^>]*)>");
    private static final Pattern CLASE = Pattern.compile("class=\"([^\"]*)\"");
    private static final Pattern DATA_SEDE = Pattern.compile("data-sede=\"([^\"]*)\"");
    private static final Pattern ESTILO_EN_LINEA = Pattern.compile("style=\"([^\"]*)\"");
    private static final Pattern ENLACE_LOCAL = Pattern.compile("<link rel=\"stylesheet\" href=\"(?!https?:)([^\"]+)\"");

    private static String leer(Path archivo) {
        try {
            return Files.readString(archivo);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static Stream<Path> archivosDeTema() throws IOException {
        try (Stream<Path> archivos = Files.list(TEMAS)) {
            return archivos.filter(a -> a.toString().endsWith(".css")).sorted().toList().stream();
        }
    }

    private static List<TemaSede> temasDe(Path carpeta) throws IOException {
        try (Stream<Path> archivos = Files.list(carpeta)) {
            return archivos.filter(a -> a.toString().endsWith(".css")).sorted()
                    .map(a -> TemaSede.desdeCss(leer(a))).toList();
        }
    }

    private static List<String> bloques(String html, String inicio, String fin) {
        List<String> encontrados = new ArrayList<>();
        int desde = html.indexOf(inicio);
        while (desde >= 0) {
            int hasta = html.indexOf(fin, desde) + fin.length();
            encontrados.add(html.substring(desde, hasta));
            desde = html.indexOf(inicio, hasta);
        }
        return encontrados;
    }

    /** Etiquetas y clases en orden: lo que define la estructura, sin textos ni valores de atributos. */
    private static String esqueleto(String bloque) {
        StringBuilder estructura = new StringBuilder();
        Matcher etiqueta = ETIQUETA.matcher(bloque);
        while (etiqueta.find()) {
            estructura.append(etiqueta.group(1)).append(etiqueta.group(2));
            Matcher clase = CLASE.matcher(etiqueta.group(3));
            if (clase.find()) {
                estructura.append('.').append(clase.group(1));
            }
            estructura.append('|');
        }
        return estructura.toString();
    }

    private static Set<String> valoresDe(Pattern patron, String texto) {
        Set<String> valores = new TreeSet<>();
        Matcher m = patron.matcher(texto);
        while (m.find()) {
            valores.add(m.group(1));
        }
        return valores;
    }

    // ---------- Temas por sede ----------

    @Test
    @DisplayName("están las 4 sedes del reto y el nombre de cada archivo es el de su sede")
    void temas_cuatroSedesDelReto_archivoPorSede() throws IOException {
        Set<String> nombres = archivosDeTema().map(a -> a.getFileName().toString().replace(".css", ""))
                .collect(Collectors.toSet());

        assertTrue(nombres.containsAll(SEDES_DEL_RETO));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("archivosDeTema")
    @DisplayName("cada tema define exactamente los 4 tokens, con su sede y contraste AA")
    void tema_archivo_defineLosCuatroTokensYCumpleContraste(Path archivo) {
        TemaSede tema = TemaSede.desdeCss(leer(archivo));

        assertEquals(archivo.getFileName().toString(), tema.sede() + ".css");
        assertTrue(tema.cumpleAccesibilidad(), "texto blanco sobre " + tema.colorPrimario());
        assertEquals(TemaSede.ALERTA_COMUN, tema.colorAlerta().toUpperCase());
    }

    @Test
    @DisplayName("dos sedes no comparten el color primario")
    void temas_colorPrimario_esUnicoPorSede() throws IOException {
        List<String> primarios = temasDe(TEMAS).stream().map(t -> t.colorPrimario().toUpperCase()).toList();

        assertEquals(primarios.size(), new HashSet<>(primarios).size());
    }

    @Test
    @DisplayName("añadir una quinta universidad es solo un archivo con sus 4 tokens: la validación es la misma")
    void quintaSede_soloTokens_pasaLaMismaValidacion(@TempDir Path carpeta) throws IOException {
        for (Path tema : archivosDeTema().toList()) {
            Files.copy(tema, carpeta.resolve(tema.getFileName()));
        }
        Files.writeString(carpeta.resolve("nueva.css"), "[data-sede=\"nueva\"] {\n"
                + "  --color-primary: #5B2C83;\n  --color-alert: #E63946;\n"
                + "  --font-ui: \"Roboto\", system-ui, sans-serif;\n  --border-radius: 6px;\n}\n");

        List<TemaSede> temas = temasDe(carpeta);

        assertEquals(5, temas.size());
        assertTrue(temas.stream().allMatch(TemaSede::cumpleAccesibilidad));
    }

    // ---------- Componentes sin valores de sede ----------

    @ParameterizedTest(name = "{0}")
    @MethodSource("cssCompartido")
    @DisplayName("el CSS compartido no tiene colores literales ni nombres de color")
    void cssCompartido_componentesYDemo_sinColoresLiterales(Path archivo) {
        if (archivo.equals(DISENO.resolve("base.css"))) {
            return;
        }
        String css = leer(archivo);

        assertFalse(COLOR_LITERAL.matcher(css).find(), archivo + " tiene un color literal");
        Matcher declaracion = DECLARACION.matcher(css.replaceAll("(?s)/\\*.*?\\*/", ""));
        while (declaracion.find()) {
            assertFalse(COLOR_CON_NOMBRE.matcher(declaracion.group(2)).find(),
                    archivo + " usa un color por nombre en " + declaracion.group(1));
        }
    }

    static Stream<Path> cssCompartido() {
        return CSS_COMPARTIDO.stream();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("cssCompartido")
    @DisplayName("solo los temas definen los 4 tokens: base, componentes y demo no los declaran")
    void cssCompartido_ningunoDeclaraTokensDeSede(Path archivo) {
        String css = leer(archivo);

        for (String token : TOKENS) {
            assertFalse(Pattern.compile(Pattern.quote(token) + "\\s*:").matcher(css).find(),
                    archivo + " declara " + token);
        }
    }

    @Test
    @DisplayName("los componentes usan la fuente y el radio solo a través del token")
    void componentes_fuenteYRadio_siempreViaToken() {
        Matcher declaracion = DECLARACION.matcher(leer(COMPONENTES));
        int fuentes = 0;
        int radios = 0;
        while (declaracion.find()) {
            String valor = declaracion.group(2).trim();
            if (declaracion.group(1).equals("font-family")) {
                fuentes++;
                assertEquals("var(--font-ui)", valor);
            }
            if (declaracion.group(1).equals("border-radius")) {
                radios++;
                assertTrue(valor.contains("var(--border-radius)"), "radio literal: " + valor);
            }
        }
        assertTrue(fuentes >= 2);
        assertTrue(radios >= 8);
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"--color-primary", "--color-alert", "--font-ui", "--border-radius"})
    @DisplayName("cada token se consume de verdad en los componentes (ninguno es letra muerta)")
    void componentes_cadaToken_seUsa(String token) {
        assertTrue(leer(COMPONENTES).contains("var(" + token + ")"));
    }

    // ---------- HTML de demostración ----------

    static Stream<Path> paginas() {
        return Stream.of(TARJETAS, PANELES);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("paginas")
    @DisplayName("las páginas no traen estilos propios: ni bloques style ni estilos en línea con colores, fuentes o radios")
    void pagina_sinEstilosPropios_soloLaVariableDeNivel(Path pagina) {
        String html = leer(pagina);

        assertFalse(html.contains("<style"));
        assertFalse(COLOR_LITERAL.matcher(html).find());
        for (String estilo : valoresDe(ESTILO_EN_LINEA, html)) {
            assertTrue(estilo.matches("--nivel: \\d{1,3}%"), "estilo en línea no permitido: " + estilo);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("paginas")
    @DisplayName("cada sede usada tiene su tema, está enlazado y todo CSS local enlazado existe")
    void pagina_sedesUsadas_tienenTemaEnlazado(Path pagina) {
        String html = leer(pagina);
        Set<String> enlaces = valoresDe(ENLACE_LOCAL, html);

        for (String sede : valoresDe(DATA_SEDE, html)) {
            assertTrue(Files.exists(TEMAS.resolve(sede + ".css")), "sin tema para " + sede);
            assertTrue(enlaces.contains("temas/" + sede + ".css"), "tema sin enlazar: " + sede);
        }
        for (String enlace : enlaces) {
            assertTrue(Files.exists(DISENO.resolve(enlace)), "no existe " + enlace);
        }
    }

    @Test
    @DisplayName("la tarjeta de drone aparece en 4 sedes x 4 estados y tiene la misma estructura HTML en todas")
    void tarjetaDeDrone_cuatroIdentidades_mismaEstructura() {
        List<String> tarjetas = bloques(leer(TARJETAS), INICIO_TARJETA, FIN_TARJETA);

        assertEquals(16, tarjetas.size());
        assertEquals(1, tarjetas.stream().map(DesignTokensTest::esqueleto).collect(Collectors.toSet()).size());
        assertEquals(SEDES_DEL_RETO, valoresDe(DATA_SEDE, leer(TARJETAS)));
    }

    @Test
    @DisplayName("el panel de misión activa se muestra en ECI, UNAL y Uniandes con la misma estructura HTML")
    void panelDeMision_tresVariantes_mismaEstructura() {
        String html = leer(PANELES);
        List<String> paneles = bloques(html, INICIO_PANEL, FIN_PANEL);

        assertEquals(3, paneles.size());
        assertEquals(1, paneles.stream().map(DesignTokensTest::esqueleto).collect(Collectors.toSet()).size());
        assertEquals(Set.of("eci", "unal", "uniandes"), valoresDe(DATA_SEDE, html));
    }

    @Test
    @DisplayName("la tarjeta dentro del panel tiene la misma estructura que la tarjeta suelta")
    void tarjetaEnElPanel_mismaEstructuraQueLaSuelta() {
        String suelta = esqueleto(bloques(leer(TARJETAS), INICIO_TARJETA, FIN_TARJETA).get(0));

        for (String panel : bloques(leer(PANELES), INICIO_PANEL, FIN_PANEL)) {
            assertEquals(suelta, esqueleto(bloques(panel, INICIO_TARJETA, FIN_TARJETA).get(0)));
        }
    }

    @Test
    @DisplayName("el panel cambia de identidad solo por el atributo data-sede del contenedor")
    void panelDeMision_variantes_difierenSoloEnElContenedor() {
        String html = leer(PANELES);
        List<String> contenedores = new ArrayList<>();
        Matcher m = Pattern.compile("<div class=\"demo-columna sede-lienzo\" data-sede=\"[a-z]+\">").matcher(html);
        while (m.find()) {
            contenedores.add(m.group().replaceAll("data-sede=\"[a-z]+\"", ""));
        }

        assertEquals(3, contenedores.size());
        assertEquals(1, new HashSet<>(contenedores).size());
    }
}
