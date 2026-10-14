package app.calidad.reservas.mapper;

import app.calidad.reservas.dto.RecursoResponse;
import app.calidad.reservas.entity.Recurso;

public final class RecursoMapper {

    private RecursoMapper() {
    }

    public static RecursoResponse aResponse(Recurso recurso) {
        if (recurso == null) {
            return null;
        }
        return new RecursoResponse(
                recurso.getId(),
                recurso.getNombre(),
                recurso.getDescripcion(),
                recurso.getTipo(),
                recurso.getCapacidad(),
                recurso.getEstado());
    }
}
