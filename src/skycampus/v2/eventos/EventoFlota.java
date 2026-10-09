package skycampus.v2.eventos;

import java.time.Instant;
import util.Validaciones;

public record EventoFlota(TipoEvento tipo, String detalle, Instant momento) {

    public EventoFlota {
        Validaciones.exigirPresente(tipo, "tipo");
        Validaciones.exigirTexto(detalle, "detalle");
        Validaciones.exigirPresente(momento, "momento");
    }
}
