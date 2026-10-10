package app;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import skycampus.enterprise.api.MisionesApi;
import skycampus.enterprise.aplicacion.AutorizadorVuelo;
import skycampus.enterprise.aplicacion.CrearMision;
import skycampus.enterprise.aplicacion.EstrategiaMayorBateria;
import skycampus.enterprise.dominio.Drone;
import skycampus.enterprise.dominio.PoliticaVuelo;
import skycampus.enterprise.dominio.RestriccionAerea;
import skycampus.enterprise.infraestructura.CatalogoSedesEnMemoria;
import skycampus.enterprise.infraestructura.RepositorioFlotaEnMemoria;

/** Demostración: levanta el endpoint en el puerto 8080 con datos de ejemplo y clima y Aerocivil siempre aptos. */
public final class MisionesApiApp {

    private MisionesApiApp() {
    }

    public static void main(String[] args) throws IOException {
        CatalogoSedesEnMemoria sedes = new CatalogoSedesEnMemoria().activar("ECI").activar("UNAL")
                .distancia("ECI", "UNAL", 9);
        PoliticaVuelo politica = new PoliticaVuelo(30);
        politica.registrarRestriccion("ECI", new RestriccionAerea(30, 120));
        AtomicInteger ids = new AtomicInteger();
        CrearMision caso = new CrearMision(sedes,
                new RepositorioFlotaEnMemoria(List.of(new Drone("ECI-D1", "ECI", 50), new Drone("ECI-D2", "ECI", 90))),
                (origen, destino) -> true, new EstrategiaMayorBateria(), evento -> System.out.println(evento),
                new AutorizadorVuelo(politica, plan -> true), () -> "M-" + ids.incrementAndGet());
        MisionesApi api = new MisionesApi(caso, 8080);
        api.iniciar();
        System.out.println("POST http://127.0.0.1:" + api.puerto() + MisionesApi.RUTA);
    }
}
