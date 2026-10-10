package skycampus.enterprise.ruta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import util.Validaciones;

/**
 * Compuesto del Composite: una secuencia de rutas (simples u otras compuestas) que forma un solo viaje.
 * Cada etapa debe empezar donde termina la anterior.
 */
public final class RutaCompuesta implements Ruta {

    private final List<Ruta> etapas;

    public RutaCompuesta(List<Ruta> etapas) {
        Validaciones.exigirPresente(etapas, "etapas");
        if (etapas.isEmpty()) {
            throw new IllegalArgumentException("Una ruta compuesta necesita al menos una etapa.");
        }
        if (etapas.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Las etapas no pueden ser nulas.");
        }
        for (int i = 1; i < etapas.size(); i++) {
            if (!etapas.get(i - 1).fin().equals(etapas.get(i).inicio())) {
                throw new IllegalArgumentException("La etapa " + (i + 1) + " no empieza donde termina la anterior.");
            }
        }
        this.etapas = List.copyOf(etapas);
    }

    public List<Ruta> etapas() {
        return etapas;
    }

    @Override
    public String descripcion() {
        return "Ruta " + inicio().nombre() + " -> " + fin().nombre() + " (" + etapas.size() + " etapas)";
    }

    @Override
    public Punto inicio() {
        return etapas.get(0).inicio();
    }

    @Override
    public Punto fin() {
        return etapas.get(etapas.size() - 1).fin();
    }

    @Override
    public double distanciaKm() {
        return etapas.stream().mapToDouble(Ruta::distanciaKm).sum();
    }

    @Override
    public int duracionMinutos() {
        return etapas.stream().mapToInt(Ruta::duracionMinutos).sum();
    }

    @Override
    public int paradasDeCarga() {
        return etapas.stream().mapToInt(Ruta::paradasDeCarga).sum();
    }

    @Override
    public double tramoMasLargoKm() {
        return etapas.stream().mapToDouble(Ruta::tramoMasLargoKm).max().orElse(0);
    }

    @Override
    public List<Punto> puntos() {
        List<Punto> recorrido = new ArrayList<>();
        for (Ruta etapa : etapas) {
            for (Punto punto : etapa.puntos()) {
                if (recorrido.isEmpty() || !recorrido.get(recorrido.size() - 1).equals(punto)) {
                    recorrido.add(punto);
                }
            }
        }
        return List.copyOf(recorrido);
    }

    @Override
    public ResultadoEjecucion ejecutar(ContextoEjecucion contexto) {
        Validaciones.exigirPresente(contexto, "contexto");
        int completadas = 0;
        for (Ruta etapa : etapas) {
            ResultadoEjecucion resultado = etapa.ejecutar(contexto);
            completadas += resultado.etapasCompletadas();
            if (!resultado.completada()) {
                return ResultadoEjecucion.fallo(completadas);
            }
        }
        return ResultadoEjecucion.exito(completadas);
    }
}
