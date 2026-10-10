package builder;

import java.time.LocalTime;
import java.util.UUID;
import model.Drone;
import model.EstadoMision;
import model.Mision;
import model.TipoCarga;

public class MisionBuilder {

    private Drone drone;
    private String origen;
    private String destino;
    private TipoCarga tipoCarga;
    private int prioridad = Mision.PRIORIDAD_POR_DEFECTO;
    private String notas = "";
    private LocalTime horaMaximaEntrega = Mision.SIN_HORA_LIMITE;

    public MisionBuilder drone(Drone drone) {
        this.drone = drone;
        return this;
    }

    public MisionBuilder origen(String origen) {
        this.origen = origen;
        return this;
    }

    public MisionBuilder destino(String destino) {
        this.destino = destino;
        return this;
    }

    public MisionBuilder tipoCarga(TipoCarga tipoCarga) {
        this.tipoCarga = tipoCarga;
        return this;
    }

    public MisionBuilder prioridad(int prioridad) {
        if (!Mision.esPrioridadValida(prioridad)) {
            throw new IllegalArgumentException("La prioridad debe estar entre "
                    + Mision.PRIORIDAD_MINIMA + " y " + Mision.PRIORIDAD_MAXIMA + ".");
        }
        this.prioridad = prioridad;
        return this;
    }

    public MisionBuilder notas(String notas) {
        if (notas == null) {
            throw new IllegalArgumentException("Las notas no pueden ser nulas; usa una cadena vacía.");
        }
        this.notas = notas;
        return this;
    }

    public MisionBuilder horaMaximaEntrega(LocalTime horaMaximaEntrega) {
        if (horaMaximaEntrega == null) {
            throw new IllegalArgumentException("La hora máxima de entrega no puede ser nula.");
        }
        this.horaMaximaEntrega = horaMaximaEntrega;
        return this;
    }

    public Mision build() {
        validarCamposObligatorios();
        return new Mision(UUID.randomUUID().toString(), drone, origen, destino, tipoCarga,
                EstadoMision.PENDIENTE, prioridad, notas, horaMaximaEntrega);
    }

    private void validarCamposObligatorios() {
        exigirDefinido(drone, "drone");
        exigirTextoDefinido(origen, "origen");
        exigirTextoDefinido(destino, "destino");
        exigirDefinido(tipoCarga, "tipoCarga");
    }

    private static void exigirDefinido(Object valor, String campo) {
        if (valor == null) {
            throw new IllegalStateException("El campo obligatorio '" + campo + "' no fue definido.");
        }
    }

    private static void exigirTextoDefinido(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException("El campo obligatorio '" + campo + "' no fue definido o está vacío.");
        }
    }
}
