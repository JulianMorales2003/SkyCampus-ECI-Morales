package reporte;

import java.util.List;
import java.util.stream.Collectors;
import model.Mision;

public class GeneradorReporte {

    public String generar(List<Mision> misiones) {
        if (misiones == null) {
            throw new IllegalArgumentException("La lista de misiones no puede ser nula.");
        }
        if (misiones.isEmpty()) {
            return "REPORTE DE MISIONES\nSin misiones registradas.";
        }
        String detalle = misiones.stream()
                .map(GeneradorReporte::describir)
                .collect(Collectors.joining("\n"));
        return "REPORTE DE MISIONES (" + misiones.size() + ")\n" + detalle;
    }

    private static String describir(Mision mision) {
        return mision.id() + " | " + mision.drone().id() + " | "
                + mision.origen() + " -> " + mision.destino() + " | "
                + mision.tipoCarga() + " | " + mision.estado();
    }
}
