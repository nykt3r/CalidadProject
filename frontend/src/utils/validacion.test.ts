import { describe, expect, it } from 'vitest';
import { esCorreoValido, sinErrores, validarUsuario } from './validacion';

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
