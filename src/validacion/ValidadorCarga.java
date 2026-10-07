package validacion;

import model.Mision;

public class ValidadorCarga extends ValidadorMision {

    private final int capacidadMaximaGramos;

    public ValidadorCarga(int capacidadMaximaGramos) {
        if (capacidadMaximaGramos <= 0) {
            throw new IllegalArgumentException("La capacidad del drone debe ser mayor que cero.");
        }
        this.capacidadMaximaGramos = capacidadMaximaGramos;
    }

    @Override
    protected ResultadoValidacion evaluar(Mision mision) {
        int pesoGramos = mision.tipoCarga().pesoGramos();
        if (pesoGramos > capacidadMaximaGramos) {
            return ResultadoValidacion.rechazada("La carga " + mision.tipoCarga() + " pesa "
                    + pesoGramos + " g y supera la capacidad del drone (" + capacidadMaximaGramos + " g).");
        }
        return ResultadoValidacion.aprobada();
    }
}
