package v2.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static v2.testutil.DatosV2.AHORA;
import static v2.testutil.DatosV2.drone;
import static v2.testutil.DatosV2.solicitud;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import v2.asignacion.EstrategiaBateriaJusta;
import v2.asignacion.EstrategiaMayorBateria;
import v2.eventos.AlertaTecnico;
import v2.eventos.EventoFlota;
import v2.eventos.ObservadorFlota;
import v2.eventos.PanelOperador;
import v2.eventos.SistemaLog;
import v2.eventos.TipoEvento;
import v2.model.Drone;
import v2.model.EstadoMision;
import v2.model.Mision;
import v2.model.Prioridad;
import v2.model.SolicitudMision;
import v2.model.TipoDrone;

@DisplayName("GestorFlota (Strategy + Observer)")
class GestorFlotaTest {

    private final List<Drone> flota = List.of(drone("D-01", TipoDrone.MINI, 95), drone("D-02", TipoDrone.MINI, 40));
    private final SolicitudMision pedido = solicitud("M-1", 300, Prioridad.NORMAL);

    @Test
    @DisplayName("asignar_hayDrone_devuelveMisionEnVueloConElDroneDeLaEstrategia")
    void asignar_hayDrone_devuelveMisionEnVueloConElDroneDeLaEstrategia() {
        GestorFlota gestor = new GestorFlota(new EstrategiaMayorBateria());

        Mision mision = gestor.asignar(pedido, flota, AHORA).orElseThrow();

        assertEquals("D-01", mision.drone().id());
        assertEquals(EstadoMision.EN_VUELO, mision.estado());
        assertEquals("M-1", mision.id());
        assertEquals("Biblioteca", mision.destino());
        assertEquals(300, mision.pesoPaqueteGramos());
        assertEquals(Prioridad.NORMAL, mision.prioridad());
        assertEquals(AHORA, mision.creadaEn());
    }

    @Test
    @DisplayName("cambiarEstrategia_enTiempoDeEjecucion_cambiaElDroneElegido")
    void cambiarEstrategia_enTiempoDeEjecucion_cambiaElDroneElegido() {
        GestorFlota gestor = new GestorFlota(new EstrategiaMayorBateria());
        assertEquals("D-01", gestor.asignar(pedido, flota, AHORA).orElseThrow().drone().id());

        gestor.cambiarEstrategia(new EstrategiaBateriaJusta());

        assertEquals("D-02", gestor.asignar(pedido, flota, AHORA).orElseThrow().drone().id());
    }

    @Test
    @DisplayName("asignar_hayDrone_notificaMisionAsignadaATodosLosObservadores")
    void asignar_hayDrone_notificaMisionAsignadaATodosLosObservadores() {
        GestorFlota gestor = new GestorFlota(new EstrategiaMayorBateria());
        List<String> pantalla = new ArrayList<>();
        SistemaLog log = new SistemaLog();
        gestor.suscribir(new PanelOperador(pantalla::add));
        gestor.suscribir(log);

        gestor.asignar(pedido, flota, AHORA);

        assertEquals(1, pantalla.size());
        assertTrue(pantalla.get(0).contains("MISION_ASIGNADA"));
        assertTrue(pantalla.get(0).contains("D-01"));
        assertEquals(1, log.registros().size());
    }

    @Test
    @DisplayName("asignar_noHayDrone_devuelveVacioYNotificaSinDroneDisponible")
    void asignar_noHayDrone_devuelveVacioYNotificaSinDroneDisponible() {
        GestorFlota gestor = new GestorFlota(new EstrategiaMayorBateria());
        SistemaLog log = new SistemaLog();
        gestor.suscribir(log);

        Optional<Mision> resultado = gestor.asignar(pedido, List.of(), AHORA);

        assertTrue(resultado.isEmpty());
        assertEquals(1, log.registros().size());
        assertTrue(log.registros().get(0).contains("SIN_DRONE_DISPONIBLE"));
    }

    @Test
    @DisplayName("completar_misionEnVuelo_devuelveCompletadaYNotifica")
    void completar_misionEnVuelo_devuelveCompletadaYNotifica() {
        GestorFlota gestor = new GestorFlota(new EstrategiaMayorBateria());
        SistemaLog log = new SistemaLog();
        gestor.suscribir(log);
        Mision enVuelo = gestor.asignar(pedido, flota, AHORA).orElseThrow();

        Mision completada = gestor.completar(enVuelo, AHORA);

        assertEquals(EstadoMision.COMPLETADA, completada.estado());
        assertEquals(enVuelo.id(), completada.id());
        assertEquals(enVuelo.drone(), completada.drone());
        assertEquals(2, log.registros().size());
        assertTrue(log.registros().get(1).contains("MISION_COMPLETADA"));
    }

    @Test
    @DisplayName("reportarFallo_droneConFallo_soloAlertaTecnicoReaccionaConAlerta")
    void reportarFallo_droneConFallo_soloAlertaTecnicoReaccionaConAlerta() {
        GestorFlota gestor = new GestorFlota(new EstrategiaMayorBateria());
        List<String> canal = new ArrayList<>();
        AlertaTecnico tecnico = new AlertaTecnico(canal::add);
        SistemaLog log = new SistemaLog();
        gestor.suscribir(tecnico);
        gestor.suscribir(log);

        gestor.reportarFallo(flota.get(0), AHORA);
        gestor.asignar(pedido, flota, AHORA);

        assertEquals(1, tecnico.alertasEnviadas());
        assertTrue(canal.get(0).contains("D-01"));
        assertEquals(2, log.registros().size());
    }

    @Test
    @DisplayName("agregarCuartoObservador_sinTocarElGestor_recibeLosEventos")
    void agregarCuartoObservador_sinTocarElGestor_recibeLosEventos() {
        GestorFlota gestor = new GestorFlota(new EstrategiaMayorBateria());
        SistemaLog log = new SistemaLog();
        gestor.suscribir(new PanelOperador(linea -> { }));
        gestor.suscribir(log);
        gestor.suscribir(new AlertaTecnico(linea -> { }));
        List<TipoEvento> contador = new ArrayList<>();
        ObservadorFlota cuarto = evento -> contador.add(evento.tipo());
        gestor.suscribir(cuarto);

        gestor.asignar(pedido, flota, AHORA);
        gestor.reportarFallo(flota.get(1), AHORA);

        assertEquals(List.of(TipoEvento.MISION_ASIGNADA, TipoEvento.FALLO_DRONE), contador);
        assertEquals(2, log.registros().size());
    }

    @Test
    @DisplayName("cancelar_observadorCancelado_dejaDeRecibirEventos")
    void cancelar_observadorCancelado_dejaDeRecibirEventos() {
        GestorFlota gestor = new GestorFlota(new EstrategiaMayorBateria());
        SistemaLog log = new SistemaLog();
        gestor.suscribir(log);
        gestor.asignar(pedido, flota, AHORA);

        gestor.cancelar(log);
        gestor.asignar(pedido, flota, AHORA);

        assertEquals(1, log.registros().size());
    }

    @Test
    @DisplayName("notificar_observadorQueSeCancelaDuranteLaNotificacion_noRompeLaIteracion")
    void notificar_observadorQueSeCancelaDuranteLaNotificacion_noRompeLaIteracion() {
        GestorFlota gestor = new GestorFlota(new EstrategiaMayorBateria());
        SistemaLog log = new SistemaLog();
        ObservadorFlota[] unaVez = new ObservadorFlota[1];
        unaVez[0] = evento -> gestor.cancelar(unaVez[0]);
        gestor.suscribir(unaVez[0]);
        gestor.suscribir(log);

        gestor.asignar(pedido, flota, AHORA);

        assertEquals(1, log.registros().size());
    }

    @Test
    @DisplayName("gestor_argumentosNulos_lanzaIllegalArgumentException")
    void gestor_argumentosNulos_lanzaIllegalArgumentException() {
        GestorFlota gestor = new GestorFlota(new EstrategiaMayorBateria());
        Mision mision = gestor.asignar(pedido, flota, AHORA).orElseThrow();
        Drone drone = flota.get(0);

        assertThrows(IllegalArgumentException.class, () -> new GestorFlota(null));
        assertThrows(IllegalArgumentException.class, () -> gestor.cambiarEstrategia(null));
        assertThrows(IllegalArgumentException.class, () -> gestor.suscribir(null));
        assertThrows(IllegalArgumentException.class, () -> gestor.asignar(null, flota, AHORA));
        assertThrows(IllegalArgumentException.class, () -> gestor.asignar(pedido, null, AHORA));
        assertThrows(IllegalArgumentException.class, () -> gestor.asignar(pedido, flota, null));
        assertThrows(IllegalArgumentException.class, () -> gestor.completar(null, AHORA));
        assertThrows(IllegalArgumentException.class, () -> gestor.completar(mision, null));
        assertThrows(IllegalArgumentException.class, () -> gestor.reportarFallo(null, AHORA));
        assertThrows(IllegalArgumentException.class, () -> gestor.reportarFallo(drone, null));
    }

    @Test
    @DisplayName("solicitudMision_datosInvalidos_lanzaIllegalArgumentException")
    void solicitudMision_datosInvalidos_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new SolicitudMision(" ", "Biblioteca", 100, Prioridad.NORMAL));
        assertThrows(IllegalArgumentException.class, () -> new SolicitudMision("M-1", null, 100, Prioridad.NORMAL));
        assertThrows(IllegalArgumentException.class, () -> new SolicitudMision("M-1", "Biblioteca", 0, Prioridad.NORMAL));
        assertThrows(IllegalArgumentException.class, () -> new SolicitudMision("M-1", "Biblioteca", 100, null));
    }
}
