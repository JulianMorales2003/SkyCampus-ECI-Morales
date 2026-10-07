package alerta;

public class AlertaOperadorConsola implements AlertaOperador {

    @Override
    public void enviar(String operador, String mensaje) {
        if (operador == null || operador.isBlank()) {
            throw new IllegalArgumentException("El operador de la alerta no puede ser nulo ni vacío.");
        }
        if (mensaje == null || mensaje.isBlank()) {
            throw new IllegalArgumentException("El mensaje de la alerta no puede ser nulo ni vacío.");
        }
        System.out.println("[ALERTA para " + operador + "] " + mensaje);
    }
}
