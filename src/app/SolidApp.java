package app;

import alerta.AlertaOperador;
import alerta.AlertaOperadorConsola;
import asignacion.AsignadorMision;
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

public class SolidApp {

    public static void main(String[] args) {
        Drone drone = new Drone("D-03", "DJI Mini 3", 91, true, "Bloque C");
        RepositorioMision repositorio = new RepositorioMisionEnMemoria();
        AlertaOperador alerta = new AlertaOperadorConsola();

        Mision asignada = new AsignadorMision().asignar(crearMisionPendiente(drone), drone);
        repositorio.guardar(asignada);
        alerta.enviar("Operador 1", "Misión asignada al drone " + drone.id()
                + " con destino " + asignada.destino() + ".");

        System.out.println(new GeneradorReporte().generar(repositorio.listarTodas()));
        mostrarRutas(asignada);
    }

    private static Mision crearMisionPendiente(Drone drone) {
        return new MisionBuilder()
                .drone(drone).origen("Bloque C").destino("Biblioteca")
                .tipoCarga(TipoCarga.CARPETA)
                .build();
    }

    private static void mostrarRutas(Mision mision) {
        List<EstrategiaRuta> estrategias = List.of(new RutaDirecta(), new RutaEvitandoEdificios());
        for (EstrategiaRuta estrategia : estrategias) {
            System.out.println(estrategia.getClass().getSimpleName() + ": "
                    + estrategia.calcular(mision.origen(), mision.destino()));
        }
    }
}
