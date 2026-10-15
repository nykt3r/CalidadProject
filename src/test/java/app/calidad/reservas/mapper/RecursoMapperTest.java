package app.calidad.reservas.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import app.calidad.reservas.dto.RecursoResponse;
import app.calidad.reservas.entity.EstadoRecurso;
import app.calidad.reservas.entity.Recurso;
import app.calidad.reservas.entity.TipoRecurso;
import org.junit.jupiter.api.Test;

class RecursoMapperTest {

    @Test
    void convierteEntidadADto() {
        Recurso recurso = new Recurso("Lab 101", "Laboratorio", TipoRecurso.LABORATORIO, 30, EstadoRecurso.DISPONIBLE);

        RecursoResponse dto = RecursoMapper.aResponse(recurso);

        assertThat(dto.nombre()).isEqualTo("Lab 101");
        assertThat(dto.tipo()).isEqualTo(TipoRecurso.LABORATORIO);
        assertThat(dto.capacidad()).isEqualTo(30);
        assertThat(dto.estado()).isEqualTo(EstadoRecurso.DISPONIBLE);
    }

    @Test
    void retornaNuloCuandoEntidadEsNula() {
        assertThat(RecursoMapper.aResponse(null)).isNull();
    }
}
