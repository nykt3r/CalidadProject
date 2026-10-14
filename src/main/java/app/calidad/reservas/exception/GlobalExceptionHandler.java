package app.calidad.reservas.exception;

import app.calidad.reservas.dto.ErrorResponse;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<ErrorResponse> cuerpo(HttpStatus http, String codigo, String mensaje) {
        return ResponseEntity.status(http).body(new ErrorResponse(http.value(), codigo, mensaje));
    }

    @ExceptionHandler(NoEncontradoException.class)
    public ResponseEntity<ErrorResponse> noEncontrado(NoEncontradoException ex) {
        return cuerpo(HttpStatus.NOT_FOUND, "NO_ENCONTRADO", ex.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> noEncontradoGenerico(NoSuchElementException ex) {
        String mensaje = ex.getMessage() != null ? ex.getMessage() : "Recurso no encontrado";
        return cuerpo(HttpStatus.NOT_FOUND, "NO_ENCONTRADO", mensaje);
    }

    @ExceptionHandler(ValidacionException.class)
    public ResponseEntity<ErrorResponse> validacion(ValidacionException ex) {
        return cuerpo(HttpStatus.BAD_REQUEST, "VALIDACION", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> beanValidation(MethodArgumentNotValidException ex) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + (e.getDefaultMessage() != null ? e.getDefaultMessage() : "inválido"))
                .collect(Collectors.joining("; "));
        if (mensaje.isBlank()) {
            mensaje = "Solicitud inválida";
        }
        return cuerpo(HttpStatus.BAD_REQUEST, "VALIDACION", mensaje);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> noLegible(HttpMessageNotReadableException ex) {
        return cuerpo(HttpStatus.BAD_REQUEST, "VALIDACION", "Cuerpo de la solicitud inválido o fecha/estado con formato incorrecto");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> tipoInvalido(MethodArgumentTypeMismatchException ex) {
        return cuerpo(HttpStatus.BAD_REQUEST, "VALIDACION", "Parámetro '" + ex.getName() + "' con formato inválido");
    }

    @ExceptionHandler(DuplicadoException.class)
    public ResponseEntity<ErrorResponse> duplicado(DuplicadoException ex) {
        return cuerpo(HttpStatus.CONFLICT, "DUPLICADO", ex.getMessage());
    }

    @ExceptionHandler(UsuarioInactivoException.class)
    public ResponseEntity<ErrorResponse> usuarioInactivo(UsuarioInactivoException ex) {
        return cuerpo(HttpStatus.CONFLICT, "USUARIO_INACTIVO", ex.getMessage());
    }

    @ExceptionHandler(RecursoEnMantenimientoException.class)
    public ResponseEntity<ErrorResponse> enMantenimiento(RecursoEnMantenimientoException ex) {
        return cuerpo(HttpStatus.CONFLICT, "RECURSO_EN_MANTENIMIENTO", ex.getMessage());
    }

    @ExceptionHandler(RecursoFueraDeServicioException.class)
    public ResponseEntity<ErrorResponse> fueraDeServicio(RecursoFueraDeServicioException ex) {
        return cuerpo(HttpStatus.CONFLICT, "RECURSO_FUERA_DE_SERVICIO", ex.getMessage());
    }

    @ExceptionHandler(ReservaSolapadaException.class)
    public ResponseEntity<ErrorResponse> solapada(ReservaSolapadaException ex) {
        return cuerpo(HttpStatus.CONFLICT, "RESERVA_SOLAPADA", ex.getMessage());
    }

    @ExceptionHandler(ReservaFinalizadaException.class)
    public ResponseEntity<ErrorResponse> finalizada(ReservaFinalizadaException ex) {
        return cuerpo(HttpStatus.CONFLICT, "RESERVA_FINALIZADA", ex.getMessage());
    }

    @ExceptionHandler(LimiteReservasException.class)
    public ResponseEntity<ErrorResponse> limite(LimiteReservasException ex) {
        return cuerpo(HttpStatus.CONFLICT, "LIMITE_RESERVAS_ACTIVAS", ex.getMessage());
    }
}
