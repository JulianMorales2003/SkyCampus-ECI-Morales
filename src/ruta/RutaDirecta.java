package ruta;

import java.util.List;
import util.Validaciones;

public class RutaDirecta implements EstrategiaRuta {

    @Override
    public List<String> calcular(String origen, String destino) {
        Validaciones.exigirTexto(origen, "origen");
        Validaciones.exigirTexto(destino, "destino");
        Validaciones.exigirDistintos(origen, destino, "El origen y el destino de la ruta no pueden ser iguales.");
        return List.of(origen, destino);
    }
}
