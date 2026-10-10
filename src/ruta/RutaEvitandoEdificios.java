package ruta;

import java.util.List;
import util.Validaciones;

public class RutaEvitandoEdificios implements EstrategiaRuta {

    private static final String PUNTO_DE_DESVIO = "Corredor aéreo libre";

    @Override
    public List<String> calcular(String origen, String destino) {
        Validaciones.exigirTexto(origen, "origen");
        Validaciones.exigirTexto(destino, "destino");
        Validaciones.exigirDistintos(origen, destino, "El origen y el destino de la ruta no pueden ser iguales.");
        return List.of(origen, PUNTO_DE_DESVIO, destino);
    }
}
