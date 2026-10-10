package skycampus.enterprise.ruta.drone;

import java.util.List;
import skycampus.enterprise.ruta.TramoSimple;
import skycampus.v2.model.TipoDrone;

/** Fábrica para entregas urgentes: solo drones EXPRESS. */
public class FabricaDroneUrgente extends FabricaDroneEtapa {

    @Override
    protected DroneEtapa crearDrone(TramoSimple tramo) {
        return primeroQueCubra(tramo, List.of(TipoDrone.EXPRESS));
    }
}
