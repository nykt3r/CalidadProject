package app.calidad.reservas.repository;

import app.calidad.reservas.entity.EstadoReserva;
import app.calidad.reservas.entity.Reserva;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    List<Reserva> findByUsuarioId(Long usuarioId);

    List<Reserva> findByUsuarioIdAndEstado(Long usuarioId, EstadoReserva estado);

    List<Reserva> findByRecursoId(Long recursoId);

    List<Reserva> findByRecursoIdAndEstado(Long recursoId, EstadoReserva estado);

    @Query("SELECT r FROM Reserva r WHERE r.recurso.id = :recursoId "
            + "AND r.estado = app.calidad.reservas.entity.EstadoReserva.ACTIVA "
            + "AND r.fechaInicio < :fin AND r.fechaFin > :inicio")
    List<Reserva> findActivasSolapadas(
            @Param("recursoId") Long recursoId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);

    @Query("SELECT r FROM Reserva r WHERE r.recurso.id = :recursoId "
            + "AND r.fechaInicio < :hasta AND r.fechaFin > :desde")
    List<Reserva> findPorRecursoEnRango(
            @Param("recursoId") Long recursoId,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);

    @Query("SELECT r FROM Reserva r WHERE r.fechaInicio < :hasta AND r.fechaFin > :desde")
    List<Reserva> findEnRango(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);

    long countByUsuarioIdAndEstado(Long usuarioId, EstadoReserva estado);
}
