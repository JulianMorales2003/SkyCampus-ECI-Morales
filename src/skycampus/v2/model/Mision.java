package skycampus.v2.model;

import java.time.Instant;
import util.Validaciones;

public record Mision(String id, Drone drone, String destino, int pesoPaqueteGramos,
                     Prioridad prioridad, EstadoMision estado, Instant creadaEn) {

    public Mision {
        Validaciones.exigirTexto(id, "id");
        Validaciones.exigirPresente(drone, "drone");
        Validaciones.exigirTexto(destino, "destino");
        Validaciones.exigirPresente(prioridad, "prioridad");
        Validaciones.exigirPresente(estado, "estado");
        Validaciones.exigirPresente(creadaEn, "creadaEn");
        if (pesoPaqueteGramos <= 0) {
            throw new IllegalArgumentException("El peso del paquete debe ser mayor que 0 gramos.");
        }
    }
}
