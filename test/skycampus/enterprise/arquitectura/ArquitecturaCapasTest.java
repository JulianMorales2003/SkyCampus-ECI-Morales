package skycampus.enterprise.arquitectura;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Regla de dependencias: infraestructura -> aplicación -> dominio. Se comprueba leyendo los
 * import de cada archivo fuente, así que Spring o JPA colados en el dominio rompen el build.
 */
@DisplayName("Arquitectura por capas")
class ArquitecturaCapasTest {

    private static final String BASE = "src/skycampus/enterprise/";
    private static final String PAQUETE_DOMINIO = "skycampus.enterprise.dominio.";
    private static final String CAPA_DOMINIO = "dominio";
    private static final String CAPA_APLICACION = "aplicacion";
    private static final String SERVICIO_SPRING = "org.springframework.stereotype.Service";

    private static List<Path> fuentes(String capa) throws IOException {
        try (Stream<Path> archivos = Files.list(Path.of(BASE + capa))) {
            return archivos.filter(ruta -> ruta.toString().endsWith(".java")).sorted().toList();
        }
    }

    private static String mensaje(Path archivo, String importado) {
        return archivo.getFileName() + " importa " + importado;
    }

    private static List<String> imports(Path fuente) throws IOException {
        return importsDe(Files.readString(fuente));
    }

    static List<String> importsDe(String codigo) {
        return codigo.lines()
                .map(String::strip)
                .filter(linea -> linea.startsWith("import "))
                .map(linea -> linea.substring("import ".length()).replace("static ", "").replace(";", "").strip())
                .toList();
    }

    static boolean permitidoEnDominio(String importado) {
        return importado.startsWith("java.") || importado.startsWith(PAQUETE_DOMINIO);
    }

    private static boolean tieneAnotaciones(String codigo) {
        return codigo.lines().map(String::strip)
                .anyMatch(linea -> linea.startsWith("@") && !linea.startsWith("@Override"));
    }

    @Test
    @DisplayName("el dominio solo importa java.* y su propio paquete")
    void dominio_noImportaFrameworksNiLibrerias() throws IOException {
        List<Path> archivos = fuentes(CAPA_DOMINIO);
        assertFalse(archivos.isEmpty(), "no se encontraron fuentes del dominio");
        for (Path archivo : archivos) {
            for (String importado : imports(archivo)) {
                assertTrue(permitidoEnDominio(importado), mensaje(archivo, importado));
            }
        }
    }

    @Test
    @DisplayName("el dominio no lleva anotaciones (@Entity, @Service, @Component...)")
    void dominio_noUsaAnotaciones() throws IOException {
        for (Path archivo : fuentes(CAPA_DOMINIO)) {
            assertFalse(tieneAnotaciones(Files.readString(archivo)), archivo.getFileName() + " tiene anotaciones");
        }
    }

    @Test
    @DisplayName("la aplicación depende del dominio y nunca de la infraestructura ni de frameworks")
    void aplicacion_noDependeDeInfraestructura() throws IOException {
        List<Path> archivos = fuentes(CAPA_APLICACION);
        assertFalse(archivos.isEmpty(), "no se encontraron fuentes de la aplicación");
        for (Path archivo : archivos) {
            for (String importado : imports(archivo)) {
                boolean valido = importado.startsWith("java.") || importado.startsWith(PAQUETE_DOMINIO);
                assertTrue(valido, mensaje(archivo, importado));
            }
        }
    }

    @Test
    @DisplayName("la infraestructura solo depende del dominio (implementa sus puertos)")
    void infraestructura_soloDependeDelDominio() throws IOException {
        for (Path archivo : fuentes("infraestructura")) {
            for (String importado : imports(archivo)) {
                assertFalse(importado.startsWith("skycampus.enterprise.aplicacion"),
                        mensaje(archivo, importado));
            }
        }
    }

    @Test
    @DisplayName("el detector reconoce una violación (Spring en el dominio)")
    void detector_reconoceViolaciones() {
        String sucio = "package x;\nimport " + SERVICIO_SPRING + ";\n@Service\nclass A {}\n";

        assertEquals(List.of(SERVICIO_SPRING), importsDe(sucio));
        assertFalse(permitidoEnDominio(SERVICIO_SPRING));
        assertFalse(permitidoEnDominio("jakarta.persistence.Entity"));
        assertFalse(permitidoEnDominio("util.Validaciones"));
        assertTrue(permitidoEnDominio("java.util.List"));
        assertTrue(permitidoEnDominio("skycampus.enterprise.dominio.Drone"));
        assertTrue(tieneAnotaciones(sucio));
        assertFalse(tieneAnotaciones("class A {\n    @Override\n    void f() {}\n}\n"));
    }

    @Test
    @DisplayName("el import estático también se detecta")
    void detector_reconoceImportEstatico() {
        assertEquals(List.of("org.mockito.Mockito.when"), importsDe("import static org.mockito.Mockito.when;"));
    }
}
