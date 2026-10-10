package skycampus.enterprise.dominio;

/** Vuelo que se quiere autorizar. La urgencia viaja en el plan, pero no cambia ninguna regla de seguridad. */
public record PlanVuelo(String misionId, String sede, double distanciaKm, int alturaM,
        boolean zonaUrbana, boolean urgente) {

    public PlanVuelo {
        if (misionId == null || misionId.isBlank()) {
            throw new IllegalArgumentException("El campo misionId es obligatorio.");
        }
        if (sede == null || sede.isBlank()) {
            throw new IllegalArgumentException("El campo sede es obligatorio.");
        }
        if (distanciaKm <= 0) {
            throw new IllegalArgumentException("La distancia debe ser mayor que 0 km.");
        }
        if (alturaM <= 0) {
            throw new IllegalArgumentException("La altura debe ser mayor que 0 m.");
        }
    }
}
