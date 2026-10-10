package skycampus.enterprise.infraestructura;

import java.util.List;
import skycampus.enterprise.dominio.Drone;
import skycampus.enterprise.dominio.RepositorioFlota;

/**
 * Adaptador de persistencia. Aquí irá JPA (EntityManager, @Entity, consultas); queda como
 * esqueleto porque el proyecto aún no incluye esa dependencia y nada del dominio la necesita.
 */
public class RepositorioFlotaJPA implements RepositorioFlota {

    @Override
    public List<Drone> findDisponibles(String sede) {
        throw new UnsupportedOperationException("JPA pendiente de integrar");
    }
}
