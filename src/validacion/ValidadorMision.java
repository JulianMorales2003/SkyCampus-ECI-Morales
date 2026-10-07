package validacion;

import model.Mision;

public abstract class ValidadorMision {

    private ValidadorMision siguiente;

    public ValidadorMision setSiguiente(ValidadorMision siguiente) {
        if (siguiente == null) {
            throw new IllegalArgumentException("El siguiente validador de la cadena no puede ser nulo.");
        }
        this.siguiente = siguiente;
        return siguiente;
    }

    public final ResultadoValidacion validar(Mision mision) {
        if (mision == null) {
            throw new IllegalArgumentException("La misión a validar no puede ser nula.");
        }
        ResultadoValidacion resultado = evaluar(mision);
        if (!resultado.valido() || siguiente == null) {
            return resultado;
        }
        return siguiente.validar(mision);
    }

    protected abstract ResultadoValidacion evaluar(Mision mision);
}
