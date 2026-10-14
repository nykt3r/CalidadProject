import { describe, expect, it } from 'vitest';
import { esCorreoValido, sinErrores, sinErroresRecurso, validarRecurso, validarUsuario } from './validacion';

describe('validarUsuario', () => {
  it('acepta un usuario válido', () => {
    const errores = validarUsuario({
      nombre: 'Ana García',
      documento: 'DOC-001',
      correo: 'ana@example.com',
    });
    expect(sinErrores(errores)).toBe(true);
  });

  it('marca los tres campos obligatorios cuando están vacíos', () => {
    const errores = validarUsuario({ nombre: ' ', documento: '', correo: '' });
    expect(errores.nombre).toBeDefined();
    expect(errores.documento).toBeDefined();
    expect(errores.correo).toBeDefined();
  });

  it('rechaza correos con formato inválido', () => {
    for (const correo of ['sin-arroba', 'a@', '@dom.com', 'a@b']) {
      expect(esCorreoValido(correo)).toBe(false);
      expect(validarUsuario({ nombre: 'N', documento: 'D', correo }).correo).toBeDefined();
    }
  });
});

describe('validarRecurso', () => {
  it('acepta un recurso válido', () => {
    const errores = validarRecurso({
      nombre: 'Lab 101',
      descripcion: 'Laboratorio',
      tipo: 'LABORATORIO',
      capacidad: 30,
    });
    expect(sinErroresRecurso(errores)).toBe(true);
  });

  it('exige nombre y capacidad mayor que 0', () => {
    expect(validarRecurso({ nombre: ' ', descripcion: '', tipo: 'OTRO', capacidad: 10 }).nombre).toBeDefined();
    for (const capacidad of [0, -5, Number.NaN]) {
      const errores = validarRecurso({ nombre: 'N', descripcion: '', tipo: 'OTRO', capacidad });
      expect(errores.capacidad).toBeDefined();
      expect(sinErroresRecurso(errores)).toBe(false);
    }
  });
});
