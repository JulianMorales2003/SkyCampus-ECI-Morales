package validacion;

import java.util.List;
import model.Drone;
import util.Validaciones;

public class ValidadorMision {

    public ValidadorMision(List<String> destinosValidos) {
    }

    public boolean tieneBateriaSuficiente(Drone drone) {
        Validaciones.exigirPresente(drone, "drone");
        return drone.bateria() >= 30;
    }

    public void validarDestino(String destino) {
        throw new UnsupportedOperationException("pendiente");
    }

    public boolean droneEstaDisponible(Drone drone) {
        throw new UnsupportedOperationException("pendiente");
    }
}
