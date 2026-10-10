package app;

import java.util.List;
import model.Drone;
import service.ConsultasFlota;
import util.Consola;

public class SkyCampusApp {

    private static final String MODELO_DRONE = "DJI Mini 3";

    public static void main(String[] args) {
        List<Drone> flota = List.of(
                new Drone("D-01", MODELO_DRONE, 85, true, "Bloque A"),
                new Drone("D-02", MODELO_DRONE, 42, false, "Biblioteca"),
                new Drone("D-03", MODELO_DRONE, 91, true, "Bloque C"),
                new Drone("D-04", MODELO_DRONE, 18, true, "Bloque B"),
                new Drone("D-05", MODELO_DRONE, 67, true, "Bloque D"));

        Consola.imprimir("1. Disponibles con batería >= 50%, de mayor a menor: "
                + ConsultasFlota.idsDisponiblesConBateriaSuficiente(flota));
        Consola.imprimir("2. ¿Hay drone disponible en Bloque C?: "
                + ConsultasFlota.hayDisponibleEn(flota, "Bloque C"));
        Consola.imprimir("3. Drones con batería crítica (< 20%): "
                + ConsultasFlota.contarBateriaCritica(flota));
        Consola.imprimir("4. Resumen de batería: "
                + ConsultasFlota.resumenBateria(flota));
    }
}
