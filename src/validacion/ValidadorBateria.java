package validacion;

import model.Mision;

public class ValidadorBateria extends ValidadorEnCadena {

    public static final int BATERIA_MINIMA = 30;

    public static boolean esSuficiente(int bateria) {
        return bateria >= BATERIA_MINIMA;
    }

    @Override
    protected ResultadoValidacion evaluar(Mision mision) {
        int bateria = mision.drone().bateria();
        if (!esSuficiente(bateria)) {
            return ResultadoValidacion.rechazada("Batería insuficiente: " + bateria
                    + "% (mínimo " + BATERIA_MINIMA + "%).");
        }
        return ResultadoValidacion.aprobada();
    }
}
