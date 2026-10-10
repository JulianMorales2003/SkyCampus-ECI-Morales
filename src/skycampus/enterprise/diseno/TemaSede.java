package skycampus.enterprise.diseno;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Los cuatro design tokens que cambian entre sedes. Es todo lo que un equipo define para añadir una
 * universidad: los componentes, la estructura HTML y el resto de variables no se tocan.
 *
 * @param sede identificador en minúsculas, el mismo de {@code data-sede} y del nombre del archivo del tema
 * @param colorPrimario valor de {@code --color-primary}, formato {@code #RRGGBB}
 * @param colorAlerta valor de {@code --color-alert}, igual en todas las sedes
 * @param fuenteUi valor de {@code --font-ui}: familias separadas por coma, la última es genérica
 * @param radioBorde valor de {@code --border-radius}, en píxeles
 */
public record TemaSede(String sede, String colorPrimario, String colorAlerta, String fuenteUi, String radioBorde) {

    /** Rojo de alerta que comparten todas las sedes: una alerta debe verse igual en cualquier universidad. */
    public static final String ALERTA_COMUN = "#E63946";
    /** Color del texto sobre el color primario y del fondo de las tarjetas. */
    public static final String BLANCO = "#FFFFFF";
    /** Razón mínima WCAG AA para texto normal. */
    public static final double MINIMO_TEXTO = 4.5;
    /** Razón mínima WCAG para elementos gráficos (bordes, íconos, rellenos). */
    public static final double MINIMO_GRAFICO = 3.0;

    private static final String TOKEN_PRIMARIO = "--color-primary";
    private static final String TOKEN_ALERTA = "--color-alert";
    private static final String TOKEN_FUENTE = "--font-ui";
    private static final String TOKEN_RADIO = "--border-radius";
    private static final Set<String> TOKENS = Set.of(TOKEN_PRIMARIO, TOKEN_ALERTA, TOKEN_FUENTE, TOKEN_RADIO);
    private static final List<String> GENERICAS = List.of("sans-serif", "serif");
    private static final Pattern SEDE_VALIDA = Pattern.compile("[a-z]{2,20}");
    private static final Pattern RADIO_VALIDO = Pattern.compile("\\d{1,2}px");
    private static final Pattern BLOQUE = Pattern.compile("\\[data-sede=\"([^\"]*)\"]\\s*\\{([^}]*)}");
    private static final Pattern DECLARACION = Pattern.compile("(--[a-z-]+)\\s*:\\s*([^;]+);");
    private static final Pattern COMENTARIO = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);

    public TemaSede {
        if (sede == null || !SEDE_VALIDA.matcher(sede).matches()) {
            throw new IllegalArgumentException("La sede va en minúsculas, de 2 a 20 letras: " + sede);
        }
        Contraste.luminancia(colorPrimario);
        Contraste.luminancia(colorAlerta);
        if (!ALERTA_COMUN.equalsIgnoreCase(colorAlerta)) {
            throw new IllegalArgumentException("--color-alert es " + ALERTA_COMUN + " en todas las sedes.");
        }
        if (!fuenteConRespaldo(fuenteUi)) {
            throw new IllegalArgumentException("--font-ui debe terminar en sans-serif o serif: " + fuenteUi);
        }
        if (radioBorde == null || !RADIO_VALIDO.matcher(radioBorde).matches()) {
            throw new IllegalArgumentException("--border-radius va en píxeles, por ejemplo 8px: " + radioBorde);
        }
    }

    /**
     * Lee el contenido de un archivo de tema ({@code [data-sede="x"] { --token: valor; ... }}).
     *
     * @throws IllegalArgumentException si falta un token, sobra uno, se repite o hay más de una sede
     */
    public static TemaSede desdeCss(String css) {
        Matcher bloque = BLOQUE.matcher(COMENTARIO.matcher(css).replaceAll(""));
        if (!bloque.find()) {
            throw new IllegalArgumentException("El tema no tiene un bloque [data-sede=\"...\"] { ... }.");
        }
        String sede = bloque.group(1);
        Map<String, String> valores = leerDeclaraciones(bloque.group(2));
        if (bloque.find()) {
            throw new IllegalArgumentException("Un archivo de tema define una sola sede.");
        }
        comprobarTokens(valores.keySet());
        return new TemaSede(sede, valores.get(TOKEN_PRIMARIO), valores.get(TOKEN_ALERTA),
                valores.get(TOKEN_FUENTE), valores.get(TOKEN_RADIO));
    }

    /** Contraste del texto blanco sobre el color primario (botones y encabezados). */
    public double contrasteTextoSobrePrimario() {
        return Contraste.razon(BLANCO, colorPrimario);
    }

    /** Contraste del color de alerta contra el fondo blanco de las tarjetas. */
    public double contrasteAlertaSobreFondo() {
        return Contraste.razon(BLANCO, colorAlerta);
    }

    /** {@code true} si el texto blanco sobre el color primario cumple WCAG AA (4,5:1). */
    public boolean cumpleAccesibilidad() {
        return contrasteTextoSobrePrimario() >= MINIMO_TEXTO;
    }

    private static Map<String, String> leerDeclaraciones(String cuerpo) {
        Map<String, String> valores = new LinkedHashMap<>();
        Matcher declaracion = DECLARACION.matcher(cuerpo);
        while (declaracion.find()) {
            String token = declaracion.group(1);
            if (valores.put(token, declaracion.group(2).trim()) != null) {
                throw new IllegalArgumentException("Token repetido: " + token);
            }
        }
        return valores;
    }

    private static void comprobarTokens(Set<String> presentes) {
        Set<String> faltan = new TreeSet<>(TOKENS);
        faltan.removeAll(presentes);
        Set<String> sobran = new HashSet<>(presentes);
        sobran.removeAll(TOKENS);
        if (!faltan.isEmpty()) {
            throw new IllegalArgumentException("Faltan tokens: " + faltan);
        }
        if (!sobran.isEmpty()) {
            throw new IllegalArgumentException("Tokens que una sede no define: " + new TreeSet<>(sobran));
        }
    }

    private static boolean fuenteConRespaldo(String fuente) {
        if (fuente == null) {
            return false;
        }
        String[] familias = fuente.split(",");
        String ultima = familias[familias.length - 1].trim();
        return familias.length >= 2 && !familias[0].isBlank() && GENERICAS.contains(ultima);
    }
}
