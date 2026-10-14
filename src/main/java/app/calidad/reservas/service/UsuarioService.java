package app.calidad.reservas.service;

import app.calidad.reservas.dto.UsuarioResponse;
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
}
