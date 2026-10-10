package skycampus.enterprise.aplicacion;

/** Por qué no se pudo crear una misión. Los últimos cinco nombres coinciden con {@code MotivoRechazo}. */
public enum MotivoMision {
    SEDE_INACTIVA("Una de las sedes no está operando."),
    PAQUETE_EXCEDE_PESO("El paquete supera el peso máximo permitido."),
    RUTA_NO_DEFINIDA("No hay una ruta definida entre las dos sedes."),
    CLIMA_ADVERSO("El clima no permite volar o no se pudo consultar."),
    SIN_DRONE_DISPONIBLE("No hay un drone disponible con batería suficiente en la sede de origen."),
    SIN_RESTRICCION_VIGENTE("La sede de origen no tiene una restricción aérea vigente de la Aerocivil."),
    ALTURA_EXCEDIDA("La altura del vuelo supera la permitida."),
    RADIO_EXCEDIDO("La ruta supera el radio de vuelo permitido."),
    AEROCIVIL_RECHAZA("La Aerocivil rechazó el vuelo."),
    AEROCIVIL_NO_RESPONDE("La Aerocivil no respondió; por seguridad el vuelo no despega.");

    private final String mensaje;

    MotivoMision(String mensaje) {
        this.mensaje = mensaje;
    }

    public String mensaje() {
        return mensaje;
    }
}
