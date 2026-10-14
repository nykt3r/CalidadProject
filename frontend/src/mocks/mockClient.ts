// Cliente mock intercambiable: implementa ReservasApi en memoria.
// Incluye las validaciones visibles del contrato para desarrollar la UI
// (duplicados, solapamiento, límite de 3 activas, recurso no disponible).

import { ApiError } from '../api/client';
import type {
  Disponibilidad,
  EstadoRecurso,
  EstadoReserva,
  EstadoUsuario,
  Recurso,
  RecursoRequest,
  Reserva,
  ReservaRequest,
  ReservasApi,
  TipoRecurso,
  Usuario,
  UsuarioRequest,
} from '../api/types';
import { recursosMock, reservasMock, usuariosMock } from './data';

function clonar<T>(valor: T): T {
  return JSON.parse(JSON.stringify(valor)) as T;
}

function solapa(aInicio: string, aFin: string, bInicio: string, bFin: string): boolean {
  return aInicio < bFin && aFin > bInicio;
}

export function createMockApi(): ReservasApi {
  let usuarios: Usuario[] = clonar(usuariosMock);
  let recursos: Recurso[] = clonar(recursosMock);
  let reservas: Reserva[] = clonar(reservasMock);
  let siguienteUsuario = 100;
  let siguienteRecurso = 100;
  let siguienteReserva = 100;

  return {
    crearUsuario: async (body: UsuarioRequest) => {
      if (!body.nombre.trim() || !body.documento.trim() || !body.correo.trim()) {
        throw new ApiError({ status: 400, error: 'VALIDACION', message: 'Campos obligatorios' });
      }
      if (usuarios.some((u) => u.documento === body.documento || u.correo === body.correo)) {
        throw new ApiError({
          status: 409,
          error: 'DUPLICADO',
          message: 'Documento o correo ya registrados',
        });
      }
      const nuevo: Usuario = { id: siguienteUsuario++, estado: 'ACTIVO', ...body };
      usuarios.push(nuevo);
      return clonar(nuevo);
    },
    listarUsuarios: async () => clonar(usuarios),
    obtenerUsuario: async (id: number) => {
      const usuario = usuarios.find((u) => u.id === id);
      if (!usuario) {
        throw new ApiError({ status: 404, error: 'NO_ENCONTRADO', message: 'Usuario no encontrado' });
      }
      return clonar(usuario);
    },
    cambiarEstadoUsuario: async (id: number, estado: EstadoUsuario) => {
      const usuario = usuarios.find((u) => u.id === id);
      if (!usuario) {
        throw new ApiError({ status: 404, error: 'NO_ENCONTRADO', message: 'Usuario no encontrado' });
      }
      usuario.estado = estado;
      return clonar(usuario);
    },

    crearRecurso: async (body: RecursoRequest) => {
      if (body.capacidad <= 0) {
        throw new ApiError({ status: 400, error: 'VALIDACION', message: 'Capacidad debe ser > 0' });
      }
      const nuevo: Recurso = { id: siguienteRecurso++, estado: 'DISPONIBLE', ...body };
      recursos.push(nuevo);
      return clonar(nuevo);
    },
    listarRecursos: async (opciones?: { estado?: EstadoRecurso; tipo?: TipoRecurso }) =>
      clonar(
        recursos.filter(
          (r) =>
            (!opciones?.estado || r.estado === opciones.estado) &&
            (!opciones?.tipo || r.tipo === opciones.tipo),
        ),
      ),
    obtenerRecurso: async (id: number) => {
      const recurso = recursos.find((r) => r.id === id);
      if (!recurso) {
        throw new ApiError({ status: 404, error: 'NO_ENCONTRADO', message: 'Recurso no encontrado' });
      }
      return clonar(recurso);
    },
    modificarRecurso: async (id: number, body: RecursoRequest) => {
      const recurso = recursos.find((r) => r.id === id);
      if (!recurso) {
        throw new ApiError({ status: 404, error: 'NO_ENCONTRADO', message: 'Recurso no encontrado' });
      }
      Object.assign(recurso, body);
      return clonar(recurso);
    },
    cambiarEstadoRecurso: async (id: number, estado: EstadoRecurso) => {
      const recurso = recursos.find((r) => r.id === id);
      if (!recurso) {
        throw new ApiError({ status: 404, error: 'NO_ENCONTRADO', message: 'Recurso no encontrado' });
      }
      recurso.estado = estado;
      return clonar(recurso);
    },
    consultarDisponibilidad: async (
      recursoId: number,
      desde: string,
      hasta: string,
    ): Promise<Disponibilidad> => {
      const recurso = recursos.find((r) => r.id === recursoId);
      if (!recurso) {
        throw new ApiError({ status: 404, error: 'NO_ENCONTRADO', message: 'Recurso no encontrado' });
      }
      const solapadas = reservas.filter(
        (r) =>
          r.recurso.id === recursoId &&
          r.estado === 'ACTIVA' &&
          solapa(r.fechaInicio, r.fechaFin, desde, hasta),
      );
      return {
        recursoId,
        desde,
        hasta,
        disponible: recurso.estado === 'DISPONIBLE' && solapadas.length === 0,
        reservasSolapadas: clonar(solapadas),
      };
    },

    crearReserva: async (body: ReservaRequest) => {
      const usuario = usuarios.find((u) => u.id === body.usuarioId);
      if (!usuario) {
        throw new ApiError({ status: 404, error: 'NO_ENCONTRADO', message: 'Usuario no encontrado' });
      }
      const recurso = recursos.find((r) => r.id === body.recursoId);
      if (!recurso) {
        throw new ApiError({ status: 404, error: 'NO_ENCONTRADO', message: 'Recurso no encontrado' });
      }
      if (!(body.fechaFin > body.fechaInicio)) {
        throw new ApiError({ status: 400, error: 'VALIDACION', message: 'Fin debe ser > inicio' });
      }
      if (usuario.estado !== 'ACTIVO') {
        throw new ApiError({ status: 409, error: 'USUARIO_INACTIVO', message: 'Usuario inactivo' });
      }
      if (recurso.estado === 'MANTENIMIENTO') {
        throw new ApiError({
          status: 409,
          error: 'RECURSO_EN_MANTENIMIENTO',
          message: 'Recurso en mantenimiento',
        });
      }
      if (recurso.estado === 'FUERA_DE_SERVICIO') {
        throw new ApiError({
          status: 409,
          error: 'RECURSO_FUERA_DE_SERVICIO',
          message: 'Recurso fuera de servicio',
        });
      }
      const activas = reservas.filter((r) => r.usuario.id === usuario.id && r.estado === 'ACTIVA');
      if (activas.length >= 3) {
        throw new ApiError({
          status: 409,
          error: 'LIMITE_RESERVAS_ACTIVAS',
          message: 'Máximo 3 reservas activas por usuario',
        });
      }
      if (
        reservas.some(
          (r) =>
            r.recurso.id === recurso.id &&
            r.estado === 'ACTIVA' &&
            solapa(r.fechaInicio, r.fechaFin, body.fechaInicio, body.fechaFin),
        )
      ) {
        throw new ApiError({
          status: 409,
          error: 'RESERVA_SOLAPADA',
          message: 'Horario solapado con otra reserva',
        });
      }
      const nueva: Reserva = {
        id: siguienteReserva++,
        usuario: { id: usuario.id, nombre: usuario.nombre, estado: usuario.estado },
        recurso: { id: recurso.id, nombre: recurso.nombre, estado: recurso.estado },
        fechaInicio: body.fechaInicio,
        fechaFin: body.fechaFin,
        estado: 'ACTIVA',
        fechaCreacion: new Date().toISOString().slice(0, 19),
      };
      reservas.push(nueva);
      return clonar(nueva);
    },
    obtenerReserva: async (id: number) => {
      const reserva = reservas.find((r) => r.id === id);
      if (!reserva) {
        throw new ApiError({ status: 404, error: 'NO_ENCONTRADO', message: 'Reserva no encontrada' });
      }
      return clonar(reserva);
    },
    cancelarReserva: async (id: number) => {
      const reserva = reservas.find((r) => r.id === id);
      if (!reserva) {
        throw new ApiError({ status: 404, error: 'NO_ENCONTRADO', message: 'Reserva no encontrada' });
      }
      if (reserva.estado !== 'ACTIVA') {
        throw new ApiError({
          status: 409,
          error: 'RESERVA_FINALIZADA',
          message: 'La reserva ya está cerrada, no se puede cancelar',
        });
      }
      reserva.estado = 'CANCELADA';
      return clonar(reserva);
    },
    reservasPorUsuario: async (usuarioId: number, estado?: EstadoReserva) =>
      clonar(
        reservas.filter(
          (r) => r.usuario.id === usuarioId && (!estado || r.estado === estado),
        ),
      ),
    reservasPorRecurso: async (
      recursoId: number,
      opciones?: { desde?: string; hasta?: string },
    ) =>
      clonar(
        reservas.filter(
          (r) =>
            r.recurso.id === recursoId &&
            (!opciones?.desde || r.fechaFin > opciones.desde) &&
            (!opciones?.hasta || r.fechaInicio < opciones.hasta),
        ),
      ),
    reservasPorFecha: async (fecha: string) =>
      clonar(reservas.filter((r) => r.fechaInicio.slice(0, 10) <= fecha && r.fechaFin.slice(0, 10) >= fecha)),
  };
}
