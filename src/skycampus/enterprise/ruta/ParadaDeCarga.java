package skycampus.enterprise.ruta;

import java.util.List;
import skycampus.enterprise.ruta.evento.EventoRuta;
import skycampus.enterprise.ruta.evento.TipoEventoRuta;
import util.Validaciones;

/** Hoja del Composite: la parada del drone en una estación de carga autónoma. */
public final class ParadaDeCarga implements Ruta {

    private final Punto estacion;
    private final int minutos;

    public ParadaDeCarga(Punto estacion, int minutos) {
        Validaciones.exigirPresente(estacion, "estacion");
        if (minutos <= 0) {
            throw new IllegalArgumentException("La carga debe durar al menos un minuto.");
        }
        this.estacion = estacion;
        this.minutos = minutos;
    }

    @Override
    public String descripcion() {
        return "Carga en " + estacion.nombre();
    }

    @Override
    public Punto inicio() {
        return estacion;
    }

    @Override
    public Punto fin() {
        return estacion;
    }

    @Override
    public double distanciaKm() {
        return 0;
    }

    @Override
    public int duracionMinutos() {
        return minutos;
    }

    @Override
    public int paradasDeCarga() {
        return 1;
    }

    @Override
    public double tramoMasLargoKm() {
        return 0;
    }

    @Override
    public List<Punto> puntos() {
        return List.of(estacion);
    }

    @Override
    public ResultadoEjecucion ejecutar(ContextoEjecucion contexto) {
        Validaciones.exigirPresente(contexto, "contexto");
        contexto.notificador().publicar(new EventoRuta(TipoEventoRuta.ETAPA_INICIADA, descripcion(), ""));
        contexto.notificador().publicar(
                new EventoRuta(TipoEventoRuta.ETAPA_COMPLETADA, descripcion(), minutos + " min"));
        return ResultadoEjecucion.exito(1);
    }
}
