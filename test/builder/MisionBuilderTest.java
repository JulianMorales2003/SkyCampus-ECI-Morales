package builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalTime;
import model.Drone;
import model.EstadoMision;
import model.Mision;
import model.TipoCarga;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import testutil.Datos;

class MisionBuilderTest {

    private final Drone drone = Datos.drone(80);

    private MisionBuilder builderMinimo() {
        return new MisionBuilder().drone(drone).origen("Bloque A").destino("Biblioteca")
                .tipoCarga(TipoCarga.SOBRE);
    }

    @Test
    @DisplayName("build: con solo los obligatorios usa los valores por defecto y estado PENDIENTE")
    void build_soloObligatorios_usaValoresPorDefecto() {
        Mision mision = builderMinimo().build();

        assertEquals(EstadoMision.PENDIENTE, mision.estado());
        assertEquals(Mision.PRIORIDAD_POR_DEFECTO, mision.prioridad());
        assertEquals("", mision.notas());
        assertEquals(Mision.SIN_HORA_LIMITE, mision.horaMaximaEntrega());
    }

    @Test
    @DisplayName("build: con todos los opcionales los conserva")
    void build_conOpcionales_losConserva() {
        Mision mision = builderMinimo().prioridad(5).notas("urgente")
                .horaMaximaEntrega(LocalTime.of(9, 0)).build();

        assertEquals(5, mision.prioridad());
        assertEquals("urgente", mision.notas());
        assertEquals(LocalTime.of(9, 0), mision.horaMaximaEntrega());
        assertEquals(drone, mision.drone());
        assertEquals("Biblioteca", mision.destino());
    }

    @Test
    @DisplayName("build: cada misión recibe un id distinto")
    void build_dosVeces_generaIdsDistintos() {
        MisionBuilder builder = builderMinimo();

        assertNotEquals(builder.build().id(), builder.build().id());
    }

    @Test
    @DisplayName("build: falta de cada campo obligatorio lanza IllegalStateException")
    void build_faltaCampoObligatorio_lanzaIllegalStateException() {
        assertThrows(IllegalStateException.class, () -> new MisionBuilder().origen("A").destino("B").tipoCarga(TipoCarga.SOBRE).build());
        assertThrows(IllegalStateException.class, () -> new MisionBuilder().drone(drone).destino("B").tipoCarga(TipoCarga.SOBRE).build());
        assertThrows(IllegalStateException.class, () -> new MisionBuilder().drone(drone).origen("A").tipoCarga(TipoCarga.SOBRE).build());
        assertThrows(IllegalStateException.class, () -> new MisionBuilder().drone(drone).origen("A").destino("B").build());
    }

    @Test
    @DisplayName("build: origen o destino en blanco lanza IllegalStateException")
    void build_textoEnBlanco_lanzaIllegalStateException() {
        assertThrows(IllegalStateException.class, () -> builderMinimo().origen("  ").build());
        assertThrows(IllegalStateException.class, () -> builderMinimo().destino("").build());
    }

    @Test
    @DisplayName("prioridad: fuera de 1..5 lanza IllegalArgumentException")
    void prioridad_fueraDeRango_lanzaExcepcion() {
        MisionBuilder builder = builderMinimo();

        assertThrows(IllegalArgumentException.class, () -> builder.prioridad(0));
        assertThrows(IllegalArgumentException.class, () -> builder.prioridad(6));
    }

    @Test
    @DisplayName("notas y horaMaximaEntrega: nulos lanzan IllegalArgumentException")
    void notasYHora_nulos_lanzaExcepcion() {
        MisionBuilder builder = builderMinimo();

        assertThrows(IllegalArgumentException.class, () -> builder.notas(null));
        assertThrows(IllegalArgumentException.class, () -> builder.horaMaximaEntrega(null));
    }
}
