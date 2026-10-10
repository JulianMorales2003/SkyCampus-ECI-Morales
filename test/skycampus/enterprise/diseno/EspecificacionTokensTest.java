package skycampus.enterprise.diseno;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/** Comprueba que la especificación escrita de los tokens coincide con los archivos de {@code docs/diseno}. */
@DisplayName("Especificación de design tokens (INFERNAPE08)")
class EspecificacionTokensTest {

    private static final Path DOCUMENTO = Path.of("docs/INFERNAPE08-DESIGN-TOKENS.md");
    private static final Path DISENO = Path.of("docs/diseno");
    private static final Pattern VARIABLE = Pattern.compile("(--[a-z0-9-]+)\\s*:");
    private static final Pattern IMAGEN = Pattern.compile("!\\[[^\\]]*]\\(([^)]+)\\)");

    private static String leer(Path archivo) {
        try {
            return Files.readString(archivo);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static Stream<Path> archivosDeTema() throws IOException {
        try (Stream<Path> archivos = Files.list(DISENO.resolve("temas"))) {
            return archivos.filter(a -> a.toString().endsWith(".css")).sorted().toList().stream();
        }
    }

    static Stream<Path> archivosDelSistema() throws IOException {
        List<Path> archivos = new ArrayList<>();
        try (Stream<Path> todos = Files.walk(DISENO)) {
            todos.filter(Files::isRegularFile).forEach(archivos::add);
        }
        return archivos.stream().sorted();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("archivosDeTema")
    @DisplayName("el color, la fuente y el radio de cada sede en el documento son los del archivo de tema")
    void tabla_valoresDeCadaSede_coincidenConElTema(Path archivo) {
        TemaSede tema = TemaSede.desdeCss(leer(archivo));
        String documento = leer(DOCUMENTO);

        assertTrue(documento.contains(tema.colorPrimario().toUpperCase()), "color de " + tema.sede());
        assertTrue(documento.contains(tema.fuenteUi()), "fuente de " + tema.sede());
        assertTrue(documento.contains("`" + tema.radioBorde() + "`"), "radio de " + tema.sede());
    }

    @Test
    @DisplayName("cada variable compartida de base.css está descrita en el documento")
    void baseCss_cadaVariable_estaEnElDocumento() {
        String documento = leer(DOCUMENTO);
        Matcher variable = VARIABLE.matcher(leer(DISENO.resolve("base.css")));
        int total = 0;

        while (variable.find()) {
            total++;
            String nombre = variable.group(1).replaceAll("-\\d$", "");
            assertTrue(documento.contains(nombre), "falta en el documento: " + nombre);
        }
        assertEquals(17, total);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("archivosDelSistema")
    @DisplayName("cada archivo de docs/diseno se menciona en el documento")
    void archivosDelSistema_cadaUno_estaMencionado(Path archivo) {
        String nombre = archivo.getFileName().toString();

        assertTrue(leer(DOCUMENTO).contains(nombre), "el documento no menciona " + nombre);
    }

    @Test
    @DisplayName("las imágenes del documento existen")
    void imagenes_rutasRelativas_existen() {
        Matcher imagen = IMAGEN.matcher(leer(DOCUMENTO));
        int total = 0;

        while (imagen.find()) {
            total++;
            assertTrue(Files.exists(DOCUMENTO.resolveSibling(imagen.group(1))), imagen.group(1));
        }
        assertTrue(total >= 2);
    }
}
