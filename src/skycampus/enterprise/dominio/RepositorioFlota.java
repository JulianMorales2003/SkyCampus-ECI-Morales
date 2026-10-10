package skycampus.enterprise.dominio;

import java.util.List;

/** Puerto de salida: de dónde salen los drones. La capa de infraestructura lo implementa. */
public interface RepositorioFlota {

    List<Drone> findDisponibles(String sede);
}
