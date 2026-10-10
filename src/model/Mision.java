package model;

import java.time.LocalTime;

public record Mision(String id, Drone drone, String origen, String destino, TipoCarga tipoCarga,
                     EstadoMision estado, int prioridad, String notas, LocalTime horaMaximaEntrega) {

    public static final int PRIORIDAD_MINIMA = 1;
    public static final int PRIORIDAD_MAXIMA = 5;
    public static final int PRIORIDAD_POR_DEFECTO = 3;
    public static final LocalTime SIN_HORA_LIMITE = LocalTime.MAX;

    public Mision {
        exigirTexto(id, "id");
        exigirPresente(drone, "drone");
        exigirTexto(origen, "origen");
        exigirTexto(destino, "destino");
        exigirPresente(tipoCarga, "tipoCarga");
        exigirPresente(estado, "estado");
        exigirPresente(notas, "notas");
        exigirPresente(horaMaximaEntrega, "horaMaximaEntrega");
        if (!esPrioridadValida(prioridad)) {
            throw new IllegalArgumentException("La prioridad debe estar entre "
                    + PRIORIDAD_MINIMA + " y " + PRIORIDAD_MAXIMA + ".");
        }
    }

    public static boolean esPrioridadValida(int prioridad) {
        return prioridad >= PRIORIDAD_MINIMA && prioridad <= PRIORIDAD_MAXIMA;
    }

    private static void exigirPresente(Object valor, String campo) {
        if (valor == null) {
            throw new IllegalArgumentException("El campo '" + campo + "' no puede ser nulo.");
        }
    }

    private static void exigirTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El campo '" + campo + "' no puede ser nulo ni vacío.");
        }
    }
}
