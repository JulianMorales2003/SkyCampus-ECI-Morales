package skycampus.enterprise.infraestructura;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import skycampus.enterprise.dominio.Drone;

@DisplayName("Adaptadores en memoria")
class AdaptadoresEnMemoriaTest {

    @Test
    @DisplayName("el repositorio devuelve solo los drones de la sede pedida")
    void repositorio_filtraPorSede() {
        RepositorioFlotaEnMemoria repo = new RepositorioFlotaEnMemoria(List.of(
                new Drone("E-1", "ECI", 80), new Drone("U-1", "UNAL", 70)));

        assertEquals(List.of(new Drone("E-1", "ECI", 80)), repo.findDisponibles("ECI"));
        assertTrue(repo.findDisponibles("EAFIT").isEmpty());
    }

    @Test
    @DisplayName("el repositorio guarda una copia: cambiar la lista original no lo afecta")
    void repositorio_guardaCopia() {
        List<Drone> original = new ArrayList<>(List.of(new Drone("E-1", "ECI", 80)));
        RepositorioFlotaEnMemoria repo = new RepositorioFlotaEnMemoria(original);

        original.clear();

        assertEquals(1, repo.findDisponibles("ECI").size());
    }

    @Test
    @DisplayName("el catálogo activa y desactiva sedes; una sede desconocida está inactiva")
    void catalogo_activaYDesactiva() {
        CatalogoSedesEnMemoria catalogo = new CatalogoSedesEnMemoria().activar("ECI");

        assertTrue(catalogo.estaActiva("ECI"));
        assertFalse(catalogo.estaActiva("UNAL"));
        catalogo.desactivar("ECI");
        assertFalse(catalogo.estaActiva("ECI"));
    }

    @Test
    @DisplayName("la distancia vale en los dos sentidos y es vacía si no está definida")
    void catalogo_distanciaSimetrica() {
        CatalogoSedesEnMemoria catalogo = new CatalogoSedesEnMemoria().distancia("UNAL", "ECI", 9);

        assertEquals(OptionalDouble.of(9), catalogo.distanciaKm("ECI", "UNAL"));
        assertEquals(OptionalDouble.of(9), catalogo.distanciaKm("UNAL", "ECI"));
        assertEquals(OptionalDouble.empty(), catalogo.distanciaKm("ECI", "EAFIT"));
    }

    @Test
    @DisplayName("una distancia no positiva se rechaza")
    void catalogo_distanciaInvalida() {
        CatalogoSedesEnMemoria catalogo = new CatalogoSedesEnMemoria();

        assertThrows(IllegalArgumentException.class, () -> catalogo.distancia("ECI", "UNAL", 0));
        assertThrows(IllegalArgumentException.class, () -> catalogo.distancia("ECI", "UNAL", -3));
    }
}
