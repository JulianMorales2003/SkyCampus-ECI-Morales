package skycampus.enterprise.infraestructura;

import java.util.List;
import skycampus.enterprise.dominio.Drone;
import skycampus.enterprise.dominio.RepositorioFlota;

/** Adaptador sin base de datos: sirve para pruebas de integración y demostraciones. */
public class RepositorioFlotaEnMemoria implements RepositorioFlota {

    private final List<Drone> drones;

    public RepositorioFlotaEnMemoria(List<Drone> drones) {
        this.drones = List.copyOf(drones);
    }

    @Override
    public List<Drone> findDisponibles(String sede) {
        return drones.stream().filter(drone -> drone.sede().equals(sede)).toList();
    }
}
