package app.calidad.reservas.exception;

public class RecursoFueraDeServicioException extends RuntimeException {
    public RecursoFueraDeServicioException(String message) {
        super(message);
    }
}
