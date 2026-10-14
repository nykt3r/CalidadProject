package app.calidad.reservas.service;

import app.calidad.reservas.dto.CambioEstadoRequest;
import app.calidad.reservas.dto.DisponibilidadResponse;
import app.calidad.reservas.dto.RecursoRequest;
import app.calidad.reservas.dto.RecursoResponse;
import app.calidad.reservas.entity.EstadoRecurso;
import app.calidad.reservas.entity.EstadoReserva;
import app.calidad.reservas.entity.Recurso;
import app.calidad.reservas.entity.Reserva;
import app.calidad.reservas.entity.TipoRecurso;
import app.calidad.reservas.exception.NoEncontradoException;
import app.calidad.reservas.exception.ValidacionException;
import app.calidad.reservas.mapper.RecursoMapper;
import app.calidad.reservas.mapper.ReservaMapper;
import app.calidad.reservas.repository.RecursoRepository;
import app.calidad.reservas.repository.ReservaRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

@Service
public class RecursoService {

    private final RecursoRepository recursoRepository;
    private final ReservaRepository reservaRepository;

    public RecursoService(RecursoRepository recursoRepository, ReservaRepository reservaRepository) {
        this.recursoRepository = recursoRepository;
        this.reservaRepository = reservaRepository;
    }

    public RecursoResponse crearRecurso(RecursoRequest request) {
        Recurso recurso = new Recurso(
                request.nombre(), request.descripcion(), request.tipo(), request.capacidad(), EstadoRecurso.DISPONIBLE);
        return RecursoMapper.aResponse(recursoRepository.save(recurso));
    }

    public List<RecursoResponse> listarRecursos(EstadoRecurso estado, TipoRecurso tipo) {
        List<Recurso> recursos;
        if (estado != null && tipo != null) {
            recursos = recursoRepository.findByEstadoAndTipo(estado, tipo);
        } else if (estado != null) {
            recursos = recursoRepository.findByEstado(estado);
        } else if (tipo != null) {
            recursos = recursoRepository.findByTipo(tipo);
        } else {
            recursos = recursoRepository.findAll();
        }
        return recursos.stream().map(RecursoMapper::aResponse).toList();
    }

    public RecursoResponse obtenerPorId(Long id) {
        return recursoRepository.findById(id)
                .map(RecursoMapper::aResponse)
                .orElseThrow(() -> new NoEncontradoException("Recurso no encontrado: " + id));
    }

    public RecursoResponse actualizarRecurso(Long id, RecursoRequest request) {
        Recurso recurso = recursoRepository.findById(id)
                .orElseThrow(() -> new NoEncontradoException("Recurso no encontrado: " + id));
        recurso.setNombre(request.nombre());
        recurso.setDescripcion(request.descripcion());
        recurso.setTipo(request.tipo());
        recurso.setCapacidad(request.capacidad());
        return RecursoMapper.aResponse(recursoRepository.save(recurso));
    }

    public RecursoResponse cambiarEstado(Long id, CambioEstadoRequest request) {
        Recurso recurso = recursoRepository.findById(id)
                .orElseThrow(() -> new NoEncontradoException("Recurso no encontrado: " + id));
        EstadoRecurso nuevoEstado;
        try {
            nuevoEstado = EstadoRecurso.valueOf(request.estado());
        } catch (IllegalArgumentException e) {
            throw new ValidacionException("Estado inválido: " + request.estado());
        }
        recurso.setEstado(nuevoEstado);
        return RecursoMapper.aResponse(recursoRepository.save(recurso));
    }

    @Transactional(readOnly = true)
    public DisponibilidadResponse consultarDisponibilidad(Long id, LocalDateTime desde, LocalDateTime hasta) {
        Recurso recurso = recursoRepository.findById(id)
                .orElseThrow(() -> new NoEncontradoException("Recurso no encontrado: " + id));
        if (desde == null || hasta == null || !desde.isBefore(hasta)) {
            throw new ValidacionException("El rango de disponibilidad debe cumplir 'desde' anterior a 'hasta'");
        }
        List<Reserva> solapadas = reservaRepository.findByRecursoIdAndEstado(id, EstadoReserva.ACTIVA).stream()
                .filter(r -> r.getFechaInicio().isBefore(hasta) && r.getFechaFin().isAfter(desde))
                .toList();
        boolean disponible = recurso.getEstado() == EstadoRecurso.DISPONIBLE && solapadas.isEmpty();
        return new DisponibilidadResponse(
                id, desde, hasta, disponible, solapadas.stream().map(ReservaMapper::aResponse).toList());
    }
}
