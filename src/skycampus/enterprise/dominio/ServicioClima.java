package skycampus.enterprise.dominio;

/** Puerto de salida: respuesta del clima para un trayecto. La infraestructura lo implementa. */
public interface ServicioClima {

    boolean condicionesAptas(String origen, String destino);
}
