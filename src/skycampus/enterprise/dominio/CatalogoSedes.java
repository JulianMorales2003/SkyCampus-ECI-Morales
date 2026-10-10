package skycampus.enterprise.dominio;

import java.util.OptionalDouble;

/** Puerto de salida: qué sedes están operando y a qué distancia queda una de otra. */
public interface CatalogoSedes {

    boolean estaActiva(String sede);

    /** Distancia entre dos sedes; vacío si la ruta no está definida. */
    OptionalDouble distanciaKm(String origen, String destino);
}
