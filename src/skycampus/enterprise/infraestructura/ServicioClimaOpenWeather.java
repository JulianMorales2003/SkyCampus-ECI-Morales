package skycampus.enterprise.infraestructura;

import skycampus.enterprise.dominio.ServicioClima;

/**
 * Adaptador del clima. Aquí irá la llamada HTTP a OpenWeather; queda como esqueleto para que
 * ninguna prueba ni el dominio dependan de la red.
 */
public class ServicioClimaOpenWeather implements ServicioClima {

    @Override
    public boolean condicionesAptas(String origen, String destino) {
        throw new UnsupportedOperationException("HTTP pendiente de integrar");
    }
}
