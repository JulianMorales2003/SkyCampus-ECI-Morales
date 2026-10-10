package ruta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RutaDirectaTest {

    private final RutaDirecta ruta = new RutaDirecta();

    @Test
    @DisplayName("calcular: la ruta directa es origen -> destino")
    void calcular_origenYDestino_retornaDosPuntos() {
        assertEquals(List.of("Bloque A", "Biblioteca"), ruta.calcular("Bloque A", "Biblioteca"));
    }

    @Test
    @DisplayName("calcular: origen o destino nulos o en blanco lanzan IllegalArgumentException")
    void calcular_argumentosInvalidos_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> ruta.calcular(null, "B"));
        assertThrows(IllegalArgumentException.class, () -> ruta.calcular(" ", "B"));
        assertThrows(IllegalArgumentException.class, () -> ruta.calcular("A", null));
        assertThrows(IllegalArgumentException.class, () -> ruta.calcular("A", ""));
    }
}
