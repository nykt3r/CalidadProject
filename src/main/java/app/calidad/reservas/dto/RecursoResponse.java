package app.calidad.reservas.dto;

import app.calidad.reservas.entity.EstadoRecurso;
import app.calidad.reservas.entity.TipoRecurso;

public record RecursoResponse(
        Long id, String nombre, String descripcion, TipoRecurso tipo, Integer capacidad, EstadoRecurso estado) {
}
