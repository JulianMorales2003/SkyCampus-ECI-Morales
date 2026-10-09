package model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TipoCargaTest {

    @ParameterizedTest(name = "{0} pesa {1} g")
    @CsvSource({"SOBRE,50", "CARPETA,300", "LIBRO,800"})
    @DisplayName("pesoGramos: cada tipo de carga expone su peso")
    void pesoGramos_cadaTipo_retornaSuPeso(TipoCarga tipo, int esperado) {
        assertEquals(esperado, tipo.pesoGramos());
    }
}
