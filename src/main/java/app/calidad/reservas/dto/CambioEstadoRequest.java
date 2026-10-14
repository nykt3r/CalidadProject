package app.calidad.reservas.dto;

import jakarta.validation.constraints.NotBlank;

public record CambioEstadoRequest(@NotBlank(message = "el estado es obligatorio") String estado) {
}
