package skycampus.enterprise.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import skycampus.enterprise.aplicacion.AutorizadorVuelo;
import skycampus.enterprise.aplicacion.CrearMision;
import skycampus.enterprise.aplicacion.EstrategiaMayorBateria;
import skycampus.enterprise.dominio.Drone;
import skycampus.enterprise.dominio.PlanVuelo;
import skycampus.enterprise.dominio.PoliticaVuelo;
import skycampus.enterprise.dominio.RestriccionAerea;
import skycampus.enterprise.dominio.ServicioAerocivil;
import skycampus.enterprise.dominio.ServicioClima;
import skycampus.enterprise.infraestructura.CatalogoSedesEnMemoria;
import skycampus.enterprise.infraestructura.RepositorioFlotaEnMemoria;

/**
 * Capa 3 (E2E simulado): una petición HTTP real al endpoint {@code POST /api/v3/misiones}, que atraviesa
 * API, aplicación, dominio y adaptadores en memoria. Solo el clima y la Aerocivil son dobles de Mockito.
 */
@DisplayName("POST /api/v3/misiones · extremo a extremo simulado (capa 3)")
class MisionesApiTest {

    private ServicioClima clima;
    private ServicioAerocivil aerocivil;
    private MisionesApi api;
    private HttpClient cliente;
    private String url;

    @BeforeEach
    void levantarServidor() throws IOException {
        clima = mock(ServicioClima.class);
        aerocivil = mock(ServicioAerocivil.class);
        CatalogoSedesEnMemoria sedes = new CatalogoSedesEnMemoria()
                .activar("ECI").activar("UNAL").activar("UNIANDES").activar("EAFIT")
                .distancia("ECI", "UNAL", 9).distancia("ECI", "UNIANDES", 45);
        RepositorioFlotaEnMemoria flota = new RepositorioFlotaEnMemoria(List.of(
                new Drone("ECI-D1", "ECI", 50), new Drone("ECI-D2", "ECI", 90), new Drone("UNAL-D1", "UNAL", 20)));
        PoliticaVuelo politica = new PoliticaVuelo(30);
        politica.registrarRestriccion("ECI", new RestriccionAerea(50, 120));
        politica.registrarRestriccion("UNAL", new RestriccionAerea(50, 120));
        AtomicInteger ids = new AtomicInteger();
        CrearMision caso = new CrearMision(sedes, flota, clima, new EstrategiaMayorBateria(), evento -> { },
                new AutorizadorVuelo(politica, aerocivil), () -> "M-" + ids.incrementAndGet());
        when(clima.condicionesAptas("ECI", "UNAL")).thenReturn(true);
        when(aerocivil.autoriza(any(PlanVuelo.class))).thenReturn(true);

        api = new MisionesApi(caso, 0);
        api.iniciar();
        cliente = HttpClient.newHttpClient();
        url = "http://127.0.0.1:" + api.puerto() + MisionesApi.RUTA;
    }

    @AfterEach
    void apagarServidor() {
        api.detener();
    }

    private static String json(String origen, String destino, String peso, String prioridad) {
        String cuerpo = "{\"origen\":\"" + origen + "\",\"destino\":\"" + destino + "\",\"pesoPaquete\":" + peso;
        return prioridad == null ? cuerpo + "}" : cuerpo + ",\"prioridad\":\"" + prioridad + "\"}";
    }

    private HttpResponse<String> post(String cuerpo) throws Exception {
        return cliente.send(HttpRequest.newBuilder(URI.create(url)).header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(cuerpo)).build(), BodyHandlers.ofString());
    }

    @Test
    @DisplayName("misión válida: 201 con el drone asignado y estado EN_VUELO")
    void crearMision_climaApto_retornaCreadaConDroneAsignado() throws Exception {
        HttpResponse<String> r = post(json("ECI", "UNAL", "300", "NORMAL"));

        assertEquals(201, r.statusCode());
        assertTrue(r.headers().firstValue("Content-Type").orElse("").startsWith("application/json"));
        assertTrue(r.body().contains("\"droneAsignado\":{\"id\":\"ECI-D2\""), r.body());
        assertTrue(r.body().contains("\"estado\":\"EN_VUELO\""), r.body());
        assertTrue(r.body().contains("\"id\":\"M-1\""), r.body());
    }

    @Test
    @DisplayName("sin prioridad en el cuerpo se toma NORMAL")
    void prioridadOmitida_esNormal() throws Exception {
        assertEquals(201, post(json("ECI", "UNAL", "300", null)).statusCode());
    }

    @Test
    @DisplayName("flujo alterno · clima adverso: 409 CLIMA_ADVERSO")
    void climaAdverso_409() throws Exception {
        when(clima.condicionesAptas("ECI", "UNAL")).thenReturn(false);

        HttpResponse<String> r = post(json("ECI", "UNAL", "300", "NORMAL"));

        assertEquals(409, r.statusCode());
        assertTrue(r.body().contains("CLIMA_ADVERSO"), r.body());
        verify(aerocivil, never()).autoriza(any(PlanVuelo.class));
    }

    @Test
    @DisplayName("flujo alterno · sin drones disponibles: 409 SIN_DRONE_DISPONIBLE")
    void sinDrones_409() throws Exception {
        when(clima.condicionesAptas("UNAL", "ECI")).thenReturn(true);

        HttpResponse<String> r = post(json("UNAL", "ECI", "300", "NORMAL"));

        assertEquals(409, r.statusCode());
        assertTrue(r.body().contains("SIN_DRONE_DISPONIBLE"), r.body());
    }

    @Test
    @DisplayName("flujo alterno · paquete pesado: 422 PAQUETE_EXCEDE_PESO")
    void paquetePesado_422() throws Exception {
        HttpResponse<String> r = post(json("ECI", "UNAL", "2001", "NORMAL"));

        assertEquals(422, r.statusCode());
        assertTrue(r.body().contains("PAQUETE_EXCEDE_PESO"), r.body());
    }

    @Test
    @DisplayName("flujo alterno · la Aerocivil rechaza: 403 AEROCIVIL_RECHAZA")
    void aerocivilRechaza_403() throws Exception {
        when(aerocivil.autoriza(any(PlanVuelo.class))).thenReturn(false);

        HttpResponse<String> r = post(json("ECI", "UNAL", "300", "URGENTE"));

        assertEquals(403, r.statusCode());
        assertTrue(r.body().contains("AEROCIVIL_RECHAZA"), r.body());
    }

    @Test
    @DisplayName("flujo alterno · sede inactiva: 409 SEDE_INACTIVA")
    void sedeInactiva_409() throws Exception {
        HttpResponse<String> r = post(json("ECI", "MARTE", "300", "NORMAL"));

        assertEquals(409, r.statusCode());
        assertTrue(r.body().contains("SEDE_INACTIVA"), r.body());
    }

    @Test
    @DisplayName("la Aerocivil no responde: 503 y el vuelo no despega")
    void aerocivilNoResponde_503() throws Exception {
        when(aerocivil.autoriza(any(PlanVuelo.class))).thenThrow(new IllegalStateException("timeout"));

        HttpResponse<String> r = post(json("ECI", "UNAL", "300", "NORMAL"));

        assertEquals(503, r.statusCode());
        assertTrue(r.body().contains("AEROCIVIL_NO_RESPONDE"), r.body());
    }

    @Test
    @DisplayName("ruta más larga que el radio permitido: 403 RADIO_EXCEDIDO")
    void radioExcedido_403() throws Exception {
        when(clima.condicionesAptas("ECI", "UNIANDES")).thenReturn(true);

        HttpResponse<String> r = post(json("ECI", "UNIANDES", "300", "NORMAL"));

        assertEquals(403, r.statusCode());
        assertTrue(r.body().contains("RADIO_EXCEDIDO"), r.body());
    }

    @Test
    @DisplayName("sedes activas sin ruta definida: 422 RUTA_NO_DEFINIDA")
    void rutaNoDefinida_422() throws Exception {
        HttpResponse<String> r = post(json("ECI", "EAFIT", "300", "NORMAL"));

        assertEquals(422, r.statusCode());
        assertTrue(r.body().contains("RUTA_NO_DEFINIDA"), r.body());
    }

    @Test
    @DisplayName("cuerpos inválidos: 400 DATOS_INVALIDOS, sin tocar el caso de uso")
    void datosInvalidos_400() throws Exception {
        String[] cuerpos = {
            "hola",                                             // no es JSON
            "{\"origen\":\"ECI\"",                              // JSON sin cerrar
            "{\"origen\":\"ECI\",\"destino\":\"UNAL\"}",         // falta el peso
            json("ECI", "UNAL", "\"abc\"", "NORMAL"),            // peso no numérico
            json("ECI", "UNAL", "300.5", "NORMAL"),              // peso con decimales
            json("ECI", "UNAL", "300", "YA"),                    // prioridad desconocida
            json("ECI", "ECI", "300", "NORMAL"),                 // origen igual a destino
            json("ECI", "UNAL", "0", "NORMAL"),                  // peso cero
            "{\"destino\":\"UNAL\",\"pesoPaquete\":300}"         // falta el origen
        };
        for (String cuerpo : cuerpos) {
            HttpResponse<String> r = post(cuerpo);
            assertEquals(400, r.statusCode(), cuerpo);
            assertTrue(r.body().contains("DATOS_INVALIDOS"), cuerpo);
        }
        verify(aerocivil, never()).autoriza(any(PlanVuelo.class));
    }

    @Test
    @DisplayName("GET no está permitido: 405 con el encabezado Allow")
    void get_405() throws Exception {
        HttpResponse<String> r = cliente.send(HttpRequest.newBuilder(URI.create(url)).GET().build(), BodyHandlers.ofString());

        assertEquals(405, r.statusCode());
        assertEquals("POST", r.headers().firstValue("Allow").orElse(""));
    }

    @Test
    @DisplayName("otra ruta: 404")
    void otraRuta_404() throws Exception {
        HttpResponse<String> r = cliente.send(HttpRequest.newBuilder(URI.create(url + "/x"))
                .POST(BodyPublishers.ofString("{}")).build(), BodyHandlers.ofString());

        assertEquals(404, r.statusCode());
    }

    @Test
    @DisplayName("el adaptador exige el caso de uso")
    void constructor_exigeCasoDeUso() {
        assertThrows(NullPointerException.class, () -> new MisionesApi(null, 0));
    }
}
