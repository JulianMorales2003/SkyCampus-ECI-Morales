package validacion;

import java.util.List;
import model.Drone;
import util.Validaciones;

public class ValidadorMision {

    private final List<String> destinosValidos;

    public ValidadorMision(List<String> destinosValidos) {
        this.destinosValidos = List.copyOf(destinosValidos);
    }

    public boolean tieneBateriaSuficiente(Drone drone) {
        Validaciones.exigirPresente(drone, "drone");
        return drone.bateria() >= 30;
    }

    public void validarDestino(String destino) {
        Validaciones.exigirPresente(destino, "destino");
        if (!destinosValidos.contains(destino)) {
            throw new DestinoInvalidoException("El destino \"" + destino
                    + "\" no existe. Destinos válidos: " + destinosValidos + ".");
        }
    }

    public boolean droneEstaDisponible(Drone drone) {
        Validaciones.exigirPresente(drone, "drone");
        return drone.disponible();
    }
}
