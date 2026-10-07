package builder;

import java.time.LocalTime;
import java.util.UUID;
import model.Drone;
import model.EstadoMision;
import model.Mision;
import model.TipoCarga;

public class MisionBuilder {

    private static final int PRIORIDAD_MINIMA = 1;
    private static final int PRIORIDAD_MAXIMA = 5;
    private static final int PRIORIDAD_POR_DEFECTO = 3;

    private Drone drone;
    private String origen;
    private String destino;
    private TipoCarga tipoCarga;
    private int prioridad = PRIORIDAD_POR_DEFECTO;
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
        if (prioridad < PRIORIDAD_MINIMA || prioridad > PRIORIDAD_MAXIMA) {
            throw new IllegalArgumentException("La prioridad debe estar entre "
                    + PRIORIDAD_MINIMA + " y " + PRIORIDAD_MAXIMA + ".");
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
        if (drone == null || origen == null || origen.isBlank()
                || destino == null || destino.isBlank() || tipoCarga == null) {
            throw new IllegalStateException("drone, origen, destino y tipoCarga son obligatorios.");
        }
        return new Mision(UUID.randomUUID().toString(), drone, origen, destino, tipoCarga,
                EstadoMision.PENDIENTE, prioridad, notas, horaMaximaEntrega);
    }
}
