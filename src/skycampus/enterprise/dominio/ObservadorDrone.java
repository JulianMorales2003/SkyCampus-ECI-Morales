package skycampus.enterprise.dominio;

/** Puerto de salida: quien quiera enterarse de lo que pasa con una asignación. */
public interface ObservadorDrone {

    void alOcurrir(EventoAsignacion evento);
}
