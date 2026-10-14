package app.calidad.reservas.service;

import app.calidad.reservas.dto.ReservaRequest;
import app.calidad.reservas.dto.ReservaResponse;
import app.calidad.reservas.entity.EstadoRecurso;
import app.calidad.reservas.entity.EstadoReserva;
import app.calidad.reservas.entity.EstadoUsuario;
import app.calidad.reservas.entity.Recurso;
import app.calidad.reservas.entity.Reserva;
import app.calidad.reservas.entity.Usuario;
import app.calidad.reservas.exception.LimiteReservasException;
import app.calidad.reservas.exception.NoEncontradoException;
import app.calidad.reservas.exception.RecursoEnMantenimientoException;
import app.calidad.reservas.exception.RecursoFueraDeServicioException;
import app.calidad.reservas.exception.ReservaFinalizadaException;
import app.calidad.reservas.exception.ReservaSolapadaException;
import app.calidad.reservas.exception.UsuarioInactivoException;
import app.calidad.reservas.exception.ValidacionException;
import app.calidad.reservas.mapper.ReservaMapper;
import app.calidad.reservas.repository.RecursoRepository;
import app.calidad.reservas.repository.ReservaRepository;
import app.calidad.reservas.repository.UsuarioRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final UsuarioRepository usuarioRepository;
    private final RecursoRepository recursoRepository;

    public ReservaService(ReservaRepository reservaRepository, UsuarioRepository usuarioRepository,
            RecursoRepository recursoRepository) {
        this.reservaRepository = reservaRepository;
        this.usuarioRepository = usuarioRepository;
        this.recursoRepository = recursoRepository;
    }

    public ReservaResponse crearReserva(ReservaRequest request) {
        Usuario usuario = usuarioRepository.findById(request.usuarioId())
                .orElseThrow(() -> new NoEncontradoException("Usuario no encontrado: " + request.usuarioId()));
        Recurso recurso = recursoRepository.findById(request.recursoId())
                .orElseThrow(() -> new NoEncontradoException("Recurso no encontrado: " + request.recursoId()));
        if (!request.fechaFin().isAfter(request.fechaInicio())) {
            throw new ValidacionException("La fecha final debe ser posterior a la fecha inicial");
        }
        if (request.fechaInicio().isBefore(LocalDateTime.now())) {
            throw new ValidacionException("No se pueden realizar reservas en fechas pasadas");
        }
        if (usuario.getEstado() != EstadoUsuario.ACTIVO) {
            throw new UsuarioInactivoException("El usuario no está activo: " + usuario.getId());
        }
        if (recurso.getEstado() == EstadoRecurso.MANTENIMIENTO) {
            throw new RecursoEnMantenimientoException("El recurso está en mantenimiento: " + recurso.getId());
        }
        if (recurso.getEstado() == EstadoRecurso.FUERA_DE_SERVICIO) {
            throw new RecursoFueraDeServicioException("El recurso está fuera de servicio: " + recurso.getId());
        }
        if (reservaRepository.countByUsuarioIdAndEstado(usuario.getId(), EstadoReserva.ACTIVA) >= 3) {
            throw new LimiteReservasException("El usuario ya tiene 3 reservas activas: " + usuario.getId());
        }
        if (!reservaRepository.findActivasSolapadas(recurso.getId(), request.fechaInicio(), request.fechaFin()).isEmpty()) {
            throw new ReservaSolapadaException("El recurso ya se encuentra reservado para este horario");
        }
        Reserva reserva = new Reserva(usuario, recurso, request.fechaInicio(), request.fechaFin(), EstadoReserva.ACTIVA);
        return ReservaMapper.aResponse(reservaRepository.save(reserva));
    }

    public ReservaResponse cancelarReserva(Long id) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new NoEncontradoException("Reserva no encontrada: " + id));
        if (reserva.getEstado() != EstadoReserva.ACTIVA) {
            throw new ReservaFinalizadaException("La reserva no está activa y no puede cancelarse: " + id);
        }
        reserva.setEstado(EstadoReserva.CANCELADA);
        return ReservaMapper.aResponse(reservaRepository.save(reserva));
    }

    public ReservaResponse obtenerPorId(Long id) {
        return reservaRepository.findById(id)
                .map(ReservaMapper::aResponse)
                .orElseThrow(() -> new NoEncontradoException("Reserva no encontrada: " + id));
    }

    public List<ReservaResponse> listarPorUsuario(Long usuarioId, EstadoReserva estado) {
        List<Reserva> reservas = estado != null
                ? reservaRepository.findByUsuarioIdAndEstado(usuarioId, estado)
                : reservaRepository.findByUsuarioId(usuarioId);
        return reservas.stream().map(ReservaMapper::aResponse).toList();
    }

    public List<ReservaResponse> listarPorRecurso(Long recursoId, LocalDateTime desde, LocalDateTime hasta) {
        List<Reserva> reservas;
        if (desde != null && hasta != null) {
            if (!desde.isBefore(hasta)) {
                throw new ValidacionException("El rango debe cumplir 'desde' anterior a 'hasta'");
            }
            reservas = reservaRepository.findPorRecursoEnRango(recursoId, desde, hasta);
        } else if (desde != null) {
            reservas = reservaRepository.findByRecursoId(recursoId).stream()
                    .filter(r -> r.getFechaFin().isAfter(desde))
                    .toList();
        } else if (hasta != null) {
            reservas = reservaRepository.findByRecursoId(recursoId).stream()
                    .filter(r -> r.getFechaInicio().isBefore(hasta))
                    .toList();
        } else {
            reservas = reservaRepository.findByRecursoId(recursoId);
        }
        return reservas.stream().map(ReservaMapper::aResponse).toList();
    }

    public List<ReservaResponse> listarPorFecha(LocalDate fecha) {
        LocalDateTime desde = fecha.atStartOfDay();
        LocalDateTime hasta = fecha.plusDays(1).atStartOfDay();
        return reservaRepository.findEnRango(desde, hasta).stream()
                .map(ReservaMapper::aResponse)
                .toList();
    }
}
