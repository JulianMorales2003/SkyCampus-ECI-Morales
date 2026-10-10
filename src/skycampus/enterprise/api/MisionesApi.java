package skycampus.enterprise.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import skycampus.enterprise.aplicacion.ComandoMision;
import skycampus.enterprise.aplicacion.CrearMision;
import skycampus.enterprise.aplicacion.MotivoMision;
import skycampus.enterprise.aplicacion.PrioridadMision;
import skycampus.enterprise.aplicacion.ResultadoMision;
import skycampus.enterprise.dominio.Drone;

/**
 * Adaptador de entrada REST: {@code POST /api/v3/misiones}. Usa solo el servidor HTTP del JDK,
 * así la aplicación y el dominio siguen sin depender de ningún framework (RNF-11).
 *
 * <p>Respuestas: 201 misión creada; 400 datos inválidos; 403 la Aerocivil no autoriza o el vuelo
 * excede sus límites; 409 clima, sin drone o sede inactiva; 422 paquete muy pesado; 503 la Aerocivil no responde.
 */
public final class MisionesApi {

    public static final String RUTA = "/api/v3/misiones";

    private static final Pattern CAMPO = Pattern.compile("\"(\\w+)\"\\s*:\\s*(?:\"([^\"\\\\]*)\"|(-?\\d+(?:\\.\\d+)?))");
    private static final Map<MotivoMision, Integer> ESTADO_HTTP = Map.of(
            MotivoMision.SEDE_INACTIVA, 409,
            MotivoMision.PAQUETE_EXCEDE_PESO, 422,
            MotivoMision.RUTA_NO_DEFINIDA, 422,
            MotivoMision.CLIMA_ADVERSO, 409,
            MotivoMision.SIN_DRONE_DISPONIBLE, 409,
            MotivoMision.SIN_RESTRICCION_VIGENTE, 403,
            MotivoMision.ALTURA_EXCEDIDA, 403,
            MotivoMision.RADIO_EXCEDIDO, 403,
            MotivoMision.AEROCIVIL_RECHAZA, 403,
            MotivoMision.AEROCIVIL_NO_RESPONDE, 503);

    private final CrearMision crearMision;
    private final HttpServer servidor;

    public MisionesApi(CrearMision crearMision, int puerto) throws IOException {
        this.crearMision = Objects.requireNonNull(crearMision, "crearMision");
        this.servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", puerto), 0);
        this.servidor.createContext("/", this::atender);
    }

    public void iniciar() {
        servidor.start();
    }

    public int puerto() {
        return servidor.getAddress().getPort();
    }

    public void detener() {
        servidor.stop(0);
    }

    private void atender(HttpExchange intercambio) throws IOException {
        try (intercambio) {
            if (!RUTA.equals(intercambio.getRequestURI().getPath())) {
                responder(intercambio, 404, error("NO_ENCONTRADO", "La ruta no existe."));
            } else if (!"POST".equals(intercambio.getRequestMethod())) {
                intercambio.getResponseHeaders().add("Allow", "POST");
                responder(intercambio, 405, error("METODO_NO_PERMITIDO", "Solo se acepta POST."));
            } else {
                String cuerpo = new String(intercambio.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                procesar(intercambio, cuerpo);
            }
        }
    }

    private void procesar(HttpExchange intercambio, String cuerpo) throws IOException {
        ComandoMision comando;
        try {
            comando = leerComando(cuerpo);
        } catch (IllegalArgumentException e) {
            responder(intercambio, 400, error("DATOS_INVALIDOS", e.getMessage()));
            return;
        }
        ResultadoMision resultado = crearMision.crear(comando);
        if (resultado.creada()) {
            responder(intercambio, 201, creada(resultado));
        } else {
            MotivoMision motivo = resultado.motivo();
            responder(intercambio, ESTADO_HTTP.get(motivo), error(motivo.name(), motivo.mensaje()));
        }
    }

    static ComandoMision leerComando(String cuerpo) {
        String texto = cuerpo.strip();
        if (!texto.startsWith("{") || !texto.endsWith("}")) {
            throw new IllegalArgumentException("El cuerpo debe ser un objeto JSON.");
        }
        Map<String, String> campos = new HashMap<>();
        Matcher m = CAMPO.matcher(texto);
        while (m.find()) {
            campos.put(m.group(1), m.group(2) != null ? m.group(2) : m.group(3));
        }
        String peso = campos.get("pesoPaquete");
        if (peso == null) {
            throw new IllegalArgumentException("El campo pesoPaquete es obligatorio.");
        }
        try {
            return new ComandoMision(campos.get("origen"), campos.get("destino"), Integer.parseInt(peso),
                    prioridad(campos.get("prioridad")));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El campo pesoPaquete debe ser un entero en gramos.", e);
        }
    }

    private static PrioridadMision prioridad(String texto) {
        if (texto == null) {
            return PrioridadMision.NORMAL;
        }
        try {
            return PrioridadMision.valueOf(texto);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("La prioridad debe ser NORMAL o URGENTE.", e);
        }
    }

    private static String creada(ResultadoMision resultado) {
        Drone drone = resultado.drone();
        return "{\"id\":\"" + resultado.misionId() + "\",\"droneAsignado\":{\"id\":\"" + drone.id()
                + "\",\"sede\":\"" + drone.sede() + "\",\"bateria\":" + drone.bateria()
                + "},\"estado\":\"EN_VUELO\"}";
    }

    private static String error(String codigo, String mensaje) {
        return "{\"error\":\"" + codigo + "\",\"mensaje\":\"" + mensaje.replace("\"", "'") + "\"}";
    }

    private static void responder(HttpExchange intercambio, int estado, String cuerpo) throws IOException {
        byte[] bytes = cuerpo.getBytes(StandardCharsets.UTF_8);
        intercambio.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        intercambio.sendResponseHeaders(estado, bytes.length);
        intercambio.getResponseBody().write(bytes);
    }
}
