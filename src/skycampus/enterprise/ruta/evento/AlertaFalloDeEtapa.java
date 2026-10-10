package skycampus.enterprise.ruta.evento;

import java.util.function.Consumer;
import util.Validaciones;

/** Observador que solo reacciona a las etapas fallidas y envía el aviso al destino que se le indique. */
public class AlertaFalloDeEtapa implements ObservadorRuta {

    private final Consumer<String> destinoDelAviso;

    public AlertaFalloDeEtapa(Consumer<String> destinoDelAviso) {
        Validaciones.exigirPresente(destinoDelAviso, "destinoDelAviso");
        this.destinoDelAviso = destinoDelAviso;
    }

    @Override
    public void alOcurrir(EventoRuta evento) {
        if (evento.tipo() == TipoEventoRuta.ETAPA_FALLIDA) {
            destinoDelAviso.accept("ALERTA: " + evento.etapa() + " - " + evento.detalle());
        }
    }
}
