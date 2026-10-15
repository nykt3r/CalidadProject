package app.calidad.reservas.mapper;

import app.calidad.reservas.dto.UsuarioResponse;
import app.calidad.reservas.entity.Usuario;

public final class UsuarioMapper {

    private UsuarioMapper() {
    }

    public static UsuarioResponse aResponse(Usuario usuario) {
        if (usuario == null) {
            return null;
        }
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getDocumento(),
                usuario.getCorreo(),
                usuario.getEstado());
    }
}
