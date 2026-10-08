package validacion;

import model.Mision;

public abstract class ValidadorEnCadena {

    private ValidadorEnCadena siguiente;

    public ValidadorEnCadena setSiguiente(ValidadorEnCadena siguiente) {
        if (siguiente == null) {
            throw new IllegalArgumentException("El siguiente validador de la cadena no puede ser nulo.");
        }
        if (siguiente == this) {
            throw new IllegalArgumentException("Un validador no puede ser su propio siguiente: la cadena sería circular.");
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
