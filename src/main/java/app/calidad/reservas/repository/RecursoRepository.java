package app.calidad.reservas.repository;

import app.calidad.reservas.entity.EstadoRecurso;
import app.calidad.reservas.entity.Recurso;
import app.calidad.reservas.entity.TipoRecurso;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RecursoRepository extends JpaRepository<Recurso, Long> {

    List<Recurso> findByEstado(EstadoRecurso estado);

    List<Recurso> findByTipo(TipoRecurso tipo);

    List<Recurso> findByEstadoAndTipo(EstadoRecurso estado, TipoRecurso tipo);

    /**
     * Carga el recurso con bloqueo pesimista de escritura para serializar la
     * comprobación de solapamiento en {@code ReservaService.crearReserva} (regla 5
     * bajo concurrencia). El bloqueo se libera al confirmar la transacción.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Recurso r WHERE r.id = :id")
    Optional<Recurso> findByIdForUpdate(@Param("id") Long id);

    boolean existsByNombre(String nombre);
}
