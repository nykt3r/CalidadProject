package app.calidad.reservas.dto;

import app.calidad.reservas.entity.TipoRecurso;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RecursoRequest(
        @NotBlank(message = "el nombre es obligatorio") String nombre,
        String descripcion,
        @NotNull(message = "el tipo es obligatorio") TipoRecurso tipo,
        @NotNull(message = "la capacidad es obligatoria")
        @Positive(message = "la capacidad debe ser mayor que 0") Integer capacidad) {
}
