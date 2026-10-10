package skycampus.enterprise.infraestructura;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import skycampus.enterprise.dominio.RepositorioFlota;
import skycampus.enterprise.dominio.ServicioClima;

@DisplayName("Adaptadores de infraestructura (esqueletos)")
class AdaptadoresInfraestructuraTest {

    @Test
    @DisplayName("implementan los puertos del dominio")
    void implementanLosPuertos() {
        assertInstanceOf(RepositorioFlota.class, new RepositorioFlotaJPA());
        assertInstanceOf(ServicioClima.class, new ServicioClimaOpenWeather());
    }

    @Test
    @DisplayName("hasta integrar JPA y HTTP fallan de forma explícita en vez de inventar datos")
    void sinIntegrar_fallanConExcepcionClara() {
        RepositorioFlota repositorio = new RepositorioFlotaJPA();
        ServicioClima clima = new ServicioClimaOpenWeather();

        assertThrows(UnsupportedOperationException.class, () -> repositorio.findDisponibles("ECI"));
        assertThrows(UnsupportedOperationException.class, () -> clima.condicionesAptas("ECI", "UNAL"));
    }
}
