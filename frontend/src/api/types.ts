// Tipos espejo del contrato API congelado v1 (frontend-plan.md §2.1 / backend-plan.md §2.1).
// Fechas en ISO-8601 (p. ej. "2026-10-20T09:00:00").

export type EstadoUsuario = 'ACTIVO' | 'INACTIVO';
export type EstadoRecurso = 'DISPONIBLE' | 'MANTENIMIENTO' | 'FUERA_DE_SERVICIO';
export type EstadoReserva = 'ACTIVA' | 'CANCELADA' | 'FINALIZADA';
export type TipoRecurso =
  | 'LABORATORIO'
  | 'AUDITORIO'
  | 'SALA_REUNIONES'
  | 'EQUIPO_AUDIOVISUAL'
  | 'OTRO';

export interface Usuario {
  id: number;
  nombre: string;
  documento: string;
  correo: string;
  estado: EstadoUsuario;
}

export interface Recurso {
  id: number;
  nombre: string;
  descripcion: string;
  tipo: TipoRecurso;
  capacidad: number;
  estado: EstadoRecurso;
}

export interface Reserva {
  id: number;
  usuario: { id: number; nombre: string; estado: EstadoUsuario };
  recurso: { id: number; nombre: string; estado: EstadoRecurso };
  fechaInicio: string;
  fechaFin: string;
  estado: EstadoReserva;
  fechaCreacion: string;
}

export interface Disponibilidad {
  recursoId: number;
  desde: string;
  hasta: string;
  disponible: boolean;
  reservasSolapadas: Reserva[];
}

/** Cuerpo de error del backend: siempre {status, error, message}. */
export interface ApiErrorBody {
  status: number;
  error: string;
  message: string;
}

// ---- Cuerpos de petición ----

export interface UsuarioRequest {
  nombre: string;
  documento: string;
  correo: string;
}

export interface CambioEstadoUsuarioRequest {
  estado: EstadoUsuario;
}

export interface RecursoRequest {
  nombre: string;
  descripcion: string;
  tipo: TipoRecurso;
  capacidad: number;
}

export interface CambioEstadoRecursoRequest {
  estado: EstadoRecurso;
}

export interface ReservaRequest {
  usuarioId: number;
  recursoId: number;
  fechaInicio: string;
  fechaFin: string;
}

// ---- Interfaz del API consumida por las vistas ----
// La implementan el cliente HTTP real y el cliente mock (intercambiables).

export interface ReservasApi {
  // Usuarios (filas 1-4)
  crearUsuario(body: UsuarioRequest): Promise<Usuario>;
  listarUsuarios(): Promise<Usuario[]>;
  obtenerUsuario(id: number): Promise<Usuario>;
  cambiarEstadoUsuario(id: number, estado: EstadoUsuario): Promise<Usuario>;
  // Recursos (filas 5-10)
  crearRecurso(body: RecursoRequest): Promise<Recurso>;
  listarRecursos(opciones?: { estado?: EstadoRecurso; tipo?: TipoRecurso }): Promise<Recurso[]>;
  obtenerRecurso(id: number): Promise<Recurso>;
  modificarRecurso(id: number, body: RecursoRequest): Promise<Recurso>;
  cambiarEstadoRecurso(id: number, estado: EstadoRecurso): Promise<Recurso>;
  consultarDisponibilidad(recursoId: number, desde: string, hasta: string): Promise<Disponibilidad>;
  // Reservas (filas 11-16)
  crearReserva(body: ReservaRequest): Promise<Reserva>;
  obtenerReserva(id: number): Promise<Reserva>;
  cancelarReserva(id: number): Promise<Reserva>;
  reservasPorUsuario(usuarioId: number, estado?: EstadoReserva): Promise<Reserva[]>;
  reservasPorRecurso(
    recursoId: number,
    opciones?: { desde?: string; hasta?: string },
  ): Promise<Reserva[]>;
  reservasPorFecha(fecha: string): Promise<Reserva[]>;
}
