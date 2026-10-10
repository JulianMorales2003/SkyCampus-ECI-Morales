package skycampus.enterprise.infraestructura;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.Set;
import skycampus.enterprise.dominio.CatalogoSedes;

/** Adaptador en memoria del catálogo de sedes: las distancias valen en los dos sentidos. */
public class CatalogoSedesEnMemoria implements CatalogoSedes {

    private final Set<String> activas = new HashSet<>();
    private final Map<String, Double> distancias = new HashMap<>();

    public CatalogoSedesEnMemoria activar(String sede) {
        activas.add(sede);
        return this;
    }

    public CatalogoSedesEnMemoria desactivar(String sede) {
        activas.remove(sede);
        return this;
    }

    public CatalogoSedesEnMemoria distancia(String a, String b, double km) {
        if (km <= 0) {
            throw new IllegalArgumentException("La distancia debe ser mayor que 0 km.");
        }
        distancias.put(clave(a, b), km);
        return this;
    }

    @Override
    public boolean estaActiva(String sede) {
        return activas.contains(sede);
    }

    @Override
    public OptionalDouble distanciaKm(String origen, String destino) {
        Double km = distancias.get(clave(origen, destino));
        return km == null ? OptionalDouble.empty() : OptionalDouble.of(km);
    }

    private static String clave(String a, String b) {
        return a.compareTo(b) <= 0 ? a + "|" + b : b + "|" + a;
    }
}
