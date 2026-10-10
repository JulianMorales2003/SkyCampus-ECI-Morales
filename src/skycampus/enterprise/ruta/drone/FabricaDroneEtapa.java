package skycampus.enterprise.ruta.drone;

import java.util.List;
import skycampus.enterprise.ruta.TramoSimple;
import skycampus.v2.model.TipoDrone;
import util.Validaciones;

/**
 * Creador del Factory Method. Las subclases deciden qué tipos de drone ofrecen (método de fábrica
 * {@link #crearDrone}); la etapa solo conoce esta clase y llama a {@link #prepararPara}.
 */
public abstract class FabricaDroneEtapa {

    /** Operación que usa el método de fábrica y comprueba que el drone creado realmente cubre el tramo. */
    public final DroneEtapa prepararPara(TramoSimple tramo) {
        Validaciones.exigirPresente(tramo, "tramo");
        DroneEtapa drone = crearDrone(tramo);
        if (tramo.distanciaKm() > drone.alcanceKm()) {
            throw new TramoSinDroneException("El drone " + drone.tipo() + " no cubre " + tramo.descripcion() + ".");
        }
        return drone;
    }

    /** Método de fábrica: devuelve el drone adecuado para el tramo o lanza {@link TramoSinDroneException}. */
    protected abstract DroneEtapa crearDrone(TramoSimple tramo);

    /** Ayuda para las subclases: el primer tipo de la lista (de menor a mayor alcance) que cubre el tramo. */
    protected final DroneEtapa primeroQueCubra(TramoSimple tramo, List<TipoDrone> tiposOfrecidos) {
        return tiposOfrecidos.stream()
                .map(DroneEtapa::de)
                .filter(drone -> tramo.distanciaKm() <= drone.alcanceKm())
                .findFirst()
                .orElseThrow(() -> new TramoSinDroneException("Ningún drone disponible cubre " + tramo.descripcion() + "."));
    }
}
