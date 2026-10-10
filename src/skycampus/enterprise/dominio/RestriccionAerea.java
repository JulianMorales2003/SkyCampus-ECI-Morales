package skycampus.enterprise.dominio;

/** Límites que la Aerocivil fija para el espacio aéreo de una sede. No los decide ningún usuario del sistema. */
public record RestriccionAerea(double radioMaximoKm, int alturaMaximaM) {

    public RestriccionAerea {
        if (radioMaximoKm <= 0) {
            throw new IllegalArgumentException("El radio máximo debe ser mayor que 0 km.");
        }
        if (alturaMaximaM <= 0) {
            throw new IllegalArgumentException("La altura máxima debe ser mayor que 0 m.");
        }
    }
}
