package app;

import java.util.List;
import model.Drone;
import service.ConsultasFlota;

public class SkyCampusApp {

    public static void main(String[] args) {
        List<Drone> flota = List.of(
                new Drone("D-01", "DJI Mini 3", 85, true, "Bloque A"),
                new Drone("D-02", "DJI Mini 3", 42, false, "Biblioteca"),
                new Drone("D-03", "DJI Mini 3", 91, true, "Bloque C"),
                new Drone("D-04", "DJI Mini 3", 18, true, "Bloque B"),
                new Drone("D-05", "DJI Mini 3", 67, true, "Bloque D"));

        System.out.println("1. Disponibles con batería >= 50%, de mayor a menor: "
                + ConsultasFlota.idsDisponiblesConBateriaSuficiente(flota));
        System.out.println("2. ¿Hay drone disponible en Bloque C?: "
                + ConsultasFlota.hayDisponibleEn(flota, "Bloque C"));
        System.out.println("3. Drones con batería crítica (< 20%): "
                + ConsultasFlota.contarBateriaCritica(flota));
        System.out.println("4. Resumen de batería: "
                + ConsultasFlota.resumenBateria(flota));
    }
}
