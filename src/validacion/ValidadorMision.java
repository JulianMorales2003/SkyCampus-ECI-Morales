package validacion;

import model.Drone;
import util.Validaciones;

public class ValidadorMision {

    private final ValidadorDestino validadorDestino;

    public ValidadorMision(ValidadorDestino validadorDestino) {
        this.validadorDestino = validadorDestino;
    }

    public boolean tieneBateriaSuficiente(Drone drone) {
        Validaciones.exigirPresente(drone, "drone");
        return ValidadorBateria.esSuficiente(drone.bateria());
    }

    public void validarDestino(String destino) {
        Validaciones.exigirPresente(destino, "destino");
        if (!validadorDestino.esValido(destino)) {
            throw new DestinoInvalidoException(validadorDestino.mensajeDeRechazo(destino));
        }
    }

    public boolean droneEstaDisponible(Drone drone) {
        Validaciones.exigirPresente(drone, "drone");
        return drone.disponible();
    }
}
