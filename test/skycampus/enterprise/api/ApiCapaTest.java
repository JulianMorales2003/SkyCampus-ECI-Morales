package skycampus.enterprise.api;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** La capa de entrada REST solo conoce la aplicación y el dominio: nada de la infraestructura ni de frameworks. */
@DisplayName("Arquitectura · capa api")
class ApiCapaTest {

    private static final List<String> PERMITIDOS = List.of(
            "java.", "com.sun.net.httpserver.", "skycampus.enterprise.aplicacion.", "skycampus.enterprise.dominio.");

    @Test
    @DisplayName("la api solo importa java.*, el servidor HTTP del JDK, la aplicación y el dominio")
    void api_soloDependeDeAplicacionYDominio() throws IOException {
        try (Stream<Path> archivos = Files.list(Path.of("src/skycampus/enterprise/api"))) {
            List<Path> fuentes = archivos.filter(ruta -> ruta.toString().endsWith(".java")).toList();
            assertFalse(fuentes.isEmpty(), "no se encontraron fuentes de la api");
            for (Path fuente : fuentes) {
                for (String linea : Files.readAllLines(fuente)) {
                    if (linea.startsWith("import ")) {
                        String importado = linea.substring("import ".length()).replace(";", "").strip();
                        assertTrue(PERMITIDOS.stream().anyMatch(importado::startsWith),
                                fuente.getFileName() + " importa " + importado);
                    }
                }
            }
        }
    }
}
