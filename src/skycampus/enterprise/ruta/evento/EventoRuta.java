package skycampus.enterprise.ruta.evento;

import util.Validaciones;

/** Aviso de lo que pasó en una etapa de la ruta; {@code detalle} puede ser un texto vacío. */
public record EventoRuta(TipoEventoRuta tipo, String etapa, String detalle) {

    public EventoRuta {
        Validaciones.exigirPresente(tipo, "tipo");
        Validaciones.exigirTexto(etapa, "etapa");
        Validaciones.exigirPresente(detalle, "detalle");
    }
}
