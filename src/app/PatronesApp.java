package app;

import asignacion.AsignadorDrones;
import asignacion.EstrategiaMayorBateria;
import builder.MisionBuilder;
import java.time.LocalTime;
import java.util.List;
import model.Drone;
import model.Mision;
import model.TipoCarga;
import validacion.ResultadoValidacion;
import validacion.ValidadorBateria;
import validacion.ValidadorCarga;
import validacion.ValidadorDestino;
import validacion.ValidadorMision;

public class PatronesApp {

    private static final List<String> DESTINOS_VALIDOS =
            List.of("Bloque A", "Bloque B", "Bloque C", "Bloque D", "Biblioteca");
    private static final int CAPACIDAD_DRONE_GRAMOS = 500;

    public static void main(String[] args) {
        List<Drone> flota = crearFlota();
        demostrarBuilder(flota);
        demostrarCadenaDeValidacion(flota);
        demostrarEstrategia(flota);
    }

    private static List<Drone> crearFlota() {
        return List.of(
                new Drone("D-01", "DJI Mini 3", 85, true, "Bloque A"),
                new Drone("D-02", "DJI Mini 3", 42, false, "Biblioteca"),
                new Drone("D-03", "DJI Mini 3", 91, true, "Bloque C"),
                new Drone("D-04", "DJI Mini 3", 18, true, "Bloque B"),
                new Drone("D-05", "DJI Mini 3", 67, true, "Bloque D"));
    }

    private static void demostrarBuilder(List<Drone> flota) {
        System.out.println("=== Problema 1: Builder ===");
        Mision minima = new MisionBuilder()
                .drone(flota.get(2)).origen("Bloque C").destino("Biblioteca")
                .tipoCarga(TipoCarga.SOBRE)
                .build();
        System.out.println("Solo obligatorios -> prioridad " + minima.prioridad()
                + ", notas \"" + minima.notas() + "\", hora límite " + minima.horaMaximaEntrega());

        Mision completa = new MisionBuilder()
                .drone(flota.get(2)).origen("Bloque C").destino("Biblioteca")
                .tipoCarga(TipoCarga.CARPETA)
                .prioridad(1)
                .notas("Urgente: examen mañana")
                .horaMaximaEntrega(LocalTime.of(10, 30))
                .build();
        System.out.println("Con opcionales    -> prioridad " + completa.prioridad()
                + ", notas \"" + completa.notas() + "\", hora límite " + completa.horaMaximaEntrega());

        try {
            new MisionBuilder().drone(flota.get(2)).build();
        } catch (IllegalStateException e) {
            System.out.println("Faltan obligatorios -> " + e.getMessage());
        }
    }

    private static void demostrarCadenaDeValidacion(List<Drone> flota) {
        System.out.println("\n=== Problema 2: Chain of Responsibility ===");
        ValidadorMision cadena = new ValidadorBateria();
        cadena.setSiguiente(new ValidadorDestino(DESTINOS_VALIDOS))
                .setSiguiente(new ValidadorCarga(CAPACIDAD_DRONE_GRAMOS));

        imprimir("Misión válida", cadena.validar(crearMision(flota.get(2), "Biblioteca", TipoCarga.CARPETA)));
        imprimir("Batería baja", cadena.validar(crearMision(flota.get(3), "Biblioteca", TipoCarga.SOBRE)));
        imprimir("Destino inexistente", cadena.validar(crearMision(flota.get(2), "Bloque Z", TipoCarga.SOBRE)));
        imprimir("Carga muy pesada", cadena.validar(crearMision(flota.get(2), "Biblioteca", TipoCarga.LIBRO)));
        imprimir("Batería baja y destino malo", cadena.validar(crearMision(flota.get(3), "Bloque Z", TipoCarga.SOBRE)));
    }

    private static Mision crearMision(Drone drone, String destino, TipoCarga tipoCarga) {
        return new MisionBuilder()
                .drone(drone).origen("Bloque A").destino(destino).tipoCarga(tipoCarga)
                .build();
    }

    private static void imprimir(String escenario, ResultadoValidacion resultado) {
        String texto = resultado.valido() ? "APROBADA" : "RECHAZADA - " + resultado.motivo();
        System.out.println(escenario + ": " + texto);
    }

    private static void demostrarEstrategia(List<Drone> flota) {
        System.out.println("\n=== Problema 3: Strategy ===");
        AsignadorDrones asignador = new AsignadorDrones(new EstrategiaMayorBateria());
        System.out.println("Mayor batería: "
                + asignador.asignar(flota, "Bloque B").map(Drone::id).orElse("sin drone disponible"));

        asignador.cambiarEstrategia((drones, origenSolicitud) -> drones.stream()
                .filter(Drone::disponible)
                .filter(drone -> origenSolicitud.equals(drone.ubicacion()))
                .findFirst());
        System.out.println("Más cercano al origen (estrategia futura): "
                + asignador.asignar(flota, "Bloque B").map(Drone::id).orElse("sin drone disponible"));

        System.out.println("Flota vacía: "
                + asignador.asignar(List.of(), "Bloque B").map(Drone::id).orElse("sin drone disponible"));
    }
}
