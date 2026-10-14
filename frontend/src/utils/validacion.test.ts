import { describe, expect, it } from 'vitest';
import { esCorreoValido, sinErrores, sinErroresRecurso, sinErroresReserva, validarRecurso, validarReserva, validarUsuario } from './validacion';

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

describe('validarReserva', () => {
  const ahora = new Date('2026-10-14T10:00:00');
  it('acepta un rango futuro válido', () => {
    const errores = validarReserva(
      { usuarioId: 1, recursoId: 1, fechaInicio: '2026-10-20T09:00', fechaFin: '2026-10-20T11:00' },
      ahora,
    );
    expect(sinErroresReserva(errores)).toBe(true);
  });

  it('rechaza fin anterior o igual al inicio (regla 4)', () => {
    for (const [inicio, fin] of [
      ['2026-10-20T11:00', '2026-10-20T09:00'],
      ['2026-10-20T09:00', '2026-10-20T09:00'],
    ]) {
      const errores = validarReserva({ usuarioId: 1, recursoId: 1, fechaInicio: inicio, fechaFin: fin }, ahora);
      expect(errores.fechas).toBeDefined();
    }
  });

  it('rechaza inicio en el pasado (regla 7)', () => {
    const errores = validarReserva(
      { usuarioId: 1, recursoId: 1, fechaInicio: '2026-10-10T09:00', fechaFin: '2026-10-10T11:00' },
      ahora,
    );
    expect(errores.fechas).toBeDefined();
  });
});
