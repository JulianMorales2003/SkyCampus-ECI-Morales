package validacion;

import model.Mision;

public class ValidadorBateria extends ValidadorEnCadena {

    private static final int BATERIA_MINIMA = 30;

    @Override
    protected ResultadoValidacion evaluar(Mision mision) {
        int bateria = mision.drone().bateria();
        if (bateria < BATERIA_MINIMA) {
            return ResultadoValidacion.rechazada("Batería insuficiente: " + bateria
                    + "% (mínimo " + BATERIA_MINIMA + "%).");
        }
        return ResultadoValidacion.aprobada();
    }
}
