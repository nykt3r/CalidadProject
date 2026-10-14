// Validaciones de formularios en cliente, espejo del backend (contrato §2.2 fila 1:
// nombre/documento/correo obligatorios, correo con formato; unicidad la arbitra el backend).

import type { RecursoRequest, ReservaRequest, UsuarioRequest } from '../api/types';

export interface ErroresUsuario {
  nombre?: string;
  documento?: string;
  correo?: string;
}

export function esCorreoValido(correo: string): boolean {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(correo.trim());
}

export function validarUsuario(body: UsuarioRequest): ErroresUsuario {
  const errores: ErroresUsuario = {};
  if (body.nombre.trim().length === 0) errores.nombre = 'El nombre es obligatorio';
  if (body.documento.trim().length === 0) errores.documento = 'El documento es obligatorio';
  if (body.correo.trim().length === 0) {
    errores.correo = 'El correo es obligatorio';
  } else if (!esCorreoValido(body.correo)) {
    errores.correo = 'Formato de correo inválido';
  }
  return errores;
}

export function sinErrores(errores: ErroresUsuario): boolean {
  return errores.nombre === undefined && errores.documento === undefined && errores.correo === undefined;
}

export interface ErroresRecurso {
  nombre?: string;
  capacidad?: string;
}

export function validarRecurso(body: RecursoRequest): ErroresRecurso {
  const errores: ErroresRecurso = {};
  if (body.nombre.trim().length === 0) errores.nombre = 'El nombre es obligatorio';
  if (!Number.isFinite(body.capacidad) || body.capacidad <= 0) {
    errores.capacidad = 'La capacidad debe ser mayor que 0';
  }
  return errores;
}

export function sinErroresRecurso(errores: ErroresRecurso): boolean {
  return errores.nombre === undefined && errores.capacidad === undefined;
}

export interface ErroresReserva {
  fechas?: string;
}

export function validarReserva(body: ReservaRequest, ahora: Date = new Date()): ErroresReserva {
  const errores: ErroresReserva = {};
  const inicio = new Date(body.fechaInicio);
  const fin = new Date(body.fechaFin);
  if (Number.isNaN(inicio.getTime()) || Number.isNaN(fin.getTime())) {
    errores.fechas = 'Fechas inválidas';
  } else if (!(fin > inicio)) {
    errores.fechas = 'La fecha de fin debe ser posterior al inicio (regla 4)';
  } else if (!(inicio > ahora)) {
    errores.fechas = 'El inicio debe estar en el futuro (regla 7)';
  }
  return errores;
}

export function sinErroresReserva(errores: ErroresReserva): boolean {
  return errores.fechas === undefined;
}
