package app;

import asignacion.AsignadorDrones;
import asignacion.EstrategiaDroneEnOrigen;
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
import validacion.ValidadorEnCadena;

public class PatronesApp {

    private static final String MODELO_DRONE = "DJI Mini 3";
    private static final String BLOQUE_A = "Bloque A";
    private static final String BLOQUE_B = "Bloque B";
    private static final String BLOQUE_C = "Bloque C";
    private static final String BLOQUE_D = "Bloque D";
    private static final String BIBLIOTECA = "Biblioteca";

    private static final List<String> DESTINOS_VALIDOS =
            List.of(BLOQUE_A, BLOQUE_B, BLOQUE_C, BLOQUE_D, BIBLIOTECA);
    private static final int CAPACIDAD_DRONE_GRAMOS = 500;
    private static final String SIN_DRONE = "sin drone disponible";

    public static void main(String[] args) {
        List<Drone> flota = crearFlota();
        demostrarBuilder(flota.get(2));
        demostrarCadenaDeValidacion(flota);
        demostrarEstrategias(flota);
    }

    private static List<Drone> crearFlota() {
        return List.of(
                new Drone("D-01", MODELO_DRONE, 85, true, BLOQUE_A),
                new Drone("D-02", MODELO_DRONE, 42, false, BIBLIOTECA),
                new Drone("D-03", MODELO_DRONE, 91, true, BLOQUE_C),
                new Drone("D-04", MODELO_DRONE, 18, true, BLOQUE_B),
                new Drone("D-05", MODELO_DRONE, 67, true, BLOQUE_D));
    }

    private static void demostrarBuilder(Drone drone) {
        System.out.println("=== Problema 1: Builder ===");
        demostrarSoloObligatorios(drone);
        demostrarConOpcionales(drone);
        demostrarCampoFaltante(drone);
    }

    private static void demostrarSoloObligatorios(Drone drone) {
        Mision mision = new MisionBuilder()
                .drone(drone).origen(BLOQUE_C).destino(BIBLIOTECA)
                .tipoCarga(TipoCarga.SOBRE)
                .build();
        System.out.println("Solo obligatorios -> " + describirOpcionales(mision));
    }

    private static void demostrarConOpcionales(Drone drone) {
        Mision mision = new MisionBuilder()
                .drone(drone).origen(BLOQUE_C).destino(BIBLIOTECA)
                .tipoCarga(TipoCarga.CARPETA)
                .prioridad(1)
                .notas("Urgente: examen mañana")
                .horaMaximaEntrega(LocalTime.of(10, 30))
                .build();
        System.out.println("Con opcionales    -> " + describirOpcionales(mision));
    }

    private static void demostrarCampoFaltante(Drone drone) {
        try {
            new MisionBuilder().drone(drone).destino(BIBLIOTECA).tipoCarga(TipoCarga.SOBRE).build();
        } catch (IllegalStateException excepcionCampoFaltante) {
            System.out.println("Falta un obligatorio -> " + excepcionCampoFaltante.getMessage());
        }
    }

    private static String describirOpcionales(Mision mision) {
        return "prioridad " + mision.prioridad() + ", notas \"" + mision.notas()
                + "\", hora límite " + mision.horaMaximaEntrega();
    }

    private static void demostrarCadenaDeValidacion(List<Drone> flota) {
        System.out.println("\n=== Problema 2: Chain of Responsibility ===");
        ValidadorEnCadena cadena = new ValidadorBateria();
        cadena.setSiguiente(new ValidadorDestino(DESTINOS_VALIDOS))
                .setSiguiente(new ValidadorCarga(CAPACIDAD_DRONE_GRAMOS));

        imprimir("Misión válida", cadena.validar(crearMision(flota.get(2), BIBLIOTECA, TipoCarga.CARPETA)));
        imprimir("Batería baja", cadena.validar(crearMision(flota.get(3), BIBLIOTECA, TipoCarga.SOBRE)));
        imprimir("Destino inexistente", cadena.validar(crearMision(flota.get(2), "Bloque Z", TipoCarga.SOBRE)));
        imprimir("Carga muy pesada", cadena.validar(crearMision(flota.get(2), BIBLIOTECA, TipoCarga.LIBRO)));
        imprimir("Batería baja y destino malo", cadena.validar(crearMision(flota.get(3), "Bloque Z", TipoCarga.SOBRE)));
    }

    private static Mision crearMision(Drone drone, String destino, TipoCarga tipoCarga) {
        return new MisionBuilder()
                .drone(drone).origen(BLOQUE_A).destino(destino).tipoCarga(tipoCarga)
                .build();
    }

    private static void imprimir(String escenario, ResultadoValidacion resultado) {
        String texto = resultado.valido() ? "APROBADA" : "RECHAZADA - " + resultado.motivo();
        System.out.println(escenario + ": " + texto);
    }

    private static void demostrarEstrategias(List<Drone> flota) {
        System.out.println("\n=== Problema 3: Strategy ===");
        AsignadorDrones asignador = new AsignadorDrones(new EstrategiaMayorBateria());
        imprimirAsignacion("Mayor batería", asignador, flota, BLOQUE_B);

        asignador.cambiarEstrategia(new EstrategiaDroneEnOrigen());
        imprimirAsignacion("Drone en el origen (Bloque B)", asignador, flota, BLOQUE_B);
        imprimirAsignacion("Drone en el origen (Biblioteca)", asignador, flota, BIBLIOTECA);
        imprimirAsignacion("Flota vacía", asignador, List.of(), BLOQUE_B);
    }

    private static void imprimirAsignacion(String etiqueta, AsignadorDrones asignador,
                                           List<Drone> flota, String origen) {
        String resultado = asignador.asignar(flota, origen).map(Drone::id).orElse(SIN_DRONE);
        System.out.println(etiqueta + ": " + resultado);
    }
}
