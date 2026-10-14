package app.calidad.reservas.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import app.calidad.reservas.dto.ReservaResponse;
import app.calidad.reservas.entity.EstadoRecurso;
import app.calidad.reservas.entity.EstadoReserva;
import app.calidad.reservas.entity.EstadoUsuario;
import app.calidad.reservas.entity.Recurso;
import app.calidad.reservas.entity.Reserva;
import app.calidad.reservas.entity.TipoRecurso;
import app.calidad.reservas.entity.Usuario;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class ReservaMapperTest {

    @Test
    void convierteEntidadADtoConResumenes() {
        Usuario usuario = new Usuario("Ana García", "DOC-001", "ana@example.com", EstadoUsuario.ACTIVO);
        Recurso recurso = new Recurso("Lab 101", "Lab", TipoRecurso.LABORATORIO, 30, EstadoRecurso.DISPONIBLE);
        Reserva reserva = new Reserva(
                usuario, recurso,
                LocalDateTime.of(2026, 10, 20, 9, 0, 0),
                LocalDateTime.of(2026, 10, 20, 11, 0, 0),
                EstadoReserva.ACTIVA);

        ReservaResponse dto = ReservaMapper.aResponse(reserva);

        assertThat(dto.fechaInicio()).isEqualTo(LocalDateTime.of(2026, 10, 20, 9, 0, 0));
        assertThat(dto.fechaFin()).isEqualTo(LocalDateTime.of(2026, 10, 20, 11, 0, 0));
        assertThat(dto.estado()).isEqualTo(EstadoReserva.ACTIVA);
        assertThat(dto.usuario().nombre()).isEqualTo("Ana García");
        assertThat(dto.usuario().estado()).isEqualTo(EstadoUsuario.ACTIVO);
        assertThat(dto.recurso().nombre()).isEqualTo("Lab 101");
        assertThat(dto.recurso().estado()).isEqualTo(EstadoRecurso.DISPONIBLE);
    }

    @Test
    void retornaNuloCuandoEntidadEsNula() {
        assertThat(ReservaMapper.aResponse(null)).isNull();
    }
}
