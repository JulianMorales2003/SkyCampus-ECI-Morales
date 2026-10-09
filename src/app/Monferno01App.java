package app;

import java.time.Instant;
import java.util.List;
import v2.model.Drone;
import v2.model.EstadoDrone;
import v2.model.EstadoMision;
import v2.model.Mision;
import v2.model.Prioridad;
import v2.model.TipoDrone;
import v2.service.EstadisticasFlota;

public class Monferno01App {

    private static final Instant AHORA = Instant.parse("2026-10-09T15:00:00Z");
    private static final String BIBLIOTECA = "Biblioteca";

    public static void main(String[] args) {
        Drone mini = new Drone("D-01", TipoDrone.MINI, 85, true, EstadoDrone.DISPONIBLE);
        Drone cargo = new Drone("D-11", TipoDrone.CARGO, 60, false, EstadoDrone.EN_VUELO);
        Drone express = new Drone("D-15", TipoDrone.EXPRESS, 40, true, EstadoDrone.DISPONIBLE);

        List<Mision> misiones = List.of(
                mision("M-01", mini, EstadoMision.COMPLETADA, Prioridad.NORMAL, 120),
                mision("M-02", mini, EstadoMision.COMPLETADA, Prioridad.BAJO, 90),
                mision("M-03", cargo, EstadoMision.COMPLETADA, Prioridad.NORMAL, 80),
                mision("M-04", express, EstadoMision.COMPLETADA, Prioridad.URGENTE, 70),
                mision("M-05", express, EstadoMision.FALLIDA, Prioridad.URGENTE, 50),
                mision("M-06", cargo, EstadoMision.EN_VUELO, Prioridad.NORMAL, 20),
                mision("M-07", mini, EstadoMision.PENDIENTE, Prioridad.URGENTE, 14),
                mision("M-08", express, EstadoMision.PENDIENTE, Prioridad.BAJO, 3));

        System.out.println("1. Completadas por tipo: " + EstadisticasFlota.completadasPorTipo(misiones));
        System.out.println("2. Drone con más completadas: "
                + EstadisticasFlota.droneConMasCompletadas(misiones).map(Drone::id).orElse("ninguno"));
        System.out.printf("3. Misiones fallidas: %.1f%%%n", EstadisticasFlota.porcentajeFallidas(misiones));
        System.out.println("4. ¿Urgente pendiente hace más de 10 min?: "
                + EstadisticasFlota.hayUrgentePendienteMasDeDiezMinutos(misiones, AHORA));
    }

    private static Mision mision(String id, Drone drone, EstadoMision estado, Prioridad prioridad,
                                 long minutosDesdeCreacion) {
        return new Mision(id, drone, BIBLIOTECA, 300, prioridad, estado,
                AHORA.minusSeconds(minutosDesdeCreacion * 60));
    }
}
