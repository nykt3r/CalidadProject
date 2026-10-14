package app.calidad.reservas.dto;

public record ErrorResponse(int status, String error, String message) {
}
