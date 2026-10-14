// Cliente HTTP tipado: antepone VITE_API_URL, parsea ApiError en respuestas
// no-2xx y lanza un error tipado {status, error, message} (frontend-plan.md F0.3).

import type {
  ApiErrorBody,
  CambioEstadoRecursoRequest,
  CambioEstadoUsuarioRequest,
  Disponibilidad,
  EstadoRecurso,
  EstadoReserva,
  Recurso,
  RecursoRequest,
  Reserva,
  ReservaRequest,
  ReservasApi,
  TipoRecurso,
  Usuario,
  UsuarioRequest,
} from './types';

export class ApiError extends Error implements ApiErrorBody {
  status: number;
  error: string;

  constructor(body: ApiErrorBody) {
    super(body.message);
    this.name = 'ApiError';
    this.status = body.status;
    this.error = body.error;
    this.message = body.message;
  }

  toBody(): ApiErrorBody {
    return { status: this.status, error: this.error, message: this.message };
  }
}

export function esApiError(valor: unknown): valor is ApiError {
  return valor instanceof ApiError;
}

export function baseUrlPorDefecto(): string {
  const configurada = import.meta.env.VITE_API_URL as string | undefined;
  return configurada && configurada.length > 0 ? configurada : 'http://localhost:8080';
}

type FetchFn = typeof fetch;

function armarUrl(base: string, ruta: string, params?: Record<string, string | undefined>): string {
  const url = new URL(ruta, base.endsWith('/') ? base : `${base}/`);
  if (params) {
    for (const [clave, valor] of Object.entries(params)) {
      if (valor !== undefined && valor !== '') {
        url.searchParams.set(clave, valor);
      }
    }
  }
  return url.toString();
}

function esCuerpoDeError(valor: unknown): valor is ApiErrorBody {
  if (typeof valor !== 'object' || valor === null) return false;
  const cuerpo = valor as Record<string, unknown>;
  return (
    typeof cuerpo.status === 'number' &&
    typeof cuerpo.error === 'string' &&
    typeof cuerpo.message === 'string'
  );
}

/** Petición genérica: lanza ApiError íntegro ante respuestas no-2xx. */
export async function request<T>(
  base: string,
  ruta: string,
  opciones: {
    metodo?: string;
    params?: Record<string, string | undefined>;
    body?: unknown;
    fetchFn?: FetchFn;
  } = {},
): Promise<T> {
  const fetchFn: FetchFn = opciones.fetchFn ?? fetch;
  let respuesta: Response;
  try {
    respuesta = await fetchFn(armarUrl(base, ruta), {
      method: opciones.metodo ?? 'GET',
      headers: opciones.body !== undefined ? { 'Content-Type': 'application/json' } : undefined,
      body: opciones.body !== undefined ? JSON.stringify(opciones.body) : undefined,
    });
  } catch {
    throw new ApiError({ status: 0, error: 'FALLO_DE_RED', message: 'No se pudo contactar la API' });
  }

  const texto = await respuesta.text();
  const datos: unknown = texto.length > 0 ? (JSON.parse(texto) as unknown) : null;

  if (!respuesta.ok) {
    if (esCuerpoDeError(datos)) {
      throw new ApiError(datos);
    }
    throw new ApiError({
      status: respuesta.status,
      error: 'ERROR_DESCONOCIDO',
      message: `La API respondió ${respuesta.status}`,
    });
  }
  return datos as T;
}

export function createHttpApi(base: string, fetchFn?: FetchFn): ReservasApi {
  const req = <T>(
    ruta: string,
    opciones?: { metodo?: string; params?: Record<string, string | undefined>; body?: unknown },
  ): Promise<T> => request<T>(base, ruta, { ...opciones, fetchFn });

  return {
    crearUsuario: (body: UsuarioRequest) => req<Usuario>('usuarios', { metodo: 'POST', body }),
    listarUsuarios: () => req<Usuario[]>('usuarios'),
    obtenerUsuario: (id: number) => req<Usuario>(`usuarios/${id}`),
    cambiarEstadoUsuario: (id: number, estado: CambioEstadoUsuarioRequest['estado']) =>
      req<Usuario>(`usuarios/${id}/estado`, { metodo: 'PATCH', body: { estado } }),

    crearRecurso: (body: RecursoRequest) => req<Recurso>('recursos', { metodo: 'POST', body }),
    listarRecursos: (opciones?: { estado?: EstadoRecurso; tipo?: TipoRecurso }) =>
      req<Recurso[]>('recursos', { params: { estado: opciones?.estado, tipo: opciones?.tipo } }),
    obtenerRecurso: (id: number) => req<Recurso>(`recursos/${id}`),
    modificarRecurso: (id: number, body: RecursoRequest) =>
      req<Recurso>(`recursos/${id}`, { metodo: 'PUT', body }),
    cambiarEstadoRecurso: (id: number, estado: CambioEstadoRecursoRequest['estado']) =>
      req<Recurso>(`recursos/${id}/estado`, { metodo: 'PATCH', body: { estado } }),
    consultarDisponibilidad: (recursoId: number, desde: string, hasta: string) =>
      req<Disponibilidad>(`recursos/${recursoId}/disponibilidad`, { params: { desde, hasta } }),

    crearReserva: (body: ReservaRequest) => req<Reserva>('reservas', { metodo: 'POST', body }),
    obtenerReserva: (id: number) => req<Reserva>(`reservas/${id}`),
    cancelarReserva: (id: number) => req<Reserva>(`reservas/${id}`, { metodo: 'DELETE' }),
    reservasPorUsuario: (usuarioId: number, estado?: EstadoReserva) =>
      req<Reserva[]>(`reservas/usuario/${usuarioId}`, { params: { estado } }),
    reservasPorRecurso: (
      recursoId: number,
      opciones?: { desde?: string; hasta?: string },
    ) =>
      req<Reserva[]>(`reservas/recurso/${recursoId}`, {
        params: { desde: opciones?.desde, hasta: opciones?.hasta },
      }),
    reservasPorFecha: (fecha: string) => req<Reserva[]>('reservas', { params: { fecha } }),
  };
}
