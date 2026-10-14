package app.calidad.reservas.repository;

import app.calidad.reservas.entity.EstadoRecurso;
import app.calidad.reservas.entity.Recurso;
import app.calidad.reservas.entity.TipoRecurso;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecursoRepository extends JpaRepository<Recurso, Long> {

    List<Recurso> findByEstado(EstadoRecurso estado);

    List<Recurso> findByTipo(TipoRecurso tipo);

    List<Recurso> findByEstadoAndTipo(EstadoRecurso estado, TipoRecurso tipo);

    boolean existsByNombre(String nombre);
}
