package app.calidad.reservas.dto;

import app.calidad.reservas.entity.EstadoUsuario;

public record UsuarioResponse(Long id, String nombre, String documento, String correo, EstadoUsuario estado) {
}
