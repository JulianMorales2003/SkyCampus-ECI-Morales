package skycampus.enterprise.ruta.evento;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import util.Validaciones;

/** Sujeto del Observer: avisa cada evento de etapa a todos los observadores suscritos. */
public class NotificadorRuta {

    private final List<ObservadorRuta> observadores = new CopyOnWriteArrayList<>();

    public void suscribir(ObservadorRuta observador) {
        Validaciones.exigirPresente(observador, "observador");
        observadores.add(observador);
    }

    public void publicar(EventoRuta evento) {
        Validaciones.exigirPresente(evento, "evento");
        observadores.forEach(observador -> observador.alOcurrir(evento));
    }
}
