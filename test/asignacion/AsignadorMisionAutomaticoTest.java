package asignacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import builder.MisionBuilder;
import java.util.List;
import java.util.Optional;
import model.Drone;
import model.EstadoMision;
import model.Mision;
import model.TipoCarga;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("AsignadorMision: asignación automática")
class AsignadorMisionAutomaticoTest {

    private final AsignadorMision asignador = new AsignadorMision();
    private final Drone inicial = new Drone("D-00", "DJI Mini 3", 50, true, "Bloque A");

    private Mision misionPendiente() {
        return new MisionBuilder().drone(inicial).origen("Bloque A").destino("Biblioteca")
                .tipoCarga(TipoCarga.SOBRE).build();
    }

    @Test
    @DisplayName("asignarAutomaticamente_variosDisponibles_eligeElDeMayorBateriaYLoReserva")
    void asignarAutomaticamente_variosDisponibles_eligeElDeMayorBateriaYLoReserva() {
        List<Drone> flota = List.of(
                new Drone("D-01", "DJI Mini 3", 85, true, "Bloque A"),
                new Drone("D-02", "DJI Mini 3", 95, false, "Bloque B"),
                new Drone("D-03", "DJI Mini 3", 91, true, "Bloque C"));

        Optional<Asignacion> resultado = asignador.asignarAutomaticamente(misionPendiente(), flota);

        assertTrue(resultado.isPresent());
        assertEquals("D-03", resultado.get().drone().id());
        assertEquals("D-03", resultado.get().mision().drone().id());
        assertEquals(EstadoMision.EN_VUELO, resultado.get().mision().estado());
        assertFalse(resultado.get().drone().disponible());
    }

    @Test
    @DisplayName("asignarAutomaticamente_empateDeBateria_eligeElPrimeroDeLaLista")
    void asignarAutomaticamente_empateDeBateria_eligeElPrimeroDeLaLista() {
        List<Drone> flota = List.of(
                new Drone("D-07", "DJI Mini 3", 80, true, "Bloque A"),
                new Drone("D-05", "DJI Mini 3", 80, true, "Bloque B"));

        Optional<Asignacion> resultado = asignador.asignarAutomaticamente(misionPendiente(), flota);

        assertEquals("D-07", resultado.orElseThrow().drone().id());
    }

    @Test
    @DisplayName("asignarAutomaticamente_ningunoDisponible_devuelveVacio")
    void asignarAutomaticamente_ningunoDisponible_devuelveVacio() {
        List<Drone> flota = List.of(new Drone("D-01", "DJI Mini 3", 85, false, "Bloque A"));

        assertTrue(asignador.asignarAutomaticamente(misionPendiente(), flota).isEmpty());
    }

    @Test
    @DisplayName("asignarAutomaticamente_flotaVacia_devuelveVacio")
    void asignarAutomaticamente_flotaVacia_devuelveVacio() {
        assertTrue(asignador.asignarAutomaticamente(misionPendiente(), List.of()).isEmpty());
    }

    @Test
    @DisplayName("asignarAutomaticamente_flotaNula_lanzaIllegalArgumentException")
    void asignarAutomaticamente_flotaNula_lanzaIllegalArgumentException() {
        Mision mision = misionPendiente();
        assertThrows(IllegalArgumentException.class, () -> asignador.asignarAutomaticamente(mision, null));
    }

    @Test
    @DisplayName("asignarAutomaticamente_misionNula_lanzaIllegalArgumentException")
    void asignarAutomaticamente_misionNula_lanzaIllegalArgumentException() {
        List<Drone> flota = List.of(inicial);
        assertThrows(IllegalArgumentException.class, () -> asignador.asignarAutomaticamente(null, flota));
    }
}
