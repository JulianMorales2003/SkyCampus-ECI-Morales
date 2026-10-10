package skycampus.enterprise.ruta;

import java.util.List;
import skycampus.enterprise.ruta.drone.DroneEtapa;
import skycampus.enterprise.ruta.drone.TramoSinDroneException;
import skycampus.enterprise.ruta.evento.EventoRuta;
import skycampus.enterprise.ruta.evento.TipoEventoRuta;
import util.Validaciones;

/** Hoja del Composite: un vuelo directo de un punto a otro, hecho por un solo drone. */
public final class TramoSimple implements Ruta {

    public static final double VELOCIDAD_CRUCERO_KMH = 30.0;

    private final Punto origen;
    private final Punto destino;

    public TramoSimple(Punto origen, Punto destino) {
        Validaciones.exigirPresente(origen, "origen");
        Validaciones.exigirPresente(destino, "destino");
        if (origen.equals(destino)) {
            throw new IllegalArgumentException("El origen y el destino de un tramo no pueden ser el mismo punto.");
        }
        this.origen = origen;
        this.destino = destino;
    }

    @Override
    public String descripcion() {
        return "Tramo " + origen.nombre() + " -> " + destino.nombre();
    }

    @Override
    public Punto inicio() {
        return origen;
    }

    @Override
    public Punto fin() {
        return destino;
    }

    @Override
    public double distanciaKm() {
        return origen.distanciaKm(destino);
    }

    @Override
    public int duracionMinutos() {
        return (int) Math.ceil(distanciaKm() / VELOCIDAD_CRUCERO_KMH * 60);
    }

    @Override
    public int paradasDeCarga() {
        return 0;
    }

    @Override
    public double tramoMasLargoKm() {
        return distanciaKm();
    }

    @Override
    public List<Punto> puntos() {
        return List.of(origen, destino);
    }

    @Override
    public ResultadoEjecucion ejecutar(ContextoEjecucion contexto) {
        Validaciones.exigirPresente(contexto, "contexto");
        contexto.notificador().publicar(new EventoRuta(TipoEventoRuta.ETAPA_INICIADA, descripcion(), ""));
        try {
            DroneEtapa drone = contexto.fabrica().prepararPara(this);
            contexto.notificador().publicar(
                    new EventoRuta(TipoEventoRuta.ETAPA_COMPLETADA, descripcion(), "drone " + drone.tipo()));
            return ResultadoEjecucion.exito(1);
        } catch (TramoSinDroneException e) {
            contexto.notificador().publicar(
                    new EventoRuta(TipoEventoRuta.ETAPA_FALLIDA, descripcion(), e.getMessage()));
            return ResultadoEjecucion.fallo(0);
        }
    }
}
