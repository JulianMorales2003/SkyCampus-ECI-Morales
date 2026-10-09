package v2.eventos;

import java.util.function.Consumer;
import util.Validaciones;

public class PanelOperador implements ObservadorFlota {

    private final Consumer<String> pantalla;

    public PanelOperador(Consumer<String> pantalla) {
        Validaciones.exigirPresente(pantalla, "pantalla");
        this.pantalla = pantalla;
    }

    @Override
    public void alOcurrir(EventoFlota evento) {
        Validaciones.exigirPresente(evento, "evento");
        pantalla.accept("[PANEL] " + evento.tipo() + ": " + evento.detalle());
    }
}
