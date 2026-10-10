package skycampus.enterprise.dominio;

/** Puerto de salida: autorización de la Aerocivil para un plan de vuelo. La infraestructura lo implementa. */
public interface ServicioAerocivil {

    boolean autoriza(PlanVuelo plan);
}
