package persistencia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalTime;
import java.util.List;
import model.Drone;
import model.EstadoMision;
import model.Mision;
import model.TipoCarga;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import testutil.Datos;

class RepositorioMisionEnMemoriaTest {

    private RepositorioMisionEnMemoria repositorio;
    private final Drone drone = Datos.drone(80);

    @BeforeEach
    void crearRepositorio() {
        repositorio = new RepositorioMisionEnMemoria();
    }

    private Mision mision(String id, EstadoMision estado) {
        return new Mision(id, drone, "Bloque A", "Biblioteca", TipoCarga.SOBRE, estado, 3, "", LocalTime.NOON);
    }

    @Test
    @DisplayName("guardar + buscarPorId: una misión guardada se encuentra por su id")
    void guardar_mision_sePuedeBuscarPorId() {
        Mision m = mision("M-1", EstadoMision.PENDIENTE);

        repositorio.guardar(m);

        assertEquals(m, repositorio.buscarPorId("M-1").orElseThrow());
    }

    @Test
    @DisplayName("buscarPorId: un id inexistente retorna Optional vacío")
    void buscarPorId_idInexistente_retornaVacio() {
        assertTrue(repositorio.buscarPorId("NO-EXISTE").isEmpty());
    }

    @Test
    @DisplayName("buscarPorId: un id nulo o en blanco lanza IllegalArgumentException")
    void buscarPorId_idInvalido_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> repositorio.buscarPorId(null));
        assertThrows(IllegalArgumentException.class, () -> repositorio.buscarPorId("  "));
    }

    @Test
    @DisplayName("guardar: una misión nula lanza IllegalArgumentException")
    void guardar_misionNula_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> repositorio.guardar(null));
    }

    @Test
    @DisplayName("guardar: con el mismo id reemplaza la misión anterior")
    void guardar_mismoId_reemplazaLaAnterior() {
        repositorio.guardar(mision("M-1", EstadoMision.PENDIENTE));

        repositorio.guardar(mision("M-1", EstadoMision.ENTREGADA));

        assertEquals(1, repositorio.listarTodas().size());
        assertEquals(EstadoMision.ENTREGADA, repositorio.buscarPorId("M-1").orElseThrow().estado());
    }

    @Test
    @DisplayName("listarTodas: conserva el orden de inserción")
    void listarTodas_variasMisiones_conservaElOrdenDeInsercion() {
        repositorio.guardar(mision("M-2", EstadoMision.PENDIENTE));
        repositorio.guardar(mision("M-1", EstadoMision.PENDIENTE));

        List<String> ids = repositorio.listarTodas().stream().map(Mision::id).toList();

        assertEquals(List.of("M-2", "M-1"), ids);
    }

    @Test
    @DisplayName("listarTodas: devuelve una copia inmodificable")
    void listarTodas_resultado_esInmodificable() {
        repositorio.guardar(mision("M-1", EstadoMision.PENDIENTE));
        List<Mision> lista = repositorio.listarTodas();
        Mision otra = mision("M-9", EstadoMision.PENDIENTE);

        assertThrows(UnsupportedOperationException.class, () -> lista.add(otra));
    }
}
