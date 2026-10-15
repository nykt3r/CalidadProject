package app.calidad.reservas.dto;

import app.calidad.reservas.entity.EstadoRecurso;
import app.calidad.reservas.entity.EstadoReserva;
import app.calidad.reservas.entity.EstadoUsuario;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public record ReservaResponse(
        Long id,
        UsuarioResumen usuario,
        RecursoResumen recurso,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime fechaInicio,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime fechaFin,
        EstadoReserva estado,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime fechaCreacion) {

    public record UsuarioResumen(Long id, String nombre, EstadoUsuario estado) {
    }

    public record RecursoResumen(Long id, String nombre, EstadoRecurso estado) {
    }
}
