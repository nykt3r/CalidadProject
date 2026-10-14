package app.calidad.reservas.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UsuarioRequest(
        @NotBlank(message = "el nombre es obligatorio") String nombre,
        @NotBlank(message = "el documento es obligatorio") String documento,
        @NotBlank(message = "el correo es obligatorio")
        @Email(message = "el correo debe tener un formato válido") String correo) {
}
