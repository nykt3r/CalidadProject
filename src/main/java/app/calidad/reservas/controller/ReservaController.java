package app.calidad.reservas.controller;

import app.calidad.reservas.dto.ReservaRequest;
import app.calidad.reservas.dto.ReservaResponse;
import app.calidad.reservas.entity.EstadoReserva;
import app.calidad.reservas.service.ReservaService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reservas")
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @PostMapping
    public ResponseEntity<ReservaResponse> crearReserva(@Valid @RequestBody ReservaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservaService.crearReserva(request));
    }

    @GetMapping("/{id}")
    public ReservaResponse obtenerPorId(@PathVariable Long id) {
        return reservaService.obtenerPorId(id);
    }

    @DeleteMapping("/{id}")
    public ReservaResponse cancelarReserva(@PathVariable Long id) {
        return reservaService.cancelarReserva(id);
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<ReservaResponse> listarPorUsuario(
            @PathVariable Long usuarioId,
            @RequestParam(required = false) EstadoReserva estado) {
        return reservaService.listarPorUsuario(usuarioId, estado);
    }

    @GetMapping("/recurso/{recursoId}")
    public List<ReservaResponse> listarPorRecurso(
            @PathVariable Long recursoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        return reservaService.listarPorRecurso(recursoId, desde, hasta);
    }

    @GetMapping
    public List<ReservaResponse> listarPorFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return reservaService.listarPorFecha(fecha);
    }
}
