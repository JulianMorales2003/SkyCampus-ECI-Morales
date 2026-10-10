package skycampus.enterprise.ruta.drone;

import java.util.Map;
import skycampus.v2.model.TipoDrone;
import util.Validaciones;

/** Producto del Factory Method: el tipo de drone asignado a una etapa y lo lejos que puede volar sin recargar. */
public record DroneEtapa(TipoDrone tipo, double alcanceKm) {

    private static final Map<TipoDrone, Double> ALCANCE_POR_TIPO = Map.of(
            TipoDrone.MINI, 3.0,
            TipoDrone.EXPRESS, 15.0,
            TipoDrone.CARGO, 30.0);

    public DroneEtapa {
        Validaciones.exigirPresente(tipo, "tipo");
        if (alcanceKm <= 0) {
            throw new IllegalArgumentException("El alcance debe ser mayor que 0 km.");
        }
    }

    public static DroneEtapa de(TipoDrone tipo) {
        Validaciones.exigirPresente(tipo, "tipo");
        return new DroneEtapa(tipo, ALCANCE_POR_TIPO.get(tipo));
    }
}
