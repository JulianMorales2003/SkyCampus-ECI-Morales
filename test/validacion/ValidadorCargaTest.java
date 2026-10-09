package validacion;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import model.EstadoMision;
import model.TipoCarga;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import testutil.Datos;

class ValidadorCargaTest {

    private static ResultadoValidacion validar(int capacidad, TipoCarga carga) {
        return new ValidadorCarga(capacidad)
                .validar(Datos.mision(Datos.drone(80), "Biblioteca", carga, EstadoMision.PENDIENTE));
    }

    @Test
    @DisplayName("validar: una carga más liviana que la capacidad se aprueba")
    void validar_cargaMenorALaCapacidad_aprueba() {
        assertTrue(validar(500, TipoCarga.SOBRE).valido());
    }

    @Test
    @DisplayName("validar: una carga exactamente igual a la capacidad se aprueba")
    void validar_cargaIgualALaCapacidad_aprueba() {
        assertTrue(validar(300, TipoCarga.CARPETA).valido());
    }

    @Test
    @DisplayName("validar: una carga mayor que la capacidad se rechaza e indica el motivo")
    void validar_cargaMayorALaCapacidad_rechazaConMotivo() {
        ResultadoValidacion r = validar(500, TipoCarga.LIBRO);

        assertFalse(r.valido());
        assertTrue(r.motivo().contains("LIBRO"));
        assertTrue(r.motivo().contains("800"));
    }

    @ParameterizedTest(name = "capacidad {0} -> IllegalArgumentException")
    @ValueSource(ints = {0, -5})
    @DisplayName("constructor: una capacidad menor o igual a cero lanza IllegalArgumentException")
    void constructor_capacidadNoPositiva_lanzaExcepcion(int capacidad) {
        assertThrows(IllegalArgumentException.class, () -> new ValidadorCarga(capacidad));
    }
}
