package app.calidad.reservas.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.List;

public record DisponibilidadResponse(
        Long recursoId,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime desde,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime hasta,
        boolean disponible,
        List<ReservaResponse> reservasSolapadas) {
}
