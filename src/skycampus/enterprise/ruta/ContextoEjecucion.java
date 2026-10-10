package skycampus.enterprise.ruta;

import skycampus.enterprise.ruta.drone.FabricaDroneEtapa;
import skycampus.enterprise.ruta.evento.NotificadorRuta;
import util.Validaciones;

/** Colaboradores que necesita una ruta para ejecutarse: quién crea los drones y a quién se avisa. */
public record ContextoEjecucion(FabricaDroneEtapa fabrica, NotificadorRuta notificador) {

    public ContextoEjecucion {
        Validaciones.exigirPresente(fabrica, "fabrica");
        Validaciones.exigirPresente(notificador, "notificador");
    }
}
