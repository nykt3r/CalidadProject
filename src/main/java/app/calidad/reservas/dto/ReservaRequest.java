package app.calidad.reservas.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record ReservaRequest(
        @NotNull(message = "el usuario es obligatorio") Long usuarioId,
        @NotNull(message = "el recurso es obligatorio") Long recursoId,
        @NotNull(message = "la fecha de inicio es obligatoria")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime fechaInicio,
        @NotNull(message = "la fecha de fin es obligatoria")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime fechaFin) {
}
