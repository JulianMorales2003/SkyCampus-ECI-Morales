package skycampus.enterprise.ruta;

/** Resultado de ejecutar una ruta: si terminó y cuántas etapas simples se completaron. */
public record ResultadoEjecucion(boolean completada, int etapasCompletadas) {

    public ResultadoEjecucion {
        if (etapasCompletadas < 0) {
            throw new IllegalArgumentException("Las etapas completadas no pueden ser negativas.");
        }
    }

    public static ResultadoEjecucion exito(int etapasCompletadas) {
        return new ResultadoEjecucion(true, etapasCompletadas);
    }

    public static ResultadoEjecucion fallo(int etapasCompletadas) {
        return new ResultadoEjecucion(false, etapasCompletadas);
    }
}
