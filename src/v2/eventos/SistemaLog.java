package v2.eventos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import util.Validaciones;

public class SistemaLog implements ObservadorFlota {

    private final List<String> registros = new ArrayList<>();

    @Override
    public void alOcurrir(EventoFlota evento) {
        Validaciones.exigirPresente(evento, "evento");
        registros.add(evento.momento() + " | " + evento.tipo() + " | " + evento.detalle());
    }

    public List<String> registros() {
        return Collections.unmodifiableList(registros);
    }
}
