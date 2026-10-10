package skycampus.enterprise.ruta.drone;

import java.util.List;
import skycampus.enterprise.ruta.TramoSimple;
import skycampus.v2.model.TipoDrone;

/** Fábrica para reparto normal: el drone más pequeño que alcance (MINI, EXPRESS o CARGO). */
public class FabricaDroneEstandar extends FabricaDroneEtapa {

    @Override
    protected DroneEtapa crearDrone(TramoSimple tramo) {
        return primeroQueCubra(tramo, List.of(TipoDrone.MINI, TipoDrone.EXPRESS, TipoDrone.CARGO));
    }
}
