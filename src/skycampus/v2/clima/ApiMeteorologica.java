package skycampus.v2.clima;

/**
 * Puerto hacia el sistema externo de clima. En las pruebas se simula con Mockito;
 * la implementación real (HTTP, tiempo máximo de 3 s) llegará en otro reto.
 */
public interface ApiMeteorologica {

    /** @return true si las condiciones permiten volar (viento y lluvia dentro de los límites). */
    boolean esApto();
}
