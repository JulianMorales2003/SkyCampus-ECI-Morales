package skycampus.enterprise.ruta;

import util.Validaciones;

/** Punto del mapa de la red, en kilómetros respecto a un origen común (sede o estación de carga). */
public record Punto(String nombre, double xKm, double yKm) {

    public Punto {
        Validaciones.exigirTexto(nombre, "nombre");
    }

    public double distanciaKm(Punto otro) {
        Validaciones.exigirPresente(otro, "otro");
        return Math.hypot(xKm - otro.xKm, yKm - otro.yKm);
    }
}
