// Comportamientos del mock usados por la vista Usuarios (F1):
// crear → aparece en tabla, DUPLICADO 409, 404 en detalle, cambio de estado.
import { describe, expect, it } from 'vitest';
import { ApiError } from '../api/client';
import { createMockApi } from './mockClient';

describe('mock de usuarios', () => {
  it('crear aparece en el listado', async () => {
    const mock = createMockApi();
    const antes = await mock.listarUsuarios();
    const creado = await mock.crearUsuario({
      nombre: 'Nuevo',
      documento: 'DOC-999',
      correo: 'nuevo@example.com',
    });
    expect(creado.estado).toBe('ACTIVO');
    const despues = await mock.listarUsuarios();
    expect(despues.length).toBe(antes.length + 1);
    expect(despues.some((u) => u.id === creado.id)).toBe(true);
  });

  it('duplicar documento o correo lanza DUPLICADO 409', async () => {
    const mock = createMockApi();
    await expect(
      mock.crearUsuario({ nombre: 'X', documento: 'DOC-001', correo: 'x@example.com' }),
    ).rejects.toMatchObject({ status: 409, error: 'DUPLICADO' });
    await expect(
      mock.crearUsuario({ nombre: 'X', documento: 'DOC-X', correo: 'ana@example.com' }),
    ).rejects.toMatchObject({ status: 409, error: 'DUPLICADO' });
  });

  it('detalle de id inexistente lanza NO_ENCONTRADO 404', async () => {
    const mock = createMockApi();
    await expect(mock.obtenerUsuario(9999)).rejects.toBeInstanceOf(ApiError);
    await expect(mock.obtenerUsuario(9999)).rejects.toMatchObject({
      status: 404,
      error: 'NO_ENCONTRADO',
    });
  });

  it('inactivar cambia el estado visible', async () => {
    const mock = createMockApi();
    const actualizado = await mock.cambiarEstadoUsuario(1, 'INACTIVO');
    expect(actualizado.estado).toBe('INACTIVO');
    expect((await mock.obtenerUsuario(1)).estado).toBe('INACTIVO');
  });
});
