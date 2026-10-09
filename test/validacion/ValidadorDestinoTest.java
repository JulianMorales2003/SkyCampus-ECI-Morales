package validacion;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import model.EstadoMision;
import model.TipoCarga;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import testutil.Datos;

class ValidadorDestinoTest {

    private static final List<String> DESTINOS = List.of("Bloque A", "Biblioteca");

    private final ValidadorDestino validador = new ValidadorDestino(DESTINOS);

    @Test
    @DisplayName("esValido: un destino de la lista es válido")
    void esValido_destinoDeLaLista_retornaTrue() {
        assertTrue(validador.esValido("Biblioteca"));
    }

    @Test
    @DisplayName("esValido: un destino fuera de la lista no es válido")
    void esValido_destinoFueraDeLaLista_retornaFalse() {
        assertFalse(validador.esValido("Edificio Inexistente"));
    }

    @Test
    @DisplayName("mensajeDeRechazo: incluye el destino y la lista de válidos")
    void mensajeDeRechazo_destino_incluyeDestinoYValidos() {
        String mensaje = validador.mensajeDeRechazo("Bloque Z");

        assertTrue(mensaje.contains("Bloque Z"));
        assertTrue(mensaje.contains("Biblioteca"));
    }

    @Test
    @DisplayName("validar: una misión a un destino válido se aprueba")
    void validar_destinoValido_aprueba() {
        var mision = Datos.mision(Datos.drone(80), "Bloque A", TipoCarga.SOBRE, EstadoMision.PENDIENTE);

        assertTrue(validador.validar(mision).valido());
    }

    @Test
    @DisplayName("validar: una misión a un destino inexistente se rechaza con motivo")
    void validar_destinoInexistente_rechazaConMotivo() {
        var mision = Datos.mision(Datos.drone(80), "Bloque Z", TipoCarga.SOBRE, EstadoMision.PENDIENTE);

        ResultadoValidacion r = validador.validar(mision);

        assertFalse(r.valido());
        assertTrue(r.motivo().contains("Bloque Z"));
    }

    @Test
    @DisplayName("constructor: una lista nula o vacía lanza IllegalArgumentException")
    void constructor_listaNulaOVacia_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> new ValidadorDestino(null));
        assertThrows(IllegalArgumentException.class, () -> new ValidadorDestino(List.of()));
    }

    @Test
    @DisplayName("constructor: copia defensiva, cambiar la lista original no afecta al validador")
    void constructor_listaMutable_haceCopiaDefensiva() {
        List<String> original = new ArrayList<>(List.of("Bloque A"));
        ValidadorDestino v = new ValidadorDestino(original);

        original.add("Bloque Z");

        assertFalse(v.esValido("Bloque Z"));
    }
}
