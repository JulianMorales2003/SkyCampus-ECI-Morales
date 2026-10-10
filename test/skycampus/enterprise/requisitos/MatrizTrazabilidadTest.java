package skycampus.enterprise.requisitos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;

/**
 * Comprueba la matriz de trazabilidad de {@code docs/trazabilidad}: cada requisito tiene caso de uso,
 * historia de usuario y al menos una prueba que existe de verdad, y ningún caso de uso queda huérfano.
 */
@DisplayName("Matriz de trazabilidad de Enterprise")
class MatrizTrazabilidadTest {

    private static final Path MATRIZ = Path.of("docs/trazabilidad/matriz-enterprise.csv");
    private static final Path CASOS_DE_USO = Path.of("docs/trazabilidad/casos-de-uso-enterprise.csv");
    private static final Set<String> MOSCOW = Set.of("MUST", "SHOULD", "COULD", "WONT");
    private static final Map<String, Integer> CONFLICTOS = Map.of("C-01", 2, "C-02", 2, "C-03", 2, "C-04", 1);
    private static final Pattern HU = Pattern.compile("HU-\\d+");
    private static final Pattern CU = Pattern.compile("SC-\\d+");
    private static final String SEPARADOR_LISTA = "\\|";
    private static final int COLUMNAS = 8;
    private static final String PRUEBA_REAL =
            "skycampus.enterprise.requisitos.MatrizTrazabilidadTest#matriz_esConsistente";
    private static final String CU_UNO = "SC-01";
    private static final String RF_REPETIDO = "RF-94";

    private static List<String[]> filas(Path archivo) throws IOException {
        List<String> lineas = Files.readAllLines(archivo);
        return lineas.stream().skip(1).filter(linea -> !linea.isBlank())
                .map(linea -> linea.split(";", -1)).toList();
    }

    private static List<String> lista(String celda) {
        return celda.isBlank() ? List.of() : Arrays.asList(celda.split(SEPARADOR_LISTA));
    }

    static boolean existePrueba(String referencia) {
        String[] partes = referencia.split("#", -1);
        if (partes.length != 2) {
            return false;
        }
        try {
            for (Method metodo : Class.forName(partes[0]).getDeclaredMethods()) {
                boolean esPrueba = metodo.isAnnotationPresent(org.junit.jupiter.api.Test.class)
                        || metodo.isAnnotationPresent(ParameterizedTest.class);
                if (esPrueba && metodo.getName().equals(partes[1])) {
                    return true;
                }
            }
            return false;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /** Devuelve todos los problemas de la matriz; una lista vacía significa que es consistente. */
    static List<String> problemas(List<String[]> matriz, Set<String> casosDeUso) {
        List<String> problemas = new ArrayList<>();
        Set<String> codigos = new HashSet<>();
        Set<String> usados = new HashSet<>();
        for (String[] fila : matriz) {
            if (fila.length != COLUMNAS) {
                problemas.add("Fila con " + fila.length + " columnas: " + String.join(";", fila));
                continue;
            }
            String codigo = fila[0];
            if (!codigos.add(codigo)) {
                problemas.add(codigo + ": código repetido");
            }
            if (fila[2].isBlank()) {
                problemas.add(codigo + ": sin nombre");
            }
            if (!MOSCOW.contains(fila[3])) {
                problemas.add(codigo + ": MoSCoW inválido '" + fila[3] + "'");
            }
            List<String> cus = lista(fila[4]);
            if (cus.isEmpty()) {
                problemas.add(codigo + ": sin caso de uso");
            }
            for (String cu : cus) {
                usados.add(cu);
                if (!casosDeUso.contains(cu)) {
                    problemas.add(codigo + ": el caso de uso " + cu + " no existe");
                }
            }
            List<String> historias = lista(fila[5]);
            if (historias.isEmpty() || !historias.stream().allMatch(h -> HU.matcher(h).matches())) {
                problemas.add(codigo + ": historia de usuario ausente o con formato inválido");
            }
            List<String> pruebas = lista(fila[6]);
            if (pruebas.isEmpty()) {
                problemas.add(codigo + ": sin prueba");
            }
            for (String prueba : pruebas) {
                if (!existePrueba(prueba)) {
                    problemas.add(codigo + ": la prueba " + prueba + " no existe");
                }
            }
            for (String conflicto : lista(fila[7])) {
                if (!CONFLICTOS.containsKey(conflicto)) {
                    problemas.add(codigo + ": conflicto desconocido " + conflicto);
                }
            }
        }
        for (String cu : casosDeUso) {
            if (!usados.contains(cu)) {
                problemas.add(cu + ": caso de uso huérfano, ningún requisito lo usa");
            }
        }
        return problemas;
    }

    private static Set<String> casosDeUsoDelArchivo() throws IOException {
        Set<String> codigos = new HashSet<>();
        for (String[] fila : filas(CASOS_DE_USO)) {
            assertTrue(CU.matcher(fila[0]).matches(), "código de caso de uso inválido: " + fila[0]);
            codigos.add(fila[0]);
        }
        return codigos;
    }

    @Test
    @DisplayName("la matriz tiene 8 RF (RF-12 a RF-19) y 4 RNF (RNF-08 a RNF-11), sin huecos ni repetidos")
    void matriz_tieneLosOchoRFYLosCuatroRNF() throws IOException {
        List<String> codigos = filas(MATRIZ).stream().map(fila -> fila[0]).toList();

        List<String> esperados = Stream.concat(
                IntStream.rangeClosed(12, 19).mapToObj(n -> "RF-" + n),
                IntStream.rangeClosed(8, 11).mapToObj(n -> String.format("RNF-%02d", n))).toList();
        assertEquals(esperados, codigos);
        assertEquals(8, filas(MATRIZ).stream().filter(fila -> "RF".equals(fila[1])).count());
        assertEquals(4, filas(MATRIZ).stream().filter(fila -> "RNF".equals(fila[1])).count());
    }

    @Test
    @DisplayName("ningún requisito queda sin caso de uso, historia ni prueba existente, y ningún caso de uso queda huérfano")
    void matriz_esConsistente() throws IOException {
        assertEquals(List.of(), problemas(filas(MATRIZ), casosDeUsoDelArchivo()));
    }

    @Test
    @DisplayName("cada conflicto detectado aparece en los requisitos de la matriz que involucra")
    void conflictos_apareceEnLosRequisitosQueInvolucra() throws IOException {
        CONFLICTOS.forEach((conflicto, minimo) -> {
            try {
                long involucrados = filas(MATRIZ).stream().filter(fila -> lista(fila[7]).contains(conflicto)).count();
                assertTrue(involucrados >= minimo, conflicto + " aparece en " + involucrados + " requisito(s)");
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    @Test
    @DisplayName("el verificador detecta un requisito sin caso de uso, sin prueba o con una prueba inventada")
    void verificador_detectaLosProblemas() {
        Set<String> cus = Set.of(CU_UNO, "SC-02");

        List<String[]> mala = List.of(
                new String[] {"RF-90", "RF", "Sin CU", "MUST", "", "HU-1", PRUEBA_REAL, ""},
                new String[] {"RF-91", "RF", "Sin prueba", "SHOULD", CU_UNO, "HU-2", "", ""},
                new String[] {"RF-92", "RF", "Prueba inventada", "COULD", CU_UNO, "HU-3", "a.b.C#d", ""},
                new String[] {"RF-93", "RF", "CU inexistente", "MUST", "SC-77", "HU-4", PRUEBA_REAL, ""},
                new String[] {RF_REPETIDO, "RF", "MoSCoW y HU malos", "TAL VEZ", CU_UNO, "H-5", PRUEBA_REAL, "C-99"},
                new String[] {RF_REPETIDO, "RF", "Repetido", "MUST", CU_UNO, "HU-6", PRUEBA_REAL, ""},
                new String[] {"RF-95", "RF", "", "MUST", CU_UNO, "HU-7", PRUEBA_REAL, ""},
                new String[] {"corta"});

        String texto = String.join("\n", problemas(mala, cus));

        for (String esperado : List.of("RF-90: sin caso de uso", "RF-91: sin prueba",
                "RF-92: la prueba a.b.C#d no existe", "RF-93: el caso de uso SC-77 no existe",
                "RF-94: MoSCoW inválido 'TAL VEZ'", "RF-94: historia de usuario ausente",
                "RF-94: conflicto desconocido C-99", "RF-94: código repetido", "RF-95: sin nombre",
                "Fila con 1 columnas", "SC-02: caso de uso huérfano")) {
            assertTrue(texto.contains(esperado), "No se detectó: " + esperado + "\n" + texto);
        }
    }

    @Test
    @DisplayName("el verificador acepta una matriz correcta y reconoce solo métodos de prueba reales")
    void verificador_aceptaUnaMatrizCorrecta() {
        List<String[]> buena = List.<String[]>of(
                new String[] {"RF-90", "RF", "Todo en orden", "MUST", CU_UNO, "HU-1", PRUEBA_REAL, "C-01"});

        assertEquals(List.of(), problemas(buena, Set.of(CU_UNO)));
        assertTrue(existePrueba(PRUEBA_REAL));
        assertFalse(existePrueba("skycampus.enterprise.requisitos.MatrizTrazabilidadTest#filas"));
        assertFalse(existePrueba("skycampus.enterprise.requisitos.MatrizTrazabilidadTest#noExiste"));
        assertFalse(existePrueba("sin-almohadilla"));
    }
}
