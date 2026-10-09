package skycampus.v2.eventos;

import java.util.function.Consumer;
import util.Validaciones;

public class AlertaTecnico implements ObservadorFlota {

    private final Consumer<String> canal;
    private int alertasEnviadas;

    public AlertaTecnico(Consumer<String> canal) {
        Validaciones.exigirPresente(canal, "canal");
        this.canal = canal;
    }

    @Override
    public void alOcurrir(EventoFlota evento) {
        Validaciones.exigirPresente(evento, "evento");
        if (evento.tipo() == TipoEvento.FALLO_DRONE) {
            alertasEnviadas++;
            canal.accept("[TÉCNICO] Revisar de inmediato: " + evento.detalle());
        }
    }

    public int alertasEnviadas() {
        return alertasEnviadas;
    }
}
