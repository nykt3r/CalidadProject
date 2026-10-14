package app.calidad.reservas.mapper;

import app.calidad.reservas.dto.ReservaResponse;
import app.calidad.reservas.entity.Recurso;
import app.calidad.reservas.entity.Reserva;
import app.calidad.reservas.entity.Usuario;

public final class ReservaMapper {

    private ReservaMapper() {
    }

    public static ReservaResponse aResponse(Reserva reserva) {
        if (reserva == null) {
            return null;
        }
        return new ReservaResponse(
                reserva.getId(),
                aUsuarioResumen(reserva.getUsuario()),
                aRecursoResumen(reserva.getRecurso()),
                reserva.getFechaInicio(),
                reserva.getFechaFin(),
                reserva.getEstado(),
                reserva.getFechaCreacion());
    }

    static ReservaResponse.UsuarioResumen aUsuarioResumen(Usuario usuario) {
        if (usuario == null) {
            return null;
        }
        return new ReservaResponse.UsuarioResumen(usuario.getId(), usuario.getNombre(), usuario.getEstado());
    }

    static ReservaResponse.RecursoResumen aRecursoResumen(Recurso recurso) {
        if (recurso == null) {
            return null;
        }
        return new ReservaResponse.RecursoResumen(recurso.getId(), recurso.getNombre(), recurso.getEstado());
    }
}
