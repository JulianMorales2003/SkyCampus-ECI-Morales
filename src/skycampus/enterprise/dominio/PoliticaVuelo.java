package skycampus.enterprise.dominio;

import java.util.HashMap;
import java.util.Map;

/**
 * Reglas de alcance de los drones de la red. Hay tres límites con jerarquía fija:
 * la Aerocivil manda sobre el superadmin (techo de la red) y este sobre el coordinador de cada sede.
 * El radio que rige es siempre el menor de los tres; la configuración del coordinador se guarda
 * tal cual y los límites superiores se aplican cada vez que se consulta, así que un cambio de la
 * Aerocivil posterior a la configuración sigue cumpliéndose.
 */
public final class PoliticaVuelo {

    /** Altura máxima en zona urbana (RNF-09). */
    public static final int ALTURA_MAXIMA_URBANA_M = 120;

    private double techoRedKm;
    private final Map<String, RestriccionAerea> restricciones = new HashMap<>();
    private final Map<String, Double> radiosConfigurados = new HashMap<>();

    public PoliticaVuelo(double techoRedKm) {
        this.techoRedKm = exigirRadio(techoRedKm);
    }

    /** RF-13: el superadmin fija el radio máximo de toda la red. */
    public void definirTechoRed(double km) {
        this.techoRedKm = exigirRadio(km);
    }

    /** RNF-09: se registra o actualiza lo que la Aerocivil exige para el espacio aéreo de una sede. */
    public void registrarRestriccion(String sede, RestriccionAerea restriccion) {
        restricciones.put(exigirSede(sede), exigirPresente(restriccion, "restriccion"));
    }

    /** RF-12: el coordinador de la sede pide un radio; se guarda y se informa cuál rige de verdad. */
    public ConfiguracionRadio configurarRadio(String sede, double km) {
        exigirSede(sede);
        exigirRadio(km);
        radiosConfigurados.put(sede, km);
        return new ConfiguracionRadio(km, radioEfectivoKm(sede));
    }

    public boolean tieneRestriccion(String sede) {
        return restricciones.containsKey(exigirSede(sede));
    }

    /** Menor entre el radio del coordinador (si lo configuró), el techo de la red y el límite de la Aerocivil. */
    public double radioEfectivoKm(String sede) {
        RestriccionAerea restriccion = restricciones.get(exigirSede(sede));
        if (restriccion == null) {
            return 0;
        }
        double limiteSuperior = Math.min(techoRedKm, restriccion.radioMaximoKm());
        Double configurado = radiosConfigurados.get(sede);
        return configurado == null ? limiteSuperior : Math.min(configurado, limiteSuperior);
    }

    /** Altura máxima permitida; en zona urbana nunca pasa de 120 m aunque la Aerocivil permita más. */
    public int alturaMaximaM(String sede, boolean zonaUrbana) {
        RestriccionAerea restriccion = restricciones.get(exigirSede(sede));
        if (restriccion == null) {
            return 0;
        }
        return zonaUrbana
                ? Math.min(ALTURA_MAXIMA_URBANA_M, restriccion.alturaMaximaM())
                : restriccion.alturaMaximaM();
    }

    private static double exigirRadio(double km) {
        if (km <= 0) {
            throw new IllegalArgumentException("El radio debe ser mayor que 0 km.");
        }
        return km;
    }

    private static String exigirSede(String sede) {
        if (sede == null || sede.isBlank()) {
            throw new IllegalArgumentException("El campo sede es obligatorio.");
        }
        return sede;
    }

    private static <T> T exigirPresente(T valor, String campo) {
        if (valor == null) {
            throw new IllegalArgumentException("El campo " + campo + " es obligatorio.");
        }
        return valor;
    }
}
