package asignacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import alerta.AlertaOperador;
import builder.MisionBuilder;
import java.util.ArrayList;
import java.util.List;
import model.Drone;
import model.EstadoMision;
import model.Mision;
import model.TipoCarga;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("AsignadorMision: asignar y notificar")
class AsignadorMisionNotificacionTest {

    private final AsignadorMision asignador = new AsignadorMision();
    private final List<String> alertasEnviadas = new ArrayList<>();
    private final AlertaOperador alertaGrabadora =
            (operador, mensaje) -> alertasEnviadas.add(operador + "|" + mensaje);

    private Mision misionPendiente(Drone drone) {
        return new MisionBuilder().drone(drone).origen("Bloque A").destino("Biblioteca")
                .tipoCarga(TipoCarga.SOBRE).build();
    }

    @Test
    @DisplayName("asignarYNotificar_misionYDroneValidos_asignaYAvisaAlOperador")
    void asignarYNotificar_misionYDroneValidos_asignaYAvisaAlOperador() {
        Drone drone = new Drone("D-03", "DJI Mini 3", 91, true, "Bloque C");

        Asignacion asignacion = asignador.asignarYNotificar(misionPendiente(drone), drone, alertaGrabadora, "Operador 1");

        assertEquals(EstadoMision.EN_VUELO, asignacion.mision().estado());
        assertEquals(List.of("Operador 1|Misión asignada al drone D-03 con destino Biblioteca."), alertasEnviadas);
    }

    @Test
    @DisplayName("asignarYNotificar_droneNoDisponible_noEnviaAlerta")
    void asignarYNotificar_droneNoDisponible_noEnviaAlerta() {
        Drone ocupado = new Drone("D-02", "DJI Mini 3", 42, false, "Biblioteca");
        Mision mision = misionPendiente(ocupado);

        assertThrows(IllegalStateException.class,
                () -> asignador.asignarYNotificar(mision, ocupado, alertaGrabadora, "Operador 1"));
        assertTrue(alertasEnviadas.isEmpty());
    }

    @Test
    @DisplayName("asignarYNotificar_alertaNula_lanzaIllegalArgumentException")
    void asignarYNotificar_alertaNula_lanzaIllegalArgumentException() {
        Drone drone = new Drone("D-03", "DJI Mini 3", 91, true, "Bloque C");
        Mision mision = misionPendiente(drone);

        assertThrows(IllegalArgumentException.class,
                () -> asignador.asignarYNotificar(mision, drone, null, "Operador 1"));
    }
}
