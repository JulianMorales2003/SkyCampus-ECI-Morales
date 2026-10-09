package v2.model;

import util.Validaciones;

public record SolicitudMision(String id, String destino, int pesoPaqueteGramos, Prioridad prioridad) {

    public SolicitudMision {
        Validaciones.exigirTexto(id, "id");
        Validaciones.exigirTexto(destino, "destino");
        Validaciones.exigirPresente(prioridad, "prioridad");
        if (pesoPaqueteGramos <= 0) {
            throw new IllegalArgumentException("El peso del paquete debe ser mayor que 0 gramos.");
        }
    }
}
