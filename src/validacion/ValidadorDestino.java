package validacion;

import java.util.List;
import model.Mision;

public class ValidadorDestino extends ValidadorMision {

    private final List<String> destinosValidos;

    public ValidadorDestino(List<String> destinosValidos) {
        if (destinosValidos == null || destinosValidos.isEmpty()) {
            throw new IllegalArgumentException("La lista de destinos válidos no puede ser nula ni vacía.");
        }
        this.destinosValidos = List.copyOf(destinosValidos);
    }

    @Override
    protected ResultadoValidacion evaluar(Mision mision) {
        if (!destinosValidos.contains(mision.destino())) {
            return ResultadoValidacion.rechazada("El destino \"" + mision.destino()
                    + "\" no existe. Destinos válidos: " + destinosValidos + ".");
        }
        return ResultadoValidacion.aprobada();
    }
}
