package app;

import java.time.Instant;
import java.util.List;
import util.Consola;
import skycampus.v2.asignacion.EstrategiaBateriaJusta;
import skycampus.v2.asignacion.EstrategiaMayorBateria;
import skycampus.v2.asignacion.EstrategiaTipoSegunPaquete;
import skycampus.v2.eventos.AlertaTecnico;
import skycampus.v2.eventos.PanelOperador;
import skycampus.v2.eventos.SistemaLog;
import skycampus.v2.model.Drone;
import skycampus.v2.model.EstadoDrone;
import skycampus.v2.model.Mision;
import skycampus.v2.model.Prioridad;
import skycampus.v2.model.SolicitudMision;
import skycampus.v2.model.TipoDrone;
import skycampus.v2.service.GestorFlota;

public class Monferno03App {

    private static final Instant AHORA = Instant.parse("2026-10-09T15:00:00Z");
    private static final String BIBLIOTECA = "Biblioteca";

    public static void main(String[] args) {
        List<Drone> flota = List.of(
                new Drone("D-01", TipoDrone.MINI, 85, true, EstadoDrone.DISPONIBLE),
                new Drone("D-02", TipoDrone.MINI, 45, true, EstadoDrone.DISPONIBLE),
                new Drone("D-11", TipoDrone.CARGO, 60, true, EstadoDrone.DISPONIBLE),
                new Drone("D-15", TipoDrone.EXPRESS, 40, true, EstadoDrone.DISPONIBLE));
        SolicitudMision normal = new SolicitudMision("M-01", BIBLIOTECA, 300, Prioridad.NORMAL);
        SolicitudMision urgente = new SolicitudMision("M-02", BIBLIOTECA, 200, Prioridad.URGENTE);

        SistemaLog log = new SistemaLog();
        GestorFlota gestor = new GestorFlota(new EstrategiaMayorBateria());
        gestor.suscribir(new PanelOperador(Consola::imprimir));
        gestor.suscribir(log);
        gestor.suscribir(new AlertaTecnico(Consola::imprimir));

        Consola.imprimir("== Estrategia: mayor batería ==");
        gestor.asignar(normal, flota, AHORA);

        Consola.imprimir("== Estrategia: batería justa (ahorra los drones más cargados) ==");
        gestor.cambiarEstrategia(new EstrategiaBateriaJusta());
        Mision enVuelo = gestor.asignar(normal, flota, AHORA).orElseThrow();

        Consola.imprimir("== Estrategia: tipo según el paquete (urgente -> EXPRESS) ==");
        gestor.cambiarEstrategia(new EstrategiaTipoSegunPaquete());
        gestor.asignar(urgente, flota, AHORA);

        Consola.imprimir("== Cierre y fallo ==");
        gestor.completar(enVuelo, AHORA);
        gestor.reportarFallo(flota.get(3), AHORA);

        Consola.imprimir("== Registro del log (" + log.registros().size() + " eventos) ==");
        log.registros().forEach(Consola::imprimir);
    }
}
