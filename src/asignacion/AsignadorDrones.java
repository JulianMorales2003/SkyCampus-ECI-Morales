package asignacion;

import java.util.List;
import java.util.Optional;
import model.Drone;
import util.Validaciones;

public class AsignadorDrones {

    private EstrategiaAsignacion estrategia;

    public AsignadorDrones(EstrategiaAsignacion estrategia) {
        this.estrategia = validarEstrategia(estrategia);
    }

    public void cambiarEstrategia(EstrategiaAsignacion nuevaEstrategia) {
        this.estrategia = validarEstrategia(nuevaEstrategia);
    }

    public Optional<Drone> asignar(List<Drone> flota, String origen) {
        Validaciones.exigirPresente(flota, "flota");
        Validaciones.exigirTexto(origen, "origen");
        return estrategia.seleccionar(flota, origen);
    }

    private static EstrategiaAsignacion validarEstrategia(EstrategiaAsignacion estrategia) {
        Validaciones.exigirPresente(estrategia, "estrategia");
        return estrategia;
    }
}
