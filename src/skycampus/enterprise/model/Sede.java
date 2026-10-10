package skycampus.enterprise.model;

import util.Validaciones;

/** Sede universitaria que forma parte de la red compartida (por ejemplo ECI, UNAL, Uniandes o EAFIT). */
public record Sede(String nombre) {

    public Sede {
        Validaciones.exigirTexto(nombre, "nombre");
    }
}
