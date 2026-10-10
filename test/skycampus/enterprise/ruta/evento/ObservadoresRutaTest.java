package skycampus.enterprise.ruta.evento;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Observer: alertas de cada etapa")
class ObservadoresRutaTest {

    private static EventoRuta evento(TipoEventoRuta tipo) {
        return new EventoRuta(tipo, "Tramo A -> B", "detalle");
    }

    @Test
    @DisplayName("publicar_variosObservadores_todosRecibenElMismoEvento")
    void publicar_variosObservadores_todosRecibenElMismoEvento() {
        NotificadorRuta notificador = new NotificadorRuta();
        RegistroDeEtapas primero = new RegistroDeEtapas();
        RegistroDeEtapas segundo = new RegistroDeEtapas();
        notificador.suscribir(primero);
        notificador.suscribir(segundo);

        EventoRuta iniciada = evento(TipoEventoRuta.ETAPA_INICIADA);
        notificador.publicar(iniciada);

        assertEquals(List.of(iniciada), primero.eventos());
        assertEquals(List.of(iniciada), segundo.eventos());
    }

    @Test
    @DisplayName("publicar_sinObservadores_noFalla")
    void publicar_sinObservadores_noFalla() {
        NotificadorRuta notificador = new NotificadorRuta();
        EventoRuta completada = evento(TipoEventoRuta.ETAPA_COMPLETADA);

        notificador.publicar(completada);

        assertTrue(new RegistroDeEtapas().eventos().isEmpty());
    }

    @Test
    @DisplayName("alertaFalloDeEtapa_soloAvisaLasEtapasFallidas")
    void alertaFalloDeEtapa_soloAvisaLasEtapasFallidas() {
        List<String> avisos = new ArrayList<>();
        AlertaFalloDeEtapa alerta = new AlertaFalloDeEtapa(avisos::add);

        alerta.alOcurrir(evento(TipoEventoRuta.ETAPA_INICIADA));
        alerta.alOcurrir(evento(TipoEventoRuta.ETAPA_COMPLETADA));
        alerta.alOcurrir(evento(TipoEventoRuta.ETAPA_FALLIDA));

        assertEquals(List.of("ALERTA: Tramo A -> B - detalle"), avisos);
    }

    @Test
    @DisplayName("registroDeEtapas_eventos_devuelveUnaCopiaQueNoSePuedeModificar")
    void registroDeEtapas_eventos_devuelveUnaCopiaQueNoSePuedeModificar() {
        RegistroDeEtapas registro = new RegistroDeEtapas();
        registro.alOcurrir(evento(TipoEventoRuta.ETAPA_INICIADA));
        List<EventoRuta> copia = registro.eventos();
        EventoRuta otro = evento(TipoEventoRuta.ETAPA_COMPLETADA);

        assertThrows(UnsupportedOperationException.class, () -> copia.add(otro));
    }

    @Test
    @DisplayName("datosInvalidos_lanzaExcepcion")
    void datosInvalidos_lanzaExcepcion() {
        NotificadorRuta notificador = new NotificadorRuta();

        assertThrows(IllegalArgumentException.class, () -> notificador.suscribir(null));
        assertThrows(IllegalArgumentException.class, () -> notificador.publicar(null));
        assertThrows(IllegalArgumentException.class, () -> new AlertaFalloDeEtapa(null));
        assertThrows(IllegalArgumentException.class, () -> new EventoRuta(null, "etapa", ""));
        assertThrows(IllegalArgumentException.class, () -> new EventoRuta(TipoEventoRuta.ETAPA_INICIADA, " ", ""));
        assertThrows(IllegalArgumentException.class, () -> new EventoRuta(TipoEventoRuta.ETAPA_INICIADA, "etapa", null));
    }
}
