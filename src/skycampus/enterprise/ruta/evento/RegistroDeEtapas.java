package skycampus.enterprise.ruta.evento;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Observador que guarda todos los eventos, por ejemplo para el panel del coordinador de la sede. */
public class RegistroDeEtapas implements ObservadorRuta {

    private final List<EventoRuta> eventos = new CopyOnWriteArrayList<>();

    @Override
    public void alOcurrir(EventoRuta evento) {
        eventos.add(evento);
    }

    public List<EventoRuta> eventos() {
        return List.copyOf(eventos);
    }
}
