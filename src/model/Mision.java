package model;

public record Mision(String id, Drone drone, String origen, String destino,
                     TipoCarga tipoCarga, EstadoMision estado) {
}
