package skycampus.enterprise.dominio;

/** Resultado de configurar el radio de una sede: lo que pidió el coordinador y lo que realmente rige. */
public record ConfiguracionRadio(double configuradoKm, double efectivoKm) {

    /** Es verdadero cuando algún límite superior obligó a reducir el radio pedido. */
    public boolean ajustado() {
        return efectivoKm < configuradoKm;
    }
}
