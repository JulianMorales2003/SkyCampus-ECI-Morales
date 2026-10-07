package ruta;

import java.util.List;

public class RutaEvitandoEdificios implements EstrategiaRuta {

    private static final String PUNTO_DE_DESVIO = "Corredor aéreo libre";

    @Override
    public List<String> calcular(String origen, String destino) {
        if (origen == null || origen.isBlank() || destino == null || destino.isBlank()) {
            throw new IllegalArgumentException("El origen y el destino de la ruta no pueden ser nulos ni vacíos.");
        }
        return List.of(origen, PUNTO_DE_DESVIO, destino);
    }
}
