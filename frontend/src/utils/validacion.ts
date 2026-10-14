// Validaciones de formularios en cliente, espejo del backend (contrato §2.2 fila 1:
// nombre/documento/correo obligatorios, correo con formato; unicidad la arbitra el backend).

import type { UsuarioRequest } from '../api/types';

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
