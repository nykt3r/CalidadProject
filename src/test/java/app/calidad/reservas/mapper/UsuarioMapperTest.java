package app.calidad.reservas.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import app.calidad.reservas.dto.UsuarioResponse;
import app.calidad.reservas.entity.EstadoUsuario;
import app.calidad.reservas.entity.Usuario;
import org.junit.jupiter.api.Test;

class UsuarioMapperTest {

    @Test
    void convierteEntidadADto() {
        Usuario usuario = new Usuario("Ana García", "DOC-001", "ana@example.com", EstadoUsuario.ACTIVO);

        UsuarioResponse dto = UsuarioMapper.aResponse(usuario);

        assertThat(dto.nombre()).isEqualTo("Ana García");
        assertThat(dto.documento()).isEqualTo("DOC-001");
        assertThat(dto.correo()).isEqualTo("ana@example.com");
        assertThat(dto.estado()).isEqualTo(EstadoUsuario.ACTIVO);
    }

    @Test
    void retornaNuloCuandoEntidadEsNula() {
        assertThat(UsuarioMapper.aResponse(null)).isNull();
    }
}
