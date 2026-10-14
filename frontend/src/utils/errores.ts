// Sugerencias de UI por código de error del catálogo (§2.3).
// El `message` del backend es el árbitro; la sugerencia orienta al usuario.

export function sugerenciaPara(codigo: string): string | null {
  switch (codigo) {
    case 'RESERVA_SOLAPADA':
      return 'Sugerencia: elige otro horario u otro recurso.';
    case 'LIMITE_RESERVAS_ACTIVAS':
      return 'Sugerencia: cancela alguna reserva activa o elige otro usuario.';
    case 'USUARIO_INACTIVO':
      return 'Sugerencia: activa al usuario o elige otro usuario activo.';
    case 'RECURSO_EN_MANTENIMIENTO':
    case 'RECURSO_FUERA_DE_SERVICIO':
      return 'Sugerencia: elige otro recurso disponible.';
    case 'RESERVA_FINALIZADA':
      return 'La reserva ya está cerrada, no se puede cancelar.';
    case 'DUPLICADO':
      return 'Revisa documento y correo: ya están registrados.';
    case 'NO_ENCONTRADO':
      return 'El registro solicitado no existe.';
    default:
      return null;
  }
}
