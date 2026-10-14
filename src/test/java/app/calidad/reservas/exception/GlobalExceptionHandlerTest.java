package app.calidad.reservas.exception;

import static org.assertj.core.api.Assertions.assertThat;

import app.calidad.reservas.dto.ErrorResponse;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void noEncontradoDevuelve404ConCuerpoExacto() {
        ResponseEntity<ErrorResponse> respuesta = handler.noEncontrado(new NoEncontradoException("Usuario no encontrado"));

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(respuesta.getBody()).isNotNull();
        assertThat(respuesta.getBody().status()).isEqualTo(404);
        assertThat(respuesta.getBody().error()).isEqualTo("NO_ENCONTRADO");
        assertThat(respuesta.getBody().message()).isEqualTo("Usuario no encontrado");
    }

    @Test
    void noSuchElementDevuelve404NoEncontrado() {
        ResponseEntity<ErrorResponse> respuesta = handler.noEncontradoGenerico(new NoSuchElementException("nada"));

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(respuesta.getBody().error()).isEqualTo("NO_ENCONTRADO");
    }

    @Test
    void validacionDevuelve400ConCuerpoExacto() {
        ResponseEntity<ErrorResponse> respuesta = handler.validacion(new ValidacionException("la fecha final debe ser posterior"));

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(respuesta.getBody().status()).isEqualTo(400);
        assertThat(respuesta.getBody().error()).isEqualTo("VALIDACION");
    }

    @Test
    void duplicadoDevuelve409() {
        ResponseEntity<ErrorResponse> respuesta = handler.duplicado(new DuplicadoException("correo ya registrado"));

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(respuesta.getBody().error()).isEqualTo("DUPLICADO");
    }

    @Test
    void reservaSolapadaDevuelve409() {
        ResponseEntity<ErrorResponse> respuesta = handler.solapada(new ReservaSolapadaException("solapada"));

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(respuesta.getBody().error()).isEqualTo("RESERVA_SOLAPADA");
    }
}
