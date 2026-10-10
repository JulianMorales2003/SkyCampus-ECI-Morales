package skycampus.enterprise.diseno;

import java.util.regex.Pattern;

/** Razón de contraste entre dos colores según la fórmula de WCAG 2.1 (luminancia relativa). */
public final class Contraste {

    private static final Pattern HEX = Pattern.compile("#[0-9A-Fa-f]{6}");
    private static final int BASE_HEX = 16;
    private static final double MAXIMO_CANAL = 255.0;
    private static final double CORTE_LINEAL = 0.03928;
    private static final double DIVISOR_LINEAL = 12.92;
    private static final double DESPLAZAMIENTO = 0.055;
    private static final double ESCALA = 1.055;
    private static final double EXPONENTE = 2.4;
    private static final double PESO_ROJO = 0.2126;
    private static final double PESO_VERDE = 0.7152;
    private static final double PESO_AZUL = 0.0722;
    private static final double AJUSTE = 0.05;

    private Contraste() {
    }

    /** Devuelve la luminancia relativa (0 negro, 1 blanco) de un color {@code #RRGGBB}. */
    public static double luminancia(String hex) {
        if (hex == null || !HEX.matcher(hex).matches()) {
            throw new IllegalArgumentException("Color inválido, se espera #RRGGBB: " + hex);
        }
        double rojo = canal(hex.substring(1, 3));
        double verde = canal(hex.substring(3, 5));
        double azul = canal(hex.substring(5, 7));
        return PESO_ROJO * rojo + PESO_VERDE * verde + PESO_AZUL * azul;
    }

    /** Devuelve la razón de contraste entre dos colores, de 1 (iguales) a 21 (negro sobre blanco). */
    public static double razon(String uno, String otro) {
        double a = luminancia(uno);
        double b = luminancia(otro);
        double claro = Math.max(a, b);
        double oscuro = Math.min(a, b);
        return (claro + AJUSTE) / (oscuro + AJUSTE);
    }

    private static double canal(String doble) {
        double valor = Integer.parseInt(doble, BASE_HEX) / MAXIMO_CANAL;
        if (valor <= CORTE_LINEAL) {
            return valor / DIVISOR_LINEAL;
        }
        return Math.pow((valor + DESPLAZAMIENTO) / ESCALA, EXPONENTE);
    }
}
