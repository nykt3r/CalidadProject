package app.calidad.reservas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import app.calidad.reservas.dto.CambioEstadoRequest;
import app.calidad.reservas.dto.UsuarioRequest;
import app.calidad.reservas.dto.UsuarioResponse;
import app.calidad.reservas.entity.EstadoUsuario;
import app.calidad.reservas.entity.Usuario;
import app.calidad.reservas.exception.DuplicadoException;
import app.calidad.reservas.exception.NoEncontradoException;
import app.calidad.reservas.exception.ValidacionException;
import app.calidad.reservas.repository.UsuarioRepository;
import java.util.List;
import java.util.Optional;
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
        List<Usuario> entidades = List.of(
                new Usuario("Ana López", "123456", "ana@correo.com", EstadoUsuario.ACTIVO),
                new Usuario("Luis Pérez", "654321", "luis@correo.com", EstadoUsuario.INACTIVO));
        when(usuarioRepository.findAll()).thenReturn(entidades);

        List<UsuarioResponse> resultado = usuarioService.listarUsuarios();

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).nombre()).isEqualTo("Ana López");
        assertThat(resultado.get(0).estado()).isEqualTo(EstadoUsuario.ACTIVO);
        assertThat(resultado.get(1).nombre()).isEqualTo("Luis Pérez");
        assertThat(resultado.get(1).estado()).isEqualTo(EstadoUsuario.INACTIVO);
    }

    @Test
    void listarUsuariosDebeRetornarListaVaciaCuandoNoHayUsuarios() {
        when(usuarioRepository.findAll()).thenReturn(List.of());

        List<UsuarioResponse> resultado = usuarioService.listarUsuarios();

        assertThat(resultado).isEmpty();
    }

    @Test
    void crearUsuarioDebePersistirConEstadoActivo() {
        UsuarioRequest request = new UsuarioRequest("Marta Ruiz", "DOC-010", "marta@example.com");
        when(usuarioRepository.findByDocumento("DOC-010")).thenReturn(Optional.empty());
        when(usuarioRepository.findByCorreo("marta@example.com")).thenReturn(Optional.empty());
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioResponse resultado = usuarioService.crearUsuario(request);

        assertThat(resultado.nombre()).isEqualTo("Marta Ruiz");
        assertThat(resultado.documento()).isEqualTo("DOC-010");
        assertThat(resultado.correo()).isEqualTo("marta@example.com");
        assertThat(resultado.estado()).isEqualTo(EstadoUsuario.ACTIVO);
    }

    @Test
    void crearUsuarioDebeFallarCuandoDocumentoExiste() {
        UsuarioRequest request = new UsuarioRequest("Otro", "DOC-001", "otro@example.com");
        when(usuarioRepository.findByDocumento("DOC-001"))
                .thenReturn(Optional.of(new Usuario("Ana García", "DOC-001", "ana@example.com", EstadoUsuario.ACTIVO)));

        assertThatThrownBy(() -> usuarioService.crearUsuario(request))
                .isInstanceOf(DuplicadoException.class);
    }

    @Test
    void crearUsuarioDebeFallarCuandoCorreoExiste() {
        UsuarioRequest request = new UsuarioRequest("Otro", "DOC-099", "ana@example.com");
        when(usuarioRepository.findByDocumento("DOC-099")).thenReturn(Optional.empty());
        when(usuarioRepository.findByCorreo("ana@example.com"))
                .thenReturn(Optional.of(new Usuario("Ana García", "DOC-001", "ana@example.com", EstadoUsuario.ACTIVO)));

        assertThatThrownBy(() -> usuarioService.crearUsuario(request))
                .isInstanceOf(DuplicadoException.class);
    }

    @Test
    void obtenerPorIdDebeRetornarElUsuario() {
        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(new Usuario("Ana García", "DOC-001", "ana@example.com", EstadoUsuario.ACTIVO)));

        UsuarioResponse resultado = usuarioService.obtenerPorId(1L);

        assertThat(resultado.nombre()).isEqualTo("Ana García");
        assertThat(resultado.estado()).isEqualTo(EstadoUsuario.ACTIVO);
    }

    @Test
    void obtenerPorIdDebeFallarCuandoNoExiste() {
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.obtenerPorId(999L))
                .isInstanceOf(NoEncontradoException.class);
    }

    @Test
    void cambiarEstadoDebeActualizarElEstado() {
        Usuario usuario = new Usuario("Ana García", "DOC-001", "ana@example.com", EstadoUsuario.ACTIVO);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioResponse resultado = usuarioService.cambiarEstado(1L, new CambioEstadoRequest("INACTIVO"));

        assertThat(resultado.estado()).isEqualTo(EstadoUsuario.INACTIVO);
    }

    @Test
    void cambiarEstadoDebeFallarCuandoNoExiste() {
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.cambiarEstado(999L, new CambioEstadoRequest("INACTIVO")))
                .isInstanceOf(NoEncontradoException.class);
    }

    @Test
    void cambiarEstadoDebeFallarCuandoEstadoEsInvalido() {
        Usuario usuario = new Usuario("Ana García", "DOC-001", "ana@example.com", EstadoUsuario.ACTIVO);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> usuarioService.cambiarEstado(1L, new CambioEstadoRequest("BLOQUEADO")))
                .isInstanceOf(ValidacionException.class);
    }
}
