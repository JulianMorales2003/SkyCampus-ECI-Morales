package service;

import java.util.Comparator;
import java.util.List;
import model.Drone;

public final class ConsultasFlota {

    private static final int BATERIA_MINIMA = 50;
    private static final int BATERIA_CRITICA = 20;

    private ConsultasFlota() {
    }

    public static List<String> idsDisponiblesConBateriaSuficiente(List<Drone> flota) {
        validarFlota(flota);
        return flota.stream()
                .filter(Drone::disponible)
                .filter(drone -> drone.bateria() >= BATERIA_MINIMA)
                .sorted(Comparator.comparingInt(Drone::bateria).reversed())
                .map(Drone::id)
                .toList();
    }

    public static boolean hayDisponibleEn(List<Drone> flota, String ubicacion) {
        validarFlota(flota);
        if (ubicacion == null || ubicacion.isBlank()) {
            throw new IllegalArgumentException("La ubicación a consultar no puede ser nula ni vacía.");
        }
        return flota.stream()
                .anyMatch(drone -> drone.disponible() && ubicacion.equals(drone.ubicacion()));
    }

    public static long contarBateriaCritica(List<Drone> flota) {
        validarFlota(flota);
        return flota.stream()
                .filter(drone -> drone.bateria() < BATERIA_CRITICA)
                .count();
    }

    public static List<String> resumenBateria(List<Drone> flota) {
        validarFlota(flota);
        return flota.stream()
                .map(drone -> drone.id() + ": " + drone.bateria() + "%")
                .toList();
    }

    private static void validarFlota(List<Drone> flota) {
        if (flota == null) {
            throw new IllegalArgumentException("La flota de drones no puede ser nula.");
        }
    }
}
