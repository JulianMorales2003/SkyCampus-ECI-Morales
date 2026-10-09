package validacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import model.EstadoMision;
import model.Mision;
import model.TipoCarga;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import testutil.Datos;

class ValidadorEnCadenaTest {

    private static final List<String> DESTINOS = List.of("Biblioteca", "Bloque A");

    /** Stub que cuenta cuántas veces fue evaluado y devuelve un resultado fijo. */
    private static class Espia extends ValidadorEnCadena {
        private final ResultadoValidacion resultado;
        int evaluaciones = 0;

        Espia(ResultadoValidacion resultado) {
            this.resultado = resultado;
        }

        @Override
        protected ResultadoValidacion evaluar(Mision mision) {
            evaluaciones++;
            return resultado;
        }
    }

    @Test
    @DisplayName("validar: un validador sin siguiente retorna su propio resultado")
    void validar_sinSiguiente_retornaSuResultado() {
        Espia unico = new Espia(ResultadoValidacion.aprobada());

        ResultadoValidacion r = unico.validar(Datos.mision(Datos.drone(80)));

        assertTrue(r.valido());
        assertEquals(1, unico.evaluaciones);
    }

    @Test
    @DisplayName("validar: si todos aprueban, recorre toda la cadena y aprueba")
    void validar_todosAprueban_recorreLaCadenaCompleta() {
        Espia a = new Espia(ResultadoValidacion.aprobada());
        Espia b = new Espia(ResultadoValidacion.aprobada());
        Espia c = new Espia(ResultadoValidacion.aprobada());
        a.setSiguiente(b).setSiguiente(c);

        ResultadoValidacion r = a.validar(Datos.mision(Datos.drone(80)));

        assertTrue(r.valido());
        assertEquals(1, a.evaluaciones);
        assertEquals(1, b.evaluaciones);
        assertEquals(1, c.evaluaciones);
    }

    @Test
    @DisplayName("validar: si el primero rechaza, los siguientes no se evalúan")
    void validar_primeroRechaza_cortaLaCadena() {
        Espia a = new Espia(ResultadoValidacion.rechazada("falla A"));
        Espia b = new Espia(ResultadoValidacion.aprobada());
        a.setSiguiente(b);

        ResultadoValidacion r = a.validar(Datos.mision(Datos.drone(80)));

        assertFalse(r.valido());
        assertEquals("falla A", r.motivo());
        assertEquals(0, b.evaluaciones);
    }

    @Test
    @DisplayName("validar: si falla uno intermedio, retorna su motivo y no evalúa el último")
    void validar_falloIntermedio_retornaElMotivoDelIntermedio() {
        Espia a = new Espia(ResultadoValidacion.aprobada());
        Espia b = new Espia(ResultadoValidacion.rechazada("falla B"));
        Espia c = new Espia(ResultadoValidacion.aprobada());
        a.setSiguiente(b).setSiguiente(c);

        ResultadoValidacion r = a.validar(Datos.mision(Datos.drone(80)));

        assertEquals("falla B", r.motivo());
        assertEquals(0, c.evaluaciones);
    }

    @Test
    @DisplayName("validar: una misión nula lanza IllegalArgumentException")
    void validar_misionNula_lanzaExcepcion() {
        Espia a = new Espia(ResultadoValidacion.aprobada());

        assertThrows(IllegalArgumentException.class, () -> a.validar(null));
    }

    @Test
    @DisplayName("setSiguiente: retorna el validador recibido para poder encadenar")
    void setSiguiente_validadorValido_retornaElSiguiente() {
        Espia a = new Espia(ResultadoValidacion.aprobada());
        Espia b = new Espia(ResultadoValidacion.aprobada());

        assertSame(b, a.setSiguiente(b));
    }

    @Test
    @DisplayName("setSiguiente: nulo lanza IllegalArgumentException")
    void setSiguiente_nulo_lanzaExcepcion() {
        Espia a = new Espia(ResultadoValidacion.aprobada());

        assertThrows(IllegalArgumentException.class, () -> a.setSiguiente(null));
    }

    @Test
    @DisplayName("setSiguiente: encadenarse consigo mismo lanza IllegalArgumentException")
    void setSiguiente_asiMismo_lanzaExcepcion() {
        Espia a = new Espia(ResultadoValidacion.aprobada());

        assertThrows(IllegalArgumentException.class, () -> a.setSiguiente(a));
    }

    @Test
    @DisplayName("cadena real: batería -> carga -> destino aprueba una misión correcta")
    void cadenaReal_misionCorrecta_aprueba() {
        ValidadorEnCadena cadena = new ValidadorBateria();
        cadena.setSiguiente(new ValidadorCarga(500)).setSiguiente(new ValidadorDestino(DESTINOS));
        Mision mision = Datos.mision(Datos.drone(80), "Biblioteca", TipoCarga.CARPETA, EstadoMision.PENDIENTE);

        assertTrue(cadena.validar(mision).valido());
    }

    @Test
    @DisplayName("cadena real: con batería baja rechaza antes de revisar carga y destino")
    void cadenaReal_bateriaBaja_rechazaPorBateria() {
        ValidadorEnCadena cadena = new ValidadorBateria();
        cadena.setSiguiente(new ValidadorCarga(500)).setSiguiente(new ValidadorDestino(DESTINOS));
        Mision mision = Datos.mision(Datos.drone(10), "Destino falso", TipoCarga.LIBRO, EstadoMision.PENDIENTE);

        ResultadoValidacion r = cadena.validar(mision);

        assertFalse(r.valido());
        assertTrue(r.motivo().contains("Batería insuficiente"));
    }

    @Test
    @DisplayName("cadena real: con batería ok y carga excesiva rechaza por carga")
    void cadenaReal_cargaExcesiva_rechazaPorCarga() {
        ValidadorEnCadena cadena = new ValidadorBateria();
        cadena.setSiguiente(new ValidadorCarga(500)).setSiguiente(new ValidadorDestino(DESTINOS));
        Mision mision = Datos.mision(Datos.drone(80), "Biblioteca", TipoCarga.LIBRO, EstadoMision.PENDIENTE);

        ResultadoValidacion r = cadena.validar(mision);

        assertFalse(r.valido());
        assertTrue(r.motivo().contains("supera la capacidad"));
    }
}
