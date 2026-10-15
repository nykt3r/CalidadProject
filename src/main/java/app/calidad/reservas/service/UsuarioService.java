package app.calidad.reservas.service;

import app.calidad.reservas.dto.CambioEstadoRequest;
import app.calidad.reservas.dto.UsuarioRequest;
import app.calidad.reservas.dto.UsuarioResponse;
import app.calidad.reservas.entity.EstadoUsuario;
import app.calidad.reservas.entity.Usuario;
import app.calidad.reservas.exception.DuplicadoException;
import app.calidad.reservas.exception.NoEncontradoException;
import app.calidad.reservas.exception.ValidacionException;
import app.calidad.reservas.mapper.UsuarioMapper;
import app.calidad.reservas.repository.UsuarioRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<UsuarioResponse> listarUsuarios() {
        return usuarioRepository.findAll().stream()
                .map(UsuarioMapper::aResponse)
                .toList();
    }

    public UsuarioResponse crearUsuario(UsuarioRequest request) {
        if (usuarioRepository.findByDocumento(request.documento()).isPresent()) {
            throw new DuplicadoException("El documento ya está registrado: " + request.documento());
        }
        if (usuarioRepository.findByCorreo(request.correo()).isPresent()) {
            throw new DuplicadoException("El correo ya está registrado: " + request.correo());
        }
        Usuario usuario = new Usuario(request.nombre(), request.documento(), request.correo(), EstadoUsuario.ACTIVO);
        return UsuarioMapper.aResponse(usuarioRepository.save(usuario));
    }

    public UsuarioResponse obtenerPorId(Long id) {
        return usuarioRepository.findById(id)
                .map(UsuarioMapper::aResponse)
                .orElseThrow(() -> new NoEncontradoException("Usuario no encontrado: " + id));
    }

    public UsuarioResponse cambiarEstado(Long id, CambioEstadoRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new NoEncontradoException("Usuario no encontrado: " + id));
        EstadoUsuario nuevoEstado;
        try {
            nuevoEstado = EstadoUsuario.valueOf(request.estado());
        } catch (IllegalArgumentException e) {
            throw new ValidacionException("Estado inválido: " + request.estado());
        }
        usuario.setEstado(nuevoEstado);
        return UsuarioMapper.aResponse(usuarioRepository.save(usuario));
    }
}
