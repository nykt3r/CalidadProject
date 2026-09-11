package app.calidad.reservas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import app.calidad.reservas.entity.EstadoUsuario;
import app.calidad.reservas.entity.Usuario;
import app.calidad.reservas.repository.UsuarioRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void listarUsuariosDebeRetornarTodosLosUsuarios() {
        List<Usuario> esperados = List.of(
                new Usuario("Ana López", "123456", "ana@correo.com", EstadoUsuario.ACTIVO),
                new Usuario("Luis Pérez", "654321", "luis@correo.com", EstadoUsuario.INACTIVO));
        when(usuarioRepository.findAll()).thenReturn(esperados);

        List<Usuario> resultado = usuarioService.listarUsuarios();

        assertThat(resultado).hasSize(2).isEqualTo(esperados);
    }

    @Test
    void listarUsuariosDebeRetornarListaVaciaCuandoNoHayUsuarios() {
        when(usuarioRepository.findAll()).thenReturn(List.of());

        List<Usuario> resultado = usuarioService.listarUsuarios();

        assertThat(resultado).isEmpty();
    }
}