package persistencia;

import java.util.List;
import java.util.Optional;
import model.Mision;

public interface RepositorioMision {

    void guardar(Mision mision);

    Optional<Mision> buscarPorId(String id);

    List<Mision> listarTodas();
}
