package validacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import testutil.Datos;

class ValidadorBateriaTest {

    @ParameterizedTest(name = "batería {0}% -> suficiente: {1}")
    @CsvSource({"29,false", "30,true", "31,true", "0,false", "100,true"})
    @DisplayName("esSuficiente: el borde está en 30%")
    void esSuficiente_valoresAlrededorDelBorde_respetaElMinimo(int bateria, boolean esperado) {
        assertEquals(esperado, ValidadorBateria.esSuficiente(bateria));
    }

    @Test
    @DisplayName("validar: batería suficiente aprueba la misión")
    void validar_bateriaSuficiente_aprueba() {
        ResultadoValidacion r = new ValidadorBateria().validar(Datos.mision(Datos.drone(30)));

        assertTrue(r.valido());
    }

    @Test
    @DisplayName("validar: batería insuficiente rechaza e informa porcentaje y mínimo")
    void validar_bateriaInsuficiente_rechazaConMotivo() {
        ResultadoValidacion r = new ValidadorBateria().validar(Datos.mision(Datos.drone(29)));

        assertFalse(r.valido());
        assertTrue(r.motivo().contains("29%"));
        assertTrue(r.motivo().contains("30%"));
    }
}
