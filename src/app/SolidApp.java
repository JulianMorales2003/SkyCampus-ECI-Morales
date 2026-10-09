package app;

import alerta.AlertaOperador;
import alerta.AlertaOperadorConsola;
import asignacion.AsignadorDrones;
import asignacion.AsignadorMision;
import asignacion.Asignacion;
import asignacion.EstrategiaMayorBateria;
import builder.MisionBuilder;
import java.util.List;
import model.Drone;
import model.Mision;
import model.TipoCarga;
import persistencia.RepositorioMision;
import persistencia.RepositorioMisionEnMemoria;
import reporte.GeneradorReporte;
import ruta.EstrategiaRuta;
import ruta.RutaDirecta;
import ruta.RutaEvitandoEdificios;
import util.Consola;

public class SolidApp {

    private static final String MODELO_DRONE = "DJI Mini 3";
    private static final String ORIGEN = "Bloque C";

    public static void main(String[] args) {
        RepositorioMision repositorio = new RepositorioMisionEnMemoria();
        AlertaOperador alerta = new AlertaOperadorConsola();
        List<Drone> flota = crearFlota();

        Drone elegido = elegirDrone(flota);
        Asignacion asignacion = new AsignadorMision().asignar(crearMisionPendiente(elegido), elegido);
        repositorio.guardar(asignacion.mision());
        flota = reemplazarDrone(flota, asignacion.drone());
        alerta.enviar("Operador 1", "Misión asignada al drone " + elegido.id()
                + " con destino " + asignacion.mision().destino() + ".");

        Consola.imprimir(new GeneradorReporte().generar(repositorio.listarTodas()));
        mostrarRutas(asignacion.mision());
        demostrarReglasDeAsignacion(flota);
    }

    private static List<Drone> crearFlota() {
        return List.of(
                new Drone("D-01", MODELO_DRONE, 85, true, "Bloque A"),
                new Drone("D-02", MODELO_DRONE, 42, false, "Biblioteca"),
                new Drone("D-03", MODELO_DRONE, 91, true, "Bloque C"),
                new Drone("D-04", MODELO_DRONE, 18, true, "Bloque B"),
                new Drone("D-05", MODELO_DRONE, 67, true, "Bloque D"));
    }

    private static Drone elegirDrone(List<Drone> flota) {
        return new AsignadorDrones(new EstrategiaMayorBateria())
                .asignar(flota, ORIGEN)
                .orElseThrow(() -> new IllegalStateException("No hay drones disponibles."));
    }

    private static Mision crearMisionPendiente(Drone drone) {
        return new MisionBuilder()
                .drone(drone).origen(ORIGEN).destino("Biblioteca")
                .tipoCarga(TipoCarga.CARPETA)
                .build();
    }

    private static List<Drone> reemplazarDrone(List<Drone> flota, Drone droneActualizado) {
        return flota.stream()
                .map(drone -> drone.id().equals(droneActualizado.id()) ? droneActualizado : drone)
                .toList();
    }

    private static void mostrarRutas(Mision mision) {
        List<EstrategiaRuta> estrategias = List.of(new RutaDirecta(), new RutaEvitandoEdificios());
        for (EstrategiaRuta estrategia : estrategias) {
            Consola.imprimir(estrategia.getClass().getSimpleName() + ": "
                    + estrategia.calcular(mision.origen(), mision.destino()));
        }
    }

    private static void demostrarReglasDeAsignacion(List<Drone> flotaActualizada) {
        Consola.imprimir("\n--- Reglas de asignación ---");
        Drone droneEnVuelo = flotaActualizada.get(2);
        Drone otroDrone = flotaActualizada.get(0);
        Mision otraMision = crearMisionPendiente(droneEnVuelo);
        intentarAsignar("Mismo drone otra vez", otraMision, droneEnVuelo);
        intentarAsignar("Drone distinto al de la misión", otraMision, otroDrone);
        Consola.imprimir("Drone que ofrece ahora la estrategia: "
                + elegirDrone(flotaActualizada).id());
    }

    private static void intentarAsignar(String escenario, Mision mision, Drone drone) {
        try {
            new AsignadorMision().asignar(mision, drone);
            Consola.imprimir(escenario + ": asignada");
        } catch (IllegalStateException | IllegalArgumentException excepcionAsignacion) {
            Consola.imprimir(escenario + ": RECHAZADA - " + excepcionAsignacion.getMessage());
        }
    }
}
