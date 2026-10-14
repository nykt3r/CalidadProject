package app.calidad.reservas.controller;

import app.calidad.reservas.dto.CambioEstadoRequest;
import app.calidad.reservas.dto.DisponibilidadResponse;
import app.calidad.reservas.dto.RecursoRequest;
import app.calidad.reservas.dto.RecursoResponse;
import app.calidad.reservas.entity.EstadoRecurso;
import app.calidad.reservas.entity.TipoRecurso;
import app.calidad.reservas.service.RecursoService;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/recursos")
public class RecursoController {

    private final RecursoService recursoService;

    public RecursoController(RecursoService recursoService) {
        this.recursoService = recursoService;
    }

    @PostMapping
    public ResponseEntity<RecursoResponse> crearRecurso(@Valid @RequestBody RecursoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(recursoService.crearRecurso(request));
    }

    @GetMapping
    public List<RecursoResponse> listarRecursos(
            @RequestParam(required = false) EstadoRecurso estado,
            @RequestParam(required = false) TipoRecurso tipo) {
        return recursoService.listarRecursos(estado, tipo);
    }

    @GetMapping("/{id}")
    public RecursoResponse obtenerPorId(@PathVariable Long id) {
        return recursoService.obtenerPorId(id);
    }

    @PutMapping("/{id}")
    public RecursoResponse actualizarRecurso(@PathVariable Long id, @Valid @RequestBody RecursoRequest request) {
        return recursoService.actualizarRecurso(id, request);
    }

    @PatchMapping("/{id}/estado")
    public RecursoResponse cambiarEstado(@PathVariable Long id, @Valid @RequestBody CambioEstadoRequest request) {
        return recursoService.cambiarEstado(id, request);
    }

    @GetMapping("/{id}/disponibilidad")
    public DisponibilidadResponse consultarDisponibilidad(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        return recursoService.consultarDisponibilidad(id, desde, hasta);
    }
}
