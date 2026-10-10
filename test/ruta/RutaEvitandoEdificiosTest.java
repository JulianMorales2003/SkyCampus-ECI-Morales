package ruta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RutaEvitandoEdificiosTest {

    private final RutaEvitandoEdificios ruta = new RutaEvitandoEdificios();

    @Test
    @DisplayName("calcular: agrega el corredor aéreo libre entre origen y destino")
    void calcular_origenYDestino_incluyeElPuntoDeDesvio() {
        assertEquals(List.of("Bloque A", "Corredor aéreo libre", "Biblioteca"),
                ruta.calcular("Bloque A", "Biblioteca"));
    }

    @Test
    @DisplayName("calcular: origen o destino nulos o en blanco lanzan IllegalArgumentException")
    void calcular_argumentosInvalidos_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> ruta.calcular(null, "B"));
        assertThrows(IllegalArgumentException.class, () -> ruta.calcular("A", "  "));
    }

    @Test
    @DisplayName("calcular: origen igual al destino lanza IllegalArgumentException")
    void calcular_origenIgualAlDestino_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> ruta.calcular("Biblioteca", "Biblioteca"));
    }
}
