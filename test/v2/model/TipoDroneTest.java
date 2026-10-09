package v2.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("TipoDrone (peso admitido por tipo)")
class TipoDroneTest {

    @ParameterizedTest(name = "{0} con {1} g -> admite: {2}")
    @CsvSource({"MINI,1,true", "MINI,1000,true", "MINI,1001,false", "MINI,0,false",
            "CARGO,99,false", "CARGO,100,true", "CARGO,2000,true", "CARGO,2001,false",
            "EXPRESS,1000,true", "EXPRESS,1001,false"})
    @DisplayName("admitePeso_limites_respetaMinimoYCapacidadIncluidos")
    void admitePeso_limites_respetaMinimoYCapacidadIncluidos(TipoDrone tipo, int gramos, boolean esperado) {
        assertEquals(esperado, tipo.admitePeso(gramos));
    }
}
