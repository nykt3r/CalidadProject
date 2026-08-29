package app.calidad.reservas.repository;

import app.calidad.reservas.entity.EstadoRecurso;
import app.calidad.reservas.entity.Recurso;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecursoRepository extends JpaRepository<Recurso, Long> {

    List<Recurso> findByEstado(EstadoRecurso estado);

    boolean existsByNombre(String nombre);
}