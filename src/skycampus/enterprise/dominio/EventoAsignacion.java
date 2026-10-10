package skycampus.enterprise.dominio;

/** Hecho que el asignador comunica hacia afuera (sin saber quién lo escucha). */
public record EventoAsignacion(TipoEventoAsignacion tipo, String solicitudId, String detalle) {

    public EventoAsignacion {
        if (tipo == null) {
            throw new IllegalArgumentException("El campo tipo es obligatorio.");
        }
        if (solicitudId == null || solicitudId.isBlank()) {
            throw new IllegalArgumentException("El campo solicitudId es obligatorio.");
        }
    }
}
