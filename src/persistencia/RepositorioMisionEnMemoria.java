package persistencia;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import model.Mision;

public class RepositorioMisionEnMemoria implements RepositorioMision {

    private final Map<String, Mision> misionesPorId = new LinkedHashMap<>();

    @Override
    public void guardar(Mision mision) {
        if (mision == null) {
            throw new IllegalArgumentException("La misión a guardar no puede ser nula.");
        }
        misionesPorId.put(mision.id(), mision);
    }

    @Override
    public Optional<Mision> buscarPorId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El id de la misión no puede ser nulo ni vacío.");
        }
        return Optional.ofNullable(misionesPorId.get(id));
    }

    @Override
    public List<Mision> listarTodas() {
        return List.copyOf(misionesPorId.values());
    }
}
