package app.calidad.reservas.repository;

import app.calidad.reservas.entity.EstadoReserva;
import app.calidad.reservas.entity.Reserva;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    List<Reserva> findByUsuarioId(Long usuarioId);

    List<Reserva> findByRecursoId(Long recursoId);

    List<Reserva> findByRecursoIdAndEstado(Long recursoId, EstadoReserva estado);

    long countByUsuarioIdAndEstado(Long usuarioId, EstadoReserva estado);
}