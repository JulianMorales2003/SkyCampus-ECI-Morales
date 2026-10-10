package validacion;

import java.util.List;
import model.Mision;

public class ValidadorDestino extends ValidadorEnCadena {

    private final List<String> destinosValidos;

    public ValidadorDestino(List<String> destinosValidos) {
        if (destinosValidos == null || destinosValidos.isEmpty()) {
            throw new IllegalArgumentException("La lista de destinos válidos no puede ser nula ni vacía.");
        }
        this.destinosValidos = List.copyOf(destinosValidos);
    }

    public boolean esValido(String destino) {
        return destinosValidos.contains(destino);
    }

    public String mensajeDeRechazo(String destino) {
        return "El destino \"" + destino + "\" no existe. Destinos válidos: " + destinosValidos + ".";
    }

    @Override
    protected ResultadoValidacion evaluar(Mision mision) {
        if (!esValido(mision.destino())) {
            return ResultadoValidacion.rechazada(mensajeDeRechazo(mision.destino()));
        }
        return ResultadoValidacion.aprobada();
    }
}
